package org.korbit.test.activiti.services;


import org.activiti.engine.*;
import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.history.HistoricProcessInstanceQuery;
import org.activiti.engine.identity.Group;
import org.activiti.engine.identity.User;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.korbit.test.activiti.domain.GroupPermission;
import org.korbit.test.activiti.dto.*;
import org.korbit.test.activiti.exceptions.TaskNotFoundException;
import org.korbit.test.activiti.exceptions.UserNotFoundException;
import org.korbit.test.activiti.models.ActionType;
import org.korbit.test.activiti.models.State;
import org.korbit.test.activiti.models.StateType;
import org.korbit.test.activiti.repository.GroupPermissionRepository;
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

    private ActionDto getLastDelegateToUser(@NotNull String taskId,@NotNull String username) {
       List<ActionDto> actionsList = getListOfActions(taskId).stream()
               .filter(actionDto -> actionDto.getType().equals(ActionType.Delegate)
               || actionDto.getType().equals(ActionType.Refinement)
               ||actionDto.getType().equals(ActionType.Create))
               .filter(actionDto -> actionDto.getData().get("recipient").equals(username)).collect(Collectors.toList());
       if (actionsList.size()==0) return null;
       return actionsList.get(actionsList.size()-1);
    }

    public Page<TaskItemDto> getListOfTaskCreatedByUser(@NotNull String username,Integer page,Integer limit) {
        HistoricProcessInstanceQuery query = historyService.createHistoricProcessInstanceQuery().variableValueEquals("initiator",username);
        return generatePage(query,username,page,limit);

    }
    public Page<TaskItemDto> getListOfCurrentTaskByUsername(@NotNull String username,Integer page ,Integer limit) {

        HistoricProcessInstanceQuery query = historyService.createHistoricProcessInstanceQuery().variableValueEquals("assigner",username).unfinished();
        return generatePage(query,username,page,limit);
    }
    @Transactional
    public Page<TaskItemDto> getDoneWithUserTasks(@NotNull String username,Integer page, Integer limit) {
        HistoricProcessInstanceQuery query = historyService.createHistoricProcessInstanceQuery().involvedUser(username).variableValueEquals("State","Done");
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

    public void doAction(@NotNull String taskId, ActionDto actionDto) {
        Task task = Optional.ofNullable(taskService.createTaskQuery().processInstanceId(taskId).includeProcessVariables().singleResult())
                .orElseThrow(() -> new TaskNotFoundException(taskId));
        Map<String, Object> taskVariables = new HashMap<String, Object>();
        taskVariables.put("actionToValidate",actionDto);
        taskService.complete(task.getId(),taskVariables);
    }
    public List<ActionDto> getListOfActions(@NotNull String taskId) {
        HistoricProcessInstance historicProcessInstance = Optional.ofNullable(historyService.createHistoricProcessInstanceQuery()
                .processInstanceId(taskId).includeProcessVariables().singleResult())
                .orElseThrow(() -> new TaskNotFoundException(taskId));

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
    private Page<TaskItemDto> generatePage(HistoricProcessInstanceQuery query,String username, Integer page,Integer limit) {
        Page<TaskItemDto> pagee = new  Page<>();
        List<TaskItemDto> content = query.includeProcessVariables().listPage(page-1,limit).stream().map( task ->  taskToTaskItemDto(task,username)).collect(Collectors.toList());
        pagee.setContent(content);
        pagee.setTotalElements(query.count());
        pagee.setTotalPages(pagee.getTotalElements()/limit+1);
        return pagee;
    }

    public TaskDto getTaskDetails(@NotNull String taskId) {
        HistoricProcessInstance historicProcessInstance = historyService.createHistoricProcessInstanceQuery().processInstanceId(taskId).includeProcessVariables().singleResult();
        TaskDto taskDto = new TaskDto();
        taskDto.setUserChain((List<String>)Optional.ofNullable(historicProcessInstance.getProcessVariables().get("userChain")).orElse(new ArrayList<>()));
        taskDto.setAssignee((String)historicProcessInstance .getProcessVariables().get("assigner"));
        taskDto.setActions((ArrayList<ActionDto>) Optional.ofNullable(historicProcessInstance .getProcessVariables().get("actions")).orElse(new ArrayList<>()));
        taskDto.setCreator((String) historicProcessInstance.getProcessVariables().get("initiator"));
        taskDto.setTitle((String) historicProcessInstance.getProcessVariables().get("title"));
        taskDto.setState(historicProcessInstance.getProcessVariables().get("state").toString());
        taskDto.setStartTime(historicProcessInstance.getStartTime());
        taskDto.setEndTime(historicProcessInstance.getEndTime());
        taskDto.setDescription((String) historicProcessInstance.getProcessVariables().get("description"));
        return taskDto;
    }

    @Transactional
    public List<ActionType> getAvailableActions(@NotNull String taskId, @NotNull String username) {
        List<Group> groups = identityService.createGroupQuery().groupMember(username).list();
        HistoricProcessInstance historicProcessInstance = historyService.createHistoricProcessInstanceQuery()
                .processInstanceId(taskId).includeProcessVariables().involvedUser(username).singleResult();
        if (historicProcessInstance == null) {
            return new ArrayList<ActionType>();
        }

        State  state = State.instanceState(StateType.valueOf(historicProcessInstance.getProcessVariables().get("state").toString())
        );
        List<ActionType> unAvailableActions = Optional.ofNullable(state.getUnavailableActions()).orElse(new ArrayList<>());
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
