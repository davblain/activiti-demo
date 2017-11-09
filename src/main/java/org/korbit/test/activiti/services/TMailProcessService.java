package org.korbit.test.activiti.services;


import org.activiti.engine.IdentityService;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
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
import java.util.*;
import java.util.stream.Collectors;

@Service
public class TMailProcessService {
    final private RuntimeService runtimeService;
    final private IdentityService identityService;
    final private TaskService taskService;
    final private RepositoryService repositoryService;
    final private GroupPermissionRepository groupPermissionRepository;


    public TMailProcessService(RuntimeService runtimeService,IdentityService identityService,
                               TaskService taskService,RepositoryService repositoryService,GroupPermissionRepository groupPermissionRepository) {
        this.runtimeService = runtimeService;
        this.identityService = identityService;
        this.taskService = taskService;
        this.repositoryService = repositoryService;
        this.groupPermissionRepository = groupPermissionRepository;
    }
    @Transactional
    public String startTask(@NotNull TaskMailRequest task) {
        try {
            identityService.setAuthenticatedUserId(task.getCreator());
            Map<String,Object> variables = new HashMap<>();
            variables.put("assignee",task.getRecipient());
            variables.put("duration",10);
            variables.put("description", task.getDescription());
            ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("tMailProcess",variables);
            return processInstance.getId();
        } finally {
            identityService.setAuthenticatedUserId(null);
        }
    }
    public List<TaskItemDto> getListOfTaskByUsername(@NotNull String username) {
        return  taskService.createTaskQuery().taskAssignee(username).includeProcessVariables().list().stream().map( task -> {
            TaskItemDto t = new TaskItemDto();
            System.out.println(task.getProcessVariables().get("initiator").toString());
            t.setCreator((String)(task.getProcessVariables().get("initiator")));
            t.setDescription((String)(task.getProcessVariables().get("description")));
            t.setId(task.getProcessInstanceId());
            return  t;
        }).collect(Collectors.toList());
    }
    public void doAction(@NotNull StepRequest stepRequest) {
        Task task = Optional.ofNullable(taskService.createTaskQuery().processInstanceId(stepRequest.getTaskId()).includeProcessVariables().singleResult())
                .orElseThrow(() -> new TaskNotFoundException(stepRequest.getTaskId()));
        Map<String, Object> taskVariables = new HashMap<String, Object>();
        taskVariables.put("lastAction",stepRequest.getAction());
        taskService.complete(task.getId(),taskVariables);
    }
    public List<ActionDto> getListOfActions(@NotNull String taskId) {
        Task task = Optional.ofNullable(taskService.createTaskQuery().processInstanceId(taskId).includeProcessVariables().singleResult())
                .orElseThrow(() -> new TaskNotFoundException(taskId));
        return Optional.ofNullable((ArrayList<ActionDto>) task.getProcessVariables().get("actions")).orElse(new ArrayList<>());

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
        Task task = taskService.createTaskQuery().processInstanceId(taskId).includeProcessVariables().singleResult();
        TaskDto taskDto = new TaskDto();
        taskDto.setUserChain((List<String>)Optional.ofNullable(task.getProcessVariables().get("userChain")).orElse(new ArrayList<>()));
        taskDto.setAssignee(task.getAssignee());
        taskDto.setActions((ArrayList<ActionDto>) Optional.ofNullable(task.getProcessVariables().get("actions")).orElse(new ArrayList<>()));
        taskDto.setCreator((String) task.getProcessVariables().get("initiator"));
        taskDto.setState((String) task.getProcessVariables().get("state"));
        return taskDto;
    }

    @Transactional
    public List<ActionType> getAvailableActions(@NotNull String taskId, @NotNull String username) {
        List<Group> groups = identityService.createGroupQuery().groupMember(username).list();
        return groups.stream()
                .map(Group::getId)
                .map(groupPermissionRepository::findGroupPermissionByGroupId)
                .flatMap(groupPermission -> groupPermission.orElse(new GroupPermission()).getActionTypes().stream())
                .distinct().collect(Collectors.toList());
    }
}
