package org.korbit.test.activiti.services;


import org.activiti.engine.*;
import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.history.HistoricProcessInstanceQuery;
import org.korbit.test.activiti.dto.*;
import org.korbit.test.activiti.exceptions.TaskNotFoundException;
import org.korbit.test.activiti.models.Action;
import org.korbit.test.activiti.models.ActionType;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.validation.constraints.NotNull;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class TMailProcessService {
    final private IdentityService identityService;
    final private UserService userService;
    final private HistoryService historyService;
    final private TasksService tasksService;
    final private ActionService actionService;
    public TMailProcessService(RuntimeService runtimeService, IdentityService identityService,
                               TaskService taskService, UserService userService, HistoryService historyService, TasksService tasksService, ActionService actionService) {
        this.identityService = identityService;
        this.tasksService = tasksService;
        this.userService = userService;
        this.historyService = historyService;
        this.actionService = actionService;
    }


    private ActionDto getLastDelegateToUser(@NotNull String taskId,@NotNull String username) {
       List<ActionDto> actionsList = tasksService.getListOfActions(taskId).stream()
               .filter(actionDto -> actionDto.getType().equals(ActionType.DelegateAction)
               || actionDto.getType().equals(ActionType.RefinementAction)
               ||actionDto.getType().equals(ActionType.CreateAction))
               .filter(actionDto -> actionDto.getData().get("recipient").equals(username)).collect(Collectors.toList());
       if (actionsList.size()==0) return null;
       return actionsList.get(actionsList.size()-1);
    }
    @Transactional
    public Page<TaskItemDto> getListOfTaskCreatedByUser(@NotNull String username,@NotNull Integer page,@NotNull Integer limit) {
        HistoricProcessInstanceQuery query = historyService.createHistoricProcessInstanceQuery().variableValueEquals("initiator",username);
        return generatePage(query,username,page,limit);

    }
    @Transactional
    public Page<TaskItemDto> getListOfExpiredTasks(@NotNull String username,@NotNull Integer page, @NotNull Integer limit) {
                return generatePage(generateHistoryQueryByState("ExpiredState").involvedUser(username),username,page,limit);
    }
    @Transactional
    public Page<TaskItemDto> getListOfCurrentTaskByUsername(@NotNull String username,@NotNull  Integer page ,@NotNull  Integer limit) {

        return generatePage(generateHistoryQueryByState("OpenedState").variableValueEquals("assigner",username),username,page,limit);
    }
    @Transactional
    public Page<TaskItemDto> getDoneWithUserTasks(@NotNull String username,@NotNull Integer page, @NotNull Integer limit) {
        return generatePage(generateHistoryQueryByState("DoneState").involvedUser(username),username,page,limit);
    }
    @Transactional
    public Page<TaskItemDto> getAllTasksOfUser(@NotNull String username, Integer page, Integer limit)  {
        HistoricProcessInstanceQuery query = historyService.createHistoricProcessInstanceQuery().involvedUser(username);
        return generatePage(query,username,page,limit);
    }
    @Transactional
    public Page <TaskItemDto> getClosedTasksOfUser(@NotNull String username,Integer page ,Integer limit) {
        return generatePage(generateHistoryQueryByState("ClosedState").involvedUser(username),username,page,limit);
    }
    private HistoricProcessInstanceQuery generateHistoryQueryByState(String state) {
        return historyService.createHistoricProcessInstanceQuery().variableValueEquals("state",state.toString());
    }


    public List<String> getAvailableActionTypesOfUser(@NotNull String taskId, @NotNull String username) {

        List<String> allAvailableActionTypes = tasksService.getAvailableActionTypesOfTask(taskId);
        List<String> authoritiesOfUser = userAuthoritesToTask(taskId, username);
        return allAvailableActionTypes.stream().filter(actionType -> {
            List<String> authoritiesOfAction = actionService.getAuthorities(actionType);
            return authoritiesOfAction.stream().anyMatch(authoritiesOfUser::contains);
        }).collect(Collectors.toList());
    }

    public List<Action> getAvailableActionsOfUser(@NotNull String taskId, @NotNull String username) {
        return actionService.actionTypesToActions(getAvailableActionTypesOfUser(taskId,username));
    }

    private boolean isUserAssignerOfTask(@NotNull String taskId,@NotNull String username) {
        HistoricProcessInstance task = tasksService.getHistoryInstanceOfTask(taskId);
        return task.getProcessVariables().get("assigner").equals(username);
    }

    private  boolean isUserCreatorOfTask(@NotNull String taskId, @NotNull String username) {
        HistoricProcessInstance task = tasksService.getHistoryInstanceOfTask(taskId);
        return task.getProcessVariables().get("initiator").equals(username);
    }
    private boolean isUserInvolvedOnTask(@NotNull String taskId, @NotNull String username) {
        HistoricProcessInstance task = tasksService.getHistoryInstanceOfTask(taskId);
        List <String> userChain = (List<String>) task.getProcessVariables().get("userChain");
        return  userChain.contains(username);
    }
    private  List<String>  userAuthoritesToTask(String taskId, String username) {
        List<String> authorities = new ArrayList<>();
        HistoricProcessInstance task = tasksService.getHistoryInstanceOfTask(taskId);
        List<String> userChain = (List<String>) task.getProcessVariables().get("userChain");
        if (userChain.contains(username)) {
            authorities.add("involved");
        }
        if (task.getProcessVariables().get("assigner").toString().equals(username)) {
            authorities.add("assigner");
        }
        if (task.getProcessVariables().get("initiator").toString().equals(username)) {
            authorities.add("creator");
        }
        return authorities;
    }
    private TaskItemDto taskToTaskItemDto(HistoricProcessInstance task,String username) {

        TaskItemDto t = new TaskItemDto();
        t.setCreator((String)(task.getProcessVariables().get("initiator")));
        t.setStatus(task.getProcessVariables().get("state").toString());
        t.setCreated(task.getStartTime());
        t.setDescription((String) (task.getProcessVariables().get("description")));
        t.setId(task.getId());
        t.setTitle((String) task.getProcessVariables().get("title"));
        Optional.ofNullable(getLastDelegateToUser(task.getId(),username)).ifPresent(lastAction -> {
            t.setReceived(lastAction.getTime());
            t.setSender(lastAction.getCreator());
        });
        return t;
    }

    private Page<TaskItemDto> generatePage(HistoricProcessInstanceQuery query,String username, Integer page,Integer limit) {
        Page<TaskItemDto> pagee = new  Page<>();
        List<TaskItemDto> content = query.includeProcessVariables().processDefinitionKey("tMailProcess").orderByProcessInstanceStartTime().desc().listPage((page-1)*limit,limit).stream().map( task ->  taskToTaskItemDto(task,username)).collect(Collectors.toList());
        pagee.setContent(content);
        pagee.setNumber(page);
        pagee.setTotalElements(query.processDefinitionKey("tMailProcess").count());
        pagee.setTotalPages(pagee.getTotalElements()/limit+1);
        return pagee;
    }



}
