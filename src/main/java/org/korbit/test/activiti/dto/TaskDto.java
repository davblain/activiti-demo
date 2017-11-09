package org.korbit.test.activiti.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;
@Getter
@Setter
public class TaskDto {
    String creator;
    List<ActionDto> actions;
    List<String> userChain;
    String description;
    String assignee;
    String state;
}
