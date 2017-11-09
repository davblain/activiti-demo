package org.korbit.test.activiti.tasks.tprocess;

import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.JavaDelegate;

import java.util.ArrayList;
import java.util.List;

public class RefinementServiceTask implements JavaDelegate {
    @Override
    public void execute(DelegateExecution delegateExecution) throws Exception {
        ArrayList<String> userChain =  (ArrayList<String>)delegateExecution.getVariable("userChain") ;
        String assigner = userChain.get(userChain.size()-1);
        delegateExecution.setVariable("assigner",assigner);
    }
}
