package org.korbit.test.activiti.tasks.tprocess;

import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.JavaDelegate;
import org.korbit.test.activiti.models.ActionType;
import org.korbit.test.activiti.models.State;
import org.korbit.test.activiti.models.StateType;

import java.util.ArrayList;
import java.util.Arrays;

public class CloseServiceTask implements JavaDelegate {
    @Override
    public void execute(DelegateExecution delegateExecution) throws Exception {
        delegateExecution.setVariable("assigner",delegateExecution.getVariable("initiator"));

        delegateExecution.setVariable("state", StateType.Closed.toString());
    }
}
