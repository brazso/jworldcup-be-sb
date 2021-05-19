package com.zematix.jworldcup.backend.scheduler;

import javax.inject.Inject;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import com.zematix.jworldcup.backend.exception.ServiceException;

/**
 * Contains test functions of {@link RegularMaintenanceJob} class.
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles({"development", "test"})
public class RegularMaintenanceJobTest {

	@Inject
	private RegularMaintenanceJob regularMaintenanceJob;
	
	@MockBean
	private SchedulerService schedulerService; // used by methods of RegularMaintenanceJob 

	/**
	 * Test {@link RegularMaintenanceJob#execute(JobExecutionContext)} method.
	 * Scenario: successfully executes method
	 */
	@Test
	public void execute(/*JobExecutionContext context*/) throws JobExecutionException, ServiceException {
		JobExecutionContext context = null;
		regularMaintenanceJob.execute(context);
		
		Mockito.verify(schedulerService).regularMaintenanceExecution();
	}
}
