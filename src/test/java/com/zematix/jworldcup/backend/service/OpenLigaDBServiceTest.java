package com.zematix.jworldcup.backend.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import java.util.List;

import javax.inject.Inject;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import com.msiggi.openligadb.model.Group;
import com.msiggi.openligadb.model.League;
import com.msiggi.openligadb.model.Match;
import com.msiggi.openligadb.model.Sport;
import com.msiggi.openligadb.model.Team;
import com.zematix.jworldcup.backend.exception.OpenLigaDBException;

/**
 * Contains test functions of {@link OpenLigaDBService} class.
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles({"develop", "test"})
@Transactional
public class OpenLigaDBServiceTest {

	@Inject
	private OpenLigaDBService openLigaDBService;
	
	/**
	 * Test {@link OpenLigaDBService#getAvailableSports()} method.
	 * Scenario: successfully retrieves a non empty list of
	 *           {@link Sport} entities which contains Fußball as sport name.
	 */
	@Test
	public void /*List<Sport>*/ getAvailableSports() throws OpenLigaDBException {
		// given
		// when
		List<Sport> result = openLigaDBService.getAvailableSports();
		// then
		assertTrue("Retrieved list if Sport entities should not be empty.", !result.isEmpty());
	
		String sportName = "Fußball";
		assertTrue(String.format("Among the retrieved Sport entities there should be %s element.", sportName),
				result.stream().anyMatch(e -> e.getSportName().equals(sportName)));
	}

	/**
	 * Test {@link OpenLigaDBService#getAvailableGroups(String, String)} method.
	 * Scenario: successfully retrieves a non empty list of {@link Group} entities.
	 */
	@Test
	public void /*List<Group>*/ getAvailableGroups(/*String leagueShortcut, String leagueSeason*/) throws OpenLigaDBException {
		// given
		String leagueShortcut = "WM-2014";
		String leagueSeason = "2014";
		int expectedGroupListSize = 6; // number of WC2014 groups
		// when
		List<Group> groups = openLigaDBService.getAvailableGroups(leagueShortcut, leagueSeason);
		// then
		assertEquals("Retrieved list of Group entitites should contain the same number of elements as the expected one.",
				expectedGroupListSize, groups.size());
	}


	/**
	 * Test {@link OpenLigaDBService#getAvailableSports()} method.
	 * Scenario: successfully retrieves a non empty list of
	 *           {@link League} entities.
	 */
	@Test
	public void /*List<League>*/ getAvailableLeagues() throws OpenLigaDBException {
		// given
		// when
		List<League> result = openLigaDBService.getAvailableLeagues();
		// then
		assertTrue("Retrieved list if League entities should not be empty.", !result.isEmpty());
	}

	/**
	 * Test {@link OpenLigaDBService#getMatchdata(String, String)} method.
	 * Scenario: retrieves an empty list of {@link Match} entities because of
	 *           {@code null} parameter.
	 */
	@Test
	public void /*List<Match>*/ getMatchdataByLeagueSeasonNull(/*String leagueShortcut, String leagueSeason*/) throws OpenLigaDBException {
		// given
		String leagueShortcut = null;
		String leagueSeason = "2014"; // unknown
		// when & then
		assertThrows(NullPointerException.class, () -> openLigaDBService.getMatchdata(leagueShortcut, leagueSeason));
	}

	/**
	 * Test {@link OpenLigaDBService#getMatchdata(String, String)} method.
	 * Scenario: retrieves an empty list of {@link Match} entities because of
	 *           unknown parameter.
	 */
	@Test
	public void /*List<Match>*/ getMatchdataByLeagueSeasonUnknown(/*String leagueShortcut, String leagueSeason*/) throws OpenLigaDBException {
		// given
		String leagueShortcut = "WM-2014";
		String leagueSeason = "20!$14";
		// when & then
		OpenLigaDBException exception = assertThrows(OpenLigaDBException.class, () -> openLigaDBService.getMatchdata(leagueShortcut, leagueSeason));
		assertEquals("HTTP 404 Not Found", exception.getMessage());
	}
	
	/**
	 * Test {@link OpenLigaDBService#getMatchdata(String, String)} method.
	 * Scenario: successfully retrieves a non empty list of {@link Match} entities.
	 */
	@Test
	public void /*List<Match>*/ getMatchdata(/*String leagueShortcut, String leagueSeason*/) throws OpenLigaDBException {
		// given
		String leagueShortcut = "WM-2014";
		String leagueSeason = "2014";
		int expectedMatchdataListSize = 64; // number of WC2014 matches
		// when
		List<Match> matchDatas = openLigaDBService.getMatchdata(leagueShortcut, leagueSeason);
		// then
		assertEquals("Retrieved list of Matchdata entitites should contain the same number of elements as the expected one.",
				expectedMatchdataListSize, matchDatas.size());
	}

	/**
	 * Test {@link OpenLigaDBService#getAvailableTeams(String, String)} method.
	 * Scenario: successfully retrieves a non empty list of {@link Team} entities.
	 */
	@Test
	public void /*List<Team>*/ getAvailableTeams(/*String leagueShortcut, String leagueSeason*/) throws OpenLigaDBException {
		// given
		String leagueShortcut = "WM-2014";
		String leagueSeason = "2014";
		int expectedTeamListSize = 32; // number of WC2014 matches
		// when
		List<Team> teams = openLigaDBService.getAvailableTeams(leagueShortcut, leagueSeason);
		// then
		assertEquals("Retrieved list of Team entitites should have the same number of elements as the expected one.",
				expectedTeamListSize, teams.size());
	}

	/**
	 * Test {@link OpenLigaDBService#getAvailableTeams(String, String)} method.
	 * Scenario: throws {@link OpenLigaDBException} because of the given non existing 
	 *           {@code null} leagueSeason parameter 
	 */
	@Test(expected=OpenLigaDBException.class)
	public void /*List<Team>*/ getAvailableTeamsByLeagueSeasonUnknown(/*String leagueShortcut, String leagueSeason*/) throws OpenLigaDBException {
		// given
		String leagueShortcut = "WM-2014";
		String leagueSeason = "20!$14"; // mistyped, non existing value
		// when
		List<Team> teams = openLigaDBService.getAvailableTeams(leagueShortcut, leagueSeason);
		// then
		assertTrue("Retrieved list of Matchdata entitites should be empty.", teams.isEmpty());
	}
}

