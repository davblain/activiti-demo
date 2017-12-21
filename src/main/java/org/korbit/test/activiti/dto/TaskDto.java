package org.korbit.test.activiti.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;
import java.util.List;
@Getter
@Setter
public class TaskDto {
    String creator;
    Date startTime;
    Date endTime;
    List<ActionHistoryDto> actions;
    List<String> userChain;
    String title;
    String description;
    String assignee;
    String state;
    String curDuration;
}
