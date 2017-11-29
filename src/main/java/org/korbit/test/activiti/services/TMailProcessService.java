package org.korbit.test.activiti.services;


import org.activiti.engine.*;
import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.history.HistoricProcessInstanceQuery;
import org.activiti.engine.identity.Group;
import org.korbit.test.activiti.dto.*;
import org.korbit.test.activiti.exceptions.TaskNotFoundException;
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
    public TMailProcessService(RuntimeService runtimeService, IdentityService identityService,
                               TaskService taskService, UserService userService, HistoryService historyService, TasksService tasksService) {
        this.identityService = identityService;
        this.tasksService = tasksService;
        this.userService = userService;
        this.historyService = historyService;
    }


    private ActionDto getLastDelegateToUser(@NotNull String taskId,@NotNull String username) {
       List<ActionDto> actionsList = tasksService.getListOfActions(taskId).stream()
               .filter(actionDto -> actionDto.getType().equals(ActionType.Delegate)
               || actionDto.getType().equals(ActionType.Refinement)
               ||actionDto.getType().equals(ActionType.Create))
               .filter(actionDto -> actionDto.getData().get("recipient").equals(username)).collect(Collectors.toList());
       if (actionsList.size()==0) return null;
       return actionsList.get(actionsList.size()-1);
    }
    @Transactional
    public Page<TaskItemDto> getListOfTaskCreatedByUser(@NotNull String username,Integer page,Integer limit) {
        HistoricProcessInstanceQuery query = historyService.createHistoricProcessInstanceQuery().variableValueEquals("initiator",username);
        return generatePage(query,username,page,limit);

    }
    @Transactional
    public Page<TaskItemDto> getListOfCurrentTaskByUsername(@NotNull String username,Integer page ,Integer limit) {

        HistoricProcessInstanceQuery query = historyService.createHistoricProcessInstanceQuery().variableValueEquals("assigner",username)
                .variableValueEquals("state","Created");
        return generatePage(query,username,page,limit);
    }
    @Transactional
    public Page<TaskItemDto> getDoneWithUserTasks(@NotNull String username,Integer page, Integer limit) {
        HistoricProcessInstanceQuery query = historyService.createHistoricProcessInstanceQuery().involvedUser(username).variableValueEquals("state","Done");
        return generatePage(query,username,page,limit);
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
    @Transactional
    public Page<TaskItemDto> getAllTasksOfUser(@NotNull String username, Integer page ,Integer limit)  {
        HistoricProcessInstanceQuery query = historyService.createHistoricProcessInstanceQuery().involvedUser(username);
        return generatePage(query,username,page,limit);
    }
    @Transactional
    public Page <TaskItemDto> getClosedTasksOfUser(@NotNull String username,Integer page ,Integer limit) {
        HistoricProcessInstanceQuery query = historyService.createHistoricProcessInstanceQuery().involvedUser(username).variableValueEquals("state","Closed");
        return generatePage(query,username,page,limit);
    }

    private Page<TaskItemDto> generatePage(HistoricProcessInstanceQuery query,String username, Integer page,Integer limit) {
        Page<TaskItemDto> pagee = new  Page<>();
        List<TaskItemDto> content = query.includeProcessVariables().listPage(page-1,limit).stream().map( task ->  taskToTaskItemDto(task,username)).collect(Collectors.toList());
        pagee.setContent(content);
        pagee.setTotalElements(query.count());
        pagee.setTotalPages(pagee.getTotalElements()/limit+1);
        return pagee;
    }

    private boolean isUserAssignerOfTask(String taskId,String username) {
        HistoricProcessInstance task = Optional.ofNullable(historyService.createHistoricProcessInstanceQuery().includeProcessVariables().processInstanceId(taskId).singleResult())
                .orElseThrow(() -> new TaskNotFoundException(taskId));
        return task.getProcessVariables().get("assigner").equals(username);
    }
    @Transactional
    public List<ActionType> getAvailableActionTypes(@NotNull String taskId, @NotNull String username) {
        List<Group> groups = identityService.createGroupQuery().groupMember(username).list();
        List<ActionType> availableActions = userService.getGroupPermissionsOfUser(username).stream()
                .flatMap(groupPermission -> {
                    List<ActionType> listOfActions = new ArrayList<>();
                    if (isUserAssignerOfTask(taskId,username))
                        listOfActions.addAll(groupPermission.getActionTypesIfAssigner());
                    listOfActions.addAll(groupPermission.getActionTypesIfNotAssigner());
                    return listOfActions.stream();
                }).collect(Collectors.toList());
        return filterUnAvailableActionTypes(taskId,availableActions);

    }
    private List<ActionType> filterUnAvailableActionTypes(String taskId, List<ActionType> actions) {
        return actions.stream().filter( actionType -> ! tasksService.getUnAvailableActionsOfTask(taskId).contains(actionType))
                .distinct()
                .collect(Collectors.toList());
    }
    public List<ActionType> getNonAssignNeedActionTypes(@NotNull String taskId, @NotNull String username) {
        List<Group> groups = identityService.createGroupQuery().groupMember(username).list();
        List<ActionType> availableActions = userService.getGroupPermissionsOfUser(username).stream()
                .flatMap(groupPermission -> groupPermission.getActionTypesIfNotAssigner().stream())
                .collect(Collectors.toList());
        return  filterUnAvailableActionTypes(taskId,availableActions);
    }
}
