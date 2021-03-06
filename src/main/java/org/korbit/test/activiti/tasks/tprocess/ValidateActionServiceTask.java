package org.korbit.test.activiti.tasks.tprocess;

import org.activiti.engine.HistoryService;
import org.activiti.engine.IdentityService;
import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.JavaDelegate;
import org.korbit.test.activiti.dto.ActionDto;
import org.korbit.test.activiti.exceptions.NoPermissionException;
import org.korbit.test.activiti.models.Action;
import org.korbit.test.activiti.models.ActionType;
import org.korbit.test.activiti.services.TMailProcessService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ValidateActionServiceTask implements JavaDelegate {
    @Autowired
    TMailProcessService tMailProcessService ;
    @Autowired
    private IdentityService identityService;
    @Autowired HistoryService historyService;
    @Override
    public void execute(DelegateExecution delegateExecution) throws Exception {

        ActionDto actionDto = delegateExecution.getVariable("actionToValidate", ActionDto.class);
        List<ActionType> actionList = tMailProcessService.getAvailableActionTypes(delegateExecution.getProcessInstanceId(),actionDto.getCreator());
        List<ActionDto> actions = (List<ActionDto>) delegateExecution.getVariable("actions");
        if (actionList.contains(actionDto.getType())) {
            actions.add(actionDto);
            delegateExecution.setVariable("actions",actions);
            delegateExecution.setVariable("action",actionDto);

        } else
            throw new NoPermissionException();
    }
}
