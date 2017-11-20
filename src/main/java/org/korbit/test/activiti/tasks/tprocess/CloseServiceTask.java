package org.korbit.test.activiti.tasks.tprocess;

import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.JavaDelegate;
import org.korbit.test.activiti.models.ActionType;

import java.util.ArrayList;
import java.util.Arrays;

public class CloseServiceTask implements JavaDelegate {
    @Override
    public void execute(DelegateExecution delegateExecution) throws Exception {
        delegateExecution.setVariable("assigner",delegateExecution.getVariable("initiator"));
        delegateExecution.setVariable("unAvailableActions", Arrays.asList(ActionType.Close,ActionType.Delegate,ActionType.Refinement));
        delegateExecution.setVariable("state","closed");
    }
}
