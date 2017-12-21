package org.korbit.test.activiti.models;

import lombok.Getter;
import lombok.Setter;
import org.activiti.engine.form.FormProperty;

import java.util.ArrayList;
import java.util.List;
@Getter
@Setter
public class Action {
    String actionType;
    boolean isStep = true;
    List<FormProperty> properties = new ArrayList<>();

}
