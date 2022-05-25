package com.zematix.jworldcup.backend.scheduler;

import javax.inject.Inject;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.stereotype.Component;

import com.zematix.jworldcup.backend.service.ServerBase;

/**
 * Test scheduler job. 
 */
@Component
public class TestJob extends ServerBase implements Job {

	@Inject
	private SchedulerService schedulerService;

	@Override
	public void execute(JobExecutionContext context) throws JobExecutionException {
		logger.info("TestJob exexution started");
//		try {
			schedulerService.testExecution();
//		} catch (ServiceException e) {
//			consumeServiceException(e);
//		}
	}
}
