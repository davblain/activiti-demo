package org.korbit.test.activiti.services;


import org.activiti.engine.*;
import org.activiti.engine.history.HistoricActivityInstance;
import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.identity.Group;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.korbit.test.activiti.domain.GroupPermission;
import org.korbit.test.activiti.dto.*;
import org.korbit.test.activiti.exceptions.TaskNotFoundException;
import org.korbit.test.activiti.models.ActionType;
import org.korbit.test.activiti.repository.GroupPermissionRepository;
import org.korbit.test.activiti.dto.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.validation.constraints.NotNull;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class TMailProcessService {
    final private RuntimeService runtimeService;
    final private IdentityService identityService;
    final private TaskService taskService;
    final private RepositoryService repositoryService;
    final private GroupPermissionRepository groupPermissionRepository;
    final private HistoryService historyService;

    public TMailProcessService(RuntimeService runtimeService, IdentityService identityService,
                               TaskService taskService, RepositoryService repositoryService, GroupPermissionRepository groupPermissionRepository, HistoryService historyService) {
        this.runtimeService = runtimeService;
        this.identityService = identityService;
        this.taskService = taskService;
        this.repositoryService = repositoryService;
        this.groupPermissionRepository = groupPermissionRepository;
        this.historyService = historyService;
    }
    @Transactional
    public String startTask(@NotNull TaskMailRequest task) {
        try {
            identityService.setAuthenticatedUserId(task.getCreator());
            Map<String,Object> variables = new HashMap<>();
            variables.put("assigner",task.getRecipient());
            ArrayList<ActionDto> actionDtos = new ArrayList<>();
            ActionDto action =  new ActionDto();
            action.setCreator(task.getCreator());
            action.setType(ActionType.Create);
            action.setData(new HashMap<>());
            action.getData().put("recipient",task.getRecipient());
            actionDtos.add(action);
            variables.put("actions",actionDtos);
            variables.put("state","open");
            variables.put("duration",10);
            variables.put("description", task.getDescription());
            variables.put("title",task.getTitle());
            ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("tMailProcess",variables);
            return processInstance.getId();
        } finally {
            identityService.setAuthenticatedUserId(null);
        }
    }

    private ActionDto getLastDelegateToUser(@NotNull String taskId,@NotNull String username) {
       List<ActionDto> actionsList = getListOfActions(taskId).stream().filter(actionDto -> actionDto.getType().equals(ActionType.Delegate) || actionDto.getType().equals(ActionType.Refinement)||actionDto.getType().equals(ActionType.Create)).filter(actionDto -> actionDto.getData().get("recipient").equals(username)).collect(Collectors.toList());
       if (actionsList.size()==0) return null;
       return actionsList.get(actionsList.size()-1);
    }

    public Page<TaskItemDto> getListOfCurrentTaskByUsername(@NotNull String username,Integer page ,Integer limit) {
        Page<TaskItemDto> pagee = new Page<>();
        taskService.createTaskQuery().taskAssignee(username).listPage((page-1),limit).stream().map( task -> {
             HistoricProcessInstance historicProcessInstance = historyService.createHistoricProcessInstanceQuery().processInstanceId(task.getProcessInstanceId()).includeProcessVariables().singleResult();
             return taskToTaskItemDto(historicProcessInstance,username);
        }).forEach(taskItemDto ->  pagee.getContent().add(taskItemDto));
        pagee.setTotalElements(taskService.createTaskQuery().taskAssignee(username).count());
        pagee.setTotalPages(pagee.getTotalElements()/limit+1);
        pagee.setNumber(page);
        return  pagee;
    }
    public Page<TaskItemDto> getDoneWithUserTasks(@NotNull String username,Integer page, Integer limit) {
        Page<TaskItemDto> pagee = new Page<>();
        historyService.createHistoricProcessInstanceQuery().involvedUser(username).includeProcessVariables().finished().listPage((page-1),limit).stream()
                .map( p -> taskToTaskItemDto(p,username)).forEach(taskItemDto -> pagee.getContent().add(taskItemDto) );
        pagee.setNumber(page);
        pagee.setTotalElements(historyService.createHistoricProcessInstanceQuery().involvedUser(username).finished().count());
        pagee.setTotalPages(pagee.getTotalElements()/limit+1);
        return pagee;
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
    public Page<TaskItemDto> getAllTasksOfUser(@NotNull String username, Integer page ,Integer limit)  {

        Page<TaskItemDto> pagee = new Page<>();
         List<TaskItemDto> content = historyService.createHistoricProcessInstanceQuery().involvedUser(username).includeProcessVariables().listPage((page-1)*limit,limit).stream()
                .map(p -> taskToTaskItemDto(p,username)).collect(Collectors.toList());
        pagee.setContent(content);
        pagee.setNumber(page);
        pagee.setTotalElements(historyService.createHistoricProcessInstanceQuery().involvedUser(username).count());
        pagee.setTotalPages(pagee.getTotalElements()/limit+1);
        return pagee;
    }

    public Page <TaskItemDto> getClosedTasksOfUser(@NotNull String username,Integer page ,Integer limit) {
        Page<TaskItemDto> pagee = new Page<>();
        historyService.createHistoricProcessInstanceQuery().involvedUser(username).variableValueEquals("state","closed").includeProcessVariables().listPage((page-1)*limit,limit).stream()
                .map( task ->  taskToTaskItemDto(task,username))
                .forEach(taskItemDto ->pagee.getContent().add(taskItemDto));
        pagee.setNumber(page);
        pagee.setTotalElements(historyService.createHistoricProcessInstanceQuery().involvedUser(username).variableValueEquals("state","closed").count());
        pagee.setTotalPages(pagee.getTotalElements()/limit+1);
        return  pagee;
    }

    public void doAction(@NotNull StepRequest stepRequest) {
        Task task = Optional.ofNullable(taskService.createTaskQuery().processInstanceId(stepRequest.getTaskId()).includeProcessVariables().singleResult())
                .orElseThrow(() -> new TaskNotFoundException(stepRequest.getTaskId()));
        Map<String, Object> taskVariables = new HashMap<String, Object>();
        taskVariables.put("actionToValidate",stepRequest.getAction());
        taskService.complete(task.getId(),taskVariables);
    }
    public List<ActionDto> getListOfActions(@NotNull String taskId) {
        HistoricProcessInstance historicProcessInstance = Optional.ofNullable(historyService.createHistoricProcessInstanceQuery().processInstanceId(taskId).includeProcessVariables().singleResult()).orElseThrow(() -> new TaskNotFoundException(taskId));

        return Optional.ofNullable((ArrayList<ActionDto>) historicProcessInstance.getProcessVariables().get("actions")).orElse(new ArrayList<>());

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
    public TaskDto getTaskDetails(@NotNull String taskId) {
        HistoricProcessInstance historicProcessInstance = historyService.createHistoricProcessInstanceQuery().processInstanceId(taskId).includeProcessVariables().singleResult();
        TaskDto taskDto = new TaskDto();
        taskDto.setUserChain((List<String>)Optional.ofNullable(historicProcessInstance .getProcessVariables().get("userChain")).orElse(new ArrayList<>()));
        taskDto.setAssignee((String)historicProcessInstance .getProcessVariables().get("assigner"));
        taskDto.setActions((ArrayList<ActionDto>) Optional.ofNullable(historicProcessInstance .getProcessVariables().get("actions")).orElse(new ArrayList<>()));
        taskDto.setCreator((String) historicProcessInstance.getProcessVariables().get("initiator"));
        taskDto.setTitle((String) historicProcessInstance.getProcessVariables().get("title"));
        taskDto.setState((String) historicProcessInstance.getProcessVariables().get("state"));
        taskDto.setStartTime(historicProcessInstance.getStartTime());
        taskDto.setEndTime(historicProcessInstance.getEndTime());
        taskDto.setDescription((String) historicProcessInstance.getProcessVariables().get("description"));
        return taskDto;
    }

    @Transactional
    public List<ActionType> getAvailableActions(@NotNull String taskId, @NotNull String username) {
        List<Group> groups = identityService.createGroupQuery().groupMember(username).list();
        HistoricProcessInstance historicProcessInstance = historyService.createHistoricProcessInstanceQuery().processInstanceId(taskId).includeProcessVariables().involvedUser(username).singleResult();
        if (historicProcessInstance == null) {
            return new ArrayList<ActionType>();
        }
        List<ActionType> unAvailableActions = Optional.ofNullable((List<ActionType>) historicProcessInstance.getProcessVariables().get("unAvailableActions")).orElse(new ArrayList<>());
        return groups.stream()
                .map(Group::getId)
                .map(groupPermissionRepository::findGroupPermissionByGroupId)
                .flatMap((groupPermission) -> {
                    ArrayList<ActionType>  list = new ArrayList<>();
                    GroupPermission permission = groupPermission.orElse(new GroupPermission());
                    if ( username.equals(historicProcessInstance.getProcessVariables().get("assigner"))) {
                        list.addAll(permission.getActionTypesIfAssigner());
                    }
                    list.addAll(permission.getActionTypesIfNotAssigner());
                    return list.stream();
        })
                .filter( actionType ->  !unAvailableActions.contains(actionType))
                .distinct().collect(Collectors.toList());
    }
}
