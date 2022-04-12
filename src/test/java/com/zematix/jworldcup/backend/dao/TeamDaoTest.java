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

import com.google.common.collect.Ordering;
import com.zematix.jworldcup.backend.entity.Event;
import com.zematix.jworldcup.backend.entity.Match;
import com.zematix.jworldcup.backend.entity.Team;

/**
 * Contains test functions of {@link TeamDao} class.
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles({"development", "test"})
@Transactional
public class TeamDaoTest {

	@Inject
	private TeamDao teamDao;

	@Inject
	private CommonDao commonDao;

	/**
	 * Test {@link TeamDao#getAllTeams()} method.
	 * Scenario: successfully retrieves a list of all {@link Team} entities
	 */
	@Test
	public void /*List<Team>*/ getAllTeams() {
		List<Team> allExpectedTeams = commonDao.findAllEntities(Team.class);
		List<Team> allTeams = teamDao.getAllTeams();

		// order does not matter
		assertEquals(new HashSet<>(allExpectedTeams), new HashSet<>(allTeams));
	}

	/**
	 * Test {@link TeamDao#retrieveFavouriteGroupTeams(Long)} method.
	 * Scenario: successfully retrieves a list of {@link Team} instances belongs to 
	 * the given eventId parameter. The result team elements must be sorted by name.
	 */
	@Test
	public void /*List<Team>*/ retrieveFavouriteGroupTeams(/*Long eventId*/) {
		Long eventId = 1L; // WC2014 Brazil
		List<Team> teams = teamDao.retrieveFavouriteGroupTeams(eventId);
		
		assertEquals(32, teams.size());

		boolean isSorted = Ordering.natural().isOrdered(teams.stream().map(t -> t.getName()).toList());
		assertTrue("Retrieved list of Team entites should be sorted by its name field.", isSorted);
	}

	/**
	 * Test {@link TeamDao#retrieveFavouriteGroupTeams(Long)} method.
	 * Scenario: retrieves an empty list of Team instances because of mismatched given parameter.
	 */
	@Test
	public void /*List<Team>*/ retrieveFavouriteGroupTeamsNotFound(/*Long eventId*/) {
		Long eventId = -1L;
		List<Team> teams = teamDao.retrieveFavouriteGroupTeams(eventId);
		
		assertTrue(teams != null && teams.isEmpty());
	}

	/**
	 * Test {@link TeamDao#retrieveFavouriteGroupTeams(Long)} method.
	 * Scenario: unsuccessfully retrieves a list of {@link Team} instances because eventId 
	 *           parameter is {@code null}, throws an exception.
	 */
	@Test(expected = IllegalArgumentException.class)
	public void /*List<Team>*/ retrieveFavouriteGroupTeamsNull(/*Long eventId*/) {
		Long eventId = null;

		/*List<Team> teams =*/ teamDao.retrieveFavouriteGroupTeams(eventId);
	}

	/**
	 * Test {@link TeamDao#retrieveFavouriteKnockoutTeams(Long)} method.
	 * Scenario: successfully retrieves a list of {@link Team} instances belongs to 
	 * the given eventId parameter. The result team elements must be sorted by name.
	 */
	@Test
	public void /*List<Team>*/ retrieveFavouriteKnockoutTeams(/*Long eventId*/) {
		Long eventId = 1L; // WC2014

		Match match = commonDao.findEntityById(Match.class, 49L); // first match in the knockout phase
		Team team1 = teamDao.getAllTeams().get(0);
		Team team2 = teamDao.getAllTeams().get(1);
		match.setTeam1(team1);
		match.setTeam2(team2);
		commonDao.flushEntityManager();
		
		match = commonDao.findEntityById(Match.class, 50L); // second match in the knockout phase
		team1 = teamDao.getAllTeams().get(2);
		match.setTeam1(team1);
		commonDao.flushEntityManager();
		
		List<Team> teams = teamDao.retrieveFavouriteKnockoutTeams(eventId);
		
		assertEquals(3, teams.size());

		boolean isSorted = Ordering.natural().isOrdered(teams.stream().map(t -> t.getName()).toList());
		assertTrue("Retrieved list of Team entites should be sorted by its name field.", isSorted);
	}

	/**
	 * Test {@link TeamDao#retrieveFavouriteKnockoutTeams(Long)} method.
	 * Scenario: retrieves an empty list of Team instances because of mismatched given parameter.
	 */
	@Test
	public void /*List<Team>*/ retrieveFavouriteKnockoutTeamsNotFound(/*Long eventId*/) {
		Long eventId = -1L;
		List<Team> teams = teamDao.retrieveFavouriteKnockoutTeams(eventId);
		
		assertTrue(teams != null && teams.isEmpty());
	}

	/**
	 * Test {@link TeamDao#retrieveFavouriteKnockoutTeams(Long)} method.
	 * Scenario: unsuccessfully retrieves a list of {@link Team} instances because eventId 
	 *           parameter is {@code null}, throws an exception.
	 */
	@Test(expected = IllegalArgumentException.class)
	public void /*List<Team>*/ retrieveFavouriteKnockoutTeamsNull(/*Long eventId*/) {
		Long eventId = null;

		/*List<Team> teams =*/ teamDao.retrieveFavouriteKnockoutTeams(eventId);
	}
	
	/**
	 * Test {@link TeamDao#retrieveTeamByWsId(Long, Long)} method.
	 * Scenario: successfully retrieves a list of {@link Team} instances belongs to 
	 * the given {@link Event#eventId} and {@link Team#wsId parameters.
	 */
	@Test
	public void /*Team*/ retrieveTeamByWsId(/*Long eventId, Long wsId*/) {
		Long eventId = 1L; // WC2014
		Long wsId = 753L; // Brazil
		Team team = teamDao.retrieveTeamByWsId(eventId, wsId);
		assertTrue("A single team should be retrieved, the famous Brazil.", team!= null && team.getName().equals("Brazil"));
	}

	/**
	 * Test {@link TeamDao#retrieveTeamByWsId(Long, Long)} method.
	 * Scenario: because one of the given parameters is empty, {@link IllegalArgumentException} is thrown.
	 */
	@Test(expected = IllegalArgumentException.class)
	public void /*Team*/ retrieveTeamByWsIdNull(/*Long eventId, Long wsId*/) {
		Long eventId = null;
		Long wsId = null;

		/*Team team =*/ teamDao.retrieveTeamByWsId(eventId, wsId);
	}

	/**
	 * Test {@link TeamDao#retrieveTeamByWsId(Long, Long)} method.
	 * Scenario: because one of the given parameters is unknown, {@code null} returns.
	 */
	@Test
	public void /*Team*/ retrieveTeamByWsIdUnknown(/*Long eventId, Long wsId*/) {
		Long eventId = -1L;
		Long wsId = -1L;
		Team team = teamDao.retrieveTeamByWsId(eventId, wsId);
		assertNull(team);
	}
}
