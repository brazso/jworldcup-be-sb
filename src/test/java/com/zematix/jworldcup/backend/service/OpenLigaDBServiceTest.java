package com.zematix.jworldcup.backend.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import javax.inject.Inject;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import com.msiggi.openligadb.client.Matchdata;
import com.msiggi.openligadb.client.Sport;
import com.zematix.jworldcup.backend.model.openligadb.client.Team;

/**
 * Contains test functions of {@link OpenLigaDBService} class.
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles({"development", "test"})
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
		List<Sport> result = openLigaDBService.getAvailableSports();
		
		assertNotNull("Retrieved list of Sport entities should not be null.", result);
		assertTrue("Retrieved list if Sport entities should not be empty.", !result.isEmpty());
		
		String sportName = "Fußball";
		assertTrue(String.format("Among the retrieved Sport entities there should be %s element.", sportName),
				result.stream().anyMatch(e -> e.getSportsName().equals(sportName)));
	}
	
	/**
	 * Test {@link OpenLigaDBService#getMatchdataByLeagueSaison(String, String)} method.
	 * Scenario: successfully retrieves a non empty list of
	 *           {@link Matchdata} entities.
	 */
	@Test
	public void /*List<Matchdata>*/ getMatchdataByLeagueSaison(/*String leagueShortcut, String leagueSaison*/) throws OpenLigaDBException {
		String leagueShortcut = "WM-2014";
		String leagueSaison = "2014";
		int expectedMatchdataListSize = 64; // number of WC2014 matches
		
		List<Matchdata> matchDatas = openLigaDBService.getMatchdataByLeagueSaison(leagueShortcut, leagueSaison);
		
		assertNotNull("Retrieved list of Matchdata entitites should not be null.", matchDatas);
		assertEquals("Retrieved list of Matchdata entitites should contain the same number of elements as the expected one.",
				expectedMatchdataListSize, matchDatas.size());
	}

	/**
	 * Test {@link OpenLigaDBService#getMatchdataByLeagueSaison(String, String)} method.
	 * Scenario: retrieves an empty list of {@link Matchdata} entities because of
	 *           {@code null} parameter.
	 */
	@Test
	public void /*List<Matchdata>*/ getMatchdataByLeagueSaisonNull(/*String leagueShortcut, String leagueSaison*/) throws OpenLigaDBException {
		String leagueShortcut = null;
		String leagueSaison = "2014"; // unknown
		
		List<Matchdata> matchDatas = openLigaDBService.getMatchdataByLeagueSaison(leagueShortcut, leagueSaison);
		
		assertNotNull("Retrieved list of Matchdata entitites should not be null.", matchDatas);
		assertTrue("Retrieved list of Matchdata entitites should be empty.", matchDatas.isEmpty());
	}

	/**
	 * Test {@link OpenLigaDBService#getMatchdataByLeagueSaison(String, String)} method.
	 * Scenario: retrieves an empty list of {@link Matchdata} entities because of
	 *           unknown parameter.
	 */
	@Test
	public void /*List<Matchdata>*/ getMatchdataByLeagueSaisonUnknown(/*String leagueShortcut, String leagueSaison*/) throws OpenLigaDBException {
		String leagueShortcut = "WM-2014";
		String leagueSaison = "20!$14";
		
		List<Matchdata> matchDatas = openLigaDBService.getMatchdataByLeagueSaison(leagueShortcut, leagueSaison);
		
		assertNotNull("Retrieved list of Matchdata entitites should not be null.", matchDatas);
		assertTrue("Retrieved list of Matchdata entitites should be empty.", matchDatas.isEmpty());
	}
	
	/**
	 * Test {@link OpenLigaDBService#getMatchdata(String, String)} method.
	 * Scenario: successfully retrieves a non empty list of {@link Matchdata} entities.
	 */
	@Test
	public void /*List<Matchdata>*/ getMatchdata(/*String leagueShortcut, String leagueSaison*/) throws OpenLigaDBException {
		String leagueShortcut = "WM-2014";
		String leagueSaison = "2014";
		int expectedMatchdataListSize = 64; // number of WC2014 matches
		
		List<com.zematix.jworldcup.backend.model.openligadb.client.Matchdata> matchDatas = openLigaDBService.getMatchdata(leagueShortcut, leagueSaison);
		
		assertNotNull("Retrieved list of Matchdata entitites should not be null.", matchDatas);
		assertEquals("Retrieved list of Matchdata entitites should contain the same number of elements as the expected one.",
				expectedMatchdataListSize, matchDatas.size());
	}

	/**
	 * Test {@link OpenLigaDBService#getAvailableTeams(String, String)} method.
	 * Scenario: successfully retrieves a non empty list of {@link Team} entities.
	 */
	@Test
	public void /*List<Team>*/ getAvailableTeams(/*String leagueShortcut, String leagueSaison*/) throws OpenLigaDBException {
		String leagueShortcut = "WM-2014";
		String leagueSaison = "2014";
		int expectedTeamListSize = 32; // number of WC2014 matches
		
		List<Team> teams = openLigaDBService.getAvailableTeams(leagueShortcut, leagueSaison);
		
		assertNotNull("Retrieved list of Team entitites should not be null.", teams);
		assertTrue("Retrieved list of Team entitites should not be empty.", !teams.isEmpty());
		assertEquals("Retrieved list of Team entitites should have the same number of elements as the expected one.",
				expectedTeamListSize, teams.size());
	}

	/**
	 * Test {@link OpenLigaDBService#getAvailableTeams(String, String)} method.
	 * Scenario: throws {@link OpenLigaDBException} because of the given non existing 
	 *           {@code null} leagueSaison parameter 
	 */
	@Test(expected=OpenLigaDBException.class)
	public void /*List<Team>*/ getAvailableTeamsByLeagueSaisonUnknown(/*String leagueShortcut, String leagueSaison*/) throws OpenLigaDBException {
		String leagueShortcut = "WM-2014";
		String leagueSaison = "20!$14"; // mistyped, non existing value
		
		List<Team> teams = openLigaDBService.getAvailableTeams(leagueShortcut, leagueSaison);
		
		assertNotNull("Retrieved list of Matchdata entitites should not be null.", teams);
		assertTrue("Retrieved list of Matchdata entitites should be empty.", teams.isEmpty());
	}
}

