package org.korbit.test.activiti.cotrollers;

import org.korbit.test.activiti.dto.ActionDto;
import org.korbit.test.activiti.dto.TaskDto;
import org.korbit.test.activiti.dto.TaskMailRequest;
import org.korbit.test.activiti.models.Action;
import org.korbit.test.activiti.services.TMailProcessService;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("api")
public class TaskController {
    final private TMailProcessService tMailProcessService;

    TaskController(TMailProcessService tMailProcessService) {
        this.tMailProcessService = tMailProcessService;
    }
    @PostMapping("tasks")
    String  startTaskProcess(@RequestBody TaskMailRequest task, Principal principal) {
        task.setCreator(principal.getName());
        return tMailProcessService.startTask(task);
    }
    @PostMapping("tasks/{taskId}/dostep")
    void doStep(@RequestBody ActionDto actionDto, @PathVariable String taskId, Principal principal) {
        actionDto.setCreator(principal.getName());
        tMailProcessService.doAction(taskId,actionDto);
    }
    @GetMapping("tasks/{taskId}/action-list")
    List<ActionDto> getHistoryList(@PathVariable String taskId) {
        return tMailProcessService.getListOfActions(taskId);
    }
    @GetMapping("tasks/{taskId}/available-actions")
    List<Action> getAvailableActions(@PathVariable String taskId, Principal principal) {
        return tMailProcessService.getAvailableActions(taskId,principal.getName()).stream()
                .map(Action::createActionByActionType).collect(Collectors.toList());
    }
    @GetMapping("tasks/{taskId}")
    TaskDto getTaskDetails(@PathVariable String taskId) {
        return  tMailProcessService.getTaskDetails(taskId);
    }
    @GetMapping("tasks/{taskId}/user-chain")
    List<String> getListOfAssigners(@PathVariable String taskId) {
        return tMailProcessService.getListOfAssigners(taskId);
    }
}
