package com.zematix.jworldcup.backend.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

import com.google.common.collect.Lists;
import com.zematix.jworldcup.backend.dao.CommonDao;
import com.zematix.jworldcup.backend.dao.GroupDao;
import com.zematix.jworldcup.backend.entity.Event;
import com.zematix.jworldcup.backend.entity.Group;
import com.zematix.jworldcup.backend.entity.Match;
import com.zematix.jworldcup.backend.entity.Team;
import com.zematix.jworldcup.backend.exception.ServiceException;
import com.zematix.jworldcup.backend.model.GroupPosition;
import com.zematix.jworldcup.backend.model.GroupTeam;

/**
 * Contains test functions of {@link GroupService} class.
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles({"development", "test"})
@Transactional
public class GroupServiceTest {

	@Inject
	private GroupService groupService;
	
	@Inject
	private CommonDao commonDao;
	
	@MockBean
	private GroupDao groupDao; // used heavily by methods of GroupService
	
//	@SuppressWarnings("unused")
//	@Inject
//	private MatchService matchService; // GroupTeam do use MatchService directly

	/**
	 * Test {@link GroupService#retrieveGroupsByEvent(Long)} method.
	 * Scenario: throws {@link NullPointerException} because of the given {@code null}
	 *           {@code eventId} parameter
	 */
	@Test(expected=NullPointerException.class)
	public void /*List<Group>*/ retrieveGroupsByEvent_Null(/*Long eventId*/) throws ServiceException {
		Long eventId = null;
		
		groupService.retrieveGroupsByEvent(eventId);
	}	

	/**
	 * Test {@link GroupService#retrieveGroupsByEvent(Long)} method.
	 * Scenario: successfully retrieves the result
	 */
	@Test
	public void /*List<Group>*/ retrieveGroupsByEvent(/*Long eventId*/) throws ServiceException {
		Long eventId = 1L;
		
		Group group1 = new Group();
		Group group2 = new Group();
		List<Group> group12 = Arrays.asList(group1, group2); // expected
		Mockito.when(groupDao.retrieveGroupsByEvent(eventId)).thenReturn(group12);

		List<Group> groups = groupService.retrieveGroupsByEvent(eventId);
		assertEquals("Result group list should be the same as the expected one.", group12, groups);
	}	

	/**
	 * Test {@link GroupService#getRankedGroupTeamsByGroup(Long)} method.
	 * Scenario: successfully retrieves the result
	 */
	@Test
	public void /*List<GroupTeam>*/ getRankedGroupTeamsByGroup(/*Long groupId*/) throws ServiceException {
		Long groupId = 1L;

		Comparator<Match> byMatchNr = (m1, m2) -> Short.compare(
				m1.getMatchN(), m2.getMatchN());
		List<Match> matches = commonDao.findAllEntities(Match.class).stream()
				.filter(m -> m.getRound().getIsGroupmatchAsBoolean() && m.getTeam1().getGroup().getGroupId().equals(groupId))
				.sorted(byMatchNr)
				.collect(Collectors.toList());
		Map<Long, List<Match>> playedMatchByTeamIdMap = new HashMap<>();
		
		// match1: team1 x team2
		// ------W-D-L-GF-GA-PT
		// team1-1-0-0-01-00-03
		// team3-0-0-0-00-00-00
		// team4-0-0-0-00-00-00
		// team2-0-0-1-00-01-00
		Match match = matches.get(0);
		Team team1 = match.getTeam1(); // teamId = 6
		playedMatchByTeamIdMap.put(team1.getTeamId(), Lists.newArrayList(match));
		Team team2 = match.getTeam2(); // teamId = 12
		playedMatchByTeamIdMap.put(team2.getTeamId(), Lists.newArrayList(match));
		match.setGoalNormalByTeam1((byte)1);
		match.setGoalNormalByTeam2((byte)0);
		commonDao.persistEntity(match);
		
		// match2: team3 x team4
		// ------W-D-L-GF-GA-SC
		// team1-1-0-0-01-00-03
		// team3-0-1-0-00-01-01
		// team4-0-1-0-00-01-01
		// team2-0-0-1-00-01-00
		match = matches.get(1);
		Team team3 = match.getTeam1(); // teamId = 24
		playedMatchByTeamIdMap.put(team3.getTeamId(), Lists.newArrayList(match));
		Team team4 = match.getTeam2(); // teamId = 7
		playedMatchByTeamIdMap.put(team4.getTeamId(), Lists.newArrayList(match));
		match.setGoalNormalByTeam1((byte)1);
		match.setGoalNormalByTeam2((byte)1);
		commonDao.persistEntity(match);
		
		// match3: team1 x team3
		// ------W-D-L-GF-GA-SC
		// team3-1-1-0-03-01-04
		// team1-1-0-1-01-03-03
		// team4-0-1-0-00-01-01
		// team2-0-0-1-00-01-00
		match = matches.get(2);
		match.setGoalNormalByTeam1((byte)0);
		match.setGoalNormalByTeam2((byte)3);
		playedMatchByTeamIdMap.get(team1.getTeamId()).add(match);
		playedMatchByTeamIdMap.get(team3.getTeamId()).add(match);
		commonDao.persistEntity(match);
		
		// match4: team4 x team2
		// ------W-D-L-GF-GA-SC
		// team3-1-1-0-03-01-04
		// team1-1-0-1-01-03-03
		// team4-0-2-0-02-03-02
		// team2-0-1-1-02-03-01
		match = matches.get(3);
		match.setGoalNormalByTeam1((byte)2);
		match.setGoalNormalByTeam2((byte)2);
		playedMatchByTeamIdMap.get(team4.getTeamId()).add(match);
		playedMatchByTeamIdMap.get(team2.getTeamId()).add(match);
		commonDao.persistEntity(match);
		
		// match5: team4 x team1
		// ------W-D-L-GF-GA-SC
		// team4-1-2-0-03-03-05
		// team3-1-1-0-03-01-04
		// team1-1-0-2-01-04-03
		// team2-0-1-1-02-03-01
		match = matches.get(4);
		match.setGoalNormalByTeam1((byte)1);
		match.setGoalNormalByTeam2((byte)0);
		playedMatchByTeamIdMap.get(team4.getTeamId()).add(match);
		playedMatchByTeamIdMap.get(team1.getTeamId()).add(match);
		commonDao.persistEntity(match);
		
		// match6: team2 x team3
		// ------W-D-L-GF-GA-SC
		// team3-1-2-0-05-03-05
		// team4-1-2-0-03-03-05
		// team1-1-0-2-01-04-03
		// team2-0-2-1-04-05-02
		match = matches.get(5);
		match.setGoalNormalByTeam1((byte)2);
		match.setGoalNormalByTeam2((byte)2);
		playedMatchByTeamIdMap.get(team2.getTeamId()).add(match);
		playedMatchByTeamIdMap.get(team3.getTeamId()).add(match);
		commonDao.persistEntity(match);
		
		List<GroupTeam> expectedGroupTeams = Arrays.asList(
				new GroupTeam(team3, playedMatchByTeamIdMap.get(team3.getTeamId())),
				new GroupTeam(team4, playedMatchByTeamIdMap.get(team4.getTeamId())),
				new GroupTeam(team1, playedMatchByTeamIdMap.get(team1.getTeamId())),
				new GroupTeam(team2, playedMatchByTeamIdMap.get(team2.getTeamId())));
		
		List<GroupTeam> groupTeams = groupService.getRankedGroupTeamsByGroup(groupId);
		
//		expectedGroupTeams.stream().forEach(gt -> System.out.println(gt.getTeam().getTeamId().toString()));
//		groupTeams.stream().forEach(gt -> System.out.println(gt.getTeam().getTeamId().toString()));
		assertEquals("Result groupTeams list should be equal to the expected one.",
				expectedGroupTeams.stream().map(g -> g.getTeam().getTeamId()).collect(Collectors.toList()),
				groupTeams.stream().map(g -> g.getTeam().getTeamId()).collect(Collectors.toList()));
	}	

	/**
	 * Test {@link GroupService#getRankedGroupTeamsByGroup(Long)} method.
	 * Scenario: successfully retrieves the result
	 */
	@Test
	public void /*List<GroupTeam>*/ getRankedGroupTeamsByGroupPartly(/*Long groupId*/) throws ServiceException {
		Long groupId = 1L;
		
		Comparator<Match> byMatchNr = (m1, m2) -> Short.compare(
				m1.getMatchN(), m2.getMatchN());
		List<Match> matches = commonDao.findAllEntities(Match.class).stream()
				.filter(m -> m.getRound().getIsGroupmatchAsBoolean() && m.getTeam1().getGroup().getGroupId().equals(groupId))
				.sorted(byMatchNr)
				.collect(Collectors.toList());
		Map<Long, List<Match>> playedMatchByTeamIdMap = new HashMap<>();
		
		// match1: team1 x team2
		// ------W-D-L-GF-GA-PT
		// team1-1-0-0-01-00-03
		// team3-0-0-0-00-00-00
		// team4-0-0-0-00-00-00
		// team2-0-0-1-00-01-00
		Match match = matches.get(0);
		Team team1 = match.getTeam1(); // teamId = 6
		playedMatchByTeamIdMap.put(team1.getTeamId(), Lists.newArrayList(match));
		Team team2 = match.getTeam2(); // teamId = 12
		playedMatchByTeamIdMap.put(team2.getTeamId(), Lists.newArrayList(match));
		match.setGoalNormalByTeam1((byte)1);
		match.setGoalNormalByTeam2((byte)0);
		commonDao.persistEntity(match);
		
		// match2: team3 x team4
		// ------W-D-L-GF-GA-SC
		// team1-1-0-0-01-00-03
		// team3-0-1-0-00-01-01
		// team4-0-1-0-00-01-01
		// team2-0-0-1-00-01-00
		match = matches.get(1);
		Team team3 = match.getTeam1(); // teamId = 24
		playedMatchByTeamIdMap.put(team3.getTeamId(), Lists.newArrayList(match));
		Team team4 = match.getTeam2(); // teamId = 7
		playedMatchByTeamIdMap.put(team4.getTeamId(), Lists.newArrayList(match));
		match.setGoalNormalByTeam1((byte)1);
		match.setGoalNormalByTeam2((byte)1);
		commonDao.persistEntity(match);
		
		// match3: team1 x team3
		// ------W-D-L-GF-GA-SC
		// team3-1-1-0-03-01-04
		// team1-1-0-1-01-03-03
		// team4-0-1-0-00-01-01
		// team2-0-0-1-00-01-00
		match = matches.get(2);
		match.setGoalNormalByTeam1((byte)0);
		match.setGoalNormalByTeam2((byte)3);
		playedMatchByTeamIdMap.get(team1.getTeamId()).add(match);
		playedMatchByTeamIdMap.get(team3.getTeamId()).add(match);
		commonDao.persistEntity(match);
		
		// match4: team4 x team2
		// ------W-D-L-GF-GA-SC
		// team3-1-1-0-03-01-04
		// team1-1-0-1-01-03-03
		// team4-0-2-0-02-03-02
		// team2-0-1-1-02-03-01
		match = matches.get(3);
		match.setGoalNormalByTeam1((byte)2);
		match.setGoalNormalByTeam2((byte)2);
		playedMatchByTeamIdMap.get(team4.getTeamId()).add(match);
		playedMatchByTeamIdMap.get(team2.getTeamId()).add(match);
		commonDao.persistEntity(match);
		
		List<GroupTeam> expectedGroupTeams = Arrays.asList(
				new GroupTeam(team3, playedMatchByTeamIdMap.get(team3.getTeamId())),
				new GroupTeam(team1, playedMatchByTeamIdMap.get(team1.getTeamId())),
				new GroupTeam(team4, playedMatchByTeamIdMap.get(team4.getTeamId())),
				new GroupTeam(team2, playedMatchByTeamIdMap.get(team2.getTeamId())));
		
		List<GroupTeam> groupTeams = groupService.getRankedGroupTeamsByGroup(groupId);
		
		assertEquals("Result groupTeams list should be equal to the expected one.",
				expectedGroupTeams.stream().map(g -> g.getTeam().getTeamId()).collect(Collectors.toList()),
				groupTeams.stream().map(g -> g.getTeam().getTeamId()).collect(Collectors.toList()));
				
	}	

	/**
	 * Test {@link GroupService#getTeamByGroupPositionMap(Long)} method.
	 * Scenario: successfully retrieves the result
	 */
	@Test
	public void /*Map<GroupPosition, Team>*/ getTeamByGroupPositionMap(/*Long eventId*/) throws ServiceException {
		Long eventId = 1L; // WC2014

		// retrieve groups belong to the given eventId
		List<Group> groups = commonDao.findAllEntities(Group.class).stream()
				.filter(g -> g.getEvent().getEventId().equals(eventId))
				.collect(Collectors.toList());
		
		Group group = groups.get(0);
		Comparator<Match> byMatchNr = (m1, m2) -> Short.compare(
				m1.getMatchN(), m2.getMatchN());
		List<Match> matches = commonDao.findAllEntities(Match.class).stream()
				.filter(m -> m.getRound().getIsGroupmatchAsBoolean() && m.getTeam1().getGroup().getGroupId().equals(group.getGroupId()))
				.sorted(byMatchNr)
				.collect(Collectors.toList());
		
		// match1: team1 x team2
		// ------W-D-L-GF-GA-PT
		// team1-1-0-0-01-00-03
		// team3-0-0-0-00-00-00
		// team4-0-0-0-00-00-00
		// team2-0-0-1-00-01-00
		Match match = matches.get(0);
//		Team team1 = match.getTeam1(); // teamId = 6
//		Team team2 = match.getTeam2(); // teamId = 12
		match.setGoalNormalByTeam1((byte)1);
		match.setGoalNormalByTeam2((byte)0);
		commonDao.persistEntity(match);
		
		// match2: team3 x team4
		// ------W-D-L-GF-GA-SC
		// team1-1-0-0-01-00-03
		// team3-0-1-0-00-01-01
		// team4-0-1-0-00-01-01
		// team2-0-0-1-00-01-00
		match = matches.get(1);
		Team team3 = match.getTeam1(); // teamId = 24
		Team team4 = match.getTeam2(); // teamId = 7
		match.setGoalNormalByTeam1((byte)1);
		match.setGoalNormalByTeam2((byte)1);
		commonDao.persistEntity(match);
		
		// match3: team1 x team3
		// ------W-D-L-GF-GA-SC
		// team3-1-1-0-03-01-04
		// team1-1-0-1-01-03-03
		// team4-0-1-0-00-01-01
		// team2-0-0-1-00-01-00
		match = matches.get(2);
		match.setGoalNormalByTeam1((byte)0);
		match.setGoalNormalByTeam2((byte)3);
		commonDao.persistEntity(match);
		
		// match4: team4 x team2
		// ------W-D-L-GF-GA-SC
		// team3-1-1-0-03-01-04
		// team1-1-0-1-01-03-03
		// team4-0-2-0-02-03-02
		// team2-0-1-1-02-03-01
		match = matches.get(3);
		match.setGoalNormalByTeam1((byte)2);
		match.setGoalNormalByTeam2((byte)2);
		commonDao.persistEntity(match);
		
		// match5: team4 x team1
		// ------W-D-L-GF-GA-SC
		// team4-1-2-0-03-03-05
		// team3-1-1-0-03-01-04
		// team1-1-0-2-01-04-03
		// team2-0-1-1-02-03-01
		match = matches.get(4);
		match.setGoalNormalByTeam1((byte)1);
		match.setGoalNormalByTeam2((byte)0);
		commonDao.persistEntity(match);
		
		// match6: team2 x team3
		// ------W-D-L-GF-GA-SC
		// team3-1-2-0-05-03-05
		// team4-1-2-0-03-03-05
		// team1-1-0-2-01-04-03
		// team2-0-2-1-04-05-02
		match = matches.get(5);
		match.setGoalNormalByTeam1((byte)2);
		match.setGoalNormalByTeam2((byte)2);
		commonDao.persistEntity(match);

		List<Group> group1 = Arrays.asList(group);
		Mockito.when(groupDao.retrieveGroupsByEvent(eventId)).thenReturn(group1);
		
		Map<GroupPosition, Team> expectedTeamByGroupPositionMap = new HashMap<>();
		expectedTeamByGroupPositionMap.put(new GroupPosition(group.getName(), 1), team3);
		expectedTeamByGroupPositionMap.put(new GroupPosition(group.getName(), 2), team4);
//		expectedTeamByGroupPositionMap.keySet().stream().forEach(m -> System.out.println(String.format("{(%s,%s),%s}", m.getGroupName(), m.getPosition().toString(), expectedTeamByGroupPositionMap.get(m).getTeamId())));
		
		Map<GroupPosition, Team> teamByGroupPositionMap = groupService.getTeamByGroupPositionMap(eventId);
//		teamByGroupPositionMap.keySet().stream().forEach(m -> System.out.println(String.format("{(%s,%s),%s}", m.getGroupName(), m.getPosition().toString(), teamByGroupPositionMap.get(m).getTeamId())));
		
		assertEquals("Result map should be equal to the expected one's", expectedTeamByGroupPositionMap, teamByGroupPositionMap);
	}

	/**
	 * Test {@link GroupService#retrieveBestTeamsOnGroupPosition(Long, int, int)} method.
	 * This is a private method and it can be called from just some supported event/tournament.
	 * Scenario: successfully retrieves the result
	 */
	@Test
	public void /*List<GroupTeam>*/ retrieveBestTeamsOnGroupPosition(/*Long eventId, int positionInGroup, int numberOfTeams*/) throws /*Service*/Exception {
		Long eventId = 2L; // EC2016
		int positionInGroup = 1;
		int numberOfTeams = 4;
		
		// retrieve groups belong to the given eventId
		Comparator<Group> byGroupName = (g1, g2) -> g1.getName().compareTo(g2.getName());
		List<Group> groups = commonDao.findAllEntities(Group.class).stream()
				.filter(g -> g.getEvent().getEventId().equals(eventId))
				.sorted(byGroupName)
				.collect(Collectors.toList());
		
		Group groupA = groups.get(0);
		Comparator<Match> byMatchNr = (m1, m2) -> Short.compare(
				m1.getMatchN(), m2.getMatchN());
		List<Match> matches = commonDao.findAllEntities(Match.class).stream()
				.filter(m -> m.getRound().getIsGroupmatchAsBoolean() 
						&& m.getTeam1().getGroup().getGroupId().equals(groupA.getGroupId()))
				.sorted(byMatchNr)
				.collect(Collectors.toList());

		Map<Long, List<Match>> playedMatchByTeamIdMap = new HashMap<>();
		
		// match1: teamA1 x teamA2
		// -------W-D-L-GF-GA-PT
		// teamA1-1-0-0-01-00-03
		// teamA3-0-0-0-00-00-00
		// teamA4-0-0-0-00-00-00
		// teamA2-0-0-1-00-01-00
		Match match = matches.get(0);
		Team teamA1 = match.getTeam1(); // teamId = 33
//		System.out.println("teamA1: " + teamA1.getTeamId());
		playedMatchByTeamIdMap.put(teamA1.getTeamId(), Lists.newArrayList(match));
		Team teamA2 = match.getTeam2(); // teamId = 34
//		System.out.println("teamA2: " + teamA2.getTeamId());
		playedMatchByTeamIdMap.put(teamA2.getTeamId(), Lists.newArrayList(match));
		match.setGoalNormalByTeam1((byte)1);
		match.setGoalNormalByTeam2((byte)0);
		commonDao.persistEntity(match);
		
		// match2: teamA3 x teamA4
		// -------W-D-L-GF-GA-SC
		// teamA1-1-0-0-01-00-03
		// teamA3-0-1-0-01-01-01
		// teamA4-0-1-0-01-01-01
		// teamA2-0-0-1-00-01-00
		match = matches.get(1);
		Team teamA3 = match.getTeam1(); // teamId = 35
//		System.out.println("teamA3: " + teamA3.getTeamId());
		playedMatchByTeamIdMap.put(teamA3.getTeamId(), Lists.newArrayList(match));
		Team teamA4 = match.getTeam2(); // teamId = 36
//		System.out.println("teamA4: " + teamA4.getTeamId());
		playedMatchByTeamIdMap.put(teamA4.getTeamId(), Lists.newArrayList(match));
		match.setGoalNormalByTeam1((byte)1);
		match.setGoalNormalByTeam2((byte)1);
		commonDao.persistEntity(match);
		
		// match3: teamA2 x teamA4
		// -------W-D-L-GF-GA-SC
		// teamA4-1-1-0-04-01-04
		// teamA1-1-0-0-01-00-03
		// teamA3-0-1-0-01-01-01
		// teamA2-0-0-2-00-04-00
		match = matches.get(2);
		match.setGoalNormalByTeam1((byte)0);
		match.setGoalNormalByTeam2((byte)3);
		playedMatchByTeamIdMap.get(teamA1.getTeamId()).add(match);
		playedMatchByTeamIdMap.get(teamA3.getTeamId()).add(match);
		commonDao.persistEntity(match);
		
		// match4: teamA1 x teamA3
		// -------W-D-L-GF-GA-SC
		// teamA4-1-1-0-04-01-04
		// teamA1-1-1-0-03-02-04
		// teamA3-0-2-0-03-03-02
		// teamA2-0-0-2-00-04-00
		match = matches.get(3);
		match.setGoalNormalByTeam1((byte)2);
		match.setGoalNormalByTeam2((byte)2);
		playedMatchByTeamIdMap.get(teamA4.getTeamId()).add(match);
		playedMatchByTeamIdMap.get(teamA2.getTeamId()).add(match);
		commonDao.persistEntity(match);
		
		// match5: teamA2 x teamA3
		// -------W-D-L-GF-GA-SC
		// teamA4-1-1-0-04-01-04
		// teamA1-1-1-0-03-02-04
		// teamA2-1-0-2-01-04-03
		// teamA3-0-2-1-03-04-02
		match = matches.get(4);
		match.setGoalNormalByTeam1((byte)1);
		match.setGoalNormalByTeam2((byte)0);
		playedMatchByTeamIdMap.get(teamA4.getTeamId()).add(match);
		playedMatchByTeamIdMap.get(teamA1.getTeamId()).add(match);
		commonDao.persistEntity(match);
		
		// match6: teamA4 x teamA1
		// -------W-D-L-GF-GA-SC
		// teamA4-1-2-0-06-03-05
		// teamA1-1-2-0-05-04-05
		// teamA2-1-0-2-01-04-03
		// teamA3-0-2-1-03-04-02
		match = matches.get(5);
		match.setGoalNormalByTeam1((byte)2);
		match.setGoalNormalByTeam2((byte)2);
		playedMatchByTeamIdMap.get(teamA2.getTeamId()).add(match);
		playedMatchByTeamIdMap.get(teamA3.getTeamId()).add(match);
		commonDao.persistEntity(match);

		Group groupB = groups.get(1);
		/*List<Match>*/ matches = commonDao.findAllEntities(Match.class).stream()
				.filter(m -> m.getRound().getIsGroupmatchAsBoolean() 
						&& m.getTeam1().getGroup().getGroupId().equals(groupB.getGroupId()))
				.sorted(byMatchNr)
				.collect(Collectors.toList());
		
		// match1: teamB1 x teamB2
		// -------W-D-L-GF-GA-PT
		// teamB1-0-1-0-02-02-01
		// teamB2-0-1-0-02-02-01
		// teamB3-0-0-0-00-00-00
		// teamB4-0-0-0-00-00-00
		/*Match*/ match = matches.get(0);
		Team teamB1 = match.getTeam1(); // teamId = 39
//		System.out.println("teamB1: " + teamB1.getTeamId());
		playedMatchByTeamIdMap.put(teamB1.getTeamId(), Lists.newArrayList(match));
		Team teamB2 = match.getTeam2(); // teamId = 40
//		System.out.println("teamB2: " + teamB2.getTeamId());
		playedMatchByTeamIdMap.put(teamB2.getTeamId(), Lists.newArrayList(match));
		match.setGoalNormalByTeam1((byte)2);
		match.setGoalNormalByTeam2((byte)2);
		commonDao.persistEntity(match);
		
		// match2: teamB3 x teamB4
		// -------W-D-L-GF-GA-SC
		// teamB3-1-0-0-02-01-03
		// teamB1-0-1-0-02-02-01
		// teamB2-0-1-0-02-02-01
		// teamB4-0-0-1-01-02-00
		match = matches.get(1);
		Team teamB3 = match.getTeam1(); // teamId = 37
//		System.out.println("teamB3: " + teamB3.getTeamId());
		playedMatchByTeamIdMap.put(teamB3.getTeamId(), Lists.newArrayList(match));
		Team teamB4 = match.getTeam2(); // teamId = 38
//		System.out.println("teamB4: " + teamB4.getTeamId());
		playedMatchByTeamIdMap.put(teamB4.getTeamId(), Lists.newArrayList(match));
		match.setGoalNormalByTeam1((byte)2);
		match.setGoalNormalByTeam2((byte)1);
		commonDao.persistEntity(match);
		
		// match3: teamB4 x teamB2
		// -------W-D-L-GF-GA-SC
		// teamB4-1-0-1-03-02-03
		// teamB3-1-0-0-02-01-03
		// teamB1-0-1-0-02-02-01
		// teamB2-0-1-1-02-04-01
		match = matches.get(2);
		match.setGoalNormalByTeam1((byte)2);
		match.setGoalNormalByTeam2((byte)0);
		playedMatchByTeamIdMap.get(teamB4.getTeamId()).add(match);
		playedMatchByTeamIdMap.get(teamB2.getTeamId()).add(match);
		commonDao.persistEntity(match);
		
		// match4: teamB3 x teamB1
		// -------W-D-L-GF-GA-SC
		// teamB3-1-1-0-02-01-04
		// teamB4-1-0-1-03-02-03
		// teamB1-0-2-0-02-02-02
		// teamB2-0-1-1-02-04-01
		match = matches.get(3);
		match.setGoalNormalByTeam1((byte)0);
		match.setGoalNormalByTeam2((byte)0);
		playedMatchByTeamIdMap.get(teamB3.getTeamId()).add(match);
		playedMatchByTeamIdMap.get(teamB1.getTeamId()).add(match);
		commonDao.persistEntity(match);
		
		// match5: teamB4 x teamB1
		// -------W-D-L-GF-GA-SC
		// teamB3-1-1-0-02-01-04
		// teamB4-1-1-1-04-03-04
		// teamB1-0-3-0-03-03-03
		// teamB2-0-1-1-02-04-01
		match = matches.get(4);
		match.setGoalNormalByTeam1((byte)1);
		match.setGoalNormalByTeam2((byte)1);
		playedMatchByTeamIdMap.get(teamB4.getTeamId()).add(match);
		playedMatchByTeamIdMap.get(teamB1.getTeamId()).add(match);
		commonDao.persistEntity(match);
		
		// match6: teamB2 x teamB3
		// -------W-D-L-GF-GA-SC
		// teamB4-1-1-1-04-03-04
		// teamB3-1-1-1-02-02-04
		// teamB2-1-1-1-03-04-04
		// teamB1-0-3-0-03-03-03
		match = matches.get(5);
		match.setGoalNormalByTeam1((byte)1);
		match.setGoalNormalByTeam2((byte)0);
		playedMatchByTeamIdMap.get(teamB2.getTeamId()).add(match);
		playedMatchByTeamIdMap.get(teamB3.getTeamId()).add(match);
		commonDao.persistEntity(match);

		List<Group> groups1 = Arrays.asList(groupA, groupB);
		Mockito.when(groupDao.retrieveGroupsByEvent(eventId)).thenReturn(groups1);

		List<GroupTeam> expectedGroupTeams = Arrays.asList(
				new GroupTeam(teamA4, playedMatchByTeamIdMap.get(teamA4.getTeamId())),
				new GroupTeam(teamB4, playedMatchByTeamIdMap.get(teamB4.getTeamId())));
//		expectedGroupTeams.stream().forEach(m -> System.out.println(m.getTeam().getTeamId()));

		List<GroupTeam> groupTeams = groupService.retrieveBestTeamsOnGroupPosition(eventId, positionInGroup, numberOfTeams);
//		groupTeams.stream().forEach(m -> System.out.println(m.getTeam().getTeamId()));
		
		assertEquals("Result groupTeams list should be equal to the expected one.",
				expectedGroupTeams.stream().map(g -> g.getTeam()).collect(Collectors.toList()),
				groupTeams.stream().map(g -> g.getTeam()).collect(Collectors.toList()));
	}	

	/**
	 * Test {@link GroupService#retrieveGroupPositionsOfParticipantRules(Long)} method.
	 * This is a private method.
	 * Scenario: successfully retrieves the result
	 */
	@Test
	public void /*List<GroupPosition>*/ retrieveGroupPositionsOfParticipantRules(/*Long eventId*/) throws /*Service*/Exception {
		Long eventId = 2L; // EC2016

		List<GroupPosition> expectedGroupPositions = Arrays.asList(
				new GroupPosition("A", 2),
				new GroupPosition("C", 2),
				new GroupPosition("B", 1),
				new GroupPosition("ACD", 3),
				new GroupPosition("D", 1),
				new GroupPosition("BEF", 3),
				new GroupPosition("A", 1),
				new GroupPosition("CDE", 3),
				new GroupPosition("C", 1),
				new GroupPosition("ABF", 3),
				new GroupPosition("F", 1),
				new GroupPosition("E", 2),
				new GroupPosition("E", 1),
				new GroupPosition("D", 2),
				new GroupPosition("B", 2),
				new GroupPosition("F", 2));
		//expectedGroupPositions.stream().forEach(gp -> System.out.println(gp));

		List<GroupPosition> groupPositions = groupService.retrieveGroupPositionsOfParticipantRules(eventId);
		//groupPositions.stream().forEach(gp -> System.out.println(gp));

		assertEquals("Result groupPositions list should be equal to the expected one.",
				expectedGroupPositions,
				groupPositions);
	}
	
	/**
	 * Test {@link GroupService#retrieveGroupByName(Long, String)} method.
	 * Scenario: successfully retrieves a group by the given parameters
	 */
	@Test
	public void /*Group*/ retrieveGroupByName(/*Long eventId, String name*/) throws ServiceException {
		Long eventId = 1L; // WC2014
		String name = "A";
		Long expectedGroupId = 1L;
		Group expectedGroup = commonDao.findEntityById(Group.class, expectedGroupId);
		Mockito.when(groupDao.retrieveGroupByName(eventId, name)).thenReturn(expectedGroup);

		Group group = groupService.retrieveGroupByName(eventId, name);
		
		assertEquals("Retrieved Group entity must be equal to the expected one.", 
				expectedGroup.getGroupId(), group.getGroupId());
	}

	/**
	 * Test {@link GroupService#retrieveGroupByName(Long, String)} method.
	 * Scenario: throws {@link NullPointerException} because given {@link Event#eventId} is {@code null}
	 */
	@Test(expected=NullPointerException.class)
	public void /*Group*/ retrieveGroupByNameNullEventId(/*Long eventId, String name*/) throws ServiceException {
		Long eventId = null;
		String name = "A";

		groupService.retrieveGroupByName(eventId, name);
	}

	/**
	 * Test {@link GroupService#retrieveGroupByName(Long, String)} method.
	 * Scenario: throws {@link IllegalArgumentException} because given {@link Group#name} is {@code null}
	 */
	@Test(expected=IllegalArgumentException.class)
	public void /*Group*/ retrieveGroupByNameNullName(/*Long eventId, String name*/) throws ServiceException {
		Long eventId = 1L;
		String name = null;

		groupService.retrieveGroupByName(eventId, name);
	}

	/**
	 * Test {@link GroupService#retrieveGroupByName(Long, String)} method.
	 * Scenario: throws {@link IllegalArgumentException} because {@link Group#name} is empty
	 */
	@Test(expected=IllegalArgumentException.class)
	public void /*Group*/ retrieveGroupByNameEmptyName(/*Long eventId, String name*/) throws ServiceException {
		Long eventId = 1L;
		String name = "";

		groupService.retrieveGroupByName(eventId, name);
	}

	/**
	 * Test {@link GroupService#retrieveGroupByName(Long, String)} method.
	 * Scenario: throws {@link IllegalArgumentException} because {@link Group#name} is empty
	 */
	@Test
	public void /*Group*/ retrieveGroupByNameUnknown(/*Long eventId, String name*/) throws ServiceException {
		Long eventId = -1L;
		String name = "@";
		Mockito.when(groupDao.retrieveGroupByName(eventId, name)).thenReturn(null);

		Group group = groupService.retrieveGroupByName(eventId, name);

		assertNull("Retrieved Group entity must be null.", group);
	}
}

