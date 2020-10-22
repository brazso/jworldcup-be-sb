package com.zematix.jworldcup.backend.scheduler;

import javax.inject.Inject;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import com.zematix.jworldcup.backend.service.ApplicationService;
import com.zematix.jworldcup.backend.service.MatchService;
import com.zematix.jworldcup.backend.service.UserService;
import com.zematix.jworldcup.backend.service.WebServiceService;

/**
 * Contains test functions of {@link SchedulerService} class.
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles({"development", "test"})
//@Transactional
public class SchedulerServiceTest {

	@Inject
	private SchedulerService schedulerService;
	
	@MockBean
	private ApplicationService applicationService;
	
	@MockBean
	private UserService userService;

	@MockBean
	private MatchService matchService;
	
	@MockBean
	private WebServiceService webServiceService;
	
//	private Map<Long, Short> futileAttemptsByEventId = new HashMap<>();
//	
//	@Inject
//	@ApplicationConfig(key="app.scheduler.enabled", defaultValue = "true")
//	private String appSchedulerEnabled;
//
//	@Inject 
//	@ApplicationConfig(key="app.shortName") 
//	private String appShortName;
//
//	private Scheduler scheduler;

	@Test
	public void todo() { // TODO
		
	}
}
