package com.zematix.jworldcup.backend.service;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.collect.Iterables;
import com.zematix.jworldcup.backend.dao.BetDao;
import com.zematix.jworldcup.backend.dao.CommonDao;
import com.zematix.jworldcup.backend.entity.Bet;
import com.zematix.jworldcup.backend.entity.Match;
import com.zematix.jworldcup.backend.entity.User;
import com.zematix.jworldcup.backend.entity.UserGroup;
import com.zematix.jworldcup.backend.entity.UserOfEvent;
import com.zematix.jworldcup.backend.exception.ServiceException;
import com.zematix.jworldcup.backend.model.Pair;
import com.zematix.jworldcup.backend.model.ParameterizedMessage;
import com.zematix.jworldcup.backend.util.CommonUtil;

/**
 * Operations around {@link Bet} elements. 
 * Usually it injects at least its DAO class belongs to the same entities. 
 * It may also inject other service and DAO classes.
 */
@Service
@Transactional
public class BetService extends ServiceBase {

	@Inject
	private ApplicationService applicationService;
	
	@Inject
	private BetDao betDao;

	@Inject
	private MatchService matchService;
	
	@Inject
	private UserOfEventService userOfEventService;

	@Inject 
	private CommonDao commonDao;

	/**
	 * Returns a list of found {@link Bet} instances with the provided {@code eventId}
	 * and {@code userId}.
	 * 
	 * @param eventId
	 * @param userId
	 * @return list of found {@Bet} instances
	 * @throws IllegalArgumentException if any of the given parameters is {@code null}
	 */
	@Transactional(readOnly = true)
	public List<Bet> retrieveBetsByEventAndUser(Long eventId, Long userId) throws ServiceException {
		checkNotNull(eventId);
		checkNotNull(userId);

		List<Bet> bets = betDao.retrieveBetsByEventAndUser(eventId, userId);
		Pair<Long> favouriteTeamIds = null;
		for (Bet bet: bets) {
			if (favouriteTeamIds == null) {
				favouriteTeamIds = retrieveFavouriteTeamIdsByBet(bet);
			}
			bet.setScore(retrieveScoreByBet(bet, favouriteTeamIds));
			bet.setFavouriteTeamIndex(retrieveFavouriteTeamIndexByBet(bet, favouriteTeamIds));
		}
		
		// load lazy associations
		for (Bet bet: bets) {
			Match match = bet.getMatch();
			if (match.getTeam1() != null) {
				match.getTeam1().getName();
				match.getTeam1().getGroup().getName();
			}
			if (match.getTeam2() != null) {
				match.getTeam2().getName();
				match.getTeam2().getGroup().getName();
			}
			bet.getUser().getLoginName();
			bet.getUser().getRoles().size();
		}

		return bets;
	}
	
	/**
	 * Returns found {@link Bet} instance with the provided {@link Bet#betId}.
	 * @param betId
	 * @return found bet
	 */
	@Transactional(readOnly = true)
	public Bet retrieveBet(Long betId) throws ServiceException {
		checkNotNull(betId);
		
		List<ParameterizedMessage> errMsgs = new ArrayList<>();
		
		Bet bet = commonDao.findEntityById(Bet.class, betId);
		if (bet == null) {
			errMsgs.add(ParameterizedMessage.create("MISSING_TIP"));
			throw new ServiceException(errMsgs);
		}

		// load lazy associations
		Match match = bet.getMatch();
		if (match.getTeam1() != null) {
			match.getTeam1().getName();
			match.getTeam1().getGroup().getName();
		}
		if (match.getTeam2() != null) {
			match.getTeam2().getName();
			match.getTeam2().getGroup().getName();
		}
		bet.getUser().getLoginName();
		bet.getUser().getRoles().size();

		bet.setScore(retrieveScoreByBet(bet, null));
		bet.setFavouriteTeamIndex(retrieveFavouriteTeamIndexByBet(bet, null));
		
		commonDao.detachEntity(bet);
	
		return bet;
	}
	
	/**
	 * Returns {@link Bet} instances belongs to the provided {@link Match#matchId} and {@link UserGroup#userGroupId}.
	 * @param matchId
	 * @param userGroupId
	 * @return found bets
	 */
	@Transactional(readOnly = true)
	public List<Bet> retrieveBetsByMatchAndUserGroup(Long matchId, Long userGroupId) throws ServiceException {
		checkNotNull(matchId);
		checkNotNull(userGroupId);
		
		List<Bet> bets = betDao.retrieveBetsByMatchAndUserGroup(matchId, userGroupId);
		
		// load lazy associations
		for (Bet bet: bets) {
			Match match = bet.getMatch();
			if (match.getTeam1() != null) {
				match.getTeam1().getName();
				match.getTeam1().getGroup().getName();
			}
			if (match.getTeam2() != null) {
				match.getTeam2().getName();
				match.getTeam2().getGroup().getName();
			}
			bet.getUser().getLoginName();
			bet.getUser().getRoles().size();
		}
		
		for (Bet bet: bets) {
			bet.setScore(retrieveScoreByBet(bet, null));
			bet.setFavouriteTeamIndex(retrieveFavouriteTeamIndexByBet(bet, null));
		}
		
		return bets;
	}
	
	/**
	 * Creates / updates a new bet or deletes an existing bet.
	 * Creates a new bet if given {@code betId} is {@code null} and both given 
	 * {@code goalNormal} parameters are non negative numbers. Updates a bet if given 
	 * {@code betId} is not {@code null} and both given {@code goalNormal} parameters 
	 * are non negative numbers. Deletes a bet if {@code betId} is not {@code null} and 
	 * both {@code goalNormal} parameters have {@code null} values.
	 * Bet cannot be created or modified if application time is over the given startTime. 
	 * 
	 * @param userId - user belongs to the bet, mandatory
	 * @param matchId - match belongs to the bet, mandatory
	 * @param betId - optional
	 * @param startTime - match start time, mandatory
	 * @param goalNormal1 - bet goal scored by team1 during normal match time, optional
	 * @param goalNormal2 - bet goal scored by team2 during normal match time, optional
	 * @throws ServiceException - operation failed
	 * @return saved/updated {@link Bet} instance or {@code null} if deleted
	 */
	public Bet saveBet(Long userId, Long matchId, Long betId, LocalDateTime startTime, Byte goalNormal1, Byte goalNormal2)
			throws ServiceException {
		checkNotNull(userId);
		checkNotNull(matchId);
		checkNotNull(startTime);
		
		List<ParameterizedMessage> errMsgs = new ArrayList<>();
		Bet bet = null;

		if (betId != null && goalNormal1 == null && goalNormal2 == null) {
			// removes bet - see later
		} else if (goalNormal1 == null || goalNormal2 == null) {
			errMsgs.add(ParameterizedMessage.create("MISSING_TIP"));
		} else if (goalNormal1 < 0 || goalNormal2 < 0) {
			errMsgs.add(ParameterizedMessage.create("NOT_POSITIVE_VALUE_INVALID"));
		} else if (applicationService.getActualDateTime().isAfter(startTime)) {
			errMsgs.add(ParameterizedMessage.create("MATCH_ALREADY_STARTED"));
		}

		if (!errMsgs.isEmpty()) {
			throw new ServiceException(errMsgs);
		}

		if (betId == null) {
			// creation of a new bet
			Match match = matchService.retrieveMatch(matchId);
			bet = new Bet();
			bet.setUser(commonDao.findEntityById(User.class, userId));
			bet.setMatch(match);
			bet.setEvent(match.getEvent());
			bet.setGoalNormalByTeam1(goalNormal1);
			bet.setGoalNormalByTeam2(goalNormal2);
			commonDao.persistEntity(bet);
		} else {
			if (goalNormal1 == null && goalNormal2 == null) {
				// removing existing bet
				bet = commonDao.findEntityById(Bet.class, betId);
				commonDao.removeEntity(bet);
				bet = null;
			} else {
				// updating existing bet
				bet = commonDao.findEntityById(Bet.class, betId);
				bet.setGoalNormalByTeam1(goalNormal1);
				bet.setGoalNormalByTeam2(goalNormal2);
			}
		}

		if (bet != null) {
			// load lazy associations
			bet.getUser().getLoginName();
			bet.getUser().getRoles().size();
		}
		
		if (bet != null) {
			bet.setScore(this.retrieveScoreByBet(bet, null));
			bet.setFavouriteTeamIndex(retrieveFavouriteTeamIndexByBet(bet, null));
		}
		
		if (!errMsgs.isEmpty()) {
			throw new ServiceException(errMsgs);
		}

		return bet;
	}

	/**
	 * Delete {@link Bet} instance belongs to the given betId.
	 *
	 * @param betId
	 * @throws ServiceException mostly if any validation error happens 
	 */
	public void deleteBet(Long betId) throws ServiceException {
		
		List<ParameterizedMessage> errMsgs = new ArrayList<>();
		Bet bet = null;
		
		checkNotNull(betId);

		if (!errMsgs.isEmpty()) {
			throw new ServiceException(errMsgs);
		}

		// retrieve bet from table
		bet = commonDao.findEntityById(Bet.class, betId);
		if (bet == null) {
			errMsgs.add(ParameterizedMessage.create("MISSING_BET"));
			throw new ServiceException(errMsgs);
		}

		// update match table
		commonDao.removeEntity(bet);
	}
	
	/**
	 * Returns favouriteTeamId pair belongs to the user of the given bet.
	 * 
	 *  @param bet
	 *  @return favouriteTeamId belong to the given bet.
	 */
	private Pair<Long> retrieveFavouriteTeamIdsByBet(Bet bet) throws ServiceException {
		checkNotNull(bet);
		
		Long favouriteGroupTeamId = null;
		Long favouriteKnockoutTeamId = null;
		
		UserOfEvent userOfEvent = userOfEventService.retrieveUserOfEvent(bet.getEvent().getEventId(), bet.getUser().getUserId());
		if (userOfEvent != null) {
			if (userOfEvent.getFavouriteGroupTeam() != null) {
				favouriteGroupTeamId = userOfEvent.getFavouriteGroupTeam().getTeamId();
			}
			if (userOfEvent.getFavouriteKnockoutTeam() != null) {
				favouriteKnockoutTeamId = userOfEvent.getFavouriteKnockoutTeam().getTeamId();
			}
		}
		
		return new Pair<>(favouriteGroupTeamId, favouriteKnockoutTeamId);
	}
	
	/**
	 * Returns calculated score gained by given {@code bet} and {@code favouriteTeamId}.
	 * If latter one is not given, its value is retrieved from database.
	 * 
	 *  @param bet
	 *  @param favouriteTeamId
	 */
	private int retrieveScoreByBet(Bet bet, Pair<Long> favouriteTeamIds) throws ServiceException {
		checkNotNull(bet);
		
		Long favouriteGroupTeamId = null;
		Long favouriteKnockoutTeamId = null;
		if (favouriteTeamIds == null) {
			favouriteTeamIds = retrieveFavouriteTeamIdsByBet(bet);
		}
		if (favouriteTeamIds != null) { 
			favouriteGroupTeamId = favouriteTeamIds.getValue1();
			favouriteKnockoutTeamId = favouriteTeamIds.getValue2();
		}
		
		Match match = bet.getMatch();
		checkNotNull(match);
		Long favouriteTeamId = match.getRound().getIsGroupmatchAsBoolean() ? favouriteGroupTeamId : favouriteKnockoutTeamId;
		int score = matchService.getScore(favouriteTeamId, 
				match.getTeam1() != null ? match.getTeam1().getTeamId() : null, 
				match.getTeam2() != null ? match.getTeam2().getTeamId() : null, 
				match.getGoalNormalByTeam1(), match.getGoalNormalByTeam2(), 
				bet.getGoalNormalByTeam1(), bet.getGoalNormalByTeam2());

		return score;
	}

	/**
	 * Returns favourite team index gained by given {@code bet} and {@code favouriteTeamId}.
	 * If latter one is not given, its value is retrieved from database.
	 * Index comes from the position of the bet match teams, its value can be 0 or 1. If no 
	 * favourite team is among the match participant teams, {@code null} returns.
	 *  @param bet
	 *  @param favouriteTeamId
	 */
	private Integer retrieveFavouriteTeamIndexByBet(Bet bet, Pair<Long> favouriteTeamIds) throws ServiceException {
		checkNotNull(bet);
		Integer favouriteTeamIndex = null;
		
		if (favouriteTeamIds == null) {
			favouriteTeamIds = retrieveFavouriteTeamIdsByBet(bet);
		}

		boolean isGroupmatch = Boolean.TRUE.equals(bet.getMatch().getRound().getIsGroupmatchAsBoolean());
		Long favouriteTeamId = isGroupmatch ? favouriteTeamIds.getValue1() : favouriteTeamIds.getValue2();
		if (bet.getMatch().getTeam1().getTeamId().equals(favouriteTeamId)) {
			favouriteTeamIndex = 0;
		}
		else if (bet.getMatch().getTeam2().getTeamId().equals(favouriteTeamId)) {
			favouriteTeamIndex = 1;
		}

		return favouriteTeamIndex;
	}

	/**
	 * Returns calculated score gained by given {@code userId} user on given 
	 * {@code eventID} event.
	 * 
	 *  @param eventId
	 *  @param userId
	 */
	@Transactional(readOnly = true)
	public int retrieveScoreByEventAndUser(Long eventId, Long userId) throws ServiceException {
		checkNotNull(eventId);
		checkNotNull(userId);

		List<Bet> bets = retrieveBetsByEventAndUser(eventId, userId);
		return bets.stream().mapToInt(Bet::getScore).sum();
	}

	/**
	 * Returns a map containing calculated score gained by given {@code userId} user on given 
	 * {@code eventID} event on days of the event. The latter one are the keys of the map and
	 * those are the dates of the bets wagered by the user.
	 * 
	 *  @param eventId
	 *  @param userId
	 */
	@Transactional(readOnly = true)
	public Map<LocalDateTime, Integer> retrieveScoresByEventAndUser(Long eventId, Long userId) throws ServiceException {
		checkNotNull(eventId);
		checkNotNull(userId);

		Map<LocalDateTime, Integer> mapScoreByDate = new HashMap<>();
		int score = 0;
		List<Bet> bets = retrieveBetsByEventAndUser(eventId, userId);
		for (Bet bet : bets) {
			LocalDateTime matchDate = CommonUtil.truncateDateTime(bet.getMatch().getStartTime());
			score += bet.getScore();
			mapScoreByDate.put(matchDate, score);
		}
		return mapScoreByDate;
	}

	/**
	 * Returns theoretically maximum score which can be gained on the given 
	 * {@code eventID} event.
	 * 
	 *  @param eventId
	 */
	@Transactional(readOnly = true)
	public int retrieveMaximumScoreByEvent(Long eventId) throws ServiceException {
		checkNotNull(eventId);
		checkState(applicationService.getEventCompletionPercentCache(eventId) == 100, 
				String.format("Event with eventId=%d must be completed.", eventId));
	
		int score = 0;
		List<Match> matches = matchService.retrieveMatchesByEvent(eventId);
		Match finalMatch = Iterables.getLast(matches, null);
		Long favouriteGroupTeamId = null;
		Long favouriteKnockoutTeamId = null;
		checkState(finalMatch != null && finalMatch.getTeam1() != null, 
				String.format("There must be final match with participant teams of a completed event with eventId=%d.", eventId));
		favouriteGroupTeamId = finalMatch.getTeam1().getTeamId();
		favouriteKnockoutTeamId = finalMatch.getTeam1().getTeamId();
		
		for (Match match : matches) {
			Long favouriteTeamId = match.getRound().getIsGroupmatchAsBoolean() ? favouriteGroupTeamId : favouriteKnockoutTeamId;
			score += matchService.getScore(favouriteTeamId, 
					match.getTeam1() != null ? match.getTeam1().getTeamId() : null, 
					match.getTeam2() != null ? match.getTeam2().getTeamId() : null, 
					match.getGoalNormalByTeam1(), match.getGoalNormalByTeam2(), 
					match.getGoalNormalByTeam1(), match.getGoalNormalByTeam2());
		}
		return score;
	}

}
