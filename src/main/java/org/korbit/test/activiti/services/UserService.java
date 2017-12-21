package org.korbit.test.activiti.services;

import org.activiti.engine.IdentityService;
import org.activiti.engine.identity.Group;
import org.activiti.engine.identity.User;
import org.korbit.test.activiti.domain.GroupPermission;
import org.korbit.test.activiti.dto.UserDto;
import org.korbit.test.activiti.repository.GroupPermissionRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserService {
    final private IdentityService identityService;
    final private GroupPermissionRepository groupPermissionRepository;
    UserService(IdentityService identityService, GroupPermissionRepository groupPermissionRepository){
        this.groupPermissionRepository = groupPermissionRepository;
        this.identityService = identityService;
    }
    public List<UserDto> getListOfUsers(String filter) {
     return identityService.createUserQuery().orderByUserId().userFirstName(filter).desc().list().stream().map(user -> {
         UserDto us = new UserDto();
         us.setUsername(user.getId());
         return us;
     }).collect(Collectors.toList());
    }
    @Deprecated
    public List<GroupPermission> getGroupPermissionsOfUser(String username) {
            List<Group> groups = identityService.createGroupQuery().groupMember(username).list();
            return groups.stream()
                    .map(Group::getId)
                    .map((id) -> groupPermissionRepository.findGroupPermissionByGroupId(id).orElse(new GroupPermission()))
                    .collect(Collectors.toList());
        }

}
