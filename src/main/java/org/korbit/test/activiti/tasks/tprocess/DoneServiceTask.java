package org.korbit.test.activiti.tasks.tprocess;

import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.JavaDelegate;
import org.korbit.test.activiti.dto.ActionDto;
import org.korbit.test.activiti.models.Action;
import org.korbit.test.activiti.models.ActionType;
import org.korbit.test.activiti.models.State;
import org.korbit.test.activiti.models.StateType;

import java.util.Arrays;
import java.util.List;

public class DoneServiceTask implements JavaDelegate {

    @Override
    public void execute(DelegateExecution delegateExecution) throws Exception {
        delegateExecution.setVariable("state", StateType.DoneState.toString());
        String user = delegateExecution.getVariable("initiator").toString();;
        delegateExecution.setVariable("assigner",user);
    }
}
