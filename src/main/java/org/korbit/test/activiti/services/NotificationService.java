package org.korbit.test.activiti.services;

import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.JavaDelegate;
import org.korbit.test.activiti.dto.ActionHistoryDto;
import org.korbit.test.activiti.dto.DelegateNotification;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
public class NotificationService  implements JavaDelegate{
    @Autowired
    private SimpMessagingTemplate template;

    @Override
    public void execute(DelegateExecution delegateExecution) throws Exception {
        ActionHistoryDto action = delegateExecution.getVariable("action",ActionHistoryDto.class);
        String assigner = action.getData().get("recipient");
        System.out.println("test2");
        template.convertAndSendToUser(assigner, "/queue/private", new DelegateNotification(delegateExecution.getProcessInstanceId(),action.getCreator()));

    }

}
