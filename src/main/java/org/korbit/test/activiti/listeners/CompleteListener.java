package org.korbit.test.activiti.listeners;

import org.activiti.engine.delegate.DelegateTask;
import org.activiti.engine.delegate.TaskListener;
import org.korbit.test.activiti.dto.ActionHistoryDto;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Date;

public class CompleteListener implements TaskListener {
    @Override
    public void notify(DelegateTask delegateTask) {

        if ((ArrayList)delegateTask.getExecution().getVariable("actions")==null) {
            delegateTask.getExecution().setVariable("actions",new ArrayList<ActionHistoryDto>());
        }


        ArrayList<ActionHistoryDto> actions = (ArrayList<ActionHistoryDto>)delegateTask.getExecution().getVariable("actions");
        Duration dur = Duration.between(delegateTask.getCreateTime().toInstant(),new Date().toInstant());
        String  duration = delegateTask.getExecution().getVariable("duration").toString();
        delegateTask.getExecution().setVariable("duration",Duration.parse(duration).minus(dur).toString() );
    }
}
