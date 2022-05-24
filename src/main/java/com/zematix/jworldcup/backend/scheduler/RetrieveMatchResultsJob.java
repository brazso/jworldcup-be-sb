package com.zematix.jworldcup.backend.scheduler;

import static com.google.common.base.Preconditions.checkState;

import javax.inject.Inject;

import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.stereotype.Component;

import com.zematix.jworldcup.backend.exception.ServiceException;
import com.zematix.jworldcup.backend.service.ServerBase;

/**
 * Retrieve missing match results or team participants of an event
 * from Open League DB web service. 
 */
@Component
//@ApplicationScope
public class RetrieveMatchResultsJob extends ServerBase implements Job {

	@Inject
	private SchedulerService schedulerService;

	@Override
	//@ActivateRequestContext
	public void execute(JobExecutionContext context) throws JobExecutionException {
		logger.info("RetrieveMatchResultsJob exexution started");

		JobDataMap dataMap = context.getJobDetail().getJobDataMap();
		Long eventId = (Long)dataMap.get("eventId"); //getLong method throws ClassCastException if the map does not contain the key
		checkState(eventId != null, "JobDataMap does not contain \"eventId\" value");
		Long firstIncompleteMatchId = (Long)dataMap.get("firstIncompleteMatchId");
		checkState(firstIncompleteMatchId != null, "JobDataMap does not contain \"firstIncompleteMatchId\" value");
		
		try {
			schedulerService.retrieveMatchResultsExecution(eventId, firstIncompleteMatchId);
		} catch (ServiceException e) {
			consumeServiceException(e);
		}
	}
}
