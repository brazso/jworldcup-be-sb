package com.zematix.jworldcup.backend.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.reflect.internal.WhiteboxImpl;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.collect.Lists;
import com.zematix.jworldcup.backend.dao.CommonDao;
import com.zematix.jworldcup.backend.entity.Event;
import com.zematix.jworldcup.backend.entity.Group;
import com.zematix.jworldcup.backend.entity.Match;
import com.zematix.jworldcup.backend.entity.Team;
import com.zematix.jworldcup.backend.model.GroupPosition;
import com.zematix.jworldcup.backend.model.GroupTeam;
import com.zematix.jworldcup.backend.model.Pair;

/**
 * Contains test functions of {@link GroupTeamService} class.
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles({"development", "test"})
@Transactional 
public class GroupTeamServiceTest {

	@Inject
	private GroupTeamService groupTeamService;

	@Inject
	private CommonDao commonDao;

	/**
	 * Test {@link GroupTestService#sortGroupTeams(List<GroupTeam>, int, int)} method.
	 * This is a private method.
	 * Scenario: successfully retrieves the result
	 * 
	 * @throws Exception - because of reflective method call the exception can be anything 
	 */
	@Test
	public void /*boolean*/ sortGroupTeams(/*List<GroupTeam> groupTeams, int level, int globalPositionInGroup*/) throws Exception {
		Group group = new Group();
		group.setName("G");
		Team teamA = new Team();
		teamA.setGroup(group);
		teamA.setName("A");
		teamA.setTeamId(1L);
		Event event = new Event();
		event.setShortDesc("EC");
		event.setYear((short)2016);
		event.setOrganizer("UEFA");
		teamA.setEvent(event);
		Team teamB = new Team();
		teamB.setName("B");
		teamB.setGroup(group);
		teamB.setTeamId(2L);
		Team teamC = new Team();
		teamC.setName("C");
		teamC.setGroup(group);
		teamC.setTeamId(3L);
		Team teamD = new Team();
		teamD.setName("D");
		teamD.setGroup(group);
		teamD.setTeamId(4L);
		group.setTeams(Arrays.asList(teamA, teamB, teamC, teamD));
		
		// result:
		// ------W-D-L-GF-GA-SC
		// teamA-2-1-0-07-03-07
		// teamC-1-2-0-04-03-05
		// teamB-1-1-1-03-02-04
		// teamD-0-0-3-01-07-00

		Match matchA1 = new Match();
		matchA1.setTeam1(teamA);
		matchA1.setTeam2(teamB);
		matchA1.setGoalNormalByTeam1((byte)1);
		matchA1.setGoalNormalByTeam2((byte)0);
		Match matchA2 = new Match();
		matchA2.setTeam1(teamC);
		matchA2.setTeam2(teamA);
		matchA2.setGoalNormalByTeam1((byte)2);
		matchA2.setGoalNormalByTeam2((byte)2);
		Match matchA3 = new Match();
		matchA3.setTeam1(teamA);
		matchA3.setTeam2(teamD);
		matchA3.setGoalNormalByTeam1((byte)4);
		matchA3.setGoalNormalByTeam2((byte)1);
		
		Match matchB1 = new Match();
		matchB1.setTeam1(teamA);
		matchB1.setTeam2(teamB);
		matchB1.setGoalNormalByTeam1((byte)1);
		matchB1.setGoalNormalByTeam2((byte)0);
		Match matchB2 = new Match();
		matchB2.setTeam1(teamB);
		matchB2.setTeam2(teamC);
		matchB2.setGoalNormalByTeam1((byte)1);
		matchB2.setGoalNormalByTeam2((byte)1);
		Match matchB3 = new Match();
		matchB3.setTeam1(teamD);
		matchB3.setTeam2(teamB);
		matchB3.setGoalNormalByTeam1((byte)0);
		matchB3.setGoalNormalByTeam2((byte)2);
		
		Match matchC1 = new Match();
		matchC1.setTeam1(teamC);
		matchC1.setTeam2(teamD);
		matchC1.setGoalNormalByTeam1((byte)1);
		matchC1.setGoalNormalByTeam2((byte)0);
		Match matchC2 = new Match();
		matchC2.setTeam1(teamA);
		matchC2.setTeam2(teamC);
		matchC2.setGoalNormalByTeam1((byte)2);
		matchC2.setGoalNormalByTeam2((byte)2);
		Match matchC3 = new Match();
		matchC3.setTeam1(teamC);
		matchC3.setTeam2(teamB);
		matchC3.setGoalNormalByTeam1((byte)1);
		matchC3.setGoalNormalByTeam2((byte)1);
		
		Match matchD1 = new Match();
		matchD1.setTeam1(teamC);
		matchD1.setTeam2(teamD);
		matchD1.setGoalNormalByTeam1((byte)1);
		matchD1.setGoalNormalByTeam2((byte)0);
		Match matchD2 = new Match();
		matchD2.setTeam1(teamD);
		matchD2.setTeam2(teamA);
		matchD2.setGoalNormalByTeam1((byte)1);
		matchD2.setGoalNormalByTeam2((byte)4);
		Match matchD3 = new Match();
		matchD3.setTeam1(teamB);
		matchD3.setTeam2(teamD);
		matchD3.setGoalNormalByTeam1((byte)2);
		matchD3.setGoalNormalByTeam2((byte)0);
		
		List<Match> matchAList = Arrays.asList(
				matchA1, matchA2, matchA3
				);

		List<Match> matchBList = Arrays.asList(
				matchB1, matchB2, matchB3
				);

		List<Match> matchCList = Arrays.asList(
				matchC1, matchC2, matchC3
				);

		List<Match> matchDList = Arrays.asList(
				matchD1, matchD2, matchD3
				);

		List<GroupTeam> groupTeams = Arrays.asList(
				new GroupTeam(teamA, matchAList),
				new GroupTeam(teamB, matchBList),
				new GroupTeam(teamC, matchCList),
				new GroupTeam(teamD, matchDList)
				);
		int level = 0;
		int globalPositionInGroup = 1;
		
		List<Team> expectedTeams = Arrays.asList(teamA, teamC, teamB, teamD);
		
		boolean isEquality = WhiteboxImpl.invokeMethod(groupTeamService, "sortGroupTeams", groupTeams, level, globalPositionInGroup);
		
		assertFalse("There should not be equality in the result.", isEquality);
		assertEquals("Result groupTeams list should be equal to the expected one.",
				expectedTeams.stream().map(t -> t.getTeamId()).toList(),
				groupTeams.stream().map(g -> g.getTeam().getTeamId()).toList());
	}

	/**
	 * Test {@link GroupTestService#sortGroupTeams(List<GroupTeam>, int, int)} method.
	 * This is a private method.
	 * Scenario: throws IllegalArgumentException because the input is invalid
	 * 
	 * @throws Exception - because of reflective method call the exception can be anything 
	 */
	@Test(expected=IllegalArgumentException.class)
	public void /*boolean*/ sortGroupTeamsNull(/*List<GroupTeam> groupTeams, int level, int globalPositionInGroup*/) throws Exception {
		List<GroupTeam> groupTeams = null;
		int level = 0;
		int globalPositionInGroup = 1;
		
		/*boolean isEquality =*/ WhiteboxImpl.invokeMethod(groupTeamService, "sortGroupTeams", groupTeams, level, globalPositionInGroup);
	}

	/**
	 * Test {@link GroupTeamService#isGroupFinished(List<GroupTeam>)} method.
	 * Scenario: successfully retrieves the result
	 */
	@Test
	public void /*boolean*/ isGroupFinished(/*List<GroupTeam> groupTeams*/) {
		Group group = new Group();
		group.setName("G");
		Team teamA = new Team();
		teamA.setGroup(group);
		teamA.setName("A");
		teamA.setTeamId(1L);
		Event event = new Event();
		event.setShortDesc("EC");
		event.setYear((short)2016);
		event.setOrganizer("UEFA");
		teamA.setEvent(event);
		Team teamB = new Team();
		teamB.setName("B");
		teamB.setGroup(group);
		teamB.setTeamId(2L);
		Team teamC = new Team();
		teamC.setName("C");
		teamC.setGroup(group);
		teamC.setTeamId(3L);
		Team teamD = new Team();
		teamD.setName("D");
		teamD.setGroup(group);
		teamD.setTeamId(4L);
		group.setTeams(Arrays.asList(teamA, teamB, teamC, teamD));
		
		// result:
		// ------W-D-L-GF-GA-SC
		// teamA-2-1-0-07-03-07
		// teamC-1-2-0-04-03-05
		// teamB-1-1-1-03-02-04
		// teamD-0-0-3-01-07-00

		Match matchA1 = new Match();
		matchA1.setTeam1(teamA);
		matchA1.setTeam2(teamB);
		matchA1.setGoalNormalByTeam1((byte)1);
		matchA1.setGoalNormalByTeam2((byte)0);
		Match matchA2 = new Match();
		matchA2.setTeam1(teamC);
		matchA2.setTeam2(teamA);
		matchA2.setGoalNormalByTeam1((byte)2);
		matchA2.setGoalNormalByTeam2((byte)2);
		Match matchA3 = new Match();
		matchA3.setTeam1(teamA);
		matchA3.setTeam2(teamD);
		matchA3.setGoalNormalByTeam1((byte)4);
		matchA3.setGoalNormalByTeam2((byte)1);
		
		Match matchB1 = new Match();
		matchB1.setTeam1(teamA);
		matchB1.setTeam2(teamB);
		matchB1.setGoalNormalByTeam1((byte)1);
		matchB1.setGoalNormalByTeam2((byte)0);
		Match matchB2 = new Match();
		matchB2.setTeam1(teamB);
		matchB2.setTeam2(teamC);
		matchB2.setGoalNormalByTeam1((byte)1);
		matchB2.setGoalNormalByTeam2((byte)1);
		Match matchB3 = new Match();
		matchB3.setTeam1(teamD);
		matchB3.setTeam2(teamB);
		matchB3.setGoalNormalByTeam1((byte)0);
		matchB3.setGoalNormalByTeam2((byte)2);
		
		Match matchC1 = new Match();
		matchC1.setTeam1(teamC);
		matchC1.setTeam2(teamD);
		matchC1.setGoalNormalByTeam1((byte)1);
		matchC1.setGoalNormalByTeam2((byte)0);
		Match matchC2 = new Match();
		matchC2.setTeam1(teamA);
		matchC2.setTeam2(teamC);
		matchC2.setGoalNormalByTeam1((byte)2);
		matchC2.setGoalNormalByTeam2((byte)2);
		Match matchC3 = new Match();
		matchC3.setTeam1(teamC);
		matchC3.setTeam2(teamB);
		matchC3.setGoalNormalByTeam1((byte)1);
		matchC3.setGoalNormalByTeam2((byte)1);
		
		Match matchD1 = new Match();
		matchD1.setTeam1(teamC);
		matchD1.setTeam2(teamD);
		matchD1.setGoalNormalByTeam1((byte)1);
		matchD1.setGoalNormalByTeam2((byte)0);
		Match matchD2 = new Match();
		matchD2.setTeam1(teamD);
		matchD2.setTeam2(teamA);
		matchD2.setGoalNormalByTeam1((byte)1);
		matchD2.setGoalNormalByTeam2((byte)4);
		Match matchD3 = new Match();
		matchD3.setTeam1(teamB);
		matchD3.setTeam2(teamD);
		matchD3.setGoalNormalByTeam1((byte)2);
		matchD3.setGoalNormalByTeam2((byte)0);
		
		List<Match> matchAList = Arrays.asList(
				matchA1, matchA2, matchA3
				);

		List<Match> matchBList = Arrays.asList(
				matchB1, matchB2, matchB3
				);

		List<Match> matchCList = Arrays.asList(
				matchC1, matchC2, matchC3
				);

		List<Match> matchDList = Arrays.asList(
				matchD1, matchD2, matchD3
				);

		List<GroupTeam> groupTeams = Arrays.asList(
				new GroupTeam(teamA, matchAList),
				new GroupTeam(teamB, matchBList),
				new GroupTeam(teamC, matchCList),
				new GroupTeam(teamD, matchDList)
				);
		
		assertTrue("Group should be finished", groupTeamService.isGroupFinished(groupTeams));
	}

	/**
	 * Test {@link GroupTeamService#isGroupFinished(List<GroupTeam>)} method.
	 * Scenario: successfully retrieves the result
	 */
	@Test
	public void /*boolean*/ isGroupFinishedNot(/*List<GroupTeam> groupTeams*/) {
		Group group = new Group();
		group.setName("G");
		Team teamA = new Team();
		teamA.setGroup(group);
		teamA.setName("A");
		teamA.setTeamId(1L);
		Event event = new Event();
		event.setShortDesc("EC");
		event.setYear((short)2016);
		event.setOrganizer("UEFA");
		teamA.setEvent(event);
		Team teamB = new Team();
		teamB.setName("B");
		teamB.setGroup(group);
		teamB.setTeamId(2L);
		Team teamC = new Team();
		teamC.setName("C");
		teamC.setGroup(group);
		teamC.setTeamId(3L);
		Team teamD = new Team();
		teamD.setName("D");
		teamD.setGroup(group);
		teamD.setTeamId(4L);
		group.setTeams(Arrays.asList(teamA, teamB, teamC, teamD));
		
		// result:
		// ------W-D-L-GF-GA-SC
		// teamA-2-1-0-07-03-07
		// teamC-1-2-0-04-03-05
		// teamB-0-1-1-01-02-01
		// teamD-0-0-2-01-05-00

		Match matchA1 = new Match();
		matchA1.setTeam1(teamA);
		matchA1.setTeam2(teamB);
		matchA1.setGoalNormalByTeam1((byte)1);
		matchA1.setGoalNormalByTeam2((byte)0);
		Match matchA2 = new Match();
		matchA2.setTeam1(teamC);
		matchA2.setTeam2(teamA);
		matchA2.setGoalNormalByTeam1((byte)2);
		matchA2.setGoalNormalByTeam2((byte)2);
		Match matchA3 = new Match();
		matchA3.setTeam1(teamA);
		matchA3.setTeam2(teamD);
		matchA3.setGoalNormalByTeam1((byte)4);
		matchA3.setGoalNormalByTeam2((byte)1);
		
		Match matchB1 = new Match();
		matchB1.setTeam1(teamA);
		matchB1.setTeam2(teamB);
		matchB1.setGoalNormalByTeam1((byte)1);
		matchB1.setGoalNormalByTeam2((byte)0);
		Match matchB2 = new Match();
		matchB2.setTeam1(teamB);
		matchB2.setTeam2(teamC);
		matchB2.setGoalNormalByTeam1((byte)1);
		matchB2.setGoalNormalByTeam2((byte)1);
//		Match matchB3 = new Match();
//		matchB3.setTeam1(teamD);
//		matchB3.setTeam2(teamB);
//		matchB3.setGoalNormalByTeam1((byte)0);
//		matchB3.setGoalNormalByTeam2((byte)2);
		
		Match matchC1 = new Match();
		matchC1.setTeam1(teamC);
		matchC1.setTeam2(teamD);
		matchC1.setGoalNormalByTeam1((byte)1);
		matchC1.setGoalNormalByTeam2((byte)0);
		Match matchC2 = new Match();
		matchC2.setTeam1(teamA);
		matchC2.setTeam2(teamC);
		matchC2.setGoalNormalByTeam1((byte)2);
		matchC2.setGoalNormalByTeam2((byte)2);
		Match matchC3 = new Match();
		matchC3.setTeam1(teamC);
		matchC3.setTeam2(teamB);
		matchC3.setGoalNormalByTeam1((byte)1);
		matchC3.setGoalNormalByTeam2((byte)1);
		
		Match matchD1 = new Match();
		matchD1.setTeam1(teamC);
		matchD1.setTeam2(teamD);
		matchD1.setGoalNormalByTeam1((byte)1);
		matchD1.setGoalNormalByTeam2((byte)0);
		Match matchD2 = new Match();
		matchD2.setTeam1(teamD);
		matchD2.setTeam2(teamA);
		matchD2.setGoalNormalByTeam1((byte)1);
		matchD2.setGoalNormalByTeam2((byte)4);
//		Match matchD3 = new Match();
//		matchD3.setTeam1(teamB);
//		matchD3.setTeam2(teamD);
//		matchD3.setGoalNormalByTeam1((byte)2);
//		matchD3.setGoalNormalByTeam2((byte)0);
		
		List<Match> matchAList = Arrays.asList(
				matchA1, matchA2, matchA3
				);

		List<Match> matchBList = Arrays.asList(
				matchB1, matchB2/*, matchB3*/
				);

		List<Match> matchCList = Arrays.asList(
				matchC1, matchC2, matchC3
				);

		List<Match> matchDList = Arrays.asList(
				matchD1, matchD2/*, matchD3*/
				);

		List<GroupTeam> groupTeams = Arrays.asList(
				new GroupTeam(teamA, matchAList),
				new GroupTeam(teamB, matchBList),
				new GroupTeam(teamC, matchCList),
				new GroupTeam(teamD, matchDList)
				);
		
		assertFalse("Group should not be finished", groupTeamService.isGroupFinished(groupTeams));
	}

	/**
	 * Test {@link GroupTeamService#getGroupTeamByGroupPosition(List<GroupTeam>, int)} method.
	 * Scenario: successfully retrieves the result
	 */
	@Test
	public void /*GroupTeam*/ getGroupTeamByGroupPosition(/*List<GroupTeam> groupTeams, int positionInGroup*/) {
		int positionInGroup = 2;
		
		Team teamA = new Team();
		teamA.setName("A");
		Team teamB = new Team();
		teamB.setName("B");
		Team teamC = new Team();
		teamC.setName("C");
		Team teamD = new Team();
		teamD.setName("D");
		
		GroupTeam groupTeamA = new GroupTeam(teamA, null);
		groupTeamA.setPositionInGroup(2);
		GroupTeam groupTeamB = new GroupTeam(teamB, null);
		groupTeamB.setPositionInGroup(4);
		GroupTeam groupTeamC = new GroupTeam(teamC, null);
		groupTeamC.setPositionInGroup(1);
		GroupTeam groupTeamD = new GroupTeam(teamD, null);
		groupTeamD.setPositionInGroup(3);
		
		List<GroupTeam> groupTeams = Arrays.asList(
				groupTeamA, groupTeamB, groupTeamC, groupTeamD
				);
		
		GroupTeam groupTeam = groupTeamService.getGroupTeamByGroupPosition(groupTeams, positionInGroup);
		GroupTeam expectedGroupTeam = groupTeamA;
		
		assertEquals("Retrieved GroupTeam instance should be the expected one.", expectedGroupTeam, groupTeam);
	}

	/**
	 * Test {@link GroupTeamService#getGroupTeamByGroupPosition(List<GroupTeam>, int)} method.
	 * Scenario: successfully retrieves {@code null} because there are more teams on the wanted position 
	 */
	@Test
	public void /*GroupTeam*/ getGroupTeamByGroupPositionUnambiguous(/*List<GroupTeam> groupTeams, int positionInGroup*/) {
		int positionInGroup = 2;
		
		Team teamA = new Team();
		teamA.setName("A");
		Team teamB = new Team();
		teamB.setName("B");
		Team teamC = new Team();
		teamC.setName("C");
		Team teamD = new Team();
		teamD.setName("D");
		
		GroupTeam groupTeamA = new GroupTeam(teamA, null);
		groupTeamA.setPositionInGroup(2);
		GroupTeam groupTeamB = new GroupTeam(teamB, null);
		groupTeamB.setPositionInGroup(4);
		GroupTeam groupTeamC = new GroupTeam(teamC, null);
		groupTeamC.setPositionInGroup(2);
		GroupTeam groupTeamD = new GroupTeam(teamD, null);
		groupTeamD.setPositionInGroup(1);
		
		List<GroupTeam> groupTeams = Arrays.asList(
				groupTeamA, groupTeamB, groupTeamC, groupTeamD
				);
		
		GroupTeam groupTeam = groupTeamService.getGroupTeamByGroupPosition(groupTeams, positionInGroup);
		
		assertNull("Retrieved GroupTeam instance should be null.", groupTeam);
	}

	/**
	 * Test {@link GroupTeamService#getGroupTeamByGroupPosition(List<GroupTeam>, int)} method.
	 * Scenario: successfully retrieves {@code null} because there is no team on the wanted position 
	 */
	@Test
	public void /*GroupTeam*/ getGroupTeamByGroupPositionNotfound(/*List<GroupTeam> groupTeams, int positionInGroup*/) {
		int positionInGroup = 3;
		
		Team teamA = new Team();
		teamA.setName("A");
		Team teamB = new Team();
		teamB.setName("B");
		Team teamC = new Team();
		teamC.setName("C");
		Team teamD = new Team();
		teamD.setName("D");
		
		GroupTeam groupTeamA = new GroupTeam(teamA, null);
		groupTeamA.setPositionInGroup(2);
		GroupTeam groupTeamB = new GroupTeam(teamB, null);
		groupTeamB.setPositionInGroup(4);
		GroupTeam groupTeamC = new GroupTeam(teamC, null);
		groupTeamC.setPositionInGroup(2);
		GroupTeam groupTeamD = new GroupTeam(teamD, null);
		groupTeamD.setPositionInGroup(1);
		
		List<GroupTeam> groupTeams = Arrays.asList(
				groupTeamA, groupTeamB, groupTeamC, groupTeamD
				);
		
		GroupTeam groupTeam = groupTeamService.getGroupTeamByGroupPosition(groupTeams, positionInGroup);
		
		assertNull("Retrieved GroupTeam instance should be null.", groupTeam);
	}
	
	/**
	 * Test {@link GroupTestService#sortGroupTeamsOnPosition(List<GroupTeam>)} method.
	 * Scenario: sorts the input as expected.
	 */
	@Test
	public void /*boolean*/ sortGroupTeamsOnPosition(/*List<GroupTeam> groupTeams*/) {
		Long eventId = 2L; // EC2016
		
		// retrieve groups belong to the given eventId
		Comparator<Group> byGroupName = (g1, g2) -> g1.getName().compareTo(g2.getName());
		List<Group> groups = commonDao.findAllEntities(Group.class).stream()
				.filter(g -> g.getEvent().getEventId().equals(eventId))
				.sorted(byGroupName)
				.toList();
		
		Group groupA = groups.get(0);
		Comparator<Match> byMatchNr = (m1, m2) -> Short.compare(
				m1.getMatchN(), m2.getMatchN());
		List<Match> matches = commonDao.findAllEntities(Match.class).stream()
				.filter(m -> m.getRound().getIsGroupmatchAsBoolean() 
						&& m.getTeam1().getGroup().getGroupId().equals(groupA.getGroupId()))
				.sorted(byMatchNr)
				.toList();

		Map<Long, List<Match>> playedMatchByTeamIdMap = new HashMap<>();
		
		// match1: teamA1 x teamA2
		// -------W-D-L-GF-GA-PT
		// teamA1-1-0-0-01-00-03
		// teamA3-0-0-0-00-00-00
		// teamA4-0-0-0-00-00-00
		// teamA2-0-0-1-00-01-00
		Match match = matches.get(0);
		Team teamA1 = match.getTeam1(); // teamId = 33
		System.out.println("teamA1: " + teamA1.getTeamId());
		playedMatchByTeamIdMap.put(teamA1.getTeamId(), Lists.newArrayList(match));
		Team teamA2 = match.getTeam2(); // teamId = 34
		System.out.println("teamA2: " + teamA2.getTeamId());
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
		System.out.println("teamA3: " + teamA3.getTeamId());
		playedMatchByTeamIdMap.put(teamA3.getTeamId(), Lists.newArrayList(match));
		Team teamA4 = match.getTeam2(); // teamId = 36
		System.out.println("teamA4: " + teamA4.getTeamId());
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
				.toList();
		
		// match1: teamB1 x teamB2
		// -------W-D-L-GF-GA-PT
		// teamB1-0-1-0-02-02-01
		// teamB2-0-1-0-02-02-01
		// teamB3-0-0-0-00-00-00
		// teamB4-0-0-0-00-00-00
		/*Match*/ match = matches.get(0);
		Team teamB1 = match.getTeam1(); // teamId = 39
		System.out.println("teamB1: " + teamB1.getTeamId());
		playedMatchByTeamIdMap.put(teamB1.getTeamId(), Lists.newArrayList(match));
		Team teamB2 = match.getTeam2(); // teamId = 40
		System.out.println("teamB2: " + teamB2.getTeamId());
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
		System.out.println("teamB3: " + teamB3.getTeamId());
		playedMatchByTeamIdMap.put(teamB3.getTeamId(), Lists.newArrayList(match));
		Team teamB4 = match.getTeam2(); // teamId = 38
		System.out.println("teamB4: " + teamB4.getTeamId());
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

		List<GroupTeam> expectedGroupTeams = Arrays.asList(
				new GroupTeam(teamB2, playedMatchByTeamIdMap.get(teamB2.getTeamId())),
				new GroupTeam(teamA2, playedMatchByTeamIdMap.get(teamA2.getTeamId()))
				);

		
		List<GroupTeam> groupTeams = Arrays.asList(
				new GroupTeam(teamA2, playedMatchByTeamIdMap.get(teamA2.getTeamId())),
				new GroupTeam(teamB2, playedMatchByTeamIdMap.get(teamB2.getTeamId()))
				);
		
		boolean isEquality = groupTeamService.sortGroupTeamsOnPosition(groupTeams);
		assertFalse("There should not be equality in the result.", isEquality);
		assertEquals("Result groupTeams list should be equal to the expected one.",
				expectedGroupTeams.stream().map(g -> g.getTeam()).toList(),
				groupTeams.stream().map(g -> g.getTeam()).toList());
	}

	/**
	 * Test {@link GroupTestService#sortGroupTeamsOnPosition(List<GroupTeam>)} method.
	 * Scenario: sorts the input as expected but the result contains equality
	 */
	@Test
	public void /*boolean*/ sortGroupTeamsOnPositionEquality(/*List<GroupTeam> groupTeams*/) {
		Long eventId = 2L; // EC2016
		
		// retrieve groups belong to the given eventId
		Comparator<Group> byGroupName = (g1, g2) -> g1.getName().compareTo(g2.getName());
		List<Group> groups = commonDao.findAllEntities(Group.class).stream()
				.filter(g -> g.getEvent().getEventId().equals(eventId))
				.sorted(byGroupName)
				.toList();
		
		Group groupA = groups.get(0);
		Comparator<Match> byMatchNr = (m1, m2) -> Short.compare(
				m1.getMatchN(), m2.getMatchN());
		List<Match> matches = commonDao.findAllEntities(Match.class).stream()
				.filter(m -> m.getRound().getIsGroupmatchAsBoolean() 
						&& m.getTeam1().getGroup().getGroupId().equals(groupA.getGroupId()))
				.sorted(byMatchNr)
				.toList();

		Map<Long, List<Match>> playedMatchByTeamIdMap = new HashMap<>();
		
		// match1: teamA1 x teamA2
		// -------W-D-L-GF-GA-PT
		// teamA1-0-1-0-01-01-01
		// teamA2-0-1-0-01-01-01
		// teamA3-0-0-0-00-00-00
		// teamA4-0-0-0-00-00-00
		Match match = matches.get(0);
		Team teamA1 = match.getTeam1(); // teamId = 33
		System.out.println("teamA1: " + teamA1.getTeamId());
		playedMatchByTeamIdMap.put(teamA1.getTeamId(), Lists.newArrayList(match));
		Team teamA2 = match.getTeam2(); // teamId = 34
		System.out.println("teamA2: " + teamA2.getTeamId());
		playedMatchByTeamIdMap.put(teamA2.getTeamId(), Lists.newArrayList(match));
		match.setGoalNormalByTeam1((byte)1);
		match.setGoalNormalByTeam2((byte)1);
		commonDao.persistEntity(match);

		Group groupB = groups.get(1);
		/*List<Match>*/ matches = commonDao.findAllEntities(Match.class).stream()
				.filter(m -> m.getRound().getIsGroupmatchAsBoolean() 
						&& m.getTeam1().getGroup().getGroupId().equals(groupB.getGroupId()))
				.sorted(byMatchNr)
				.toList();
		
		// match1: teamB1 x teamB2
		// -------W-D-L-GF-GA-PT
		// teamB1-0-1-0-01-01-01
		// teamB2-0-1-0-01-01-01
		// teamB3-0-0-0-00-00-00
		// teamB4-0-0-0-00-00-00
		/*Match*/ match = matches.get(0);
		Team teamB1 = match.getTeam1(); // teamId = 39
		System.out.println("teamB1: " + teamB1.getTeamId());
		playedMatchByTeamIdMap.put(teamB1.getTeamId(), Lists.newArrayList(match));
		Team teamB2 = match.getTeam2(); // teamId = 40
		System.out.println("teamB2: " + teamB2.getTeamId());
		playedMatchByTeamIdMap.put(teamB2.getTeamId(), Lists.newArrayList(match));
		match.setGoalNormalByTeam1((byte)1);
		match.setGoalNormalByTeam2((byte)1);
		commonDao.persistEntity(match);

		List<GroupTeam> expectedGroupTeams = Arrays.asList(
				new GroupTeam(teamA2, playedMatchByTeamIdMap.get(teamA2.getTeamId())),
				new GroupTeam(teamB2, playedMatchByTeamIdMap.get(teamB2.getTeamId()))
				);

		
		List<GroupTeam> groupTeams = Arrays.asList(
				new GroupTeam(teamA2, playedMatchByTeamIdMap.get(teamA2.getTeamId())),
				new GroupTeam(teamB2, playedMatchByTeamIdMap.get(teamB2.getTeamId()))
				);
		
		boolean isEquality = groupTeamService.sortGroupTeamsOnPosition(groupTeams);
		assertTrue("There should be equality in the result.", isEquality);
		assertEquals("Result groupTeams list should be equal to the expected one.",
				expectedGroupTeams.stream().map(g -> g.getTeam()).toList(),
				groupTeams.stream().map(g -> g.getTeam()).toList());
	}

	/**
	 * Test {@link GroupTestService#sortGroupTeamsOnPosition(List<GroupTeam>)} method.
	 * This is a private method.
	 * Scenario: throws IllegalArgumentException because the input is invalid, 
	 *           tournament event of the first team is not supported.
	 */
	@Test(expected=IllegalArgumentException.class)
	public void /*boolean*/ sortGroupTeamsOnPositionInvalid(/*List<GroupTeam> groupTeams*/) {
		Team teamA = new Team();
		teamA.setName("A");
		Event event = new Event();
		event.setShortDesc("WC");
		event.setYear((short)2014);
		event.setOrganizer("FIFA");
		teamA.setEvent(event);
		Team teamB = new Team();
		teamB.setName("B");
		
		GroupTeam groupTeamA = new GroupTeam(teamA, null);
		GroupTeam groupTeamB = new GroupTeam(teamB, null);
		
		List<GroupTeam> groupTeams = Arrays.asList(
				groupTeamA, groupTeamB
				);
		
		/*boolean isEquality =*/  groupTeamService.sortGroupTeamsOnPosition(groupTeams);
	}

	/**
	 * Test {@link GroupTestService#sortGroupTeamsOnPosition(List<GroupTeam>)} method.
	 * This is a private method.
	 * Scenario: throws IllegalArgumentException because the input is invalid 
	 * 
	 * @throws Exception - because of reflective method call the exception can be anything 
	 */
	@Test(expected=IllegalArgumentException.class)
	public void /*boolean*/ sortGroupTeamsOnPositionNull(/*List<GroupTeam> groupTeams*/) {
		List<GroupTeam> groupTeams = null;
		
		/*boolean isEquality =*/  groupTeamService.sortGroupTeamsOnPosition(groupTeams);
	}

	
	/**
	 * Test {@link GroupTeamService#convertParticipantRuleToGroupPositionPair(String)} method.
	 * Scenario: successfully retrieves the result
	 */
	@Test
	public void /*Pair<GroupPosition>*/ convertParticipantRuleToGroupPositionPair(/*String participantRule*/) {
		String participantRule = "A1-B2";

		Pair<GroupPosition> expectedGroupPositionPair = new Pair<>(new GroupPosition("A", 1), new GroupPosition("B", 2));
				
		Pair<GroupPosition> groupPositionPair = groupTeamService.convertParticipantRuleToGroupPositionPair(participantRule);

		assertEquals("Result groupPostion pair should be equal to the expected one.",
				expectedGroupPositionPair,
				groupPositionPair);
	}

	/**
	 * Test {@link GroupTeamService#convertParticipantRuleToGroupPositionPair(String)} method.
	 * Scenario: successfully retrieves the result
	 */
	@Test
	public void /*Pair<GroupPosition>*/ convertParticipantRuleToGroupPositionPairComplex(/*String participantRule*/) {
		String participantRule = "B1-ACD3"; 

		Pair<GroupPosition> expectedGroupPositionPair = new Pair<>(new GroupPosition("B", 1), new GroupPosition("ACD", 3));
				
		Pair<GroupPosition> groupPositionPair = groupTeamService.convertParticipantRuleToGroupPositionPair(participantRule);

		assertEquals("Result groupPostion pair should be equal to the expected one.",
				expectedGroupPositionPair,
				groupPositionPair);
	}

	/**
	 * Test {@link GroupTeamService#convertParticipantRuleToGroupPositionPair(String)} method.
	 * Scenario: throws IllegalArgumentException because the input is invalid
	 */
	@Test(expected=IllegalArgumentException.class)
	public void /*Pair<GroupPosition>*/ convertParticipantRuleToGroupPositionPairNull(/*String participantRule*/) {
		String participantRule = null;

		/*Pair<GroupPosition> groupPositionPair =*/ groupTeamService.convertParticipantRuleToGroupPositionPair(participantRule);
	}

	/**
	 * Test {@link GroupTeamService#convertParticipantRuleToGroupPositionPair(String)} method.
	 * Scenario: throws IllegalArgumentException because the input is invalid
	 */
	@Test(expected=IllegalArgumentException.class)
	public void /*Pair<GroupPosition>*/ convertParticipantRuleToGroupPositionPairInvalid(/*String participantRule*/) {
		String participantRule = "A2xB2";

		/*Pair<GroupPosition> groupPositionPair =*/ groupTeamService.convertParticipantRuleToGroupPositionPair(participantRule);
	}
}
