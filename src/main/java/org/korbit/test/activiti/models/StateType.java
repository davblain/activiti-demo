package org.korbit.test.activiti.models;

import com.fasterxml.jackson.annotation.JsonFormat;

@JsonFormat(shape = JsonFormat.Shape.STRING)
public enum StateType {
    Created,Closed,Done,Expired,Cancelled;
}