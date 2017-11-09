package org.korbit.test.activiti.exceptions;

public class TaskNotFoundException extends RuntimeException {
    public TaskNotFoundException(String taskId) {
        super("Task with id:"+taskId + " not found");
    }
}
