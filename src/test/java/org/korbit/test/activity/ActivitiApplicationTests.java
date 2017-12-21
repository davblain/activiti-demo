package org.korbit.test.activity;
import static org.assertj.core.api.Assertions.assertThat;

import org.activiti.engine.form.FormProperty;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.korbit.test.activiti.ActivitiApplication;
import org.korbit.test.activiti.services.ActionService;
import org.korbit.test.activiti.services.TMailProcessService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = ActivitiApplication.class)
@Import(ActivitiApplication.class)
public class ActivitiApplicationTests {

	@Autowired
	ActionService actionService;
	@Autowired
	TMailProcessService tMailProcessService;
	@Test
	public void contextLoads() {
	}

	@Test
	public  void getActionsOfStateTest() {
		String state = "OpenedState";
		List<String>  actions = actionService.getAvailableActionTypesOfState(state);
		assertThat(actions).contains("DelegateAction");
	}
	@Test
	public void getActionProperties() {
		List<FormProperty> properties = actionService.getPropertiesOfAction("RefinementAction");
		assertThat(properties).isEmpty();
	}

	@Test
	public void getAuthorities() {
		System.out.println(actionService.getAuthoritiesOfActionFlow("DelegateAction","OpenedState"));
	}
	@Test
	public void isStepAction() {
		assertThat( actionService.isStepAction("ChangeDescriptionAction")).isFalse();
	}

}
