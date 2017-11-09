package org.korbit.test.activiti.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TaskMailRequest {
    String creator;
    String recipient;
    String description;
    Long duration;
}
