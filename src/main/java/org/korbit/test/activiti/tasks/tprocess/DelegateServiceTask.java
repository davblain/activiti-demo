package org.korbit.test.activiti.tasks.tprocess;

import org.activiti.engine.IdentityService;
import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.JavaDelegate;
import org.activiti.engine.identity.User;
import org.korbit.test.activiti.exceptions.UserNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service

public class DelegateServiceTask implements JavaDelegate {
    @Autowired
    IdentityService identityService;
    @Autowired
    private SimpMessagingTemplate template;
    @Override
    public void execute(DelegateExecution delegateExecution) throws Exception {
      String recipient = delegateExecution.getVariable("recipient").toString();
      User user = Optional.ofNullable(identityService.createUserQuery().userId(recipient).singleResult())
              .orElseThrow(() -> new UserNotFoundException(recipient));
        List<String> userChain =  Optional.ofNullable((ArrayList<String>)delegateExecution.getVariable("userChain"))
                .orElse(new ArrayList<>());
        userChain.add(recipient);
        delegateExecution.setVariable("userChain",userChain);
        delegateExecution.setVariable("assigner",recipient);
        //template.convertAndSendToUser(recipient, "/queue/private", new DelegateNotification(delegateExecution.getProcessInstanceId(),action.getCreator()));
    }
}
