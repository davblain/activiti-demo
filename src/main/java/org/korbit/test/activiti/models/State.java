package org.korbit.test.activiti.models;

import lombok.Getter;
import lombok.Setter;

import javax.swing.Action;
import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
@Getter
@Setter
public class State  implements Serializable{
    List<ActionType> unavailableActions;
    StateType state;
    private State(StateType state) {
        this.state = state;
    }
    static public State instanceState(StateType state){
        State statee = new  State(state);
        if (state.equals(StateType.Created)) {
            statee.setUnavailableActions(Arrays.asList(ActionType.ReOpen));
        } else if (state.equals(StateType.Closed)) {
            statee.setUnavailableActions(Arrays.asList(ActionType.Refinement,ActionType.Delegate,ActionType.Close));
        } else
            statee.setUnavailableActions(Arrays.asList(ActionType.ReOpen, ActionType.Delegate,ActionType.Cancel,ActionType.Done,ActionType.Close,ActionType.Refinement,ActionType.ChangeDescription));

        return statee;
    }
    @Override
    public String toString() {
        return  state.toString();
    }
}

