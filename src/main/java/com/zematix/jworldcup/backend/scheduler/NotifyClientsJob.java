package com.zematix.jworldcup.backend.scheduler;

import javax.inject.Inject;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import com.zematix.jworldcup.backend.service.ServerBase;

/**
 * Notify clients scheduler job. 
 */
public class NotifyClientsJob extends ServerBase implements Job {

	@Inject
	private SchedulerService schedulerService;

	@Override
	public void execute(JobExecutionContext context) throws JobExecutionException {
		logger.info("NotifyClientsJob execution started");
		schedulerService.notifyClientsJob();
	}
}
