package com.zematix.jworldcup.backend.scheduler;

import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import com.zematix.jworldcup.backend.service.ServerBase;

/**
 * Chat initialization scheduler job.
 * Loads all messages from chat table and transfers them to proper message queues.  
 */
public class ChatInitializationJob extends ServerBase implements Job {

	@Inject
	private SchedulerService schedulerService;

	@Override
	public void execute(JobExecutionContext context) throws JobExecutionException {
		logger.info("ChatInitializationJob execution started: schedulerService="+schedulerService);
		try {
			schedulerService.chatInitalizationJob();
			TimeUnit.MILLISECONDS.sleep(250);
//		} catch (ServiceException e) {
//			consumeServiceException(e);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
