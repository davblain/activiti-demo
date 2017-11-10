package org.korbit.test.activiti.listeners;

import org.activiti.engine.delegate.DelegateTask;
import org.activiti.engine.delegate.TaskListener;
import org.korbit.test.activiti.dto.ActionDto;

import java.util.ArrayList;

public class CompleteListener implements TaskListener {
    @Override
    public void notify(DelegateTask delegateTask) {
        if ((ArrayList)delegateTask.getExecution().getVariable("userChain")== null) {
           delegateTask.getExecution().setVariable("userChain",new ArrayList<>());
        }

        if ((ArrayList)delegateTask.getExecution().getVariable("actions")==null) {
            delegateTask.getExecution().setVariable("actions",new ArrayList<ActionDto>());
        }
        ArrayList<String> userChain = (ArrayList<String>) delegateTask.getExecution().getVariable("userChain");
        userChain.add(delegateTask.getAssignee());
        delegateTask.getExecution().setVariable("userChain",userChain);
        ArrayList<ActionDto> actions = (ArrayList<ActionDto>)delegateTask.getExecution().getVariable("actions");
        //ActionDto lastAction = delegateTask.getExecution().getVariable("lastAction",ActionDto.class);
       // actions.add(lastAction);
        //delegateTask.getExecution().setVariable("state",lastAction.getType().toString() );

    }
}
