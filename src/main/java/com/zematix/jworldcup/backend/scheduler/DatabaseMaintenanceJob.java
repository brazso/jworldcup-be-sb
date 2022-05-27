package com.zematix.jworldcup.backend.scheduler;

import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import com.zematix.jworldcup.backend.exception.ServiceException;
import com.zematix.jworldcup.backend.service.ServerBase;

/**
 * Database maintenance operations are called in this scheduler job. 
 */
public class DatabaseMaintenanceJob extends ServerBase implements Job {

	@Inject
	private SchedulerService schedulerService;

	@Override
	public void execute(JobExecutionContext context) throws JobExecutionException {
		logger.info("DatabaseMaintenanceJob execution started: schedulerService="+schedulerService);
		try {
			schedulerService.databaseMaintenanceJob();
			TimeUnit.MILLISECONDS.sleep(250);
		} catch (ServiceException e) {
			consumeServiceException(e);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
