package org.korbit.test.activiti.dto;

import lombok.Getter;
import lombok.Setter;
import org.korbit.test.activiti.domain.GroupPermission;

import javax.validation.constraints.NotNull;

@Getter
@Setter
public class TaskMailRequest {
    @NotNull
    String creator;
    @NotNull
    String title;
    @NotNull
    String recipient;
    @NotNull
    String description;
    @NotNull
    String duration;

}
