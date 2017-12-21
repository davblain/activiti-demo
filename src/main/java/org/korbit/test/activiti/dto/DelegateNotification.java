package org.korbit.test.activiti.dto;

import lombok.Data;

import java.util.Date;
import java.util.Optional;

@Data
public class DelegateNotification extends Notification {
    String taskId;

    public DelegateNotification (String taskId, String creator) {
        super(creator);
        this.taskId = taskId;
    }
}
