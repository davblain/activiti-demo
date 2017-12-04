package org.korbit.test.activiti.cotrollers;

import org.korbit.test.activiti.dto.*;
import org.korbit.test.activiti.models.Action;
import org.korbit.test.activiti.models.ActionType;
import org.korbit.test.activiti.security.TokenUtils;
import org.korbit.test.activiti.services.TMailProcessService;
import org.korbit.test.activiti.services.UserService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("api")
public class UserController {
    final private TMailProcessService tMailProcessService;
    final private UserService userService;
    final private UserDetailsService userDetailsService;
    final private AuthenticationManager authenticationManager;
    final  private TokenUtils tokenUtils;
    UserController(TMailProcessService tMailProcessService, UserService userService, AuthenticationManager authenticationManager,
                   TokenUtils tokenUtils, UserDetailsService userDetailsService) {
        this.tMailProcessService = tMailProcessService;
        this.userService = userService;
        this.userDetailsService = userDetailsService;
        this.authenticationManager = authenticationManager;
        this.tokenUtils = tokenUtils;
    }

    @GetMapping("users/{username}/tasks/{filter}")
    Page<TaskItemDto> getListOfTasks(@PathVariable  String username, @PathVariable(name = "filter") String filter, @RequestParam(required = false, defaultValue = "1") Integer page, @RequestParam(required = false, defaultValue ="10" ) Integer limit){
        if (filter.equals("all")) return tMailProcessService.getAllTasksOfUser(username,page,limit);
        else if(filter.equals("completed")) return tMailProcessService.getDoneWithUserTasks(username,page,limit);
        else if(filter.equals("current")) return tMailProcessService.getListOfCurrentTaskByUsername(username,page,limit);
        else if (filter.equals("closed")) return tMailProcessService.getClosedTasksOfUser(username,page,limit);
        else if (filter.equals("created-by-user")) return tMailProcessService.getListOfTaskCreatedByUser(username,page,limit);
        else  if (filter.equals("expired")) return  tMailProcessService.getListOfExpiredTasks(username,page,limit);
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
    @PostMapping("login")
    LoginResponse sign_in(@RequestBody LoginRequest user) {
        Authentication authentication = this.authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        user.getUsername(),
                        user.getPassword() )
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);
        UserDetails userDetails = this.userDetailsService.loadUserByUsername(user.getUsername());
        LoginResponse response = new LoginResponse();
        response.setToken(this.tokenUtils.generateToken(userDetails));
        response.setUsername(userDetails.getUsername());
        response.setExpirationTime(this.tokenUtils.getExpirationDateFromToken(response.getToken()));

        return response;
    }





}
