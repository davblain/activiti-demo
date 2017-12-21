package org.korbit.test.activiti.listeners;

import org.activiti.engine.RepositoryService;
import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.ExecutionListener;
import org.korbit.test.activiti.dto.ActionHistoryDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component
public class AddHistoryListener implements ExecutionListener{
    @Autowired
    private SimpMessagingTemplate template;
    @Autowired
    private RepositoryService repositoryService;
    @Override
    public void notify(DelegateExecution delegateExecution) throws Exception {

        List<ActionHistoryDto> actions = Optional.ofNullable((List<ActionHistoryDto>) delegateExecution.getVariable("actions"))
                .orElse(new ArrayList<>());
        ActionHistoryDto action = (ActionHistoryDto) delegateExecution.getVariable("action");
        actions.add(action);
        System.out.println(delegateExecution.getProcessInstanceId());
        template.convertAndSend("/app/"+ delegateExecution.getProcessInstanceId(),action);
        delegateExecution.setVariable("actions",actions);
    }
}
