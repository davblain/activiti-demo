package org.korbit.test.activiti.tasks.tprocess;

import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.JavaDelegate;

public class CancelServiceTask implements JavaDelegate{
    @Override
    public void execute(DelegateExecution delegateExecution) throws Exception {
        delegateExecution.setVariable("state","cancelled");
    }
}
