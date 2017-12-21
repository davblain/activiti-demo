package org.korbit.test.activiti.services;

import org.activiti.engine.*;
import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.identity.User;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.korbit.test.activiti.dto.ActionHistoryDto;
import org.korbit.test.activiti.dto.TaskDto;
import org.korbit.test.activiti.dto.TaskMailRequest;
import org.korbit.test.activiti.exceptions.TaskNotFoundException;
import org.korbit.test.activiti.exceptions.UserNotFoundException;
import org.korbit.test.activiti.models.Action;
import org.korbit.test.activiti.models.ActionType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.validation.constraints.NotNull;
import java.util.*;
import java.util.stream.Collectors;
@Service
public class TasksService {

    final private HistoryService historyService;
    final private IdentityService identityService;
    final  private TaskService taskService;
    final private RuntimeService runtimeService;
    final private ActionService actionService;
    public TasksService(TaskService taskService, HistoryService historyService, RuntimeService runtimeService,
                        IdentityService identityService,
                        ActionService actionService) {
        this.historyService = historyService;
        this.taskService = taskService;
        this.runtimeService = runtimeService;
        this.identityService = identityService;
        this.actionService = actionService;
    }

    @Transactional
    public String startTask(@NotNull TaskMailRequest task) {
        try {
            identityService.setAuthenticatedUserId(task.getCreator());
            Map<String,Object> variables = new HashMap<>();
            User recipient = Optional.ofNullable(identityService.createUserQuery().userId(task.getRecipient()).singleResult())
                    .orElseThrow(() -> new UserNotFoundException(task.getRecipient()));
            variables.put("assigner",recipient.getId());
            ActionHistoryDto action =  new ActionHistoryDto();
            action.setCreator(task.getCreator());
            action.setType(ActionType.CreateAction);
            variables.put("action",action);
            variables.put("state",  "CreatedState");
            variables.put("userChain",new ArrayList<String>());
            variables.put("duration",task.getDuration());
            variables.put("description", task.getDescription());
            variables.put("title",task.getTitle());
            ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("tMailProcess",variables);
            return processInstance.getId();
        } finally {
            identityService.setAuthenticatedUserId(null);
        }
    }
    @Transactional
    public void doAction(@NotNull String taskId, ActionHistoryDto actionDto) {
        Task task = Optional.ofNullable(taskService.createTaskQuery().processInstanceId(taskId).includeProcessVariables().singleResult())
                .orElseThrow(() -> new TaskNotFoundException(taskId));
        Map<String, Object> taskVariables = new HashMap<String, Object>();
        taskVariables.put("actionToValidate",actionDto);
        taskService.complete(task.getId(),taskVariables);
    }

    public TaskDto getTaskDetails(@NotNull String taskId) {
        HistoricProcessInstance historicProcessInstance = getHistoryInstanceOfTask(taskId);
        TaskDto taskDto = new TaskDto();
        taskDto.setUserChain((List<String>) Optional.ofNullable(historicProcessInstance.getProcessVariables().get("userChain")).orElse(new ArrayList<>()));
        taskDto.setAssignee((String)historicProcessInstance .getProcessVariables().get("assigner"));
        taskDto.setActions((ArrayList<ActionHistoryDto>) Optional.ofNullable(historicProcessInstance.getProcessVariables().get("actions")).orElse(new ArrayList<>()));
        taskDto.setCreator((String) historicProcessInstance.getProcessVariables().get("initiator"));
        taskDto.setTitle((String) historicProcessInstance.getProcessVariables().get("title"));
        taskDto.setState(historicProcessInstance.getProcessVariables().get("state").toString());
        taskDto.setStartTime(historicProcessInstance.getStartTime());
        taskDto.setCurDuration((String) historicProcessInstance.getProcessVariables().get("duration"));
        taskDto.setEndTime(historicProcessInstance.getEndTime());
        taskDto.setDescription((String) historicProcessInstance.getProcessVariables().get("description"));
        return taskDto;
    }

    @Transactional
    public void  changeDescriptionOfTask(String taskId,  String desc) {
        Task task = Optional.ofNullable(taskService.createTaskQuery().processInstanceId(taskId).singleResult())
                .orElseThrow(() -> new TaskNotFoundException(taskId));
        runtimeService.setVariable(task.getExecutionId(),"description",desc);
    }

    public List<String> getListOfAssigners(@NotNull String taskId) {
        Task task = Optional.ofNullable(taskService.createTaskQuery().processInstanceId(taskId).includeProcessVariables().singleResult())
                .orElseThrow(() -> new TaskNotFoundException(taskId));

        return Optional.ofNullable((ArrayList<ActionHistoryDto>) task.getProcessVariables().get("actions"))
                .map( actionDtos -> actionDtos.stream()
                        .map(ActionHistoryDto::getCreator)
                        .collect(Collectors.toList()))
                .orElse(new ArrayList<String>());
    }

    public List<String> getAvailableActionTypesOfTask(@NotNull String taskId) {
        HistoricProcessInstance historicProcessInstance = getHistoryInstanceOfTask(taskId);
        String state = historicProcessInstance.getProcessVariables().get("state").toString();
        return  actionService.getAvailableActionTypesOfState(state);
    }

    public List <Action> getAvailableActionsOfTask(@NotNull String taskId) {

        return actionService.actionTypesToActions(getAvailableActionTypesOfTask(taskId));
    }

    public String getState(String taskId) {
        return getHistoryInstanceOfTask(taskId).getProcessVariables().get("state").toString();
    }


    @Transactional
    public List<ActionHistoryDto> getListOfActions(@NotNull String taskId) {
        HistoricProcessInstance historicProcessInstance = Optional.ofNullable(historyService.createHistoricProcessInstanceQuery()
                .processInstanceId(taskId).includeProcessVariables().singleResult())
                .orElseThrow(() -> new TaskNotFoundException(taskId));

        return Optional.ofNullable((ArrayList<ActionHistoryDto>) historicProcessInstance.getProcessVariables().get("actions")).orElse(new ArrayList<>());

    }

    public  HistoricProcessInstance getHistoryInstanceOfTask(String taskId) {
        return Optional.ofNullable(historyService.createHistoricProcessInstanceQuery().processInstanceId(taskId).processDefinitionKey("tMailProcess").includeProcessVariables().singleResult())
                .orElseThrow(() -> new TaskNotFoundException(taskId));
    }
}
