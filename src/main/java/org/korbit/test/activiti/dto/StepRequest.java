package org.korbit.test.activiti.dto;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotNull;

@Getter
@Setter
public class StepRequest {

    @NotNull
    String taskId;
    @NotNull
    ActionDto action;
    public StepRequest(String taskId,ActionDto actionDto) {
        this.taskId = taskId;
        this.action = actionDto;
    }
    public StepRequest() {}


}
