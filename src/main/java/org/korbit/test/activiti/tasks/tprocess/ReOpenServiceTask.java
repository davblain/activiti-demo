package org.korbit.test.activiti.tasks.tprocess;

import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.JavaDelegate;
import org.korbit.test.activiti.models.ActionType;

import java.util.Arrays;

public class ReOpenServiceTask implements JavaDelegate {
    @Override
    public void execute(DelegateExecution delegateExecution) throws Exception {
        delegateExecution.setVariable("unAvailableActions", Arrays.asList(ActionType.ReOpen));
    }
}
