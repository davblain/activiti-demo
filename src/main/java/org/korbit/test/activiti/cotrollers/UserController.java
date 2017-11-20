package org.korbit.test.activiti.cotrollers;

import org.korbit.test.activiti.dto.*;
import org.korbit.test.activiti.models.ActionType;
import org.korbit.test.activiti.services.TMailProcessService;
import org.korbit.test.activiti.services.UserService;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("api")
public class UserController {
    final private TMailProcessService tMailProcessService;
    final private UserService userService;
    UserController(TMailProcessService tMailProcessService, UserService userService) {
        this.tMailProcessService = tMailProcessService;
        this.userService = userService;
    }
    @PostMapping("tasks")
    String  startTaskProcess(@RequestBody TaskMailRequest task, Principal principal) {
        System.out.println("break");
        task.setCreator(principal.getName());
        return tMailProcessService.startTask(task);
    }
    @GetMapping("users/{username}/tasks/{filter}")
    Page<TaskItemDto> getListOfTasks(@PathVariable  String username, @PathVariable(name = "filter") String filter, @RequestParam(required = false, defaultValue = "1") Integer page, @RequestParam(required = false, defaultValue ="10" ) Integer limit){
        if (filter.equals("all")) return tMailProcessService.getAllTasksOfUser(username,page,limit);
        else if(filter.equals("completed")) return tMailProcessService.getDoneWithUserTasks(username,page,limit);
        else if(filter.equals("current")) return tMailProcessService.getListOfCurrentTaskByUsername(username,page,limit);
        else if (filter.equals("closed")) return tMailProcessService.getClosedTasksOfUser(username,page,limit);

        else  {
            Page<TaskItemDto> pagee =  new Page<TaskItemDto>();
            pagee.setNumber(page);
            return  pagee;
        }
    }

    @GetMapping("users")
    List<UserDto> getUsers() {
        return userService.getListOfUsers();
    }
    @PostMapping("dostep")
    void doStep(@RequestBody StepRequest step, Principal principal) {
        tMailProcessService.doAction(step);
    }
    @GetMapping("tasks/{taskId}/action-list")
    List<ActionDto> getHistoryList(@PathVariable String taskId) {
        return tMailProcessService.getListOfActions(taskId);
    }
    @GetMapping("tasks/{taskId}/user-chain")
    List<String> getListOfAssigners(@PathVariable String taskId) {
        return tMailProcessService.getListOfAssigners(taskId);
    }
    @GetMapping("tasks/{taskId}/available-actions")
    List<ActionType> getAvailableActions(@PathVariable String taskId, Principal principal) {
       return tMailProcessService.getAvailableActions(taskId,principal.getName());

    }
    @GetMapping("tasks/{taskId}")
    TaskDto getTaskDetails(@PathVariable String taskId) {
        return  tMailProcessService.getTaskDetails(taskId);
    }


}
