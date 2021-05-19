package com.zematix.jworldcup.backend.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import com.zematix.jworldcup.backend.dao.BetDao;
import com.zematix.jworldcup.backend.dao.CommonDao;
import com.zematix.jworldcup.backend.dao.UserDao;
import com.zematix.jworldcup.backend.entity.Bet;
import com.zematix.jworldcup.backend.entity.Event;
import com.zematix.jworldcup.backend.entity.Match;
import com.zematix.jworldcup.backend.entity.Team;
import com.zematix.jworldcup.backend.entity.User;
import com.zematix.jworldcup.backend.entity.UserOfEvent;
import com.zematix.jworldcup.backend.exception.ServiceException;
import com.zematix.jworldcup.backend.util.CommonUtil;

/**
 * Contains test functions of {@link BetService} class.
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles({"development", "test"})
@Transactional
public class BetServiceTest {
	
	@Inject
	private BetService betService;
	
	@Inject
	private CommonDao commonDao;
	
	@MockBean
	private BetDao betDao; // used by methods of BetService
	
	@MockBean
	private UserDao userDao;

	@MockBean
	private ApplicationService applicationService;
	
	@MockBean
	private MatchService matchService;
	
	/**
	 * Test {@link BetService#retrieveBetsByEventAndUser(Long, Long)} method.
	 * * Scenario: successfully retrieves the result
	 */
	@Test
	public void /*List<Bet>*/ retrieveBetsByEventAndUser(/*Long eventId, Long userId*/) throws ServiceException {
		Long eventId = 1L;
		Long userId = 1L;
		
		Bet bet1 = new Bet();
		bet1.setBetId(1L);
		Bet bet2 = new Bet();
		bet2.setBetId(2L);
		List<Bet> bet12 = Arrays.asList(bet1, bet2);
		Mockito.when(betDao.retrieveBetsByEventAndUser(eventId, userId)).thenReturn(bet12);

		List<Bet> bets = betService.retrieveBetsByEventAndUser(eventId, userId);
		assertEquals("Result bet list should be the same as the expected one.", bet12, bets);
	}	

	/**
	 * Test {@link BetService#retrieveBetsByEventAndUser(Long, Long)} method.
	 * Scenario: throws {@link NullPointerException} because of the given {@code null} {@code eventId} value
	 */
	@Test(expected=NullPointerException.class)
	public void /*List<Bet>*/ retrieveBetsByEventAndUserNullEventId(/*Long eventId, Long userId*/) throws ServiceException {
		Long eventId = null;
		Long userId = 1L;
		
		Mockito.when(betDao.retrieveBetsByEventAndUser(eventId, userId)).thenThrow(new IllegalArgumentException());
		
		betService.retrieveBetsByEventAndUser(eventId, userId);
	}	

	/**
	 * Test {@link BetService#retrieveBetsByEventAndUser(Long, Long)} method.
	 * Scenario: throws {@link NullPointerException} because of the given {@code null} {@code userId} value
	 */
	@Test(expected=NullPointerException.class)
	public void /*List<Bet>*/ retrieveBetsByEventAndUserNullUserId(/*Long eventId, Long userId*/) throws ServiceException {
		Long eventId = 1L;
		Long userId = null;
		
		Mockito.when(betDao.retrieveBetsByEventAndUser(eventId, userId)).thenThrow(new IllegalArgumentException());
		
		betService.retrieveBetsByEventAndUser(eventId, userId);
	}	

	/**
	 * Test {@link BetService#retrieveBet(Long)} method.
	 * Scenario: successfully retrieves a {@link Bet} instance.
	 */
	@Test
	public void /*Bet*/ retrieveBet(/*Long betId*/) throws ServiceException {
		Bet bet1 = new Bet();
		Event event = commonDao.findEntityById(Event.class, 1L);
		event.getBets().add(bet1);
		bet1.setEvent(event);
		Match match = commonDao.findEntityById(Match.class, 1L);
		match.getBets().add(bet1);
		bet1.setMatch(match);
		User user = commonDao.findEntityById(User.class, 1L); 
		user.getBets().add(bet1);
		bet1.setUser(user);
		bet1.setGoalNormalByTeam1((byte)1);
		bet1.setGoalNormalByTeam2((byte)1);
		commonDao.persistEntity(bet1);
		
		Long betId = bet1.getBetId();
		Bet bet = betService.retrieveBet(betId);
		assertEquals("Result Bet should be equal to the expected one.", bet1, bet);
		assertTrue("Result Bet should be detached.", !commonDao.containsEntity(bet));
	}	

	/**
	 * Test {@link BetService#retrieveBet(Long)} method.
	 * Scenario: when mismatched betId parameter is used ServiceException is thrown.
	 */
	@Test(expected=ServiceException.class)
	public void /*Bet*/ retrieveBetNotFound(/*Long betId*/) throws ServiceException {
		Long betId = -1L;

		try {
			betService.retrieveBet(betId);
		}
		catch (ServiceException e) {
			assertTrue("There must be a single message in ServiceException named MISSING_TIP", 
					e.getMessages().size()==1 && e.getMessages().get(0).getMsgCode().equals("MISSING_TIP"));
			throw e;
		}
	}	
	
	/**
	 * Test {@link BetService#saveBet(Long, Long, Long, Date, Byte, Byte)} method.
	 * Scenario: creates and saves successfully a new bet
	 */
	@Test
	public void /*Bet*/ saveBet(/*Long userId, Long matchId, Long betId, Date startTime, Byte goalNormal1, Byte goalNormal2*/)
			throws ServiceException {
		Long userId = 1L;
		Long matchId = 1L;
		Match match = commonDao.findEntityById(Match.class, matchId);
		commonDao.detachEntity(match);
		Long betId = null;
		LocalDateTime startTime = match.getStartTime();
		Byte goalNormal1 = (byte)1;
		Byte goalNormal2 = (byte)1;

		Mockito.when(applicationService.getActualDateTime()).thenReturn(startTime);
		
		Mockito.when(matchService.retrieveMatch(matchId)).thenReturn(match);
		
		Bet bet = betService.saveBet(userId, matchId, betId, startTime, goalNormal1, goalNormal2);
		assertNotNull("Result Bet instance should not be null.", bet);
		assertTrue("Result Bet instance should not be detached", commonDao.containsEntity(bet));
	}

	/**
	 * Test {@link BetService#saveBet(Long, Long, Long, Date, Byte, Byte)} method.
	 * Scenario: updates an existing bet
	 */
	@Test
	public void /*Bet*/ saveBetUpdate(/*Long userId, Long matchId, Long betId, Date startTime, Byte goalNormal1, Byte goalNormal2*/)
			throws ServiceException {
		Long userId = 1L;
		Long matchId = 1L;
		Match match = commonDao.findEntityById(Match.class, matchId);
		LocalDateTime startTime = match.getStartTime();
		Byte goalNormal1 = (byte)1;
		Byte goalNormal2 = (byte)1;

		Bet bet1 = new Bet();
		bet1.setUser(commonDao.findEntityById(User.class, userId));
		bet1.setMatch(match);
		bet1.setEvent(match.getEvent());
		bet1.setGoalNormalByTeam1(goalNormal1);
		bet1.setGoalNormalByTeam2(goalNormal2);
		commonDao.persistEntity(bet1);

		goalNormal1 = (byte)0;
		goalNormal2 = (byte)2;
		
		Mockito.when(applicationService.getActualDateTime()).thenReturn(startTime);
		
		Bet bet = betService.saveBet(userId, matchId, bet1.getBetId(), startTime, goalNormal1, goalNormal2);
		assertNotNull("Result Bet instance should not be null.", bet);
		
		assertEquals("Result Bet goalNormal1 value should be updated to the expected value.", goalNormal1, bet.getGoalNormalByTeam1());
		assertEquals("Result Bet goalNormal2 value should be updated to the expected value.", goalNormal2, bet.getGoalNormalByTeam2());
	}

	/**
	 * Test {@link BetService#saveBet(Long, Long, Long, Date, Byte, Byte)} method.
	 * Scenario: deletes an existing bet
	 */
	@Test
	public void /*Bet*/ saveBetDelete(/*Long userId, Long matchId, Long betId, Date startTime, Byte goalNormal1, Byte goalNormal2*/)
			throws ServiceException {
		Long userId = 1L;
		Long matchId = 1L;
		Match match = commonDao.findEntityById(Match.class, matchId);
		LocalDateTime startTime = match.getStartTime();
		Byte goalNormal1 = (byte)1;
		Byte goalNormal2 = (byte)1;

		Bet bet1 = new Bet();
		bet1.setUser(commonDao.findEntityById(User.class, userId));
		bet1.setMatch(match);
		bet1.setEvent(match.getEvent());
		bet1.setGoalNormalByTeam1(goalNormal1);
		bet1.setGoalNormalByTeam2(goalNormal2);
		commonDao.persistEntity(bet1);

		goalNormal1 = null;
		goalNormal2 = null;
		
		Mockito.when(applicationService.getActualDateTime()).thenReturn(startTime);
		
		Bet bet = betService.saveBet(userId, matchId, bet1.getBetId(), startTime, goalNormal1, goalNormal2);
		assertNull("Result Bet instance should be null.", bet);
		
		Bet bet2 = commonDao.findEntityById(Bet.class, bet1.getBetId());
		assertNull("Deleted Bet should not exist in database.", bet2);
	}

	/**
	 * Test {@link BetService#saveBet(Long, Long, Long, Date, Byte, Byte)} method.
	 * Scenario: missing tip hinders the creation of a new bet
	 */
	@Test(expected=ServiceException.class)
	public void /*Bet*/ saveBetMissingTip(/*Long userId, Long matchId, Long betId, Date startTime, Byte goalNormal1, Byte goalNormal2*/)
			throws ServiceException {
		Long userId = 1L;
		Long matchId = 1L;
		Match match = commonDao.findEntityById(Match.class, matchId);
		Long betId = null;
		LocalDateTime startTime = match.getStartTime();
		Byte goalNormal1 = null; // missing tip
		Byte goalNormal2 = (byte)1;

		Mockito.when(applicationService.getActualDateTime()).thenReturn(startTime);
		
		try {
			betService.saveBet(userId, matchId, betId, startTime, goalNormal1, goalNormal2);
		}
		catch (ServiceException e) {
			assertTrue("There must be a single message in ServiceException named MISSING_TIP", 
					e.getMessages().size()==1 && e.getMessages().get(0).getMsgCode().equals("MISSING_TIP"));
			throw e;
		}
	}

	/**
	 * Test {@link BetService#saveBet(Long, Long, Long, Date, Byte, Byte)} method.
	 * Scenario: missing tip hinders the creation of a new bet
	 */
	@Test(expected=ServiceException.class)
	public void /*Bet*/ saveBetInvalidTip(/*Long userId, Long matchId, Long betId, Date startTime, Byte goalNormal1, Byte goalNormal2*/)
			throws ServiceException {
		Long userId = 1L;
		Long matchId = 1L;
		Match match = commonDao.findEntityById(Match.class, matchId);
		Long betId = null;
		LocalDateTime startTime = match.getStartTime();
		Byte goalNormal1 = (byte)1;
		Byte goalNormal2 = (byte)-1; // invalid tip

		Mockito.when(applicationService.getActualDateTime()).thenReturn(startTime);
		
		try {
			betService.saveBet(userId, matchId, betId, startTime, goalNormal1, goalNormal2);
		}
		catch (ServiceException e) {
			assertTrue("There must be a single message in ServiceException named NOT_POSITIVE_VALUE_INVALID", 
					e.getMessages().size()==1 && e.getMessages().get(0).getMsgCode().equals("NOT_POSITIVE_VALUE_INVALID"));
			throw e;
		}
	}

	/**
	 * Test {@link BetService#saveBet(Long, Long, Long, Date, Byte, Byte)} method.
	 * Scenario: missing tip hinders the creation of a new bet
	 */
	@Test(expected=ServiceException.class)
	public void /*Bet*/ saveBetMatchAlreadyStarted(/*Long userId, Long matchId, Long betId, Date startTime, Byte goalNormal1, Byte goalNormal2*/)
			throws ServiceException {
		Long userId = 1L;
		Long matchId = 1L;
		Match match = commonDao.findEntityById(Match.class, matchId);
		Long betId = null;
		LocalDateTime startTime = match.getStartTime();

		// let actualTime be startTime + 1 hour, so the match is already started
		LocalDateTime actualTime = startTime.plus(1, ChronoUnit.HOURS);
		
		
		Byte goalNormal1 = (byte)1;
		Byte goalNormal2 = (byte)1;

		Mockito.when(applicationService.getActualDateTime()).thenReturn(actualTime);
		
		try {
			betService.saveBet(userId, matchId, betId, startTime, goalNormal1, goalNormal2);
		}
		catch (ServiceException e) {
			assertTrue("There must be a single message in ServiceException named MATCH_ALREADY_STARTED", 
					e.getMessages().size()==1 && e.getMessages().get(0).getMsgCode().equals("MATCH_ALREADY_STARTED"));
			throw e;
		}
	}

	/**
	 * Test {@link BetService#retrieveScoreByEventAndUser(Long, Long)} method.
	 * Scenario: calculates the actual score of the given user in the given event
	 */
	@Test
	public void /*int*/ retrieveScoreByEventAndUser(/*Long eventId, Long userId*/) throws ServiceException {
		Long eventId = 1L;
		Event event = commonDao.findEntityById(Event.class, eventId);
		Long userId = 1L;
		User user = commonDao.findEntityById(User.class, userId);
		int expectedScore = 0;
		
		List<Match> matches = new ArrayList<>();
		List<Bet> bets = new ArrayList<>();
		
		// favourite team
		UserOfEvent userOfEvent = new UserOfEvent();
		userOfEvent.setEvent(event);
		userOfEvent.setUser(user);
		
		// match1
		Match match = commonDao.findEntityById(Match.class, 1L);
		matches.add(match);
		Team finalTeam1 = match.getTeam1();
		Team finalTeam2 = match.getTeam2();
		userOfEvent.setFavouriteGroupTeam(finalTeam1); // favourite team in group stage
		userOfEvent.setFavouriteKnockoutTeam(finalTeam1); // favourite team in knockout stage
		commonDao.persistEntity(userOfEvent);
		match.setGoalNormalByTeam1((byte)1);
		match.setGoalNormalByTeam2((byte)1);
		commonDao.flushEntityManager();

		// bet1
		Bet bet = new Bet();
		bet.setUser(user);
		bet.setMatch(match);
		bet.setEvent(match.getEvent());
		bet.setGoalNormalByTeam1((byte)2);
		bet.setGoalNormalByTeam2((byte)2);
		commonDao.persistEntity(bet);
		bets.add(bet);
		expectedScore += 2 * 2; // favourite team involved, double score
		
		// match2
		match = commonDao.findEntityById(Match.class, 2L);
		matches.add(match);
		match.setGoalNormalByTeam1((byte)0);
		match.setGoalNormalByTeam2((byte)1);
		commonDao.flushEntityManager();
		
		// bet2
		bet = new Bet();
		bet.setUser(user);
		bet.setMatch(match);
		bet.setEvent(match.getEvent());
		bet.setGoalNormalByTeam1((byte)1);
		bet.setGoalNormalByTeam2((byte)0);
		commonDao.persistEntity(bet);
		bets.add(bet);
		expectedScore += 0;
		
		// match3
		match = commonDao.findEntityById(Match.class, 3L);
		matches.add(match);
		match.setGoalNormalByTeam1((byte)2);
		match.setGoalNormalByTeam2((byte)0);
		commonDao.flushEntityManager();

		// bet3
		bet = new Bet();
		bet.setUser(user);
		bet.setMatch(match);
		bet.setEvent(match.getEvent());
		bet.setGoalNormalByTeam1((byte)1);
		bet.setGoalNormalByTeam2((byte)0);
		commonDao.persistEntity(bet);
		bets.add(bet);
		expectedScore += 1;

		// match4
		match = commonDao.findEntityById(Match.class, 4L);
		matches.add(match);
		match.setGoalNormalByTeam1((byte)2);
		match.setGoalNormalByTeam2((byte)0);
		commonDao.flushEntityManager();

		// bet4
		bet = new Bet();
		bet.setUser(user);
		bet.setMatch(match);
		bet.setEvent(match.getEvent());
		bet.setGoalNormalByTeam1((byte)2);
		bet.setGoalNormalByTeam2((byte)0);
		commonDao.persistEntity(bet);
		bets.add(bet);
		expectedScore += 3;
		
		// final match
		Match finalMatch = commonDao.findEntityById(Match.class, 64L);
		matches.add(finalMatch);
		finalMatch.setGoalNormalByTeam1((byte)2);
		finalMatch.setGoalNormalByTeam2((byte)0);
		finalMatch.setTeam1(finalTeam1);
		finalMatch.setTeam2(finalTeam2);
		commonDao.flushEntityManager();

		// final bet
		bet = new Bet();
		bet.setUser(user);
		bet.setMatch(finalMatch);
		bet.setEvent(finalMatch.getEvent());
		bet.setGoalNormalByTeam1((byte)2);
		bet.setGoalNormalByTeam2((byte)0);
		commonDao.persistEntity(bet);
		bets.add(bet);
		expectedScore += 3 * 2; // favourite team involved, double score

		Mockito.when(userDao.retrieveUserOfEvent(eventId, userId)).thenReturn(userOfEvent);
		
		Mockito.when(betDao.retrieveBetsByEventAndUser(eventId, userId)).thenReturn(bets);		
		
		// call real method with arbitrary arguments
		Mockito.when(matchService.getScore(
				ArgumentMatchers.anyLong(), ArgumentMatchers.anyLong(), ArgumentMatchers.anyLong(),
				ArgumentMatchers.anyByte(), ArgumentMatchers.anyByte(),
				ArgumentMatchers.anyByte(), ArgumentMatchers.anyByte())).thenCallRealMethod();
		Mockito.when(matchService.sign(ArgumentMatchers.anyByte())).thenCallRealMethod();

		int score = betService.retrieveScoreByEventAndUser(eventId, userId);
		assertEquals("Retrieved score should be the expected one.", expectedScore, score);
	}

	/**
	 * Test {@link BetService#retrieveScoreByEventAndUser(Long, Long)} method.
	 * Scenario: calculated actual score must be 0 because of given invalid {@code eventId}
	 */
	@Test
	public void /*int*/ retrieveScoreByEventAndUser_InvalidEventId(/*Long eventId, Long userId*/) throws ServiceException {
		Long eventId = -1L;
		Long userId = 1L;
		
		int score = betService.retrieveScoreByEventAndUser(eventId, userId);
		assertEquals("Retrieved score should be the expected one.", 0, score);
	}

	/**
	 * Test {@link BetService#retrieveScoreByEventAndUser(Long, Long)} method.
	 * Scenario: calculated actual score must be 0 because of given invalid {@code userId}
	 */
	@Test
	public void /*int*/ retrieveScoreByEventAndUser_InvalidUserId(/*Long eventId, Long userId*/) throws ServiceException {
		Long eventId = 1L;
		Long userId = -1L;
		
		int score = betService.retrieveScoreByEventAndUser(eventId, userId);
		assertEquals("Retrieved score should be the expected one.", 0, score);
	}

	/**
	 * Test {@link BetService#retrieveScoreByEventAndUser(Long, Long)} method.
	 * Scenario: throws {@link NullPointerException} because of then given {@code null} {@code eventId} value. 
	 */
	@Test(expected=NullPointerException.class)
	public void /*int*/ retrieveScoreByEventAndUser_NullEventId(/*Long eventId, Long userId*/) throws ServiceException {
		Long eventId = null;
		Long userId = 1L;
		
		betService.retrieveScoreByEventAndUser(eventId, userId);
	}

	/**
	 * Test {@link BetService#retrieveScoreByEventAndUser(Long, Long)} method.
	 * Scenario: throws {@link NullPointerException} because of then given {@code null} {@code userId} value. 
	 */
	@Test(expected=NullPointerException.class)
	public void /*int*/ retrieveScoreByEventAndUser_NullUserId(/*Long eventId, Long userId*/) throws ServiceException {
		Long eventId = 1L;
		Long userId = null;
		
		betService.retrieveScoreByEventAndUser(eventId, userId);
	}
	
	/**
	 * Test {@link BetService#retrieveScoresByEventAndUser(Long, Long)} method.
	 * Scenario: retrieves a map containing scores on bet dates of the given 
	 *           user in the given event
	 */
	@Test
	public void /*Map<Date, Integer>*/ retrieveScoresByEventAndUser(/*Long eventId, Long userId*/) throws ServiceException {
		Long eventId = 1L;
		Event event = commonDao.findEntityById(Event.class, eventId);
		Long userId = 1L;
		User user = commonDao.findEntityById(User.class, userId);
		
		int score = 0;
		Map<LocalDateTime, Integer> expectedMapScoreByDate = new HashMap<>();
		List<Match> matches = new ArrayList<>();
		List<Bet> bets = new ArrayList<>();
		
		// favourite team
		UserOfEvent userOfEvent = new UserOfEvent();
		userOfEvent.setEvent(event);
		userOfEvent.setUser(user);
		
		// match1
		Match match = commonDao.findEntityById(Match.class, 1L);
		matches.add(match);
		Team finalTeam1 = match.getTeam1();
		Team finalTeam2 = match.getTeam2();
		userOfEvent.setFavouriteGroupTeam(finalTeam1); // favourite team in group stage
		userOfEvent.setFavouriteKnockoutTeam(finalTeam1); // favourite team in knockout stage
		commonDao.persistEntity(userOfEvent);
		match.setGoalNormalByTeam1((byte)1);
		match.setGoalNormalByTeam2((byte)1);
		commonDao.flushEntityManager();

		// bet1
		Bet bet = new Bet();
		bet.setUser(user);
		bet.setMatch(match);
		bet.setEvent(match.getEvent());
		bet.setGoalNormalByTeam1((byte)2);
		bet.setGoalNormalByTeam2((byte)2);
		commonDao.persistEntity(bet);
		bets.add(bet);
		LocalDateTime matchDate = CommonUtil.truncateDateTime(bet.getMatch().getStartTime());
		score += 2 * 2; // favourite team involved, double score
		expectedMapScoreByDate.put(matchDate, score);
		
		// match2
		match = commonDao.findEntityById(Match.class, 2L);
		matches.add(match);
		match.setGoalNormalByTeam1((byte)0);
		match.setGoalNormalByTeam2((byte)1);
		commonDao.flushEntityManager();
		
		// bet2
		bet = new Bet();
		bet.setUser(user);
		bet.setMatch(match);
		bet.setEvent(match.getEvent());
		bet.setGoalNormalByTeam1((byte)1);
		bet.setGoalNormalByTeam2((byte)0);
		commonDao.persistEntity(bet);
		bets.add(bet);
		matchDate = CommonUtil.truncateDateTime(bet.getMatch().getStartTime());
		score += 0;
		expectedMapScoreByDate.put(matchDate, score);
		
		// match3
		match = commonDao.findEntityById(Match.class, 3L);
		matches.add(match);
		match.setGoalNormalByTeam1((byte)2);
		match.setGoalNormalByTeam2((byte)0);
		commonDao.flushEntityManager();

		// bet3
		bet = new Bet();
		bet.setUser(user);
		bet.setMatch(match);
		bet.setEvent(match.getEvent());
		bet.setGoalNormalByTeam1((byte)1);
		bet.setGoalNormalByTeam2((byte)0);
		commonDao.persistEntity(bet);
		bets.add(bet);
		matchDate = CommonUtil.truncateDateTime(bet.getMatch().getStartTime());
		score += 1;
		expectedMapScoreByDate.put(matchDate, score);


		// match4
		match = commonDao.findEntityById(Match.class, 4L);
		matches.add(match);
		match.setGoalNormalByTeam1((byte)2);
		match.setGoalNormalByTeam2((byte)0);
		commonDao.flushEntityManager();

		// bet4
		bet = new Bet();
		bet.setUser(user);
		bet.setMatch(match);
		bet.setEvent(match.getEvent());
		bet.setGoalNormalByTeam1((byte)2);
		bet.setGoalNormalByTeam2((byte)0);
		commonDao.persistEntity(bet);
		bets.add(bet);
		matchDate = CommonUtil.truncateDateTime(bet.getMatch().getStartTime());
		score += 3;
		expectedMapScoreByDate.put(matchDate, score);
		
		// final match
		Match finalMatch = commonDao.findEntityById(Match.class, 64L);
		matches.add(finalMatch);
		finalMatch.setGoalNormalByTeam1((byte)2);
		finalMatch.setGoalNormalByTeam2((byte)0);
		finalMatch.setTeam1(finalTeam1);
		finalMatch.setTeam2(finalTeam2);
		commonDao.flushEntityManager();

		// final bet
		bet = new Bet();
		bet.setUser(user);
		bet.setMatch(finalMatch);
		bet.setEvent(finalMatch.getEvent());
		bet.setGoalNormalByTeam1((byte)2);
		bet.setGoalNormalByTeam2((byte)0);
		commonDao.persistEntity(bet);
		bets.add(bet);
		matchDate = CommonUtil.truncateDateTime(bet.getMatch().getStartTime());
		score += 3 * 2; // favourite team involved, double score
		expectedMapScoreByDate.put(matchDate, score);

		Mockito.when(userDao.retrieveUserOfEvent(eventId, userId)).thenReturn(userOfEvent);
		
		Mockito.when(betDao.retrieveBetsByEventAndUser(eventId, userId)).thenReturn(bets);		
		
		// call real method with arbitrary arguments
		Mockito.when(matchService.getScore(
				ArgumentMatchers.anyLong(), ArgumentMatchers.anyLong(), ArgumentMatchers.anyLong(),
				ArgumentMatchers.anyByte(), ArgumentMatchers.anyByte(),
				ArgumentMatchers.anyByte(), ArgumentMatchers.anyByte())).thenCallRealMethod();
		Mockito.when(matchService.sign(ArgumentMatchers.anyByte())).thenCallRealMethod();

		Map<LocalDateTime, Integer> mapScoreByDate = betService.retrieveScoresByEventAndUser(eventId, userId);
		assertEquals("Retrieved score map should be the expected one.", expectedMapScoreByDate, mapScoreByDate);
	}

	/**
	 * Test {@link BetService#retrieveScoresByEventAndUser(Long, Long)} method.
	 * Scenario: calculated actual score must be 0 because of given invalid {@code eventId}
	 */
	@Test
	public void /*Map<Date, Integer>*/ retrieveScoresByEventAndUser_InvalidEventId(/*Long eventId, Long userId*/) throws ServiceException {
		Long eventId = -1L;
		Long userId = 1L; // admin
		
		Map<LocalDateTime, Integer> expectedMapScoreByDate = new HashMap<>();
		Map<LocalDateTime, Integer> mapScoreByDate = betService.retrieveScoresByEventAndUser(eventId, userId);
		assertEquals("Retrieved score should be the expected one.", expectedMapScoreByDate, mapScoreByDate);
	}

	/**
	 * Test {@link BetService#retrieveScoresByEventAndUser(Long, Long)} method.
	 * Scenario: calculated actual score must be 0 because of given invalid {@code userId}
	 */
	@Test
	public void /*Map<Date, Integer>*/ retrieveScoresByEventAndUser_InvalidUserId(/*Long eventId, Long userId*/) throws ServiceException {
		Long eventId = 1L; // WC2014
		Long userId = -1L;
		
		Map<LocalDateTime, Integer> expectedMapScoreByDate = new HashMap<>();
		Map<LocalDateTime, Integer> mapScoreByDate = betService.retrieveScoresByEventAndUser(eventId, userId);
		assertEquals("Retrieved score should be the expected one.", expectedMapScoreByDate, mapScoreByDate);
	}

	/**
	 * Test {@link BetService#retrieveScoresByEventAndUser(Long, Long)} method.
	 * Scenario: throws {@link NullPointerException} because of then given {@code null} {@code eventId} value. 
	 */
	@Test(expected=NullPointerException.class)
	public void /*Map<Date, Integer>*/ retrieveScoresByEventAndUser_NullEventId(/*Long eventId, Long userId*/) throws ServiceException {
		Long eventId = null;
		Long userId = 1L; // admin
		
		betService.retrieveScoresByEventAndUser(eventId, userId);
	}

	/**
	 * Test {@link BetService#retrieveScoresByEventAndUser(Long, Long)} method.
	 * Scenario: throws {@link NullPointerException} because of then given {@code null} {@code userId} value. 
	 */
	@Test(expected=NullPointerException.class)
	public void /*Map<Date, Integer>*/ retrieveScoresByEventAndUser_NullUserId(/*Long eventId, Long userId*/) throws ServiceException {
		Long eventId = 1L; // WC2014
		Long userId = null;
		
		betService.retrieveScoresByEventAndUser(eventId, userId);
	}
	
	/**
	 * Test {@link BetService#retrieveMaximumScoreByEvent(Long)} method.
	 * Scenario: calculates the actual score of the given user in the given event
	 */
	@Test
	public void /*int*/ retrieveMaximumScoreByEvent(/*Long eventId*/) throws ServiceException {
		Long eventId = 1L; // WC2014
		
		List<Match> matches = new ArrayList<>();
		Map<Match, Integer> scoreByMatchMap = new HashMap<>();
		int expectedScore = 0;
		
		// match1
		Match match = commonDao.findEntityById(Match.class, 1L);
		matches.add(match);
		Team finalTeam1 = match.getTeam1();
		Team finalTeam2 = match.getTeam2();
		match.setGoalNormalByTeam1((byte)1);
		match.setGoalNormalByTeam2((byte)1);
		commonDao.flushEntityManager();
		scoreByMatchMap.put(match, 3 * 2);// favourite team involved, double score
		expectedScore += scoreByMatchMap.get(match);

		// match2
		/*Match*/ match = commonDao.findEntityById(Match.class, 2L);
		matches.add(match);
		match.setGoalNormalByTeam1((byte)0);
		match.setGoalNormalByTeam2((byte)1);
		commonDao.flushEntityManager();
		scoreByMatchMap.put(match, 3);
		expectedScore += scoreByMatchMap.get(match);
		
		// match3
		/*Match*/ match = commonDao.findEntityById(Match.class, 3L);
		matches.add(match);
		match.setGoalNormalByTeam1((byte)2);
		match.setGoalNormalByTeam2((byte)0);
		commonDao.flushEntityManager();
		scoreByMatchMap.put(match, 3);
		expectedScore += scoreByMatchMap.get(match);


		// match4
		/*Match*/ match = commonDao.findEntityById(Match.class, 4L);
		matches.add(match);
		match.setGoalNormalByTeam1((byte)2);
		match.setGoalNormalByTeam2((byte)0);
		commonDao.flushEntityManager();
		scoreByMatchMap.put(match, 3);
		expectedScore += scoreByMatchMap.get(match);

		// final match
		Match finalMatch = commonDao.findEntityById(Match.class, 64L);
		matches.add(finalMatch);
		finalMatch.setGoalNormalByTeam1((byte)2);
		finalMatch.setGoalNormalByTeam2((byte)0);
		finalMatch.setTeam1(finalTeam1);
		finalMatch.setTeam2(finalTeam2);
		commonDao.flushEntityManager();
		scoreByMatchMap.put(finalMatch, 3 * 2);// favourite team involved, double score
		expectedScore += scoreByMatchMap.get(finalMatch);

		Mockito.when(applicationService.getEventCompletionPercentCache(eventId)).thenReturn(100); // complete event

		Mockito.when(matchService.retrieveMatchesByEvent(eventId)).thenReturn(matches);
		
		// full mock for every match
//		matches.stream().forEach(rethrowConsumer(m -> Mockito.when(matchService.getScore(finalTeam1.getTeamId(), 
//					m.getTeam1().getTeamId(), 
//					m.getTeam2().getTeamId(), 
//					m.getGoalNormalByTeam1(), m.getGoalNormalByTeam2(), 
//					m.getGoalNormalByTeam1(), m.getGoalNormalByTeam2())).thenReturn(scoreByMatchMap.get(m))));

		// call real method at each match with full argument matching
//		matches.stream().forEach(rethrowConsumer(m -> Mockito.when(matchService.getScore(finalTeam1.getTeamId(), 
//				m.getTeam1().getTeamId(), 
//				m.getTeam2().getTeamId(), 
//				m.getGoalNormalByTeam1(), m.getGoalNormalByTeam2(), 
//				m.getGoalNormalByTeam1(), m.getGoalNormalByTeam2())).thenCallRealMethod()));

		// call real method with arbitrary arguments
		Mockito.when(matchService.getScore(
				ArgumentMatchers.anyLong(), ArgumentMatchers.anyLong(), ArgumentMatchers.anyLong(),
				ArgumentMatchers.anyByte(), ArgumentMatchers.anyByte(),
				ArgumentMatchers.anyByte(), ArgumentMatchers.anyByte())).thenCallRealMethod();
		Mockito.when(matchService.sign(ArgumentMatchers.anyByte())).thenCallRealMethod();

		int score = betService.retrieveMaximumScoreByEvent(eventId);
		assertEquals("Retrieved score should be the expected one.", expectedScore, score);
	}

	/**
	 * Test {@link BetService#retrieveMaximumScoreByEvent(Long)} method.
	 * Scenario: throws {@link NullPointerException} because of then given {@code null} {@code eventId} value. 
	 */
	@Test(expected=NullPointerException.class)
	public void /*int*/ retrieveMaximumScoreByEvent_NullEventId(/*Long eventId*/) throws ServiceException {
		Long eventId = null;
		
		betService.retrieveMaximumScoreByEvent(eventId);
	}

	/**
	 * Test {@link BetService#retrieveMaximumScoreByEvent(Long)} method.
	 * Scenario: calculated actual score cannot be retrieved because of then given 
	 * {@code null} {@code userId} value. 
	 */
	@Test(expected=IllegalStateException.class)
	public void /*int*/ retrieveMaximumScoreByEvent_IncompleteEventId(/*Long eventId*/) throws ServiceException {
		Long eventId = 1L; // WC2014
		
		Mockito.when(applicationService.getEventCompletionPercentCache(eventId)).thenReturn(99); //incomplete event
		
		betService.retrieveMaximumScoreByEvent(eventId);
	}

}
