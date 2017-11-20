package org.korbit.test.activiti.tasks.tprocess;

import org.activiti.engine.IdentityService;
import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.Expression;
import org.activiti.engine.delegate.JavaDelegate;
import org.activiti.engine.identity.User;
import org.korbit.test.activiti.dto.ActionDto;
import org.korbit.test.activiti.exceptions.UserNotFoundException;
import org.korbit.test.activiti.services.TMailProcessService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Date;
import java.util.Optional;

@Service

public class DelegateServiceTask implements JavaDelegate {
    @Autowired
    IdentityService identityService;
    @Override
    public void execute(DelegateExecution delegateExecution) throws Exception {
      ActionDto action = delegateExecution.getVariable("action",ActionDto.class);
      String recipient = Optional.ofNullable(action.getData().get("recipient")).orElseThrow(IllegalArgumentException::new);
      User user = Optional.ofNullable(identityService.createUserQuery().userId(recipient).singleResult())
              .orElseThrow(() -> new UserNotFoundException(recipient));
      action.setTime(new Date());
    }
}
