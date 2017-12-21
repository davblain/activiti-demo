package org.korbit.test.activiti.models;

import lombok.Getter;
import lombok.Setter;
import org.activiti.engine.form.FormProperty;

import java.util.ArrayList;
import java.util.List;
@Getter
@Setter
public class ActionEntity {
    String actionType;
    boolean isStep = true;
    List<String> authority = new ArrayList<>();
    List<FormProperty> properties = new ArrayList<>();

}
