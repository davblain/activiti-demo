package org.korbit.test.activiti.models;

import com.fasterxml.jackson.annotation.JsonFormat;

@JsonFormat(shape = JsonFormat.Shape.STRING)
public enum StateType {
    CreatedState,ClosedState,DoneState,ExpiredState,CancelledState,OpenedState;
}