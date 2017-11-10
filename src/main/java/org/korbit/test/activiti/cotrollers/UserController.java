package org.korbit.test.activiti.cotrollers;

import org.korbit.test.activiti.dto.*;
import org.korbit.test.activiti.models.ActionType;
import org.korbit.test.activiti.services.TMailProcessService;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.*;

@RestController
public class UserController {
    final private TMailProcessService tMailProcessService;
    UserController(TMailProcessService tMailProcessService) {
        this.tMailProcessService = tMailProcessService;
    }
    @PostMapping("start")
    String  startTaskProcess(@RequestBody TaskMailRequest task, Principal principal) {
        task.setCreator(principal.getName());
        return tMailProcessService.startTask(task);
    }
    @GetMapping("users/{username}/tasks")
    List<TaskItemDto> getListOfTasks(@PathVariable  String username){
      return tMailProcessService.getListOfCurrentTaskByUsername(username);
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
