package com.zematix.jworldcup.backend.scheduler;

import static com.google.common.base.Preconditions.checkNotNull;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import org.quartz.JobDataMap;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.impl.JobDetailImpl;
import org.quartz.impl.triggers.SimpleTriggerImpl;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.session.SessionInformation;
import org.springframework.stereotype.Service;
import org.springframework.web.context.annotation.ApplicationScope;

import com.google.common.base.Stopwatch;
import com.zematix.jworldcup.backend.configuration.SessionListener;
import com.zematix.jworldcup.backend.entity.Match;
import com.zematix.jworldcup.backend.exception.ServiceException;
import com.zematix.jworldcup.backend.model.SessionData;
import com.zematix.jworldcup.backend.service.ApplicationService;
import com.zematix.jworldcup.backend.service.MatchService;
import com.zematix.jworldcup.backend.service.MessageQueueService;
import com.zematix.jworldcup.backend.service.ServiceBase;
import com.zematix.jworldcup.backend.service.SessionService;
import com.zematix.jworldcup.backend.service.UserService;
import com.zematix.jworldcup.backend.service.WebServiceService;
import com.zematix.jworldcup.backend.util.CommonUtil;

/**
 * Operations around scheduler. 
 */
@Service
@ApplicationScope
public class SchedulerService extends ServiceBase {

	@Inject
	private ApplicationService applicationService;
	
	@Inject
	private UserService userService;

	@Inject
	private MatchService matchService;
	
	@Inject
	private WebServiceService webServiceService;
	
	@Inject
	private MessageQueueService messageQueueService;
	
	private Map<Long, Short> futileAttemptsByEventId = new HashMap<>();
	
	@Value("${app.scheduler.enabled:true}")
	private String appSchedulerEnabled;

	@Value("${app.shortName}") 
	private String appShortName;

	private Scheduler scheduler;
	
	/**
	 * Initializes Quartz scheduler service. Usually it is called from a Quartz listener.
	 * Creates scheduler jobs based on first incomplete (and to be triggered) matches of all events.
	 * 
	 * @param scheduler
	 * @throws ServiceException
	 */
	//@ActivateRequestContext
	public void init(Scheduler scheduler) throws ServiceException {
		this.scheduler = scheduler;
		if (!isAppSchedulerEnabled()) {
			logger.info("Programmatic scheduler service is disabled in application configuration.");
			return;
		}
		
		List<Match> matches = matchService.retrieveFirstIncompleteMatchesOfEvents();
		for (Match match : matches) {
			scheduleByIncompleteMatch(match);
		}
	}

	/**
	 * Returns {@code true} if application scheduler is enabled, {@code false} otherwise.
	 * @return {@code true} if application scheduler is enabled, {@code false} otherwise
	 */
	public boolean isAppSchedulerEnabled() {
		return Boolean.valueOf(appSchedulerEnabled);
	}
	
	/**
	 * Creates a scheduler job based on the given incomplete match.
	 * First execution is started at once. If n-th execution must be run, where n>=2, it starts
	 * after 2^^(n-2) minute(s). No trigger is created if the event belongs to the given {@code match}
	 * is not inside the determined expiration modification time.
	 * 
	 * @param match
	 * @throws ServiceException
	 */
	public void scheduleByIncompleteMatch(Match match) throws ServiceException {
		checkNotNull(match);
		
		LocalDateTime actualDateTime = applicationService.getActualDateTime();
		LocalDateTime triggerStartTime = matchService.getMatchTriggerStartTime(match, actualDateTime);
		if (actualDateTime.equals(triggerStartTime)) { // escalated now
			short attempt = futileAttemptsByEventId.get(match.getEvent().getEventId()) == null ? 
					0 : futileAttemptsByEventId.get(match.getEvent().getEventId());
			triggerStartTime = LocalDateTime.now().plus(1000, ChronoUnit.MILLIS);
			if (attempt > 0) {
				triggerStartTime = CommonUtil.plusMinutes(triggerStartTime, (long)Math.pow(2, attempt-1));
			}
		}
		if (matchService.isInsideExpiredModificationTimeByEventId(match.getEvent().getEventId(), triggerStartTime)) {
			createRetrieveMatchResultsJobTrigger(match.getEvent().getEventId(), match.getMatchId(), triggerStartTime);
		}
	}
	
	/**
	 * A regular scheduler maintenance job execution.
	 * It deletes expired objects and refreshes {@link ApplicationService#topUsersCache}.
	 */
	public void regularMaintenanceExecution() throws ServiceException {
		try {
			int n = userService.deleteExpiredCandidateUsers();
			logger.info(String.format("Scheduled deleteExpiredCandidateUsers deleted %d elements.", n));
			n = userService.deleteExpiredEmailModifications();
			logger.info(String.format("Scheduled deleteExpiredEmailModifications deleted %d elements.", n));
			n = userService.deleteExpiredPasswordResets();
			logger.info(String.format("Scheduled deleteExpiredPasswordResets deleted %d elements.", n));
			
			Stopwatch stopwatch = Stopwatch.createStarted();
			applicationService.refreshTopUsersCache();
			stopwatch.stop(); // optional
			logger.info(String.format("Scheduled topUserCache refreshed in %d milliseconds.", stopwatch.elapsed(TimeUnit.MILLISECONDS)));
		} catch (ServiceException e) {
			consumeServiceException(e);
		}		
	}
	
	/**
	 * A scheduler job execution which updates the database from retrieved match results of the 
	 * given event from the given match where data comes from web service.
	 * 
	 * @param eventId
	 * @param firstIncompleteMatchId
	 * @throws ServiceException
	 */
	public void retrieveMatchResultsExecution(Long eventId, Long firstIncompleteMatchId) throws ServiceException {
		checkNotNull(eventId);
		checkNotNull(firstIncompleteMatchId);
		
		long updatedMatches = 0; 
		try {
			updatedMatches = webServiceService.updateMatchResults(eventId);
		} catch (ServiceException e) {
			consumeServiceException(e);
		}
		
		if (updatedMatches > 0) {
			// update cached value
			applicationService.refreshEventCompletionPercentCache(eventId);
		}
		
		Match match = null;
		try {
			match = matchService.retrieveFirstIncompleteMatchByEvent(eventId);
		} catch (ServiceException e) {
			consumeServiceException(e);
		}
		if (match != null) {
			short attempt = futileAttemptsByEventId.get(match.getEvent().getEventId()) == null ? 
					0 : futileAttemptsByEventId.get(match.getEvent().getEventId());
			if (match.getMatchId().equals(firstIncompleteMatchId)) {
				// in fact no match result was retrieved by web service for match belongs to firstIncompleteMatchId, try again
				attempt += 1;
			}
			else {
				// there is another incomplete match after match belongs to firstIncompleteMatchId
				attempt = 0;
			}
			futileAttemptsByEventId.put(match.getEvent().getEventId(), attempt);
			if (attempt == 0) {
				applicationService.getRetrieveMatchResultsJobTriggerStartTimesCache().refresh(eventId); // reset cache
			}
			scheduleByIncompleteMatch(match);
		}
		else {
			// there is no incomplete match at all, no trigger/job has to be created
			futileAttemptsByEventId.remove(eventId);
			applicationService.getRetrieveMatchResultsJobTriggerStartTimesCache().refresh(eventId); // reset cache
		}
	}
	
	/**
	 * Helper method to create a simple trigger for {@link RetrieveMatchResultsJob} quartz job
	 *  
	 * @param eventId
	 * @param firstIncompleteMatchId
	 * @param triggerStartTime
	 */
	private boolean createRetrieveMatchResultsJobTrigger(Long eventId, Long firstIncompleteMatchId, LocalDateTime triggerStartTime) {
		checkNotNull(eventId);
		checkNotNull(firstIncompleteMatchId);
		checkNotNull(triggerStartTime);

		boolean createRetrieveMatchResultsJobTrigger = false;
		// Creating JobDetailImpl and link to our Job class
		JobDetailImpl jobDetail = new JobDetailImpl();
		jobDetail.setName(String.format("RetrieveMatchResultsForEvent%dJob", eventId));
		JobDataMap dataMap = new JobDataMap();
		dataMap.put("eventId", eventId);
		dataMap.put("firstIncompleteMatchId", firstIncompleteMatchId);
		jobDetail.setJobDataMap(dataMap);
		jobDetail.setJobClass(RetrieveMatchResultsJob.class);

		// Creating schedule time with trigger
		SimpleTriggerImpl simpleTrigger = new SimpleTriggerImpl();
		simpleTrigger.setStartTime(Timestamp.valueOf(triggerStartTime));
		simpleTrigger.setRepeatCount(0);
		simpleTrigger.setRepeatInterval(0);
		simpleTrigger.setName(String.format("triggerByEvent%d", eventId));

		// Tell quartz to schedule the job using our trigger
		try {
			this.scheduler.deleteJob(new JobKey(jobDetail.getName()));
			this.scheduler.scheduleJob(jobDetail, simpleTrigger);
			createRetrieveMatchResultsJobTrigger = true;
		} catch (SchedulerException e) {
			logger.error("Problem scheduling a new {} job!", jobDetail.getName(), e);
		}

		if (createRetrieveMatchResultsJobTrigger) {
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			logger.info(String.format("Added scheduler with job %s triggered on %s.", 
					jobDetail.getName(), sdf.format(simpleTrigger.getStartTime())));
			applicationService.getRetrieveMatchResultsJobTriggerStartTimesCache().getIfPresent(eventId).add(triggerStartTime);
		}
		
		return createRetrieveMatchResultsJobTrigger;
	}
	
	/**
	 * Returns a flag that there is an existing trigger of {@link RetrieveMatchResultsJob} quartz job 
	 * belongs to the given {@code eventId} parameter.
	 * 
	 * @param eventId
	 * @return {@code true} if there is retrieval math job trigger, {@code false} otherwise
	 */
	public boolean isExistsRetrieveMatchResultsJobTrigger(Long eventId) {
		checkNotNull(eventId);
		
		boolean isExistsRetrieveMatchResultsJobTrigger = false;
		String jobDetailName = String.format("RetrieveMatchResultsForEvent%dJob", eventId);
		JobKey jobKey = new JobKey(jobDetailName);
		try {
			isExistsRetrieveMatchResultsJobTrigger = this.scheduler.checkExists(jobKey);
		} catch (SchedulerException e) {
			logger.error("Problem checking a scheduled existing {} job!", jobDetailName, e);
		}
		return isExistsRetrieveMatchResultsJobTrigger;
	}
	
	/**
	 * Executes an existing trigger of {@link RetrieveMatchResultsJob} quartz job belongs to the given 
	 * {@code eventId} parameter at once if it is already exist.
	 * Moreover {@link SchedulerService#futileAttemptsByEventId is reset for the given {@code eventId} parameter.
	 * It might be called from UI by an impatient admin. Returned flag shows that the operation was successfull.
	 * 
	 * @param eventId
	 * @param firstIncompleteMatchId
	 * @return {@code true} if the existing trigger was restarted, {@code false} otherwise 
	 */
	public boolean relaunchRetrieveMatchResultsJobTrigger(Long eventId, Long firstIncompleteMatchId) {
		checkNotNull(eventId);
		checkNotNull(firstIncompleteMatchId);
		
		boolean relaunchRetrieveMatchResultsJobTrigger = false;
		String jobDetailName = String.format("RetrieveMatchResultsForEvent%dJob", eventId);
		JobKey jobKey = new JobKey(jobDetailName);
		try {
			if (this.scheduler.checkExists(jobKey)) {
				JobDataMap dataMap = new JobDataMap();
				dataMap.put("eventId", eventId);
				dataMap.put("firstIncompleteMatchId", firstIncompleteMatchId);
				futileAttemptsByEventId.put(eventId, /*attempt*/ (short)0);
				this.scheduler.triggerJob(new JobKey(jobDetailName), dataMap);
				relaunchRetrieveMatchResultsJobTrigger = true;
			}
		} catch (SchedulerException e) {
			logger.error("Problem rescheduling an existing new {} job!", jobDetailName, e);
		}
		
		if (relaunchRetrieveMatchResultsJobTrigger) {
			futileAttemptsByEventId.put(eventId, /*attempt*/ (short)0);
			applicationService.getRetrieveMatchResultsJobTriggerStartTimesCache().refresh(eventId); // reset cache
		}
		
		return relaunchRetrieveMatchResultsJobTrigger;
	}
	
	/**
	 * Test scheduler job execution.
	 */
	public void testExecution() /*throws ServiceException*/ {
//		try {
			logger.info("testExecution started");
			List<org.springframework.security.core.userdetails.User> users = applicationService.getAllAuthenticatedPrincipals();
			users.stream().forEach( user -> {
				logger.info("authenticated user: " + user.getUsername());
				List<SessionInformation> sessionInfos = applicationService.getAllAuthenticatedSessions(user);
				sessionInfos.stream().map(info -> SessionListener.getSession(info.getSessionId())).filter(Objects::nonNull).forEach(session -> {
					SessionService sessionService = (SessionService)session.getAttribute("scopedTarget.sessionService");
					SessionData sessionData = sessionService.refreshSessionData(null); // load from local
					Map<String, Object> localUpdateMap = Map.of("newsLine", "Hello");
					sessionData = sessionService.refreshSessionData(sessionData, localUpdateMap);
					messageQueueService.sendSession(sessionData);
					//...
				});
			});
//		} catch (ServiceException e) {
//			consumeServiceException(e);
//		}		
	}

}
