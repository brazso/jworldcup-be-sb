package com.zematix.jworldcup.backend.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.collect.Ordering;
import com.zematix.jworldcup.backend.dao.CommonDao;
import com.zematix.jworldcup.backend.dao.TeamDao;
import com.zematix.jworldcup.backend.entity.Group;
import com.zematix.jworldcup.backend.entity.Team;

/**
 * Contains test functions of {@link TeamService} class.
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles({"development", "test"})
@Transactional
public class TeamServiceTest {

	@Inject
	private TeamService teamService;

	@Inject
	private CommonDao commonDao;

	@MockBean
	private TeamDao teamDao; // used by methods of TeamService

	@MockBean
	private GroupService groupService; // used by methods of TeamService

	/**
	 * Test {@link TeamService#retrieveFavouriteGroupTeams(Long)} method.
	 * Scenario: successfully retrieves a list of {@link Team} instances belongs to 
	 * the given eventId parameter. The result team elements must be sorted by name.
	 */
	@Test
	public void /*List<Team>*/ retrieveFavouriteGroupTeams(/*Long eventId*/) {
		Long eventId = 1L; // WC2014 Brazil

		Team team1 = commonDao.findEntityById(Team.class, 1L);
		Team team2 = commonDao.findEntityById(Team.class, 2L);
		List<Team> expectedTeams = Arrays.asList(team1, team2);
		Mockito.when(teamDao.retrieveFavouriteGroupTeams(eventId)).thenReturn(expectedTeams);
		
		List<Team> teams = teamService.retrieveFavouriteGroupTeams(eventId);
		
		assertEquals("Retrieved list of Team instances must have the same as the expected one.", expectedTeams, teams);

		boolean isSorted = Ordering.natural().isOrdered(teams.stream().map(t -> t.getName()).collect(Collectors.toList()));
		assertTrue("Retrieved list of Team entites should be sorted by its name field.", isSorted);
	}

	/**
	 * Test {@link Team#retrieveFavouriteGroupTeams(Long)} method.
	 * Scenario: retrieves an empty list of Team instances because of mismatched given parameter.
	 */
	@Test
	public void /*List<Team>*/ retrieveFavouriteGroupTeamsNotFound(/*Long eventId*/) {
		Long eventId = -1L;
		
		List<Team> expectedTeams = new ArrayList<>();
		Mockito.when(teamDao.retrieveFavouriteGroupTeams(eventId)).thenReturn(expectedTeams);
		
		List<Team> teams = teamService.retrieveFavouriteGroupTeams(eventId);
		
		assertTrue("Should retrieve an empty list of Team instance because of the given parameter.", teams != null && teams.isEmpty());
	}

	/**
	 * Test {@link Team#retrieveFavouriteGroupTeams(Long)} method.
	 * Scenario: unsuccessfully retrieves a list of {@link Team} instances because eventId 
	 *           parameter is {@code null}, throws an exception.
	 */
	@Test(expected=IllegalArgumentException.class)
	public void /*List<Team>*/ retrieveFavouriteGroupTeamsNull(/*Long eventId*/) {
		Long eventId = null;
		
		Mockito.when(teamDao.retrieveFavouriteGroupTeams(eventId)).thenThrow(new IllegalArgumentException());
		
		/*List<Team> teams =*/ teamService.retrieveFavouriteGroupTeams(eventId);
	}

	/**
	 * Test {@link Team#retrieveFavouriteKnockoutTeams(Long)} method.
	 * Scenario: successfully retrieves a list of {@link Team} instances belongs to 
	 * the given eventId parameter. The result team elements must be sorted by name.
	 */
	@Test
	public void /*List<Team>*/ retrieveFavouriteKnockoutTeams(/*Long eventId*/) {
		Long eventId = 1L; // WC2014

		Team team1 = commonDao.findEntityById(Team.class, 1L);
		Team team2 = commonDao.findEntityById(Team.class, 2L);
		List<Team> expectedTeams = Arrays.asList(team1, team2);
		Mockito.when(teamDao.retrieveFavouriteKnockoutTeams(eventId)).thenReturn(expectedTeams);
		
		List<Team> teams = teamService.retrieveFavouriteKnockoutTeams(eventId);
		
		assertEquals("Retrieved list of Team instances must have the same as the expected one.", expectedTeams, teams);

		boolean isSorted = Ordering.natural().isOrdered(teams.stream().map(t -> t.getName()).collect(Collectors.toList()));
		assertTrue("Retrieved list of Team entites should be sorted by its name field.", isSorted);
	}

	/**
	 * Test {@link Team#retrieveFavouriteKnockoutTeams(Long)} method.
	 * Scenario: retrieves an empty list of Team instances because of mismatched given parameter.
	 */
	@Test
	public void /*List<Team>*/ retrieveFavouriteKnockoutTeamsNotFound(/*Long eventId*/) {
		Long eventId = -1L;
		
		List<Team> expectedTeams = new ArrayList<>();
		Mockito.when(teamDao.retrieveFavouriteKnockoutTeams(eventId)).thenReturn(expectedTeams);

		List<Team> teams = teamService.retrieveFavouriteKnockoutTeams(eventId);
		
		assertTrue("Should retrieve an empty list of Team instance because of the given parameter.", teams != null && teams.isEmpty());
	}

	/**
	 * Test {@link Team#retrieveFavouriteKnockoutTeams(Long)} method.
	 * Scenario: unsuccessfully retrieves a list of {@link Team} instances because eventId 
	 *           parameter is {@code null}, throws an exception.
	 */
	@Test(expected=IllegalArgumentException.class)
	public void /*List<Team>*/ retrieveFavouriteKnockoutTeamsNull(/*Long eventId*/) {
		Long eventId = null;
		
		Mockito.when(teamDao.retrieveFavouriteKnockoutTeams(eventId)).thenThrow(new IllegalArgumentException());
		
		/*List<Team> teams =*/ teamService.retrieveFavouriteKnockoutTeams(eventId);
	}

	
	/**
	 * Test {@link Team#retrieveTeamByWsId(Long, Long)} method.
	 * Scenario: successfully retrieves a {@link Team} entity belongs to 
	 *           the given parameters.
	 */
	@Test
	public void /*Team*/ retrieveTeamByWsId(/*Long eventId, Long wsId*/) {
		Long eventId = 1L;
		Long wsId = 1L;
		Team expectedTeam = new Team();
		
		Mockito.when(teamDao.retrieveTeamByWsId(eventId, wsId)).thenReturn(expectedTeam);

		Team team = teamService.retrieveTeamByWsId(eventId, wsId);
		assertEquals("Retrieved Team entity should be the same as the expected one.", 
				expectedTeam, team);
	}

	/**
	 * Test {@link Team#retrieveTeamByWsId(Long, Long)} method.
	 * Scenario: throws {@link IllegalArgumentException} because eventId 
	 *           parameter is {@code null}.
	 */
	@Test(expected=IllegalArgumentException.class)
	public void /*Team*/ retrieveTeamByWsIdNullEventId(/*Long eventId, Long wsId*/) {
		Long eventId = null;
		Long wsId = 1L;

		teamService.retrieveTeamByWsId(eventId, wsId);
	}

	/**
	 * Test {@link Team#retrieveTeamByWsId(Long, Long)} method.
	 * Scenario: throws {@link IllegalArgumentException} because wsId 
	 *           parameter is {@code null}.
	 */
	@Test(expected=IllegalArgumentException.class)
	public void /*Team*/ retrieveTeamByWsIdNullWsId(/*Long eventId, Long wsId*/) {
		Long eventId = 1L;
		Long wsId = null;
		
		teamService.retrieveTeamByWsId(eventId, wsId);
	}

	
	/**
	 * Test {@link Team#retrieveTeamsByGroupName(Long, String)} method.
	 * Scenario: successfully retrieves a list of {@link Team} entities belongs to 
	 *           the given parameters.
	 */
	@Test
	public void /*List<Team>*/ retrieveTeamsByGroupName(/*Long eventId, String groupName*/) throws ServiceException {
		Long eventId = 1L;
		String groupName = "ABC";
		
		List<Group> expectedGroups = Arrays.asList(groupName.split("")).stream().map(e -> {Group g = new Group(); g.setName(e); g.setTeams(Arrays.asList(new Team(), new Team())); return g;}).collect(Collectors.toList());
		List<Team> expectedTeams = expectedGroups.stream().flatMap(e -> e.getTeams().stream()).collect(Collectors.toList());
		
		expectedGroups.stream().forEach(e -> {
			try {
				Mockito.when(groupService.retrieveGroupByName(eventId, e.getName())).thenReturn(e);
			} catch (ServiceException e1) {
				fail("Should not throw ServiceException: " + e);
			}
		});

		List<Team> teams = teamService.retrieveTeamsByGroupName(eventId, groupName);
		
		assertEquals("Retrieved list of Team entities should be the same as the expected one.", 
				expectedTeams, teams);
	}

	/**
	 * Test {@link Team#retrieveTeamsByGroupName(Long, String)} method.
	 * Scenario: throws {@link IllegalArgumentException} because eventId parameter 
	 *           is {@code null}.
	 */
	@Test(expected=IllegalArgumentException.class)
	public void /*List<Team>*/ retrieveTeamsByGroupName_NullEventId(/*Long eventId, String groupName*/) throws ServiceException {
		Long eventId = null;
		String groupName = "ABC";
		
		teamService.retrieveTeamsByGroupName(eventId, groupName);
	}

	/**
	 * Test {@link Team#retrieveTeamsByGroupName(Long, String)} method.
	 * Scenario: throws {@link IllegalArgumentException} because groupName parameter 
	 *           is {@code null}.
	 */
	@Test(expected=IllegalArgumentException.class)
	public void /*List<Team>*/ retrieveTeamsByGroupName_NullGroupName(/*Long eventId, String groupName*/) throws ServiceException {
		Long eventId = 1L;
		String groupName = null;
		
		teamService.retrieveTeamsByGroupName(eventId, groupName);
	}

	/**
	 * Test {@link Team#retrieveTeamsByGroupName(Long, String)} method.
	 * Scenario: throws {@link IllegalArgumentException} because groupName parameter 
	 *           is {@code null}.
	 */
	@Test(expected=IllegalArgumentException.class)
	public void /*List<Team>*/ retrieveTeamsByGroupName_EmptyGroupName(/*Long eventId, String groupName*/) throws ServiceException {
		Long eventId = 1L;
		String groupName = "";
		
		teamService.retrieveTeamsByGroupName(eventId, groupName);
	}

	/**
	 * Test {@link Team#retrieveTeamsByGroupName(Long, String)} method.
	 * Scenario: throws {@link IllegalArgumentException} because eventId parameter 
	 *           is unknown.
	 */
	@Test(expected=IllegalArgumentException.class)
	public void /*List<Team>*/ retrieveTeamsByGroupName_UnknownEventId(/*Long eventId, String groupName*/) throws ServiceException {
		Long eventId = -1L;
		String groupName = "ABC";
		
		List<String> expectedGroupNames = Arrays.asList(groupName.split("")).stream().collect(Collectors.toList());

		expectedGroupNames.stream().forEach(e -> {
			try {
				Mockito.when(groupService.retrieveGroupByName(eventId, e)).thenReturn(null);
			} catch (ServiceException e1) {
				fail("Should not throw ServiceException: " + e);
			}
		});

		teamService.retrieveTeamsByGroupName(eventId, groupName);
	}

	/**
	 * Test {@link Team#retrieveTeamsByGroupName(Long, String)} method.
	 * Scenario: throws {@link IllegalArgumentException} because groupName parameter 
	 *           contains unknown group.
	 */
	@Test(expected=IllegalArgumentException.class)
	public void /*List<Team>*/ retrieveTeamsByGroupName_UnknownGroupName(/*Long eventId, String groupName*/) throws ServiceException {
		Long eventId = 1L;
		String groupName = "$";
		
		Mockito.when(groupService.retrieveGroupByName(eventId, groupName)).thenReturn(null);

		teamService.retrieveTeamsByGroupName(eventId, groupName);
	}
}
