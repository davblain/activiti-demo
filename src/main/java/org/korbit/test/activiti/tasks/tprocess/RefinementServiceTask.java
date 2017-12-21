package org.korbit.test.activiti.tasks.tprocess;

import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.JavaDelegate;
import org.korbit.test.activiti.dto.ActionDto;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class    RefinementServiceTask implements JavaDelegate {

    @Override
    public void execute(DelegateExecution delegateExecution) throws Exception {
        ArrayList<String> userChain =  Optional.ofNullable((ArrayList<String>)delegateExecution.getVariable("userChain")).orElse(new ArrayList<>()) ;
        String assigner;
        if (userChain.size() == 1) {
            assigner = userChain.get(userChain.size() - 1);
        } else {
            assigner = userChain.get(userChain.size() - 2);
        }
        delegateExecution.setVariable("assigner",assigner);
    }
}
