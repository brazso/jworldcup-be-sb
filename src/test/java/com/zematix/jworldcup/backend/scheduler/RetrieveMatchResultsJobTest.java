package com.zematix.jworldcup.backend.scheduler;

import javax.inject.Inject;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import com.zematix.jworldcup.backend.exception.ServiceException;

/**
 * Contains test functions of {@link RetrieveMatchResultsJob} class.
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles({"develop", "test"})
public class RetrieveMatchResultsJobTest {

	@Inject
	private RetrieveMatchResultsJob retrieveMatchResultsJob;
	
	@MockBean
	private SchedulerService schedulerService; // used by methods of RetrieveMatchResultsJob 

	/**
	 * Test {@link RetrieveMatchResultsJob#execute(JobExecutionContext)} method.
	 * Scenario: throws {@link IllegalStateException} because {@code context} parameter
	 *           value does not contain {@code eventId} JobDataMap parameter
	 */ 
	@Test(expected=IllegalStateException.class)
	public void execute_NullEventId(/*JobExecutionContext context*/) throws JobExecutionException {
		JobExecutionContext context = Mockito.mock(JobExecutionContext.class);
		JobDetail detail = Mockito.mock(JobDetail.class);
		JobDataMap map = new JobDataMap();
		//map.put("eventId", 1L); // WC2014
		map.put("firstIncompleteMatchId", 1L);
		Mockito.when(detail.getJobDataMap()).thenReturn(map);
		Mockito.when(context.getJobDetail()).thenReturn(detail);

		retrieveMatchResultsJob.execute(context);
	}

	/**
	 * Test {@link RetrieveMatchResultsJob#execute(JobExecutionContext)} method.
	 * Scenario: throws {@link IllegalStateException} because {@code context} parameter
	 *           value does not contain {@code firstIncompleteMatchId} JobDataMap parameter
	 */ 
	@Test(expected=IllegalStateException.class)
	public void execute_NullFirstIncompleteMatchId(/*JobExecutionContext context*/) throws JobExecutionException {
		JobExecutionContext context = Mockito.mock(JobExecutionContext.class);
		JobDetail detail = Mockito.mock(JobDetail.class);
		JobDataMap map = new JobDataMap();
		map.put("eventId", 1L); // WC2014
		//map.put("firstIncompleteMatchId", 1L);
		Mockito.when(detail.getJobDataMap()).thenReturn(map);
		Mockito.when(context.getJobDetail()).thenReturn(detail);

		retrieveMatchResultsJob.execute(context);
	}

	/**
	 * Test {@link RetrieveMatchResultsJob#execute(JobExecutionContext)} method.
	 * Scenario: successfully executes method
	 */
	@Test
	public void execute(/*JobExecutionContext context*/) throws JobExecutionException, ServiceException {
		JobExecutionContext context = Mockito.mock(JobExecutionContext.class);
		JobDetail detail = Mockito.mock(JobDetail.class);
		JobDataMap map = new JobDataMap();
		Long eventId = 1L; // WC2014
		map.put("eventId", eventId); // WC2014
		Long firstIncompleteMatchId = 1L;
		map.put("firstIncompleteMatchId", firstIncompleteMatchId);
		Mockito.when(detail.getJobDataMap()).thenReturn(map);
		Mockito.when(context.getJobDetail()).thenReturn(detail);

		retrieveMatchResultsJob.execute(context);
		
		Mockito.verify(schedulerService).retrieveMatchResultsJob(eventId, firstIncompleteMatchId);
	}
}
