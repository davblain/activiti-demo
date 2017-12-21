package org.korbit.test.activiti.services;

import org.activiti.engine.HistoryService;
import org.activiti.engine.TaskService;
import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.JavaDelegate;
import org.korbit.test.activiti.dto.ActionDto;
import org.korbit.test.activiti.dto.DelegateNotification;
import org.korbit.test.activiti.dto.Notification;
import org.korbit.test.activiti.models.Action;
import org.korbit.test.activiti.models.ActionType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
public class NotificationService  implements JavaDelegate{
    @Autowired
    private SimpMessagingTemplate template;

    @Override
    public void execute(DelegateExecution delegateExecution) throws Exception {
        ActionDto action = delegateExecution.getVariable("action",ActionDto.class);
        String assigner = action.getData().get("recipient");
        System.out.println("test2");
        template.convertAndSendToUser(assigner, "/queue/private", new DelegateNotification(delegateExecution.getProcessInstanceId(),action.getCreator()));

    }

}
