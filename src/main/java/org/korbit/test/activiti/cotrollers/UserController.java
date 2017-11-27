package org.korbit.test.activiti.cotrollers;

import org.korbit.test.activiti.dto.*;
import org.korbit.test.activiti.models.Action;
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

    @GetMapping("users/{username}/tasks/{filter}")
    Page<TaskItemDto> getListOfTasks(@PathVariable  String username, @PathVariable(name = "filter") String filter, @RequestParam(required = false, defaultValue = "1") Integer page, @RequestParam(required = false, defaultValue ="10" ) Integer limit){
        if (filter.equals("all")) return tMailProcessService.getAllTasksOfUser(username,page,limit);
        else if(filter.equals("completed")) return tMailProcessService.getDoneWithUserTasks(username,page,limit);
        else if(filter.equals("current")) return tMailProcessService.getListOfCurrentTaskByUsername(username,page,limit);
        else if (filter.equals("closed")) return tMailProcessService.getClosedTasksOfUser(username,page,limit);
        else if (filter.equals("created-by-user")) return tMailProcessService.getListOfTaskCreatedByUser(username,page,limit);
        else  {
            Page<TaskItemDto> pagee =  new Page<TaskItemDto>();
            pagee.setNumber(page);
            return  pagee;
        }
    }

    @GetMapping("users")
    List<UserDto> getUsers(@RequestParam(required = false,defaultValue = "") String filter) {
        return userService.getListOfUsers(filter);
    }





}
