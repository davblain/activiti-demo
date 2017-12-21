package org.korbit.test.activiti.models;

import com.fasterxml.jackson.annotation.JsonFormat;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import java.io.Serializable;

@JsonFormat(shape = JsonFormat.Shape.STRING)
public enum ActionType implements Serializable{
    DelegateAction,
    RefinementAction,
    DoneAction,
    CloseAction,
    ReOpenAction,
    CreateAction,
    ChangeDescriptionAction,
    CancelAction,
    ExpireAction
}
