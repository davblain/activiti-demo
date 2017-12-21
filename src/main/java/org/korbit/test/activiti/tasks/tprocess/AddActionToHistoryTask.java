package org.korbit.test.activiti.tasks.tprocess;

import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.JavaDelegate;
import org.korbit.test.activiti.dto.ActionDto;

import java.util.List;

public class AddActionToHistoryTask implements JavaDelegate{
    @Override
    public void execute(DelegateExecution delegateExecution) throws Exception {
        List<ActionDto> actions = (List<ActionDto>) delegateExecution.getVariable("actions");
        actions.add((ActionDto) delegateExecution.getVariable("action"));
        delegateExecution.setVariable("actions",actions);
    }
}
