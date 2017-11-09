package org.korbit.test.activiti;

import org.activiti.engine.IdentityService;
import org.activiti.engine.identity.Group;
import org.activiti.engine.identity.User;
import org.korbit.test.activiti.domain.GroupPermission;
import org.korbit.test.activiti.models.ActionType;
import org.korbit.test.activiti.repository.GroupPermissionRepository;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.util.Arrays;

@SpringBootApplication
//@ImportResource("classpath:activiti-config.xml")
public class ActivitiApplication {
	@Autowired
	GroupPermissionRepository groupPermissionRepository;
	public static void main(String[] args) {
		SpringApplication.run(ActivitiApplication.class, args);
	}
	@Bean
	InitializingBean usersAndGroupsInitializer(final IdentityService identityService) {

		return new InitializingBean() {
			public void afterPropertiesSet() throws Exception {

				Group group = identityService.newGroup("user");
				group.setName("users");
				group.setType("security-role");
				identityService.saveGroup(group);

				Group group2 = identityService.newGroup("admin");
				group2.setName("admins");
				group2.setType("security-role");
				identityService.saveGroup(group2);

				GroupPermission groupPermission = new GroupPermission();
				groupPermission.setActionTypes(Arrays.asList(ActionType.Close,ActionType.Cancel,ActionType.Done,
						ActionType.Delegate,ActionType.ReOpen,ActionType.Refinement));
				groupPermission.setGroupId(identityService.createGroupQuery().groupName("users").singleResult().getId());
				groupPermissionRepository.save(groupPermission);
				//identityService.saveGroup(group);
				User admin = identityService.newUser("admin");
				admin.setPassword("admin");
				identityService.saveUser(admin);
				identityService.createMembership("admin","user");
				identityService.createMembership("admin","admin");

			}
		};
	}
}
