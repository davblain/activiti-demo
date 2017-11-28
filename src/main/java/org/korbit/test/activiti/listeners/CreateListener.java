package org.korbit.test.activiti.listeners;

import org.activiti.engine.delegate.DelegateTask;
import org.activiti.engine.delegate.TaskListener;

import java.util.ArrayList;
import java.util.List;

public class CreateListener implements TaskListener {
    @Override
    public void notify(DelegateTask delegateTask) {

       delegateTask.getExecution().setVariable("lastUserTaskBeginTime",delegateTask.getCreateTime());

    }
}
