package com.zematix.jworldcup.backend.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.HashSet;
import java.util.List;

import javax.inject.Inject;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import com.zematix.jworldcup.backend.entity.Bet;
import com.zematix.jworldcup.backend.entity.Event;
import com.zematix.jworldcup.backend.entity.Match;
import com.zematix.jworldcup.backend.entity.User;

/**
 * Contains test functions of {@link BetDao} class.
 * Remember to add @RunWith(SpringRunner.class) annotation to @SpringBootTest because of JUnit 4 usage.
 * Add @DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD) if you want to 
 * reset the context, e.g. reset generated values (sequences) between each tests.
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles({"development", "test"})
@Transactional
public class BetDaoTest {

	@Inject
	private BetDao betDao;

	@Inject
	private CommonDao commonDao;

	/**
	 * Test {@link BetDao#getAllBets()} method.
	 * Scenario: successfully retrieves a list of all {@link Bet} entities
	 */
	@Test
	public void /*List<Bet>*/ getAllBets() {
		List<Bet> allExpectedBets = commonDao.findAllEntities(Bet.class);
		List<Bet> allBets = betDao.getAllBets();
		
		assertEquals(new HashSet<>(allExpectedBets), new HashSet<>(allBets));
	}
	
	/**
	 * Test {@link BetDao#findBetByMatchAndUser(Long, Long)} method.
	 * Scenario: successfully retrieves a {@link Bet} instance.
	 */
	@Test
	public void /*Bet*/ findBetByMatchAndUser(/*Long matchId, Long userId*/) {
		Long matchId = 1L;
		Long userId = 1L;
		Match match = commonDao.findEntityById(Match.class, matchId);
		User user = commonDao.findEntityById(User.class, userId);
		
		Bet expectedBet = new Bet();
		expectedBet.setUser(user);
		expectedBet.setMatch(match);
		expectedBet.setEvent(match.getEvent());
		expectedBet.setGoalNormalByTeam1((byte)0);
		expectedBet.setGoalNormalByTeam2((byte)0);
		commonDao.persistEntity(expectedBet);
		
		Bet bet = betDao.findBetByMatchAndUser(matchId, userId);

		assertEquals(expectedBet, bet);
	}
	
	/**
	 * Test {@link BetDao#retrieveBetsByEvent(Long)} method.
	 * Scenario: retrieves {@code null} value because of mismatched given parameters.
	 */
	@Test
	public void /*Bet*/ findBetByMatchAndUserNotFound(/*Long matchId, Long userId*/) {
		Long matchId = 1L;
		Long userId = 1L;
		Match match = commonDao.findEntityById(Match.class, matchId);
		User user = commonDao.findEntityById(User.class, userId);
		
		Bet expectedBet = new Bet();
		expectedBet.setUser(user);
		expectedBet.setMatch(match);
		expectedBet.setEvent(match.getEvent());
		expectedBet.setGoalNormalByTeam1((byte)0);
		expectedBet.setGoalNormalByTeam2((byte)0);
		commonDao.persistEntity(expectedBet);
		
		Bet bet = betDao.findBetByMatchAndUser(/*matchId*/ 2L, userId);
		assertNull(bet);
	}
	
	/**
	 * Test {@link BetDao#findBetByMatchAndUserMatchIdNull(Long, Long)} method.
	 * Scenario: unsuccessfully retrieves a {@link Bet} instance because matchId 
	 *           parameter is {@code null}, throws an exception.
	 */
	@Test(expected = IllegalArgumentException.class)
	public void /*Bet*/ findBetByMatchAndUserMatchIdNull(/*Long matchId, Long userId*/) {
		Long matchId = null;
		Long userId = 1L;

		/*List<Bet> bets =*/ betDao.findBetByMatchAndUser(matchId, userId);
	}

	/**
	 * Test {@link BetDao#findBetByMatchAndUserMatchIdNull(Long, Long)} method.
	 * Scenario: unsuccessfully retrieves a {@link Bet} instance because userId
	 *           parameter is {@code null}, throws exception.
	 */
	@Test(expected = IllegalArgumentException.class)
	public void /*Bet*/ findBetByMatchAndUserUserIdNull(/*Long matchId, Long userId*/) {
		Long matchId = 1L;
		Long userId = null;

		/*List<Bet> bets =*/ betDao.findBetByMatchAndUser(matchId, userId);
	}
	
	/**
	 * Test {@link BetDao#retrieveBetsByEventAndUser(Long, Long)} method.
	 * Scenario: successfully retrieves a list of {@link Bet} instances.
	 */
	@Test
	public void /*List<Bet>*/ retrieveBetsByEventAndUser(/*Long eventId, Long userId*/) {
		final int BETS_SIZE = 5;
		Long eventId = 1L;
		Long userId = 1L;
		Event event = commonDao.findEntityById(Event.class, eventId);
		User user = commonDao.findEntityById(User.class, userId);
		
		for (int i=0; i<event.getMatches().size() && i<BETS_SIZE; i++) {
			Match match = event.getMatches().get(i);
		
			Bet bet = new Bet();
			bet.setUser(user);
			bet.setMatch(match);
			bet.setEvent(match.getEvent());
			bet.setGoalNormalByTeam1((byte)0);
			bet.setGoalNormalByTeam2((byte)0);
			commonDao.persistEntity(bet);
		}
		
		List<Bet> bets = betDao.retrieveBetsByEventAndUser(eventId, userId);
		assertEquals(BETS_SIZE, bets.size());
	}

	/**
	 * Test {@link BetDao#retrieveBetsByEventAndUser(Long, Long)} method.
	 * Scenario: retrieves an empty list of {@link Bet} instances because of mismatched parameters
	 */
	@Test
	public void /*List<Bet>*/ retrieveBetsByEventAndUserEmpty(/*Long eventId, Long userId*/) {
		final int BETS_SIZE = 5;
		Long eventId = 1L;
		Long userId = 1L;
		Event event = commonDao.findEntityById(Event.class, eventId);
		User user = commonDao.findEntityById(User.class, userId);
		
		for (int i=0; i<event.getMatches().size() && i<BETS_SIZE; i++) {
			Match match = event.getMatches().get(i);
		
			Bet bet = new Bet();
			bet.setUser(user);
			bet.setMatch(match);
			bet.setEvent(match.getEvent());
			bet.setGoalNormalByTeam1((byte)0);
			bet.setGoalNormalByTeam2((byte)0);
			commonDao.persistEntity(bet);
		}
		
		List<Bet> bets = betDao.retrieveBetsByEventAndUser(/*eventId*/ 2L, userId);
		assertTrue(bets.isEmpty());
	}

	/**
	 * Test {@link BetDao#retrieveBetsByEventAndUser(Long, Long)} method.
	 * Scenario: unsuccessfully retrieves a list of {@link Bet} entities because 
	 *           eventId parameter is null, throws an exception.
	 */
	@Test(expected = IllegalArgumentException.class)
	public void /*List<Bet>*/ retrieveBetsByEventAndUserNullEventId(/*Long eventId, Long userId*/) {
		Long eventId = null;
		Long userId = 1L;
		
		/*List<Bet> bets =*/ betDao.retrieveBetsByEventAndUser(eventId, userId);
	}

	/**
	 * Test {@link BetDao#retrieveBetsByEventAndUser(Long, Long)} method.
	 * Scenario: unsuccessfully retrieves a list of {@link Bet} entities because 
	 *           userId parameter is null, throws an exception.
	 */
	@Test(expected = IllegalArgumentException.class)
	public void /*List<Bet>*/ retrieveBetsByEventAndUserNullUserId(/*Long eventId, Long userId*/) {
		Long eventId = 1L;
		Long userId = null;
		
		/*List<Bet> bets =*/ betDao.retrieveBetsByEventAndUser(eventId, userId);
	}

	/**
	 * Test {@link BetDao#retrieveBetsByEventAndUser(Long, Long)} method.
	 * Scenario: retrieves an empty list of {@link Bet} entities because 
	 *           eventId parameter specifies no {@link Event} instance. 
	 */
	@Test
	public void /*List<Bet>*/ retrieveBetsByEventAndUserUnknownEventId(/*Long eventId, Long userId*/) {
		Long eventId = -1L;
		Long userId = 1L;
		
		List<Bet> bets = betDao.retrieveBetsByEventAndUser(eventId, userId);
		assertTrue(bets.isEmpty());		
	}

	/**
	 * Test {@link BetDao#retrieveBetsByEventAndUser(Long, Long)} method.
	 * Scenario: retrieves an empty list of {@link Bet} entities because 
	 *           userId parameter specifies no {@link User} instance. 
	 */
	@Test
	public void /*List<Bet>*/ retrieveBetsByEventAndUserUnknownUserId(/*Long eventId, Long userId*/) {
		Long eventId = 1L;
		Long userId = -1L;
		
		List<Bet> bets = betDao.retrieveBetsByEventAndUser(eventId, userId);
		assertTrue(bets.isEmpty());		
	}
}
