package org.korbit.test.activiti;

import org.activiti.engine.IdentityService;
import org.activiti.engine.identity.Group;
import org.activiti.engine.identity.User;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
//@ImportResource("classpath:activiti-config.xml")
public class ActivitiApplication {

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
				Group group2 = identityService.newGroup("admin");
				group2.setName("admins");
				group2.setType("security-role");
				identityService.saveGroup(group);
				identityService.saveGroup(group);
				User admin = identityService.newUser("admin");
				admin.setPassword("admin");
				identityService.saveUser(admin);
				identityService.createMembership("admin","user");
				identityService.createMembership("admin","admin");

			}
		};
	}
}
