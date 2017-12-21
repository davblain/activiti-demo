package org.korbit.test.activiti.listeners;

import org.activiti.bpmn.model.BpmnModel;
import org.activiti.bpmn.model.ExclusiveGateway;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.DelegateHelper;
import org.activiti.engine.delegate.ExecutionListener;
import org.activiti.engine.repository.ProcessDefinition;
import org.korbit.test.activiti.dto.ActionDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class AddHistoryListener implements ExecutionListener{
    @Autowired
    private SimpMessagingTemplate template;
    @Autowired
    private RepositoryService repositoryService;
    @Override
    public void notify(DelegateExecution delegateExecution) throws Exception {

        List<ActionDto> actions = Optional.ofNullable((List<ActionDto>) delegateExecution.getVariable("actions"))
                .orElse(new ArrayList<>());
        ActionDto action = (ActionDto) delegateExecution.getVariable("action");
        actions.add(action);
        //ExclusiveGateway exGw = (ExclusiveGateway) repositoryService.getBpmnModel("myProcess").getFlowElement("stateGw");

        //String nameOfGw = exGw.getOutgoingFlows().stream().filter(sequenceFlow -> sequenceFlow.getId()==delegateExecution.getVariable("state")).map(sequenceFlow ->  sequenceFlow.getTargetRef())
        //        .findFirst().get();
        //ExclusiveGateway exGw2 = (ExclusiveGateway) repositoryService.getBpmnModel("myProcess").getFlowElement(nameOfGw);
        //List<String> AvailableActions =  exGw2.getOutgoingFlows().stream().map(sequenceFlow -> sequenceFlow.getId()).collect(Collectors.toList());

        System.out.println(delegateExecution.getProcessInstanceId());
        template.convertAndSend("/app/"+ delegateExecution.getProcessInstanceId(),action);
        delegateExecution.setVariable("actions",actions);
    }
}
