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
        if (state.equals(StateType.CreatedState)) {
            statee.setUnavailableActions(Arrays.asList(ActionType.ReOpenAction,ActionType.CloseAction));

        } else if (state.equals(StateType.ClosedState)) {

            statee.setUnavailableActions(Arrays.asList(ActionType.RefinementAction, ActionType.DelegateAction, ActionType.CloseAction,ActionType.DoneAction,ActionType.ReOpenAction,ActionType.ChangeDescriptionAction,ActionType.CancelAction));

        } else if (state.equals(StateType.DoneState)) {
                statee.setUnavailableActions(Arrays.asList(ActionType.DelegateAction,ActionType.CancelAction,ActionType.DoneAction,ActionType.RefinementAction,ActionType.ChangeDescriptionAction));
        }
         else
            statee.setUnavailableActions(Arrays.asList(ActionType.ReOpenAction, ActionType.DelegateAction,ActionType.CancelAction,ActionType.DoneAction,ActionType.CloseAction,ActionType.RefinementAction,ActionType.ChangeDescriptionAction));

        return statee;
    }
    @Override
    public String toString() {
        return  state.toString();
    }
}

