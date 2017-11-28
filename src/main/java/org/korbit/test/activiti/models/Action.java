package org.korbit.test.activiti.models;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
@Getter
@Setter
public class Action {
    ActionType actionType;
    boolean isStep;
    List<String> properties = new ArrayList<>();
    private Action(ActionType actionType) {
        this.actionType = actionType;
    }

    static  public Action createActionByActionType(ActionType actionType) {
        Action action = new Action(actionType);
        action.isStep = true;
        if (actionType.equals(ActionType.Create)||actionType.equals(ActionType.Delegate)){
           action.properties.add("recipient");
        }
        if (actionType.equals(ActionType.ChangeDescription)) {
            action.properties.add("Description");
            action.isStep = false;
        }
        return action;
    }
}
