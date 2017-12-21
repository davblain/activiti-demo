package org.korbit.test.activiti.tasks.tprocess;

import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.JavaDelegate;
import org.korbit.test.activiti.dto.ActionDto;
import org.korbit.test.activiti.models.Action;
import org.korbit.test.activiti.models.ActionType;
import org.korbit.test.activiti.models.State;
import org.korbit.test.activiti.models.StateType;

public class ExpiredServiceTask implements JavaDelegate{

    @Override
    public void execute(DelegateExecution delegateExecution) throws Exception {
        delegateExecution.setVariable("state",StateType.ExpiredState.toString());
        ActionDto action = new ActionDto();
        action.setCreator("System");
        action.setType(ActionType.ExpireAction);
        delegateExecution.setVariable("action",action);
    }
}
