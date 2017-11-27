package org.korbit.test.activiti.tasks.tprocess;

import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.JavaDelegate;
import org.korbit.test.activiti.dto.ActionDto;

import java.util.ArrayList;
import java.util.List;

public class RefinementServiceTask implements JavaDelegate {
    @Override
    public void execute(DelegateExecution delegateExecution) throws Exception {
        ArrayList<String> userChain =  (ArrayList<String>)delegateExecution.getVariable("userChain") ;
        String assigner;
        if (userChain.size()==1) {
            assigner = userChain.get(userChain.size() - 1);
        } else {
            assigner = userChain.get(userChain.size() - 2);
        }
        List<ActionDto> actions = (List<ActionDto>) delegateExecution.getVariable("actions");
        actions.get(actions.size()-1).getData().put("recipient",assigner);
        delegateExecution.setVariable("actions",actions);
        delegateExecution.setVariable("assigner",assigner);
        userChain.add(assigner);
    }
}
