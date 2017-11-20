package org.korbit.test.activiti.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Date;

@Setter
@Getter
public class TaskItemDto {
    String id;
    String creator;
    String title;
    String description;
    Date created;
    Date received;
    String sender;
    String status;


}
