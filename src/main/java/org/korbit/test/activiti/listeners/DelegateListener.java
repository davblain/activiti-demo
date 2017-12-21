package org.korbit.test.activiti.listeners;

import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.ExecutionListener;
import org.korbit.test.activiti.dto.ActionDto;
import org.korbit.test.activiti.dto.DelegateNotification;
import org.korbit.test.activiti.models.Action;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
@Component
public class DelegateListener implements ExecutionListener {
    @Autowired
    private SimpMessagingTemplate template;
    @Override
    public void notify(DelegateExecution delegateExecution) throws Exception {

        ActionDto action = (ActionDto) delegateExecution.getVariable("action");

        List<String> userChain =  Optional.ofNullable((ArrayList<String>)delegateExecution.getVariable("userChain"))
                .orElse(new ArrayList<>());
        String assigner = action.getData().get("recipient");
        userChain.add(assigner);
        delegateExecution.setVariable("assigner",assigner);
        delegateExecution.setVariable("userChain",userChain);
        template.convertAndSendToUser(assigner, "/queue/private", new DelegateNotification(delegateExecution.getProcessInstanceId(),action.getCreator()));
    }
}
