package org.korbit.test.activiti.listeners;

import org.activiti.engine.HistoryService;
import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.ExecutionListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class EndActionListener implements ExecutionListener {
    @Autowired
    HistoryService historyService;
    @Override
    public void notify(DelegateExecution delegateExecution) throws Exception {
        historyService.createHistoricActivityInstanceQuery().processInstanceId(delegateExecution.getVariable("test").toString()).singleResult().getActivityId();
        ;
    }
}
