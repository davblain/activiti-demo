package org.korbit.test.activiti.cotrollers;

import org.korbit.test.activiti.dto.ActionDto;
import org.korbit.test.activiti.dto.TaskDto;
import org.korbit.test.activiti.dto.TaskMailRequest;
import org.korbit.test.activiti.models.Action;
import org.korbit.test.activiti.models.ActionType;
import org.korbit.test.activiti.services.TMailProcessService;
import org.korbit.test.activiti.services.TasksService;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.security.Principal;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("api")
public class TaskController {
    final private TMailProcessService tMailProcessService;
    final  private  TasksService tasksService;
    TaskController(TMailProcessService tMailProcessService, TasksService tasksService) {
        this.tasksService = tasksService;
        this.tMailProcessService = tMailProcessService;
    }
    @PostMapping("tasks")
    String  startTaskProcess(@RequestBody @Valid TaskMailRequest task, Principal principal) {
        task.setCreator(principal.getName());
        return tasksService.startTask(task);
    }
    @PutMapping("tasks/{taskId}")
    String changeDescription(Principal principal,@PathVariable String taskId,@RequestBody String description){
        if (tMailProcessService.getNonAssignNeedActionTypes(taskId,principal.getName()).contains(ActionType.ChangeDescription)){
            tasksService.changeDescriptionOfTask(taskId,description);
            return "SUCCESS";
        }
        return "ERR";
    }
    @PostMapping("tasks/{taskId}/dostep")
    void doStep(@RequestBody @Valid ActionDto actionDto, @PathVariable String taskId, Principal principal) {
        actionDto.setCreator(principal.getName());
        tasksService.doAction(taskId,actionDto);
    }
    @GetMapping("tasks/{taskId}/action-list")
    List<ActionDto> getHistoryList(@PathVariable String taskId) {
        return tasksService.getListOfActions(taskId);
    }
    @GetMapping("tasks/{taskId}/available_actions")
    List<Action> getAvailableActions(@PathVariable String taskId, Principal principal) {
        return tMailProcessService.getAvailableActionTypes(taskId,principal.getName()).stream()
                .map(Action::createActionByActionType).collect(Collectors.toList());
    }
    @GetMapping("task/{taskId}/non_assign_need_available_actions")
    List<Action> getNonStepAvailableActions(@PathVariable String taskId,Principal principal) {
        return tMailProcessService.getNonAssignNeedActionTypes(taskId,principal.getName()).stream()
                .map((Action::createActionByActionType)).collect(Collectors.toList());
    }
    @GetMapping("tasks/{taskId}")
    TaskDto getTaskDetails(@PathVariable String taskId) {
        return  tasksService.getTaskDetails(taskId);
    }
    @GetMapping("tasks/{taskId}/user-chain")
    List<String> getListOfAssigners(@PathVariable String taskId) {
        return tasksService.getListOfAssigners(taskId);
    }
}
