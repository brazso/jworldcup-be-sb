package com.zematix.jworldcup.backend.repository;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.HashSet;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import com.zematix.jworldcup.backend.dao.CommonDao;
import com.zematix.jworldcup.backend.entity.Bet;
import com.zematix.jworldcup.backend.entity.Event;
import com.zematix.jworldcup.backend.entity.Match;
import com.zematix.jworldcup.backend.entity.User;

@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles({"development", "test"})
@Transactional
public class BetRepositoryTest {
  
    @Autowired
    private BetRepository betRepository;
    
    @Autowired
    private CommonDao commonDao;
 
	/**
	 * Test {@link BetRepository#getAllBets()} method.
	 * Scenario: successfully retrieves a list of all {@link Bet} entities
	 */
	@Test
	public void /*List<Bet>*/ getAllBets() {
		List<Bet> allExpectedBets = commonDao.findAllEntities(Bet.class);
		List<Bet> allBets = betRepository.getAllBets();
		
		assertEquals(new HashSet<>(allExpectedBets), new HashSet<>(allBets));
	}
	
	/**
	 * Test {@link BetRepository#findBetByMatchAndUser(Long, Long)} method.
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
		
		Bet bet = betRepository.findBetByMatchAndUser(matchId, userId);

		assertEquals(expectedBet, bet);
	}
	
	/**
	 * Test {@link BetRepository#retrieveBetsByEvent(Long)} method.
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
		
		Bet bet = betRepository.findBetByMatchAndUser(/*matchId*/ 2L, userId);

		assertNull(bet);
	}
	
	/**
	 * Test {@link BetRepository#findBetByMatchAndUserMatchIdNull(Long, Long)} method.
	 * Scenario: unsuccessfully retrieves a {@link Bet} instance because matchId 
	 *           parameter is {@code null}, throws an exception.
	 */
	public void /*Bet*/ findBetByMatchAndUserMatchIdNull(/*Long matchId, Long userId*/) {
		Long matchId = null;
		Long userId = 1L;

		/*Bet bet =*/ betRepository.findBetByMatchAndUser(matchId, userId);
	}

	/**
	 * Test {@link BetRepository#findBetByMatchAndUserMatchIdNull(Long, Long)} method.
	 * Scenario: throws exception because {@code userId} parameter is {@code null}
	 */
	@Test(expected = NullPointerException.class)
	public void /*Bet*/ findBetByMatchAndUserUserIdNull(/*Long matchId, Long userId*/) {
		Long matchId = 1L;
		Long userId = null;

		try {
			/*Bet bet =*/ betRepository.findBetByMatchAndUser(matchId, userId);
		} catch (InvalidDataAccessApiUsageException e) { // rethrows cause
			if (e.getCause() instanceof IllegalArgumentException) {
				throw (IllegalArgumentException) e.getCause();
			}
		}
	}
	
	/**
	 * Test {@link BetRepository#retrieveBetsByEventAndUser(Long, Long)} method.
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
		
		List<Bet> bets = betRepository.retrieveBetsByEventAndUser(eventId, userId);

		assertEquals(BETS_SIZE, bets.size());
	}

	/**
	 * Test {@link BetRepository#retrieveBetsByEventAndUser(Long, Long)} method.
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
		
		List<Bet> bets = betRepository.retrieveBetsByEventAndUser(/*eventId*/ 2L, userId);

		assertTrue(bets.isEmpty());
	}

	/**
	 * Test {@link BetRepository#retrieveBetsByEventAndUser(Long, Long)} method.
	 * Scenario: throws exception because {@code eventId} parameter is {@code null}
	 */
	@Test(expected = NullPointerException.class)
	public void /*List<Bet>*/ retrieveBetsByEventAndUserNullEventId(/*Long eventId, Long userId*/) {
		Long eventId = null;
		Long userId = 1L;

		try {
			/*List<Bet> bets =*/ betRepository.retrieveBetsByEventAndUser(eventId, userId);
		} catch (InvalidDataAccessApiUsageException e) { // rethrows cause
			if (e.getCause() instanceof IllegalArgumentException) {
				throw (IllegalArgumentException) e.getCause();
			}
		}
	}

	/**
	 * Test {@link BetRepository#retrieveBetsByEventAndUser(Long, Long)} method.
	 * Scenario: throws exception because {@link userId} parameter is {@code null}
	 */
	@Test(expected = NullPointerException.class)
	public void /*List<Bet>*/ retrieveBetsByEventAndUserNullUserId(/*Long eventId, Long userId*/) {
		Long eventId = 1L;
		Long userId = null;

		try {
			/*List<Bet> bets =*/ betRepository.retrieveBetsByEventAndUser(eventId, userId);
		} catch (InvalidDataAccessApiUsageException e) { // rethrows cause
			if (e.getCause() instanceof IllegalArgumentException) {
				throw (IllegalArgumentException) e.getCause();
			}
		}
		
	}

	/**
	 * Test {@link BetRepository#retrieveBetsByEventAndUser(Long, Long)} method.
	 * Scenario: retrieves an empty list of {@link Bet} entities because 
	 *           eventId parameter specifies no {@link Event} instance. 
	 */
	@Test
	public void /*List<Bet>*/ retrieveBetsByEventAndUserUnknownEventId(/*Long eventId, Long userId*/) {
		Long eventId = -1L;
		Long userId = 1L;
		List<Bet> bets = betRepository.retrieveBetsByEventAndUser(eventId, userId);
		
		assertTrue(bets.isEmpty());		
	}

	/**
	 * Test {@link BetRepository#retrieveBetsByEventAndUser(Long, Long)} method.
	 * Scenario: retrieves an empty list of {@link Bet} entities because 
	 *           userId parameter specifies no {@link User} instance. 
	 */
	@Test
	public void /*List<Bet>*/ retrieveBetsByEventAndUserUnknownUserId(/*Long eventId, Long userId*/) {
		Long eventId = 1L;
		Long userId = -1L;
		List<Bet> bets = betRepository.retrieveBetsByEventAndUser(eventId, userId);
		
		assertTrue(bets.isEmpty());		
	}

}