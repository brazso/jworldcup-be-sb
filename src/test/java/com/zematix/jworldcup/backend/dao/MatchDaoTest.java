package com.zematix.jworldcup.backend.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import com.zematix.jworldcup.backend.entity.Event;
import com.zematix.jworldcup.backend.entity.Group;
import com.zematix.jworldcup.backend.entity.Match;
import com.zematix.jworldcup.backend.entity.Round;
import com.zematix.jworldcup.backend.entity.Team;
import com.zematix.jworldcup.backend.entity.model.EventShortDescWithYearEnum;

/**
 * Contains test functions of {@link MatchDao} class.
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles({"development", "test"})
@Transactional
public class MatchDaoTest {

	@Inject
	private MatchDao matchDao;

	@Inject
	private CommonDao commonDao;

	@Inject
	private EventDao eventDao;

	/**
	 * Test {@link MatchDao#getAllMatchs()} method.
	 * Scenario: successfully retrieves a list of all {@link Match} entities
	 */
	@Test
	public void /*List<Match>*/ getAllMatchs() {
		List<Match> allExpectedMatches = commonDao.findAllEntities(Match.class);
		List<Match> allMatches = matchDao.getAllMatches();
		
		// order does not matter
		assertEquals(new HashSet<>(allExpectedMatches), new HashSet<>(allMatches));
	}

	/**
	 * Test {@link MatchDao#retrieveMatchesByEvent(Long)} method.
	 * Scenario: successfully retrieves a list of all {@link Match} entities belongs
	 *           to the {@link Event} specified by the given {@link Event#eventId}.
	 */
	@Test
	public void /*List<Match>*/ retrieveMatchesByEvent(/*Long eventId*/) {
		Event event = eventDao.findEventByShortDescWithYear(EventShortDescWithYearEnum.WC2014.name());
		Long eventId = event.getEventId();
		int EXPECTED_MATCHES_SIZE = 64;
		
		List<Match> matches = matchDao.retrieveMatchesByEvent(eventId);

		assertEquals(EXPECTED_MATCHES_SIZE, matches.size());
		
		boolean isAllMatchesBelongToEvent = matches.stream().allMatch(m -> m.getEvent().getEventId().equals(eventId));
		assertTrue("All elements of the retrieved Match list should belong to the expected event.", 
				isAllMatchesBelongToEvent);
	}

	/**
	 * Test {@link MatchDao#retrieveMatchesByEvent(Long)} method.
	 * Scenario: unsuccessfully retrieves a list of all {@link Match} entities because
	 *           of null parameter, it throws an exception.
	 */
	@Test(expected = IllegalArgumentException.class)
	public void /*List<Match>*/ retrieveMatchesByEventNull(/*Long eventId*/) {
		Long eventId = null;
		
		/*List<Match> matches =*/ matchDao.retrieveMatchesByEvent(eventId);
	}

	/**
	 * Test {@link MatchDao#retrieveRoundsByEvent(Long)} method.
	 * Scenario: successfully retrieves an empty list of {@link Round} entities because
	 *           of unknown event parameter.
	 */
	@Test
	public void /*List<Match>*/ retrieveMatchesByEventUnknown(/*Long eventId*/) {
		Long eventId = -1L;

		List<Match> matches = matchDao.retrieveMatchesByEvent(eventId);
		assertTrue(matches.isEmpty());
	}

	/**
	 * Test {@link MatchDao#retrieveFinishedGroupMatchesByTeam(Long)} method.
	 * Scenario: successfully retrieves a list of all {@link Match} entities belongs
	 *           to the {@link Team} specified by the given {@link Team#teamId}. Any
	 *           match element must be finished and located in the group stage.
	 */
	@Test
	public void /*List<Match>*/ retrieveFinishedGroupMatchesByTeam(/*Long teamId*/) {
		Long matchId = 1L; // opening match of WC2014
		Match match = commonDao.findEntityById(Match.class, matchId);
		// adds a fancy result to the match
		match.setGoalNormalByTeam1((byte)1);
		match.setGoalNormalByTeam2((byte)4);
		Long team1Id = match.getTeam1().getTeamId();
		Long team2Id = match.getTeam1().getTeamId();
		commonDao.flushEntityManager();
		List<Match> expectedMatches = Arrays.asList(match);
		
		List<Match> matches = matchDao.retrieveFinishedGroupMatchesByTeam(team1Id);
		// order does not matter
		assertEquals(new HashSet<>(expectedMatches), new HashSet<>(matches));

		/*List<Match>*/ matches = matchDao.retrieveFinishedGroupMatchesByTeam(team2Id);
		assertEquals(new HashSet<>(expectedMatches), new HashSet<>(matches));
	}

	/**
	 * Test {@link MatchDao#retrieveFinishedGroupMatchesByTeam(Long)} method.
	 * Scenario: unsuccessfully retrieves a list of {@link Match} entities because
	 *           of null parameter, it throws an exception.
	 */
	@Test(expected = IllegalArgumentException.class)
	public void /*List<Match>*/ retrieveFinishedGroupMatchesByTeamNull(/*Long teamId*/) {
		Long teamId = null;
		
		/*List<Match> matches =*/ matchDao.retrieveFinishedGroupMatchesByTeam(teamId);
	}

	/**
	 * Test {@link MatchDao#retrieveFinishedGroupMatchesByTeam(Long)} method.
	 * Scenario: successfully retrieves an empty list of {@link Match} entities because
	 *           of unknown team parameter.
	 */
	@Test
	public void /*List<Match>*/ retrieveFinishedGroupMatchesByTeamUnknown(/*Long teamId*/) {
		Long teamId = -1L;

		List<Match> matches = matchDao.retrieveMatchesByEvent(teamId);
		assertTrue(matches.isEmpty());
	}

	/**
	 * Test {@link MatchDao#retrieveFinishedGroupMatchesByGroup(Long)} method.
	 * Scenario: successfully retrieves a list of all {@link Match} entities belongs
	 *           to the {@link Group} specified by the given {@link Group#groupId}. Any
	 *           match element must be finished and located in the group stage.
	 */
	@Test
	public void /*List<Match>*/ retrieveFinishedGroupMatchesByGroup(/*Long groupId*/) {
		Long matchId = 1L; // opening match of WC2014
		Match match = commonDao.findEntityById(Match.class, matchId);
		// adds a fancy result to the match
		match.setGoalNormalByTeam1((byte)1);
		match.setGoalNormalByTeam2((byte)4);
		Long groupId = match.getTeam1().getGroup().getGroupId();
		commonDao.flushEntityManager();
		List<Match> expectedMatches = Arrays.asList(match);
		
		List<Match> matches = matchDao.retrieveFinishedGroupMatchesByGroup(groupId);
		assertEquals(new HashSet<>(expectedMatches), new HashSet<>(matches));
	}

	/**
	 * Test {@link MatchDao#retrieveFinishedGroupMatchesByGroup(Long)} method.
	 * Scenario: unsuccessfully retrieves a list of {@link Match} entities because
	 *           of null parameter, it throws an exception.
	 */
	@Test(expected = IllegalArgumentException.class)
	public void /*List<Match>*/ retrieveFinishedGroupMatchesByGroupNull(/*Long groupId*/) {
		Long groupId = null;

		/*List<Match> matches =*/ matchDao.retrieveFinishedGroupMatchesByGroup(groupId);
	}

	/**
	 * Test {@link MatchDao#retrieveFinishedGroupMatchesByGroup(Long)} method.
	 * Scenario: successfully retrieves an empty list of {@link Match} entities because
	 *           of unknown group parameter.
	 */
	@Test
	public void /*List<Match>*/ retrieveFinishedGroupMatchesByGroupUnknown(/*Long groupId*/) {
		Long groupId = -1L;

		List<Match> matches = matchDao.retrieveFinishedGroupMatchesByGroup(groupId);
		assertTrue(matches.isEmpty());
	}
	
	/**
	 * Test {@link MatchDao#retrieveParticipantRulesOfMatchesByEvent(Long)} method.
	 * Scenario: successfully retrieves an list containing participant rules.
	 */
	@Test
	public void /*List<String>*/ retrieveParticipantRulesOfMatchesByEvent(/*Long eventId*/) {
		int EXPECTED_RULES_SIZE = 16;
		String EXPECTED_FIRST_RULE = "A1-B2";
		String EXPECTED_LAST_RULE = "W61-W62";
		Long eventId = 1L;
		
		List<String> rules = matchDao.retrieveParticipantRulesOfMatchesByEvent(eventId);

		assertEquals(EXPECTED_RULES_SIZE, rules.size());
		assertEquals(EXPECTED_FIRST_RULE, rules.get(0));
		assertEquals(EXPECTED_LAST_RULE, rules.get(EXPECTED_RULES_SIZE-1));
	}

	/**
	 * Test {@link MatchDao#retrieveParticipantRulesOfMatchesByEvent(Long)} method.
	 * Scenario: unsuccessfully retrieves a list of participant rules because
	 *           of null parameter, it throws an exception.
	 */
	@Test(expected = IllegalArgumentException.class)
	public void /*List<String>*/ retrieveParticipantRulesOfMatchesByEventNull(/*Long eventId*/) {
		Long eventId = null;
		
		/*List<String> rules =*/ matchDao.retrieveParticipantRulesOfMatchesByEvent(eventId);
	}

	/**
	 * Test {@link MatchDao#retrieveParticipantRulesOfMatchesByEvent(Long)} method.
	 * Scenario: successfully retrieves an empty list of participantrules because
	 *           of unknown group parameter.
	 */
	@Test
	public void /*List<String>*/ retrieveParticipantRulesOfMatchesByEventUnknown(/*Long eventId*/) {
		Long eventId = -1L;

		List<String> rules = matchDao.retrieveParticipantRulesOfMatchesByEvent(eventId);
		assertTrue(rules.isEmpty());
	}
	
	/**
	 * Test {@link MatchDao#retrieveMatchByMatchN(Long, Short)} method.
	 * Scenario: successfully retrieves a {@link Match} entity belongs to the given parameters. 
	 */
	@Test
	public void /*Match*/ retrieveMatchByMatchN(/*Long eventId, Short matchN*/) {
		Long eventId = 1L;
		Short matchN = 6;
		
		Match match = matchDao.retrieveMatchByMatchN(eventId, matchN);
		assertNotNull(match);
		assertEquals(eventId, match.getEvent().getEventId());
		assertEquals(matchN, match.getMatchN());
	}

	/**
	 * Test {@link MatchDao#retrieveMatchByMatchN(Long, Short)} method.
	 * Scenario: unsuccessfully retrieves a {@link Match} entity because
	 *           of null {@link Event#eventId} parameter, it throws an exception.
	 */
	@Test(expected = IllegalArgumentException.class)
	public void /*Match*/ retrieveMatchByMatchNNullEventId(/*Long eventId, Short matchN*/) {
		Long eventId = null;
		Short matchN = 6;

		/*Match match =*/ matchDao.retrieveMatchByMatchN(eventId, matchN);
	}

	/**
	 * Test {@link MatchDao#retrieveMatchByMatchN(Long, Short)} method.
	 * Scenario: unsuccessfully retrieves a {@link Match} entity because
	 *           of null matchN parameter, it throws an exception.
	 */
	@Test(expected = IllegalArgumentException.class)
	public void /*Match*/ retrieveMatchByMatchNNullMatchN(/*Long eventId, Short matchN*/) {
		Long eventId = 1L;
		Short matchN = null;

		/*Match match =*/ matchDao.retrieveMatchByMatchN(eventId, matchN);
	}

	/**
	 * Test {@link MatchDao#retrieveMatchByMatchN(Long, Short)} method.
	 * Scenario: retrieves a {@code null} value because of unknown {@link Event#eventId} parameter.
	 */
	@Test
	public void /*Match*/ retrieveMatchByMatchNUnknownEventId(/*Long eventId, Short matchN*/) {
		Long eventId = -1L;
		Short matchN = 6;

		Match match = matchDao.retrieveMatchByMatchN(eventId, matchN);
		assertNull(match);
	}

	/**
	 * Test {@link MatchDao#retrieveMatchByMatchN(Long, Short)} method.
	 * Scenario: retrieves a {@code null} value because of unknown {@link Match#matchN} parameter.
	 */
	@Test
	public void /*Match*/ retrieveMatchByMatchNUnknownMatchN(/*Long eventId, Short matchN*/) {
		Long eventId = 1L;
		Short matchN = -1;

		Match match = matchDao.retrieveMatchByMatchN(eventId, matchN);
		assertNull(match);
	}

	/**
	 * Test {@link MatchDao#retrieveMatchesWithoutParticipantsByEvent(Long)} method.
	 * Scenario: successfully retrieves an list of {@Match} instances containing missing participant(s).
	 */
	@Test
	public void /*List<Match>*/ retrieveMatchesWithoutParticipantsByEvent(/*Long eventId*/) {
		int EXPECTED_MATCHES_SIZE = 16;
		String EXPECTED_FIRST_RULE = "A1-B2";
		String EXPECTED_LAST_RULE = "W61-W62";
		Long eventId = 1L;
		
		List<Match> matches = matchDao.retrieveMatchesWithoutParticipantsByEvent(eventId);

		assertEquals(EXPECTED_MATCHES_SIZE, matches.size());
		assertEquals(EXPECTED_FIRST_RULE, matches.get(0).getParticipantsRule());
		assertEquals(EXPECTED_LAST_RULE, matches.get(EXPECTED_MATCHES_SIZE-1).getParticipantsRule());
	}

	/**
	 * Test {@link MatchDao#retrieveMatchesWithoutParticipantsByEvent(Long)} method.
	 * Scenario: successfully retrieves an list of {@Match} instances containing missing participant(s).
	 *           Adding participants to a match, we can expect less elements in the result list by one. 
	 */
	@Test
	public void /*List<Match>*/ retrieveMatchesWithoutParticipantsByEventMinus(/*Long eventId*/) {
		int EXPECTED_MATCHES_SIZE = 15;
		String EXPECTED_FIRST_RULE = "C1-D2";
		String EXPECTED_LAST_RULE = "W61-W62";
		Long eventId = 1L;
		
		// adds participants to a match
		Match match = commonDao.findEntityById(Match.class, 49L);
		match.setTeam1(commonDao.findEntityById(Team.class, 1L));
		match.setTeam2(commonDao.findEntityById(Team.class, 2L));
		commonDao.flushEntityManager();
		
		List<Match> matches = matchDao.retrieveMatchesWithoutParticipantsByEvent(eventId);

		assertEquals(EXPECTED_MATCHES_SIZE, matches.size());
		assertEquals(EXPECTED_FIRST_RULE, matches.get(0).getParticipantsRule());
		assertEquals(EXPECTED_LAST_RULE, matches.get(EXPECTED_MATCHES_SIZE-1).getParticipantsRule());
	}

	/**
	 * Test {@link MatchDao#retrieveMatchesWithoutParticipantsByEvent(Long)} method.
	 * Scenario: unsuccessfully retrieves a list of {@link Match} entities because
	 *           of null {@link Event#eventId} parameter, it throws an exception.
	 */
	@Test(expected = IllegalArgumentException.class)
	public void /*List<Match>*/ retrieveMatchesWithoutParticipantsByEventNull(/*Long eventId*/) {
		Long eventId = null;

		/*List<Match> matches =*/ matchDao.retrieveMatchesWithoutParticipantsByEvent(eventId);
	}

	/**
	 * Test {@link MatchDao#retrieveMatchesWithoutParticipantsByEvent(Long)} method.
	 * Scenario: retrieves an empty list of {@link Match} entities because of unknown {@link Event#eventId} parameter.
	 */
	@Test
	public void /*List<Match>*/ retrieveMatchesWithoutParticipantsByEventUnknown(/*Long eventId*/) {
		Long eventId = -1L;

		List<Match> matches = matchDao.retrieveMatchesWithoutParticipantsByEvent(eventId);
		assertTrue(matches.isEmpty());
	}
	
	/**
	 * Test {@link MatchDao#retrieveMatchesByGroup(Long)} method.
	 * Scenario: successfully retrieves an list of {@Match} instances belongs to the given group.
	 */
	@Test
	public void /*List<Match>*/ retrieveMatchesByGroup(/*Long groupId*/) {
		Long groupId = 1L; // WC2014 Group A
		List<Long> expectedMatchIds = Arrays.asList(1L, 2L, 16L, 20L, 35L, 36L); // ordered list
		List<Match> matches = matchDao.retrieveMatchesByGroup(groupId);
		assertEquals(expectedMatchIds, 
				matches.stream().map(m -> m.getMatchId()).sorted().collect(Collectors.toList()));
	}

	/**
	 * Test {@link MatchDao#retrieveMatchesByGroup(Long)} method.
	 * Scenario: unsuccessfully retrieves a list of {@link Match} entities because
	 *           of {@code null} {@link Group#groupId} parameter and throws 
	 *           {@link IllegalArgumentException} exception.
	 */
	@Test(expected = IllegalArgumentException.class)
	public void /*List<Match>*/ retrieveMatchesByGroupNull(/*Long groupId*/) {
		Long groupId = null;

		/*List<Match> matches =*/ matchDao.retrieveMatchesByGroup(groupId);
	}

	/**
	 * Test {@link MatchDao#retrieveMatchesByGroup(Long)} method.
	 * Scenario: retrieves an empty list of {@link Match} entities because of unknown 
	 *           {@link Group#groupId} parameter.
	 */
	@Test
	public void /*List<Match>*/ retrieveMatchesByGroupUnknown(/*Long groupId*/) {
		Long groupId = -1L;
		List<Match> matches = matchDao.retrieveMatchesByGroup(groupId);
		assertTrue(matches.isEmpty());
	}
}
