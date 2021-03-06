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

@SpringBootApplication(exclude = org.activiti.spring.boot.SecurityAutoConfiguration.class)
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
				group.setName("ROLE_USER");
				group.setType("USER");
				identityService.saveGroup(group);

				Group group2 = identityService.newGroup("admin");
				group2.setName("ROLE_ADMIN");
				group2.setType("ADMIN");
				identityService.saveGroup(group2);

				GroupPermission groupPermission = new GroupPermission();
				groupPermission.setActionTypesIfAssigner(Arrays.asList(ActionType.Close,ActionType.Cancel,ActionType.Done,
						ActionType.Delegate,ActionType.ReOpen,ActionType.Refinement));
				groupPermission.setActionTypesIfNotAssigner(Arrays.asList(ActionType.ChangeDescription));
				groupPermission.setActionTypesIfCreator(Arrays.asList(ActionType.Done));
				groupPermission.setGroupId(identityService.createGroupQuery().groupName("ROLE_USER").singleResult().getId());
				groupPermissionRepository.save(groupPermission);
				User user = identityService.newUser("davblain");
				user.setFirstName("davblain");
				user.setPassword("587238");
				identityService.saveUser(user);
				User admin = identityService.newUser("admin");
				admin.setPassword("admin");
				admin.setFirstName("admin");
				identityService.saveUser(admin);
				identityService.createMembership("admin","user");
				identityService.createMembership("admin","admin");
				identityService.createMembership("davblain","user");

			}
		};
	}
}
