package org.korbit.test.activiti.models;

import com.fasterxml.jackson.annotation.JsonFormat;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import java.io.Serializable;

@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum ActionType implements Serializable{
    Delegate,
    Refinement,
    Done,
    Close,
    ReOpen,
    Cancel
}
