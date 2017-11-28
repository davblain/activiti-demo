package org.korbit.test.activiti.services;

import org.activiti.engine.HistoryService;
import org.activiti.engine.IdentityService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.identity.User;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.korbit.test.activiti.dto.ActionDto;
import org.korbit.test.activiti.dto.TaskDto;
import org.korbit.test.activiti.dto.TaskMailRequest;
import org.korbit.test.activiti.exceptions.TaskNotFoundException;
import org.korbit.test.activiti.exceptions.UserNotFoundException;
import org.korbit.test.activiti.models.ActionType;
import org.korbit.test.activiti.models.State;
import org.korbit.test.activiti.models.StateType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

import javax.validation.constraints.NotNull;
import java.util.*;
import java.util.stream.Collectors;
@Service
public class TasksService {

    final private HistoryService historyService;
    final private IdentityService identityService;
    final  private TaskService taskService;
    final private RuntimeService runtimeService;
    public TasksService(TaskService taskService, HistoryService historyService, RuntimeService runtimeService,IdentityService identityService) {
        this.historyService = historyService;
        this.taskService = taskService;
        this.runtimeService = runtimeService;
        this.identityService = identityService;
    }

    @Transactional
    public String startTask(@NotNull TaskMailRequest task) {
        try {
            identityService.setAuthenticatedUserId(task.getCreator());
            Map<String,Object> variables = new HashMap<>();
            User recipient = Optional.ofNullable(identityService.createUserQuery().userId(task.getRecipient()).singleResult())
                    .orElseThrow(() -> new UserNotFoundException(task.getRecipient()));
            variables.put("assigner",recipient.getId());
            ArrayList<ActionDto> actionDtos = new ArrayList<>();
            ActionDto action =  new ActionDto();
            action.setCreator(task.getCreator());
            action.setType(ActionType.Create);
            action.setData(new HashMap<>());
            action.getData().put("recipient",task.getRecipient());
            actionDtos.add(action);
            variables.put("actions",actionDtos);
            variables.put("state",  "Created");
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
    public void doAction(@NotNull String taskId, ActionDto actionDto) {
        Task task = Optional.ofNullable(taskService.createTaskQuery().processInstanceId(taskId).includeProcessVariables().singleResult())
                .orElseThrow(() -> new TaskNotFoundException(taskId));
        Map<String, Object> taskVariables = new HashMap<String, Object>();
        taskVariables.put("actionToValidate",actionDto);
        taskService.complete(task.getId(),taskVariables);
    }
    public TaskDto getTaskDetails(@NotNull String taskId) {
        HistoricProcessInstance historicProcessInstance = historyService.createHistoricProcessInstanceQuery().processInstanceId(taskId).includeProcessVariables().singleResult();
        TaskDto taskDto = new TaskDto();
        taskDto.setUserChain((List<String>) Optional.ofNullable(historicProcessInstance.getProcessVariables().get("userChain")).orElse(new ArrayList<>()));
        taskDto.setAssignee((String)historicProcessInstance .getProcessVariables().get("assigner"));
        taskDto.setActions((ArrayList<ActionDto>) Optional.ofNullable(historicProcessInstance.getProcessVariables().get("actions")).orElse(new ArrayList<>()));
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

        return Optional.ofNullable((ArrayList<ActionDto>) task.getProcessVariables().get("actions"))
                .map( actionDtos -> actionDtos.stream()
                        .map(ActionDto::getCreator)
                        .collect(Collectors.toList()))
                .orElse(new ArrayList<String>());
    }
    public List<ActionType> getUnAvailableActionsOfTask(@NotNull String taskID) {
        HistoricProcessInstance historicProcessInstance = historyService.createHistoricProcessInstanceQuery()
                .processInstanceId(taskID).includeProcessVariables().singleResult();
        State state = State.instanceState(StateType.valueOf(historicProcessInstance.getProcessVariables().get("state").toString())
        );
        return Optional.ofNullable(state.getUnavailableActions()).orElse(new ArrayList<>());
    }
    @Transactional
    public List<ActionDto> getListOfActions(@NotNull String taskId) {
        HistoricProcessInstance historicProcessInstance = Optional.ofNullable(historyService.createHistoricProcessInstanceQuery()
                .processInstanceId(taskId).includeProcessVariables().singleResult())
                .orElseThrow(() -> new TaskNotFoundException(taskId));

        return Optional.ofNullable((ArrayList<ActionDto>) historicProcessInstance.getProcessVariables().get("actions")).orElse(new ArrayList<>());

    }
}
