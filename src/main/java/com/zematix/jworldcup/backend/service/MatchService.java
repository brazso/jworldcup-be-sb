package com.zematix.jworldcup.backend.service;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;
import org.springframework.lang.NonNull;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Strings;
import com.zematix.jworldcup.backend.configuration.CachingConfig;
import com.zematix.jworldcup.backend.dao.CommonDao;
import com.zematix.jworldcup.backend.dao.MatchDao;
import com.zematix.jworldcup.backend.dao.RoundDao;
import com.zematix.jworldcup.backend.emun.ParameterizedMessageType;
import com.zematix.jworldcup.backend.entity.Event;
import com.zematix.jworldcup.backend.entity.Group;
import com.zematix.jworldcup.backend.entity.Match;
import com.zematix.jworldcup.backend.entity.Round;
import com.zematix.jworldcup.backend.entity.Team;
import com.zematix.jworldcup.backend.exception.ServiceException;
import com.zematix.jworldcup.backend.model.GroupPosition;
import com.zematix.jworldcup.backend.model.Pair;
import com.zematix.jworldcup.backend.model.ParameterizedMessage;
import com.zematix.jworldcup.backend.scheduler.SchedulerService;
import com.zematix.jworldcup.backend.util.CommonUtil;
import com.zematix.jworldcup.backend.util.LambdaExceptionUtil;
import com.zematix.jworldcup.backend.util.UpdateEntityEvent;

/**
 * Operations around {@link Match} elements. 
 * Usually it injects at least its DAO class belongs to the same entities. 
 * However it may also inject other services and DAO classes.
 */
@Service
@Configuration
@Transactional
public class MatchService extends ServiceBase {

	@Inject
	private ApplicationService applicationService;
	
	@Inject 
	private MatchDao matchDao;

	@Inject 
	private RoundDao roundDao;

	@Inject 
	private CommonDao commonDao;

	@Inject
	private GroupService groupService;

	@Inject 
	private GroupTeamService groupTeamService;
	
	@Inject
	private EventService eventService;
	
	@Inject
	private TeamService teamService;
	
	@Inject
	private SchedulerService schedulerService;
	
	@Inject
    private ApplicationEventPublisher applicationEventPublisher;

	@Inject
	private CacheManager cacheManager;
	
	@Value("${app.expiredDays.event:0}")
	private String appExpiredDaysEvent;
	
	/**
	 * Returns a list of {@link Round} instances belongs to the given {@code eventId} 
	 * parameter. The retrieved elements are ordered by {@code roundId}.
	 * Otherwise it may throw ServiceException.
	 *  
	 * @param eventId - event belongs to the rounds to be retrieved
	 * @throws ServiceException if the rounds cannot be retrieved 
	 */
	@Transactional(readOnly = true)
	public List<Round> retrieveRoundsByEvent(Long eventId) throws ServiceException {

		List<ParameterizedMessage> errMsgs = new ArrayList<>();

		checkNotNull(eventId);
		
		List<Round> rounds = roundDao.retrieveRoundsByEvent(eventId);
		
		// load lazy entity associations
		for (Round round : rounds) {
			for (Match match : round.getMatches()) {
				if (match.getTeam1() != null) {
					match.getTeam1().getTeamId();
				}
				if (match.getTeam2() != null) {
					match.getTeam2().getTeamId();
				}
			}
		}

		if (!errMsgs.isEmpty()) {
			throw new ServiceException(errMsgs);
		}
		
		return rounds; 
	}
		
	/**
	 * Returns a list of {@link Match} instances belongs to the {@link Event} specified 
	 * by the given {@code eventId} parameter. Returned list is ordered
	 * by {@link Match#startTime}.
	 *  
	 * @param eventId - event belongs to the matches to be retrieved
	 * @throws ServiceException if the matches cannot be retrieved 
	 */
	@Transactional(readOnly = true)
	public List<Match> retrieveMatchesByEvent(Long eventId) throws ServiceException {

		List<ParameterizedMessage> errMsgs = new ArrayList<>();

		checkNotNull(eventId);
		
		List<Match> matches = matchDao.retrieveMatchesByEvent(eventId);
		
		// load lazy associations
		for (Match match : matches) {
			match.getRound().getRoundId();
			if (match.getTeam1() != null) {
				match.getTeam1().getTeamId();
			}
			if (match.getTeam2() != null) {
				match.getTeam2().getTeamId();
			}
			if (match.getTeam1() != null && match.getTeam2() != null) {
				match.setResultSignByTeam1(getMatchResult(match, match.getTeam1().getTeamId()));
			}
		}
		
		if (!errMsgs.isEmpty()) {
			throw new ServiceException(errMsgs);
		}

		return matches;
	}

	/**
	 * Retrieves a list of {@link Match} instances belongs to the  given {@code eventId}, 
	 * with not null {@link Match#participantsRule} value, located in the 
	 * knockout stage and has at least one {@link Team} participant with {@code null} value.
	 * 
	 * @param eventId
	 * @return list of {@link Match} instances where each element is in the knockout stage with 
	 *         participant rule and with missing participant team(s)
	 * @throws IllegalArgumentException if any of the parameters is null
	 * @throws ServiceException
	 */
	@Transactional(readOnly = true)
	public List<Match> retrieveMatchesWithoutParticipantsByEvent(Long eventId) throws ServiceException {

		List<ParameterizedMessage> errMsgs = new ArrayList<>();

		checkNotNull(eventId);
		
		List<Match> matches = matchDao.retrieveMatchesWithoutParticipantsByEvent(eventId);
		
		// load lazy associations
		for (Match match : matches) {
			if (match.getTeam1() != null) {
				match.getTeam1().getTeamId();
			}
			if (match.getTeam2() != null) {
				match.getTeam2().getTeamId();
			}
		}
		
		if (!errMsgs.isEmpty()) {
			throw new ServiceException(errMsgs);
		}

		return matches;
	}

	/**
	 * Retrieves a {@link Match} instance from database. Loads some lazy field associations
	 * and detaches the result object from the persistence context.
	 * 
	 * @param matchId
	 * @return retrieved {@link Match} instance or ServiceException unless it is found
	 * @throws ServiceException
	 */
	@Transactional(readOnly = true)
	public Match retrieveMatch(Long matchId) throws ServiceException {
		checkNotNull(matchId);
		
		List<ParameterizedMessage> errMsgs = new ArrayList<>();
		
		Match match = commonDao.findEntityById(Match.class, matchId);
		if (match == null) {
			errMsgs.add(ParameterizedMessage.create("MISSING_MATCH"));
			throw new ServiceException(errMsgs);
		}
		
		// load lazy associations
		match.getRound().getRoundId();
		if (match.getTeam1() != null) {
			match.getTeam1().getTeamId();
		}
		if (match.getTeam2() != null) {
			match.getTeam2().getTeamId();
		}

		commonDao.detachEntity(match);
		
		return match;
	}

	/**
	 * Updates result of a {@link Match} instance belongs to the given matchId
	 * based on the other provided parameter values.
	 *
	 * @param matchId
	 * @param isGroupmatch
	 * @param startTime
	 * @param goalNormal1
	 * @param goalNormal2
	 * @param goalExtra1
	 * @param goalExtra2
	 * @param goalPenalty1
	 * @param goalPenalty2
	 * @return updated {@link Match} instance
	 * @throws ServiceException mostly if any validation error happens 
	 */
	public Match saveMatch(Long matchId, boolean isGroupmatch, Boolean isOvertime, LocalDateTime startTime, 
			Byte goalNormal1, Byte goalNormal2, Byte goalExtra1, Byte goalExtra2, 
			Byte goalPenalty1, Byte goalPenalty2) throws ServiceException {
		
		List<ParameterizedMessage> errMsgs = new ArrayList<>();
		Match match = null;
		
		checkNotNull(matchId);
		checkArgument(isGroupmatch || isOvertime != null, "Argument \"isOvertime\" value must not be null if argument \"isGroupmatch\" value is false.");
		checkNotNull(startTime);

		if (applicationService.getActualDateTime().isBefore(getEndDateTime(startTime))) {
			errMsgs.add(ParameterizedMessage.create("MATCH_NOT_FINISHED_YET"));
		} else if (goalNormal1 == null || goalNormal2 == null) {
			errMsgs.add(ParameterizedMessage
					.create(isGroupmatch ? "MISSING_MATCH_RESULT" : "MISSING_MATCH_AFTER_90_RESULT"));
		} else if (goalNormal1 < 0 || goalNormal2 < 0) {
			errMsgs.add(ParameterizedMessage.create("NOT_POSITIVE_VALUE_INVALID"));
		}

		if (!errMsgs.isEmpty()) {
			throw new ServiceException(errMsgs);
		}

		if (isGroupmatch) {
			if (goalExtra1 != null || goalExtra2 != null) {
				errMsgs.add(ParameterizedMessage.create("GROUP_MATCH_WITH_OVERTIME"));
			}
			if (goalPenalty1 != null || goalPenalty2 != null) {
				errMsgs.add(ParameterizedMessage.create("GROUP_MATCH_WITH_PENALTY"));
			}
		} else /* knockout match */ {
			if (!isOvertime && (goalExtra1 != null || goalExtra2 != null)) {
				errMsgs.add(ParameterizedMessage.create("KNOCKOUT_MATCH_WITH_DISALLOWED_OVERTIME"));
			}
			else if (!goalNormal1.equals(goalNormal2)) {
				if (goalExtra1 != null || goalExtra2 != null) {
					errMsgs.add(ParameterizedMessage.create("MATCH_FINISHED_AFTER_90_MIN_NO_OVERTIME"));
				}
				if (goalPenalty1 != null || goalPenalty2 != null) {
					errMsgs.add(ParameterizedMessage.create("MATCH_FINISHED_AFTER_90_MIN_NO_PENALTY"));
				}
			} else {
				if (isOvertime && (goalExtra1 == null && goalExtra2 == null)) {
					errMsgs.add(ParameterizedMessage.create("ENTER_RESULT_AFTER_OVERTIME"));
				} else if (isOvertime && (goalExtra1 == null || goalExtra2 == null)) {
					errMsgs.add(ParameterizedMessage.create("PARTIAL_RESULT_AFTER_OVERTIME"));
				} else if (isOvertime && (goalExtra1 < 0 || goalExtra2 < 0)) {
					errMsgs.add(ParameterizedMessage.create("NOT_POSITIVE_VALUE_INVALID"));
				} else {
					if (isOvertime && (!goalExtra1.equals(goalExtra2))) {
						if (goalPenalty1 != null || goalPenalty2 != null) {
							errMsgs.add(ParameterizedMessage.create("MATCH_FINISHED_AFTER_OVERTIME"));
						}
					} else {
						if (goalPenalty1 == null && goalPenalty2 == null) {
							errMsgs.add(ParameterizedMessage.create("ENTER_RESULT_AFTER_PENALTIES"));
						} else if (goalPenalty1 == null || goalPenalty2 == null) {
							errMsgs.add(ParameterizedMessage.create("PARTIAL_RESULT_AFTER_PENALTIES"));
						} else if (goalPenalty1 < 0 || goalPenalty2 < 0) {
							errMsgs.add(ParameterizedMessage.create("NOT_POSITIVE_VALUE_INVALID"));
						} else if (goalPenalty1.equals(goalPenalty2)) {
							errMsgs.add(ParameterizedMessage.create("PENALTIES_RESULT_CANNOT_BE_DRAWN"));
						}
					}
				}
			}
		}

		if (!errMsgs.isEmpty()) {
			throw new ServiceException(errMsgs);
		}

		// retrieve match from table and validate it
		match = commonDao.findEntityById(Match.class, matchId);
		if (match == null) {
			errMsgs.add(ParameterizedMessage.create("MISSING_MATCH"));
			throw new ServiceException(errMsgs);
		}
		if (match.getTeam1() == null || match.getTeam2() == null) {
			errMsgs.add(ParameterizedMessage.create("MISSING_MATCH_TEAMS"));
			throw new ServiceException(errMsgs);
		}

		// update match table
		match.getEvent().getEventId();
		match.setGoalNormalByTeam1(goalNormal1);
		match.setGoalNormalByTeam2(goalNormal2);
		if (!isGroupmatch) {
			match.setGoalExtraByTeam1(goalExtra1);
			match.setGoalExtraByTeam2(goalExtra2);
			match.setGoalPenaltyByTeam1(goalPenalty1);
			match.setGoalPenaltyByTeam2(goalPenalty2);
		}
		match.setResultSignByTeam1(getMatchResult(match, match.getTeam1().getTeamId()));
		
		// After successful transaction commit after the end of this method a new transaction
		// is called asynchronously by event handler, which might update the upcoming matches. 
		// A wrapper service method calling this method and the mentioned asynchronous method 
		// might be easier, but it needs an extra service. 
		final Match finalMatch = match;
		executeAfterTransactionCommits(() -> {
			UpdateEntityEvent<Match> event = new UpdateEntityEvent<>(finalMatch, true);
			applicationEventPublisher.publishEvent(event);			
		});
		
		return match;
	}
	
	private void executeAfterTransactionCommits(Runnable task) {
		TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
			@Override
			public void afterCommit() {
				task.run();
			}
		});
	}
	
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	@Async
	@EventListener(condition = "#event.success")
	public void onUpdateMatchEvent(@NonNull UpdateEntityEvent/*<Match>*/ event) throws ServiceException {
		Match match = (Match)event.getEntity();
		logger.info("onUpdateMatchEvent matchId: {}", match.getMatchId());
		
		updateMatchParticipants(match.getEvent().getEventId(), match.getMatchId());
		// update cached value
		applicationService.refreshEventCompletionPercentCache(match.getEvent().getEventId());
		// invalidate dependent cache(s)
		Cache cache = cacheManager.getCache(CachingConfig.CACHE_EVENT_KNOCKOUT_START_TIME);
		if (cache != null) {
			cache.evictIfPresent(match.getEvent().getEventId());
		}
	}
	
	/**
	 * Returns the signature of the given number.
	 * 
	 * @param number - value to be evaluated
	 * @return 0 if the input is 0, -1 if it is less than 0 and +1 otherwise
	 */
	@VisibleForTesting
	/*private*/ byte sign(byte number) {
		return (byte) (number == 0 ? 0 : (number < 0 ? -1 : +1));
	}
	
	/**
	 * Resets result of a {@link Match} instance belongs to the given matchId.
	 *
	 * @param matchId
	 * @return updated {@link Match} instance
	 * @throws ServiceException mostly if any validation error happens 
	 */
	public Match resetMatch(Long matchId) throws ServiceException {
		
		List<ParameterizedMessage> errMsgs = new ArrayList<>();
		Match match = null;
		
		checkNotNull(matchId);

		if (!errMsgs.isEmpty()) {
			throw new ServiceException(errMsgs);
		}

		// retrieve match from table and validate it
		match = commonDao.findEntityById(Match.class, matchId);
		if (match == null) {
			errMsgs.add(ParameterizedMessage.create("MISSING_MATCH"));
			throw new ServiceException(errMsgs);
		}
		if (match.getTeam1() == null || match.getTeam2() == null) {
			errMsgs.add(ParameterizedMessage.create("MISSING_MATCH_TEAMS"));
			throw new ServiceException(errMsgs);
		}

		// update match table
		match.getEvent().getEventId();
		match.setGoalNormalByTeam1(null);
		match.setGoalNormalByTeam2(null);
		match.setGoalExtraByTeam1(null);
		match.setGoalExtraByTeam2(null);
		match.setGoalPenaltyByTeam1(null);
		match.setGoalPenaltyByTeam2(null);
		match.setResultSignByTeam1(getMatchResult(match, match.getTeam1().getTeamId()));
		
		// After successful transaction commit after the end of this method a new transaction
		// is called asynchronously by event handler, which might update the upcoming matches. 
		// A wrapper service method calling this method and the mentioned asynchronous method 
		// might be easier, but it needs an extra service. 
		final Match finalMatch = match;
		executeAfterTransactionCommits(() -> {
			UpdateEntityEvent<Match> event = new UpdateEntityEvent<>(finalMatch, true);
			applicationEventPublisher.publishEvent(event);			
		});
		
		return match;
	}
	
	/**
	 * Returns given match result on behalf of the provided side team.
	 *  
	 * @param side - this team is taken into account, its possible values are 1 or 2
	 * @param goalNormal1 - goals scored in normal time by team #1
	 * @param goalExtra1 - goals scored in overtime by team #1
	 * @param goalPenalty1 - goals scored in penalties after overtime by team #1
	 * @param goalNormal2 - goals scored in normal time by team #2
	 * @param goalExtra2 - goals scored in overtime by team #2
	 * @param goalPenalty2 - goals scored in penalties after overtime by team #2
	 * @return team on given side 1 is winner, 0 is draw, -1 is defeat, -2 is unknown
	 * @throws ServiceException
	 */
	@Transactional(propagation = Propagation.NOT_SUPPORTED)
	public int getMatchResult(int side, Byte goalNormal1, Byte goalExtra1, Byte goalPenalty1, 
			Byte goalNormal2, Byte goalExtra2, Byte goalPenalty2) throws ServiceException {
		checkArgument(side==1 || side==2, "Argument \"side\" value must be 1 or 2.");
		int result = -2;
		side = (byte) (-1*(side*2-3)); // 1 or 2 -> +1 or -1
		if (goalNormal1 != null && goalNormal2 != null) {
			result = sign((byte) (goalNormal1-goalNormal2))*side;
			if (result != 0) {
				return result;
			}
		}
		if (goalExtra1 != null && goalExtra2 != null) {
			result = sign((byte) (goalExtra1-goalExtra2))*side;
			if (result != 0) {
				return result;
			}
		}
		if (goalPenalty1 != null && goalPenalty2 != null) {
			result = sign((byte) (goalPenalty1-goalPenalty2))*side;
			if (result != 0) {
				return result;
			}
		}
		return result;
	}

	/**
	 * Returns given match result on behalf of the provided team.
	 * 
	 * @param match
	 * @param teamId
	 * @return given team 1 is winner, 0 is draw, -1 is defeat, null is unknown
	 * @throws ServiceException
	 */
	@Transactional(propagation = Propagation.NOT_SUPPORTED)
	public Integer getMatchResult(Match match, Long teamId) throws ServiceException {
		checkNotNull(match);
		checkNotNull(teamId);
		checkArgument(match.getTeam1()!=null && match.getTeam2()!=null, 
				"Argument \"match\" entity must have non-empty \"team1\" and \"team2\" entities.");
		
		if (!teamId.equals(match.getTeam1().getTeamId()) && !teamId.equals(match.getTeam2().getTeamId())) {
			return null;
		}
		int side = teamId.equals(match.getTeam1().getTeamId()) ? 1 : 2;
		Integer matchResult = getMatchResult(side, match.getGoalNormalByTeam1(), match.getGoalExtraByTeam1(), 
				match.getGoalPenaltyByTeam1(), match.getGoalNormalByTeam2(), 
				match.getGoalExtraByTeam2(), match.getGoalPenaltyByTeam2());
		if (matchResult.equals(-2)) {
			matchResult = null;
		}
		return matchResult;
	}

	/**
	 * Returns {@code true} if the match specified by the given parameters is complete. 
	 * The latter means that it is finished, it has final result. 
	 * 
	 * @param isGroupMatch - true if the match is in group stage
	 * @param goalNormal1 - goals scored in normal time by team #1
	 * @param goalExtra1 - goals scored in overtime by team #1
	 * @param goalPenalty1 - goals scored in penalties after overtime by team #1
	 * @param goalNormal2 - goals scored in normal time by team #2
	 * @param goalExtra2 - goals scored in overtime by team #2
	 * @param goalPenalty2 - goals scored in penalties after overtime by team #2
	 * @return {@code true} if the match is complete
	 * @throws ServiceException
	 */
	@VisibleForTesting
	/*private*/ boolean isMatchCompleted(boolean isGroupMatch, Byte goalNormal1, Byte goalExtra1, Byte goalPenalty1, 
			Byte goalNormal2, Byte goalExtra2, Byte goalPenalty2) {
		if (goalNormal1 != null && goalNormal2 != null) {
			int result = sign((byte) (goalNormal1-goalNormal2));
			if (isGroupMatch || result != 0) return true;
		}
		if (goalExtra1 != null && goalExtra2 != null) {
			int result = sign((byte) (goalExtra1-goalExtra2));
			if (result !=0) return true;
		}
		if (goalPenalty1 != null && goalPenalty2 != null) {
			int result = sign((byte) (goalPenalty1-goalPenalty2));
			if (result != 0) return true;
		}
		return false;
	}

	/**
	 * Returns {@code true} if the match specified by the given parameters is complete.
	 * For more details check {@link MatchService#isMatchCompleted(Match)}
	 * 
	 * @param match
	 * @return {@code true} if given match is completed
	 * @throws ServiceException
	 */
	@VisibleForTesting
	/*private*/ boolean isMatchCompleted(Match match) {
		checkNotNull(match);

		return isMatchCompleted(match.getRound().getIsGroupmatchAsBoolean(), 
				match.getGoalNormalByTeam1(), match.getGoalExtraByTeam1(), 
				match.getGoalPenaltyByTeam1(), match.getGoalNormalByTeam2(), 
				match.getGoalExtraByTeam2(), match.getGoalPenaltyByTeam2());
	}
	
	/**
	 * Returns score value after the evaluation of the given parameters.
	 *
	 * @param fav_team_id - favourite team identifier of the user
	 * @param team1_id - team #1 identifier
	 * @param team2_id - team #2 identifier
	 * @param goal_result1 - goals scored in normal time by team #1
	 * @param goal_result2 - goals scored in normal time by team #2
	 * @param goal_bet1 - goals tipped for team #1
	 * @param goal_bet2 - goals tipped for team #2
	 * @return score value after the evaluation
	 * @throws ServiceException
	 */
	@Transactional(propagation = Propagation.NOT_SUPPORTED)
	public int getScore(Long favTeamId, Long team1Id, Long team2Id, Byte goalResult1, Byte goalResult2,
			Byte goalBet1, Byte goalBet2) {
		int score = 0;

		if (team1Id == null || team2Id == null)
			score = 0;
		else if (goalResult1 == null || goalResult2 == null)
			score = 0;
		else if (goalBet1 == null || goalBet2 == null)
			score = 0;
		else if (goalResult1.equals(goalBet1) && goalResult2.equals(goalBet2))
			score = 3;
		else if (goalResult1-goalResult2 == goalBet1-goalBet2) // Byte - Byte => byte - byte = int
			score = 2;
		else if (sign((byte) (goalResult1-goalResult2)) == sign((byte) (goalBet1-goalBet2)))
			score = 1;

		if ((team1Id != null && team1Id.equals(favTeamId)) || (team2Id != null && team2Id.equals(favTeamId)))
			score *= 2;

		return score;
	}
	
	/**
	 * Returns the calculated match endTime from the given {@code startTime}. 
	 * A match may last 45 minutes + 15 minutes (break) + 45 minutes.
	 * 
	 * @param startTime - start datetime of a match
	 * @return calculated endTime which equals to given {@code startTime} + 105 minutes
	 * @throws ServiceException
	 */
	@Transactional(propagation = Propagation.NOT_SUPPORTED)
	public LocalDateTime getEndDateTime(LocalDateTime startTime) {
		checkNotNull(startTime);
		return CommonUtil.plusMinutes(startTime, 105);
	}

	/**
	 * Returns the calculated match endTime after extra time 
	 * from the given {@code startTime}.
	 * The match may last normal 105 minutes + 15 minutes + 5 minutes (break) + 15 minutes.
	 * 
	 * @param startTime - start datetime of a match
	 * @return calculated endTime which equals to given {@code startTime} + 140 minutes
	 * @throws ServiceException
	 */
	@Transactional(propagation = Propagation.NOT_SUPPORTED)
	public LocalDateTime getExtraDateTime(LocalDateTime startTime) {
		checkNotNull(startTime);
		return CommonUtil.plusMinutes(startTime, 140);
	}

	/**
	 * Returns the calculated match endTime after penalty shoot out 
	 * from the given {@code startTime}.
	 * The match lasts normal 105 minutes + 35 minutes AET + 10 minutes PSO.
	 * 
	 * @param startTime - start datetime of a match
	 * @return calculated endTime which equals to given {@code startTime} + 150 minutes
	 */
	@Transactional(propagation = Propagation.NOT_SUPPORTED)
	public LocalDateTime getPenaltyDateTime(LocalDateTime startTime) {
		checkNotNull(startTime);
		return CommonUtil.plusMinutes(startTime, 150);
	}

	/**
	 * Returns the calculated match endTime after penalty shoot out but without
	 * extra time from the given {@code startTime}.
	 * The match lasts normal 105 minutes + 10 minutes PSO.
	 * 
	 * @param startTime - start datetime of a match
	 * @return calculated endTime which equals to given {@code startTime} + 115 minutes
	 */
	@Transactional(propagation = Propagation.NOT_SUPPORTED)
	public LocalDateTime getPenaltyWithoutExtraDateTime(LocalDateTime startTime) {
		checkNotNull(startTime);
		return CommonUtil.plusMinutes(startTime, 115);
	}
	
	/**
	 * Returns a list of {@link GroupPosition} instances belongs to all matches of the
	 * round after group rounds.
	 *  
	 * @param eventId
	 * @return
	 * @throws ServiceException
	 */
	@VisibleForTesting
	/*private*/ List<GroupPosition> retrieveGroupPositionsOfParticipantRules(Long eventId) {
		checkNotNull(eventId);
		
		final String PARTICIPANT_RULE_REGEX = "^([WL])([0-9]+)-([WL])([0-9]+)$";
		List<GroupPosition> groupPositions = new ArrayList<>();

		// retrieve participant rules, e.g. "A1-B2", "A2-BCD3", "W34-W35", "L32-L33", ...
		List<String> participantRules = matchDao.retrieveParticipantRulesOfMatchesByEvent(eventId);
		
		// filter non-group rules, e.g. "W34-W35", "L32-L33"
		participantRules = participantRules.stream()
				.filter(rule->rule.matches(PARTICIPANT_RULE_REGEX))
				.toList();
		
		for (String participantRule : participantRules) {
			Pair<GroupPosition> groupPositionPair = groupTeamService.convertParticipantRuleToGroupPositionPair(participantRule);
			groupPositions.addAll(groupPositionPair.getList());
		}
		
		return groupPositions;
	}
	
	/**
	 * Returns a map containing all {@link Team} instances of the provided {@link Event#getEventId()}
	 * where the key are {@link GroupPosition} instances.
	 * If on a position there are more teams, those teams are not included in the map.
	 *
	 * @param eventId 
	 * @return a map containing group position as key and team as value
	 * @throws ServiceException
	 */
	@VisibleForTesting
	/*private*/ Map<GroupPosition, Team> getTeamByGroupPositionMap(Long eventId) throws ServiceException {
		checkNotNull(eventId);
		
		Map<GroupPosition, Team> teamByGroupPositionMap = new HashMap<>();
		
		for (GroupPosition groupPosition : retrieveGroupPositionsOfParticipantRules(eventId)) {
			String groupName = groupPosition.getGroupName();
			Short matchN = groupPosition.getPosition().shortValue();
			Match match = matchDao.retrieveMatchByMatchN(eventId, matchN);
			if (match == null || (match.getTeam1() == null || match.getTeam2() == null)) {
				continue;
			}
			Integer matchResult = getMatchResult(match, match.getTeam1().getTeamId());
			if (matchResult != null && (matchResult == 1 || matchResult == -1)) {
				Team team = null;
				switch (groupName) {
					case "W" :
						team = matchResult == 1 ? match.getTeam1() : match.getTeam2();
						break;
					case "L" :
						team = matchResult == -1 ? match.getTeam1() : match.getTeam2();
						break;
					default:
						throw new IllegalStateException();
				}
				teamByGroupPositionMap.put(groupPosition, team);
			}
		}
		return teamByGroupPositionMap;
	}

	/**
	 * Updates participants of knockout matches belongs to the provided event by its 
	 * {@code eventId} where at least one participant team is missing.
	 * <p>
	 * <b>Note:</b> it should start a new transaction. 
	 * 
	 * @param eventId
	 * @param updatedMatchId - recently updated {@link Match} instance 
	 * @return number of updated matches
	 * @throws ServiceException 
	 */
//TODO	@Transactional(txType=TransactionAttributeType.REQUIRES_NEW)
	public int updateMatchParticipants(Long eventId, Long updatedMatchId) throws ServiceException {
		checkNotNull(eventId);
		checkNotNull(updatedMatchId);

		int updatedMatches = 0;
		Match updatedMatch = retrieveMatch(updatedMatchId);
		
		Map<GroupPosition, Team> teamByGroupPositionMap;
		if (Boolean.TRUE.equals(updatedMatch.getRound().getIsGroupmatchAsBoolean())) {
			teamByGroupPositionMap = groupService.getTeamByGroupPositionMap(eventId);
		}
		else {
			teamByGroupPositionMap = getTeamByGroupPositionMap(eventId);
		}
		
		List<Match> matches = retrieveMatchesWithoutParticipantsByEvent(eventId);
		for (Match match : matches) {
			if (Strings.isNullOrEmpty(match.getParticipantsRule())) {
				continue;
			}
			Pair<Team> origTeamPair = new Pair<>(match.getTeam1(), match.getTeam2());
			Pair<Team> updatedTeamPair = new Pair<>(match.getTeam1(), match.getTeam2());
			Pair<GroupPosition> groupPositionPair = groupTeamService.
					convertParticipantRuleToGroupPositionPair(match.getParticipantsRule());
			if (updatedTeamPair.getValue1() == null) {
				updatedTeamPair.setValue1(teamByGroupPositionMap.get(groupPositionPair.getValue1()));
			}
			if (updatedTeamPair.getValue2() == null) {
				updatedTeamPair.setValue2(teamByGroupPositionMap.get(groupPositionPair.getValue2()));
			}
			if (!updatedTeamPair.equals(origTeamPair)) {
				match.setTeam1(updatedTeamPair.getValue1());
				match.setTeam2(updatedTeamPair.getValue2());
				updatedMatches++;
			}
		}
		
		if (updatedMatches > 0) {
			commonDao.flushEntityManager();
		}
		return updatedMatches;
	}
	
	/**
	 * Returns a not {@code null} datetime if the given match is finished and has
	 * final result. In this case the calculated end datetime of the match is returned.
	 * Otherwise {@code null} is returned.
	 * 
	 * @param match
	 * @return end datetime of the given finished match or {@code null}
	 * @throws ServiceException
	 */
	@VisibleForTesting
	/*private*/ LocalDateTime getFinishedMatchEndTime(Match match) throws ServiceException {
		checkNotNull(match);
		if (!isMatchCompleted(match)) {
			return null;
		}

		LocalDateTime matchEndTime = getEndDateTime(match.getStartTime());
		if (Boolean.FALSE.equals(match.getRound().getIsGroupmatchAsBoolean())) {
			if (match.getGoalPenaltyByTeam1() != null && match.getGoalPenaltyByTeam2() != null) {
				if (Boolean.TRUE.equals(match.getRound().getIsOvertimeAsBoolean())) {
					matchEndTime = getPenaltyDateTime(match.getStartTime());
				}
				else {
					matchEndTime = getPenaltyWithoutExtraDateTime(match.getStartTime());
				}
			}
			else if (match.getGoalExtraByTeam1() != null && match.getGoalExtraByTeam2() != null) {
				matchEndTime = getExtraDateTime(match.getStartTime());
			}
		}
		
		return matchEndTime;
	}

	/**
	 * Returns a not {@code null} datetime if the given match is incomplete and
	 * it should be escalated. The latter means that it should have final result 
	 * by the returned datetime. 
	 * The function may return also {@code null} value, if the match is not about
	 * escalated at all, it does have already final result.
	 * 
	 * @param match
	 * @return escalation datetime or {@code null}
	 * @throws ServiceException
	 */
	@VisibleForTesting
	/*private*/ LocalDateTime getMatchResultEscalationTime(Match match) throws ServiceException {
		LocalDateTime matchEscalationTime = null;
		checkNotNull(match);
		
		if (match.getGoalNormalByTeam1() == null || match.getGoalNormalByTeam2() == null) {
			matchEscalationTime = getEndDateTime(match.getStartTime());
		}
		else if (Boolean.FALSE.equals(match.getRound().getIsGroupmatchAsBoolean()) && match.getGoalNormalByTeam1() != null
				&& match.getGoalNormalByTeam2() != null
				&& match.getGoalNormalByTeam1() - match.getGoalNormalByTeam2() == 0) {
			if (Boolean.TRUE.equals(match.getRound().getIsOvertimeAsBoolean()) 
					&& (match.getGoalExtraByTeam1() == null || match.getGoalExtraByTeam2() == null)) {
				matchEscalationTime = getExtraDateTime(match.getStartTime());
			}
			else if (Boolean.TRUE.equals(match.getRound().getIsOvertimeAsBoolean())
					&& match.getGoalExtraByTeam1() != null && match.getGoalExtraByTeam2() != null
					&& match.getGoalExtraByTeam1() - match.getGoalExtraByTeam2() == 0
					&& (match.getGoalPenaltyByTeam1() == null || match.getGoalPenaltyByTeam2() == null)) {
				matchEscalationTime = getPenaltyDateTime(match.getStartTime());
			}
			else if (Boolean.FALSE.equals(match.getRound().getIsOvertimeAsBoolean())
					&& (match.getGoalPenaltyByTeam1() == null || match.getGoalPenaltyByTeam2() == null)) {
				matchEscalationTime = getPenaltyWithoutExtraDateTime(match.getStartTime());
			}
		}
		
		return matchEscalationTime;
	}

	/**
	 * Returns a not {@code null} datetime if the given match is in the knock-out 
	 * stage, at least one of its participant teams is missing, however based on 
	 * its participant rule it should have both participant teams by the returned
	 * datetime. 
	 * The function may return also {@code null} value, if the match is not about
	 * escalated at all, it does have both participant teams.
	 *  
	 * @param match
	 * @return escalation datetime or {@code null}
	 * @throws ServiceException
	 */
	@VisibleForTesting
	/*private*/ LocalDateTime getMatchParticipantsEscalationTime(Match match) throws ServiceException {
		LocalDateTime matchEscalationTime = null;
		final String KNOCKOUT_RULE_REGEX = "^([WL])([0-9]+)-([WL])([0-9]+)$";
		checkNotNull(match);
		if (match.getTeam1() != null && match.getTeam2() != null) {
			return null;
		}
		if (match.getRound().getIsGroupmatchAsBoolean()) {
			throw new IllegalStateException("Match in the group stage must have participant teams!");
		}
		// match is in the knock-out stage so
		// retrieve its participant rule, e.g. "A1-B2", "A2-BCD3", "W34-W35", "L32-L33", ...
		String participantsRule = match.getParticipantsRule();
		Pair<GroupPosition> groupPositions = groupTeamService.convertParticipantRuleToGroupPositionPair(participantsRule);
		
		if (participantsRule.matches(KNOCKOUT_RULE_REGEX)) {
			// knock-out participant rule, e.g. "W34-W35", "L32-L33"
			// collect both parent matches
			Match parentMatch1 = matchDao.retrieveMatchByMatchN(match.getEvent().getEventId(), 
					groupPositions.getValue1().getPosition().shortValue());
			Match parentMatch2 = matchDao.retrieveMatchByMatchN(match.getEvent().getEventId(), 
					groupPositions.getValue2().getPosition().shortValue());
			// set the escalation time to the latest one
			LocalDateTime parentMatch1EscalationTime = isMatchCompleted(parentMatch1) ? getFinishedMatchEndTime(parentMatch1) : getMatchResultEscalationTime(parentMatch1);
			LocalDateTime parentMatch2EscalationTime = isMatchCompleted(parentMatch2) ? getFinishedMatchEndTime(parentMatch2) : getMatchResultEscalationTime(parentMatch2);
			matchEscalationTime = Arrays.asList(parentMatch1EscalationTime, parentMatch2EscalationTime).stream()
					.max(LocalDateTime::compareTo).orElse(null);
		}
		else {
			// group stage participant rule, e.g. "A1-B2", "A2-BCD3"
			// collect the unique group names from the participant rule
			String groupPositionNames = groupPositions.getValue1().getGroupName() + groupPositions.getValue2().getGroupName();
			List<String> groupNames = groupPositionNames.chars().mapToObj(e -> String.valueOf((char) e)).distinct()
					.toList();
			// collect all group matches belong to the group names
			List<Match> allGroupMatches = new ArrayList<>();
			for (String groupName : groupNames) {
				Group group = groupService.retrieveGroupByName(match.getEvent().getEventId(), groupName);
				List<Match> groupMatches = retrieveMatchesByGroup(group.getGroupId());
				allGroupMatches.addAll(groupMatches);
			}
			// set the escalation time to the latest one
			matchEscalationTime = allGroupMatches.stream()
					.map(LambdaExceptionUtil.rethrowFunction(e -> isMatchCompleted(e) ? getFinishedMatchEndTime(e) : getMatchResultEscalationTime(e)))
					.max(LocalDateTime::compareTo).orElse(null);
		}
		
		return matchEscalationTime;
	}

	/**
	 * Returns a not {@code null} datetime if the given match is incomplete and
	 * it should be triggered, ie. it should have result on the returned
	 * datetime or it should have participant teams. If the returned datetime is 
	 * equal to the given actualDateTime then the given match should be already 
	 * escalated, otherwise the returned value is an estimated future datetime 
	 * where the match will be probably escalated.
	 * The function may return {@code null} value, if the match is not about
	 * escalated at all, it has already result and it has participant teams.
	 * 
	 * @param match
	 * @param actualDateTime
	 * @return escalation datetime
	 * @throws ServiceException
	 */
	@Transactional(propagation = Propagation.NOT_SUPPORTED)
	public LocalDateTime getMatchTriggerStartTime(Match match, LocalDateTime actualDateTime) throws ServiceException {
		LocalDateTime matchTriggerStartTime = null;
		checkNotNull(match);
		checkNotNull(actualDateTime);

		if (match.getTeam1() == null || match.getTeam2() == null) {
			if (Boolean.TRUE.equals(match.getRound().getIsGroupmatchAsBoolean())) {
				throw new IllegalStateException("Match without participant teams cannot be exist in group stage.");
			}
			// can be only in knock-out stage
			matchTriggerStartTime = getMatchParticipantsEscalationTime(match);
		}
		else {
			matchTriggerStartTime = getMatchResultEscalationTime(match);
		}
		
		if (matchTriggerStartTime != null && matchTriggerStartTime.isBefore(actualDateTime)) {
			matchTriggerStartTime = actualDateTime;
		}

		return matchTriggerStartTime;
	}

	/**
	 * Retrieves all incomplete matches of the given event ordered by 
	 * {@link Match#startTime}. 
	 * A match is incomplete if either it has no final result or any of 
	 * its participant team is missing.
	 * 
	 * @param eventId
	 * @return incomplete matches 
	 * @throws ServiceException 
	 */
	@Transactional(readOnly = true)
	public List<Match> retrieveIncompleteMatchesByEvent(Long eventId) throws ServiceException {
		List<Match> matches = new ArrayList<>();
		
		checkNotNull(eventId);

		List<Match> allMatches = retrieveMatchesByEvent(eventId);
		for (Match match : allMatches) {
			if (match.getTeam1() == null || match.getTeam2() == null || !isMatchCompleted(match)) {
				matches.add(match);
			}
		}
		
		retrieveMatchesByEvent(eventId).stream().filter(e -> e.getTeam1() == null || e.getTeam2() == null || !isMatchCompleted(e)).toList();
		
		return matches;
	}

	/**
	 * Retrieves all incomplete but escalated matches of the given event 
	 * ordered by {@link Match#startTime}. 
	 * A match is incomplete if either it has no final result or any of 
	 * its participant team is missing, however it is supposed to be 
	 * complete on the given actualDateTime.
	 * 
	 * @param eventId
	 * @return escalated incomplete matches 
	 * @throws ServiceException 
	 */
	@Transactional(readOnly = true)
	public List<Match> retrieveIncompleteEscalatedMatchesByEvent(Long eventId, LocalDateTime actualDateTime) throws ServiceException {
		List<Match> matches = new ArrayList<>();
		
		checkNotNull(eventId);
		checkNotNull(actualDateTime);

		List<Match> allMatches = retrieveMatchesByEvent(eventId);
		for (Match match : allMatches) {
			LocalDateTime matchTriggerStartTime = getMatchTriggerStartTime(match, actualDateTime);
			if (matchTriggerStartTime == actualDateTime) {
				matches.add(match);
			}
		}
		return matches;
	}

	/**
	 * Retrieves first incomplete match of the given {@code eventId}. Only event that 
	 * is inside the determined expiration modification time is accounted for.
	 * A match is incomplete if it either has no final result or 
	 * any of its participant teams is missing. First match means that it has 
	 * the earliest startTime.
	 * Returns {@code null} if there is no incomplete match belongs to the given {@code eventId}. 
	 * 
	 * @param eventId
	 * @return match - first incomplete match of the given event
	 * @throws ServiceException 
	 */
	@Transactional(readOnly = true)
	public Match retrieveFirstIncompleteMatchByEvent(Long eventId) throws ServiceException {
		checkNotNull(eventId);
		
		Match firstIncompleteMatch = null;
		LocalDateTime actualDateTime = applicationService.getActualDateTime();
		if (!isInsideExpiredModificationTimeByEventId(eventId, actualDateTime)) {
			return null;
		}
		
		List<Match> matches = retrieveMatchesByEvent(eventId);
		for (Match match : matches) {
			LocalDateTime matchTriggerStartTime = getMatchTriggerStartTime(match, actualDateTime);
			if (matchTriggerStartTime != null) {
				if (firstIncompleteMatch ==  null || matchTriggerStartTime.isBefore(getMatchTriggerStartTime(firstIncompleteMatch, actualDateTime))) {
					firstIncompleteMatch = match;
				}
			}
		}
		return firstIncompleteMatch;
	}

	/**
	 * Retrieves first incomplete matches of all events. Only events that are 
	 * inside the determined expiration modification time are accounted for.
	 * A match is incomplete if it has no final result but it has participant teams.
	 * First match means that it has the earliest startTime.
	 * 
	 * @return list of first incomplete matches of all events 
	 * @throws ServiceException
	 */
	@Transactional(readOnly = true)
	public List<Match> retrieveFirstIncompleteMatchesOfEvents() throws ServiceException {
		List<Match> matches = new ArrayList<>();
		LocalDateTime actualDateTime = applicationService.getActualDateTime();
		
		List<Event> events = eventService.findAllEvents();
		for (Event event : events) {
			if (isInsideExpiredModificationTimeByEventId(event.getEventId(), actualDateTime)) {
				Match match = retrieveFirstIncompleteMatchByEvent(event.getEventId());
				if (match != null) {
					match.getEvent(); // lazy read
					matches.add(match);
				}
			}
		}
		
		return matches;
	}

	/**
	 * Returns a flag which indicates that current sysdate is inside the alloted period after a finished tournament.
	 * 
	 * @param eventId
	 * @return {@code true} if current sysdate is inside of the alloted period after a finished tournament.
	 * @throws ServiceException
	 */
	@Transactional(readOnly = true)
	public boolean isInsideExpiredModificationTimeByEventId(Long eventId, LocalDateTime actualDateTime) throws ServiceException {
		checkNotNull(eventId);
		checkNotNull(actualDateTime);
		Event event = eventService.findEventByEventId(eventId);
		checkState(event != null, String.format("No \"Event\" entity belongs to \"eventId\"=%d in database.", eventId));
		
		int expiredDays = Integer.parseInt(appExpiredDaysEvent);
		if (expiredDays == 0) {
			return false;
		}
		LocalDateTime expiredModificationTime = CommonUtil.plusDays(event.getEndTime(), expiredDays);
		return actualDateTime.isBefore(expiredModificationTime);
	}
	
	/**
	 * Returns a list of {@link Match} instances belongs to the {@link Group} specified 
	 * by the given {@code groupId} parameter. Returned list is ordered 
	 * by {@link Match#startTime}.
	 *  
	 * @param groupId - group belongs to the matches to be retrieved
	 * @throws ServiceException if the matches cannot be retrieved 
	 */
	@Transactional(readOnly = true)
	public List<Match> retrieveMatchesByGroup(Long groupId) throws ServiceException {
		checkNotNull(groupId);
		
		List<Match> matches = matchDao.retrieveMatchesByGroup(groupId);
		
		// load lazy associations
		for (Match match : matches) {
			match.getRound().getRoundId();
			if (match.getTeam1() != null) {
				match.getTeam1().getTeamId();
			}
			if (match.getTeam2() != null) {
				match.getTeam2().getTeamId();
			}
		}
		
		return matches;
	}

	/**
	 * Returns {@code true} if given match participant team on given index is the one
	 * given by teamWsId. If there is no participant there, but from knock-out rules the
	 * team given by teamWsId can be on the position, it return also {@code true}. 
	 * Otherwise returns {@code false}.
	 * 
	 * @param match - searched object
	 * @param matchdata - list where given match must be located
	 * @return {@code true} if match teams are equal to teams in matchdata but they are in reversed order
	 * @throws ServiceException
	 */
	@Transactional(readOnly = true)
	public boolean isCandidateMatchTeam(Match match, long teamWsId, int index) throws ServiceException {
		boolean isExpected = false;
		checkNotNull(match);
		checkArgument(index == 1 || index == 2, "Argument \"index\" must be either 1 or 2.");
		
		Team team = index ==1 ? match.getTeam1() : match.getTeam2();
		if (team != null)
			return team.getWsId().equals(teamWsId);

		// there is no participant team on index, the match cannot be in group stage
		checkState(!match.getRound().getIsGroupmatchAsBoolean(),
				"Match without participant teams cannot be exist in group stage.");
		
		// can given teamWsId be attached to it based on the participants rule of the knock-out match? 
		team = teamService.retrieveTeamByWsId(match.getEvent().getEventId(), teamWsId);
		checkState(team != null, String.format(
				"Team given in \"teamWsId\" with value %d does not exist in \"team\" database table.", teamWsId));
		
		Pair<GroupPosition> groupPositionPair = groupTeamService.
				convertParticipantRuleToGroupPositionPair(match.getParticipantsRule());
		GroupPosition groupPosition = index ==1 ? groupPositionPair.getValue1() : groupPositionPair.getValue2();
		
		final String PARTICIPANT_KNOCKOUT_RULE_REGEX = "^([WL])([0-9]+)-([WL])([0-9]+)$";
		boolean isKnockoutRule = match.getParticipantsRule().matches(PARTICIPANT_KNOCKOUT_RULE_REGEX);
		List<Team> candidateTeams = new ArrayList<>();
		if (!isKnockoutRule) {
			// groupPosition.getGroupName() may contain more group names
			candidateTeams.addAll(teamService.retrieveTeamsByGroupName(match.getEvent().getEventId(), groupPosition.getGroupName()));
		}
		else {
			// groupPosition.getPosition() contains the parent match number  
			Match matchParent = matchDao.retrieveMatchByMatchN(match.getEvent().getEventId(), groupPosition.getPosition().shortValue());
			if (matchParent.getTeam1() != null) {
				candidateTeams.add(matchParent.getTeam1());
			}
			if (matchParent.getTeam2() != null) {
				candidateTeams.add(matchParent.getTeam2());
			}
		}
		isExpected = candidateTeams.contains(team);
		
		return isExpected;
	}

	/**
	 * Updates a {@link Match} object given by the matchId parameter with the given other 
	 * parameters. Those can be its participant teams and/or match final result.
	 * The knock-out stage match result is updated only if the result can be considered 
	 * final.
	 * 
	 * @return updated Match object or {@code null} if no update happened
	 * @throws ServiceException if the matches cannot be retrieved
	 */
	public Match updateMatchByMatchdata(Long matchId, Long team1WsId, Long team2WsId, 
			Byte goalNormal1, Byte goalExtra1, Byte goalPenalty1,
			Byte goalNormal2, Byte goalExtra2, Byte goalPenalty2) throws ServiceException {
		
		List<ParameterizedMessage> errMsgs = new ArrayList<>();
		Match match = null;
		boolean isUpdated = false;
		checkNotNull(matchId);
		
		// update match table

		match = commonDao.findEntityById(Match.class, matchId);
		if (match == null) {
			errMsgs.add(ParameterizedMessage.create("MISSING_MATCH"));
			throw new ServiceException(errMsgs);
		}

		// participant teams
		if (team1WsId != null) {
			Team team = teamService.retrieveTeamByWsId(match.getEvent().getEventId(), (long)team1WsId);
			if (team != null) {
				match.setTeam1(team);
				team.getMatches1().add(match);
				isUpdated = true;
			}
		}
		
		if (team2WsId != null) {
			Team team = teamService.retrieveTeamByWsId(match.getEvent().getEventId(), (long)team2WsId);
			if (team != null) {
				match.setTeam2(team);
				team.getMatches2().add(match);
				isUpdated = true;
			}
		}
		
		// final result
		if (!isMatchCompleted(match) && isMatchCompleted(match.getRound().getIsGroupmatchAsBoolean(), 
				goalNormal1, goalExtra1, goalPenalty1,
				goalNormal2, goalExtra2, goalPenalty2)) {
			
			match.setGoalNormalByTeam1(goalNormal1);
			match.setGoalNormalByTeam2(goalNormal2);
			match.setGoalExtraByTeam1(goalExtra1);
			match.setGoalExtraByTeam2(goalExtra2);
			match.setGoalPenaltyByTeam1(goalPenalty1);
			match.setGoalPenaltyByTeam2(goalPenalty2);
			
			isUpdated = true;
		}
		
		if (isUpdated) {
			commonDao.flushEntityManager();
			
			// Update additional matches setting teams on them.
			updateMatchParticipants(match.getEvent().getEventId(), match.getMatchId());
		}
		
		return isUpdated ? match : null;
	}
	
	/**
	 * Returns a distinct list of start time of matches belongs to the given event. 
	 * The result list is ordered by start time.
	 * 
	 * @param eventId
	 * @return distinct list of start time of matches belongs to the given event
	 * @throws ServiceException
	 */
	@Transactional(readOnly = true)
	public List<LocalDateTime> retrieveMatchStartDatesByEvent(Long eventId) throws ServiceException {
		checkNotNull(eventId);

		List<LocalDateTime> matchDates;
		List<Match> matches = retrieveMatchesByEvent(eventId);
		
		matchDates = matches.stream().map(e -> CommonUtil.truncateDateTime(e.getStartTime())).distinct()
				.toList();
		return matchDates;
	}
	
	/**
	 * Returns a percent value which is the number of all completed matches divided by number 
	 * of all matches belongs to the given eventId parameter. 0 value means the event is not 
	 * started yet, 100 means the event is already finished.
	 * 
	 *  @param eventId
	 *  @return percent value of complete matches divided by all matches
	 *  @throws ServiceException
	 */
	@Transactional(readOnly = true)
	public int retriveMatchesAccomplishedInPercent(Long eventId) throws ServiceException {
		checkNotNull(eventId);
		
		List<Match> matches = retrieveMatchesByEvent(eventId);
		if (matches.isEmpty()) {
			return 0;
		}
		
		List<Match> incompleteMatches = retrieveIncompleteMatchesByEvent(eventId);
		return (matches.size() - incompleteMatches.size()) * 100 / matches.size();
	}

	/**
	 * Relaunches scheduled retrieval of missing match results job if the trigger exists.
	 *  
	 * @param eventId
	 * @throws ServiceException
	 */
	@Transactional(readOnly = true)
	public void refreshMatchesByScheduler(Long eventId) throws ServiceException {
		checkNotNull(eventId);
		
		List<ParameterizedMessage> errMsgs = new ArrayList<>();		

		if (!schedulerService.isSchedulerEnabled()) {
			errMsgs.add(ParameterizedMessage.create("SCHEDULER_DISABLED"));
			throw new ServiceException(errMsgs);
		}
		if (!schedulerService.isExistsRetrieveMatchResultsJobTrigger(eventId)) {
			errMsgs.add(ParameterizedMessage.create("MISSING_SCHEDULED_RETRIEVAL_MATCH_RESULTS_JOB"));
			throw new ServiceException(errMsgs);
		}
		
		Match match = retrieveFirstIncompleteMatchByEvent(eventId);
		if (match != null) {
			boolean isRelaunched = schedulerService.relaunchRetrieveMatchResultsJobTrigger(eventId, match.getMatchId());
			if (isRelaunched) {
				errMsgs.add(ParameterizedMessage.create("SCHEDULED_RMRJ_RELAUNCH_DONE", ParameterizedMessageType.INFO));
			}
			else {
				errMsgs.add(ParameterizedMessage.create("SCHEDULED_RMRJ_RELAUNCH_FAILED"));
			}
		}
		else {
			errMsgs.add(ParameterizedMessage.create("NO_INCOMPLETE_MATCH", ParameterizedMessageType.INFO));
		}

		if (!errMsgs.isEmpty()) {
			throw new ServiceException(errMsgs);
		}
	}
}
