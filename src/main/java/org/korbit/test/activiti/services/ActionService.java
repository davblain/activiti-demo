package org.korbit.test.activiti.services;

import org.activiti.bpmn.model.*;
import org.activiti.engine.FormService;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.form.FormData;
import org.korbit.test.activiti.exceptions.ActionNotFoundException;
import org.korbit.test.activiti.models.Action;
import org.korbit.test.activiti.models.ActionEntity;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@PropertySource(value = { "classpath:application.properties" })
public class ActionService {
    @Value("${action-process.definition}")
    String actionProcessDefKey;
    @Value("${action-process.actionTypeSuffix}")
    String actionTypeSuffix;
    private BpmnModel bpmnModel;
    private RepositoryService repositoryService;
    private FormService formService;

    public ActionService(RepositoryService repositoryService, FormService formService) {
        this.repositoryService = repositoryService;
        this.formService = formService;
    }

    @PostConstruct
    public void init() {
        bpmnModel = getBpmnModelByKey(actionProcessDefKey);
    }

    private List <String> getStatesWithAvailableActions() {
        ExclusiveGateway stateGw = (ExclusiveGateway) bpmnModel.getFlowElement("stateGw");
        return stateGw.getOutgoingFlows().stream()
                .map(BaseElement::getId)
                .collect(Collectors.toList());
    }

    public List<String> getAvailableActionTypesOfState (String state) {
        if (!getStatesWithAvailableActions().contains(state)) {
            return new ArrayList<>();
        }
        return Optional.ofNullable((ExclusiveGateway) bpmnModel.getFlowElement(state + actionTypeSuffix))
                .map(actionGw -> actionGw.getOutgoingFlows().stream()
                        .map(FlowElement::getName)
                        .collect(Collectors.toList())
                ).orElse(new ArrayList<>());
    }

    private FlowElement getActionFlow(String actionType) {

    return Optional.ofNullable(bpmnModel.getFlowElement(actionType)).orElseThrow(()-> new ActionNotFoundException(actionType));
   }
    public List<String> getAuthorities(String actionType) {
        List<String> authorities = new ArrayList<>();
        System.out.println(actionType);
        String doc = Optional.ofNullable(getActionFlow(actionType).getDocumentation()).orElse("");
        if (doc.equals("")|| doc.contains("assigner")) authorities.add("assigner");
        if (doc.contains("involved")) authorities.add("involved");
        if (doc.contains("creator")) authorities.add("creator");
        return authorities;
    }



    public boolean isStepAction(String action) {
        return !Optional.ofNullable(getActionFlow(action).getDocumentation()).orElse("").contains("isNotStep");
    }


    public List<org.activiti.engine.form.FormProperty> getPropertiesOfAction(String actionType) {

        return  Optional.ofNullable(getIdOfProcessByKey(actionType+"Form"))
                .map(id -> formService.getStartFormData(id).getFormProperties())
                .orElse(new ArrayList<>());
    }



    public List<Action> actionTypesToActions(List<String> actionTypes) {
        return actionTypes.stream().map(actionType -> {
                Action action = new Action();
                action.setActionType(actionType);
                action.setProperties(getPropertiesOfAction(actionType));
                action.setStep(isStepAction(actionType));
                return action;
        }).collect(Collectors.toList());
    }

    private BpmnModel getBpmnModelByKey(String processKey) {
        return repositoryService.getBpmnModel(repositoryService.createProcessDefinitionQuery().processDefinitionKey(processKey).singleResult().getId());
    }

    private String  getIdOfProcessByKey(String processKey) {
        return Optional.ofNullable(repositoryService.createProcessDefinitionQuery().processDefinitionKey(processKey).singleResult()).map(process -> process.getId()).orElse(null);
    }

    private List<CallActivity> findActivitiCallFlows(SequenceFlow startFlow) {
        List<CallActivity> activities = new ArrayList<>();
        if (bpmnModel.getFlowElement(startFlow.getTargetRef()) instanceof CallActivity) {
            activities.add((CallActivity) bpmnModel.getFlowElement(startFlow.getTargetRef()));
        }
        if (bpmnModel.getFlowElement(startFlow.getTargetRef()) instanceof ParallelGateway) {
             ((ParallelGateway) bpmnModel.getFlowElement(startFlow.getTargetRef())).getOutgoingFlows()
                    .forEach(sequenceFlow -> activities.addAll(findActivitiCallFlows(sequenceFlow)));
        }
        return  activities;
    }



}
