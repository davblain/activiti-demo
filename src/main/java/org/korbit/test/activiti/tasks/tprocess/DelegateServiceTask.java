package org.korbit.test.activiti.tasks.tprocess;

import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.Expression;
import org.activiti.engine.delegate.JavaDelegate;
import org.korbit.test.activiti.dto.ActionDto;

import java.time.LocalTime;

public class DelegateServiceTask implements JavaDelegate {
    Expression assignee;
    @Override
    public void execute(DelegateExecution delegateExecution) throws Exception {
      ActionDto action = delegateExecution.getVariable("lastAction",ActionDto.class);
      delegateExecution.setVariable("assignee",action.getData().get("recipient"));
      action.setTime(LocalTime.now());
    }
}
