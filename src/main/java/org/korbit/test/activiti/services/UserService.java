package org.korbit.test.activiti.services;

import org.activiti.engine.IdentityService;
import org.activiti.engine.identity.User;
import org.korbit.test.activiti.dto.UserDto;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserService {
    final private IdentityService identityService;
    UserService(IdentityService identityService){
        this.identityService = identityService;
    }
    public List<UserDto> getListOfUsers() {
     return identityService.createUserQuery().orderByUserId().desc().list().stream().map(user -> {
         UserDto us = new UserDto();
         us.setUsername(user.getId());
         return us;
     }).collect(Collectors.toList());
    }
}
