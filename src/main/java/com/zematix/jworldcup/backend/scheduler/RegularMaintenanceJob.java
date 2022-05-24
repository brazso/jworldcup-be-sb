package com.zematix.jworldcup.backend.scheduler;

import javax.inject.Inject;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.stereotype.Component;

import com.zematix.jworldcup.backend.exception.ServiceException;
import com.zematix.jworldcup.backend.service.ServerBase;

/**
 * Regular maintenance operations are called in this scheduler job. 
 */
@Component
//@ApplicationScope
public class RegularMaintenanceJob extends ServerBase implements Job {

	@Inject
	private SchedulerService schedulerService;

	@Override
	//@ActivateRequestContext
	public void execute(JobExecutionContext context) throws JobExecutionException {
		logger.info("RegularMaintenanceJob exexution started");
		try {
			schedulerService.regularMaintenanceExecution();
		} catch (ServiceException e) {
			consumeServiceException(e);
		}
	}
}
