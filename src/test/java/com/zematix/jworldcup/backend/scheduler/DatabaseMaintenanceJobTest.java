package com.zematix.jworldcup.backend.scheduler;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit4.SpringRunner;

import com.zematix.jworldcup.backend.exception.ServiceException;

import jakarta.inject.Inject;

/**
 * Contains test functions of {@link DatabaseMaintenanceJob} class.
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles({"develop", "test"})
public class DatabaseMaintenanceJobTest {

	@Inject
	private DatabaseMaintenanceJob databaseMaintenanceJob;
	
	@MockitoBean
	private SchedulerService schedulerService; // used by methods of DatabaseMaintenanceJob 

	/**
	 * Test {@link DatabaseMaintenanceJob#execute(JobExecutionContext)} method.
	 * Scenario: successfully executes method
	 */
	@Test
	public void execute(/*JobExecutionContext context*/) throws JobExecutionException, ServiceException {
		JobExecutionContext context = null;
		databaseMaintenanceJob.execute(context);
		
		Mockito.verify(schedulerService).databaseMaintenanceJob();
	}
}
