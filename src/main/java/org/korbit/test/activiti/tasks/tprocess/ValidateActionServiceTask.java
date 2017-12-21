package org.korbit.test.activiti.tasks.tprocess;

import org.activiti.engine.HistoryService;
import org.activiti.engine.IdentityService;
import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.DelegateHelper;
import org.activiti.engine.delegate.JavaDelegate;
import org.korbit.test.activiti.dto.ActionDto;
import org.korbit.test.activiti.exceptions.NoPermissionException;
import org.korbit.test.activiti.models.Action;
import org.korbit.test.activiti.models.ActionType;
import org.korbit.test.activiti.services.ActionService;
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
    @Autowired
    private HistoryService historyService;
    @Autowired
    private ActionService actionService;
    @Override
    public void execute(DelegateExecution delegateExecution) throws Exception {

        ActionDto actionDto = delegateExecution.getVariable("actionToValidate", ActionDto.class);
        List<String> actionList = tMailProcessService.getAvailableActionTypesOfUser(delegateExecution.getProcessInstanceId(),actionDto.getCreator());

        if (actionList.contains(actionDto.getType().toString())) {
            delegateExecution.setVariable("action",actionDto);
        } else
            throw new NoPermissionException();
    }
}
