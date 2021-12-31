package com.zematix.jworldcup.backend.service;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.google.common.base.Strings;
import com.google.common.collect.Iterables;
import com.zematix.jworldcup.backend.model.EventShortDescWithYearEnum;
import com.zematix.jworldcup.backend.model.GroupPosition;
import com.zematix.jworldcup.backend.model.GroupTeam;
import com.zematix.jworldcup.backend.model.Pair;

/**
 * Contains helper methods and comparators around teams of groups of an event. 
 */
@Service
//@Transactional - no database usage here
public class GroupTeamService extends ServiceBase {

	/**
	 * First level sorting of teams in a group based on the following tie-breaking criterion.
	 * <li>Higher number of points obtained in the group</li>
	 */
	private Comparator<GroupTeam> groupTeamComparatorPoints = new Comparator<GroupTeam>() {
		@Override
		public int compare(GroupTeam team1, GroupTeam team2) {
			if (team1.getPoints() != team2.getPoints())
				return team1.getPoints() < team2.getPoints() ? +1 : -1;
			return 0;
		}
	};

	/**
	 * Second level sorting of teams in a group based on the following tie-breaking criteria.
	 * <li>Higher number of points obtained in the matches played between the teams in question</li>
	 * <li>Superior goal difference resulting from the matches played between the teams in question</li>
	 * <li>Higher number of goals scored in the matches played between the teams in question</li>
	 */
	private Comparator<GroupTeam> groupTeamComparatorPGdGf = new Comparator<GroupTeam>() {
		@Override
		public int compare(GroupTeam team1, GroupTeam team2) {
			if (team1.getPoints() != team2.getPoints())
				return team1.getPoints() < team2.getPoints() ? +1 : -1;
			if (team1.getGoalDifference() != team2.getGoalDifference())
				return team1.getGoalDifference() < team2.getGoalDifference() ? +1 : -1;
			if (team1.getGoalsFor() != team2.getGoalsFor())
				return team1.getGoalsFor() < team2.getGoalsFor() ? +1 : -1;
			return 0;
		}
	};

	/**
	 * First level sorting of teams in a group based on the following tie-breaking criterion.
	 * <li>Superior goal difference resulting from the matches played between the teams in question</li>
	 * <li>Higher number of goals scored in the matches played between the teams in question</li>
	 */
	private Comparator<GroupTeam> groupTeamComparatorGdGf = new Comparator<GroupTeam>() {
		@Override
		public int compare(GroupTeam team1, GroupTeam team2) {
			if (team1.getGoalDifference() != team2.getGoalDifference())
				return team1.getGoalDifference() < team2.getGoalDifference() ? +1 : -1;
			if (team1.getGoalsFor() != team2.getGoalsFor())
				return team1.getGoalsFor() < team2.getGoalsFor() ? +1 : -1;
			return 0;
		}
	};

	/**
	 * Sorts teams in a group based on the tie-breaking criteria of the organizer of the event.
	 *   
	 * @param groupTeams - list of teams in a group
	 * @return {@code true} if after sorting there are equal teams in the group
	 */
	public boolean sortGroupTeams(List<GroupTeam> groupTeams) {
		return sortGroupTeams(groupTeams, /*level*/ 0, /*globalPositionInGroup*/ 1);
	}

	/**
	 * Sorts teams in a group based on the tie-breaking criteria of the organizer of the event.
	 *   
	 * @param groupTeams - list of teams in a group
	 * @param level - crieria group to be executed
	 * @param globalPositionInGroup - position of the first team in the group
	 * @return {@code true} if after sorting there are equal teams in the group
	 */
	private boolean sortGroupTeams(List<GroupTeam> groupTeams, int level, int globalPositionInGroup) {
		boolean hasEqualRankings = false;
		checkArgument(groupTeams != null && !groupTeams.isEmpty(), "Argument \"groupTeams\" list must be neither null nor empty.");
		String eventShortDescWithYear = groupTeams.get(0).getEventShortDescWithYear();
		checkNotNull(eventShortDescWithYear);
		EventShortDescWithYearEnum eventShortDescWithYearEnum = EventShortDescWithYearEnum.valueOf(eventShortDescWithYear); // may throw IllegalArgumentException
		List<List<GroupTeam>> twinGroupTeamsList = new ArrayList<>();
		
		Comparator<GroupTeam> comparator = null;
		
		if (Arrays.asList(EventShortDescWithYearEnum.WC2014, 
				EventShortDescWithYearEnum.EC2016, 
				EventShortDescWithYearEnum.WC2018,
				EventShortDescWithYearEnum.AFC2019,
				EventShortDescWithYearEnum.CAF2019,
				EventShortDescWithYearEnum.EC2020
				).contains(eventShortDescWithYearEnum)) {
			switch (level) {
				case 0:
					comparator = groupTeamComparatorPoints; // globally in group
					break;
				case 1:
					comparator = groupTeamComparatorPGdGf; // against each other, but it might be repeated
					break;
				case 2:
					comparator = groupTeamComparatorGdGf; // globally in group
					break;
				default:
					return true;
			}
		}
		else if (Arrays.asList(
				EventShortDescWithYearEnum.CA2016,
				EventShortDescWithYearEnum.CA2019, 
				EventShortDescWithYearEnum.CA2021,
				EventShortDescWithYearEnum.ABC2021
				).contains(eventShortDescWithYearEnum)) {
			switch (level) {
				case 0:
					comparator = groupTeamComparatorPGdGf;// globally in group
					break;
				case 1:
					comparator = groupTeamComparatorPGdGf;// against each other, but it might be repeated
					break;
				default:
					return true;
			}
		}
		
		Collections.sort(groupTeams, comparator);
		
		GroupTeam groupTeamPrev = null;
		int positionInGroup = 1;
		int twins = 1;
		for (GroupTeam groupTeam : groupTeams) {
			if (groupTeamPrev == null) {
				groupTeam.setPositionInGroup(globalPositionInGroup + positionInGroup - 1);
				twinGroupTeamsList.add(groupTeams.subList(0, 1));
			}
			else {
				if (comparator.compare(groupTeamPrev, groupTeam) == 0) {
					twins++;
					groupTeam.setPositionInGroup(globalPositionInGroup + positionInGroup - 1);
					twinGroupTeamsList.remove(Iterables.getLast(twinGroupTeamsList));
					twinGroupTeamsList.add(groupTeams.subList(positionInGroup-1, positionInGroup-1+twins));
					
				}
				else {
					positionInGroup += twins;
					twins = 1;
					groupTeam.setPositionInGroup(globalPositionInGroup + positionInGroup - 1);
					twinGroupTeamsList.add(groupTeams.subList(positionInGroup-1, positionInGroup));
				}
			}
			groupTeamPrev = groupTeam;
		}
		
		for (List<GroupTeam> twinGroupTeams : twinGroupTeamsList) {
			if (twinGroupTeams.size() >= 2) {
				int nextLevel = level + 1;
				boolean isAgainstEachOther = false;
				
				if (Arrays.asList(
						EventShortDescWithYearEnum.WC2014, 
						EventShortDescWithYearEnum.EC2016, 
						EventShortDescWithYearEnum.WC2018,
						EventShortDescWithYearEnum.AFC2019,
						EventShortDescWithYearEnum.CAF2019,
						EventShortDescWithYearEnum.EC2020
						).contains(eventShortDescWithYearEnum)) {
					if (level == 1 && twinGroupTeams.size() < groupTeams.size()) {
						// Level sorting must be repeated to the matches between the teams
						// who are still level to determine their final rankings.
						nextLevel = level;
					}
					isAgainstEachOther = (nextLevel == 1);
				}
				else if (Arrays.asList(
						EventShortDescWithYearEnum.CA2016,
						EventShortDescWithYearEnum.CA2019,
						EventShortDescWithYearEnum.CA2021,
						EventShortDescWithYearEnum.ABC2021
						).contains(eventShortDescWithYearEnum)) {
					if (level == 1 && twinGroupTeams.size() < groupTeams.size()) {
						// Level sorting must be repeated to the matches between the teams
						// who are still level to determine their final rankings.
						nextLevel = level;
					}
					isAgainstEachOther = (nextLevel == 1);
				}
			
				if (isAgainstEachOther) {
					// If, after level sorting, teams still have an equal ranking, then next 
					// level sorting is reapplied exclusively to the matches between the teams
					// who are still level to determine their final rankings.
					List<Long> filterTeamIds = twinGroupTeams.stream()
							.map(e->e.getTeam().getTeamId())
							.collect(Collectors.toList()); // mutable List is a must here
					twinGroupTeams.forEach(item->item.setFilterTeamIds(filterTeamIds));
				}
				else {
					// reset filterTeamIds
					twinGroupTeams.forEach(item->item.getFilterTeamIds().clear());
				}

				// recursive call to sort sublist
				hasEqualRankings |= sortGroupTeams(twinGroupTeams, nextLevel, twinGroupTeams.get(0).getPositionInGroup());

				// reset filterTeamIds
				twinGroupTeams.forEach(item->item.getFilterTeamIds().clear());
			}
		}
		
		return hasEqualRankings;
	}

	/**
	 * Returns {@code true} if all teams of the given {@code groupTeams} list 
	 * have already played all of their group matches.
	 * 
	 * @param groupTeams - list of {@link GroupTeam} instances in a group
	 * @return {@code true} if all teams finished their group matches
	 */
	public boolean isGroupFinished(List<GroupTeam> groupTeams) {
		checkArgument(groupTeams != null && !groupTeams.isEmpty(), "Argument \"groupTeams\" list must be neither null nor empty.");
		
		boolean isGroupFinished = groupTeams.stream()
				.map(groupTeam->groupTeam.isTeamInGroupFinished())
				.reduce((b1, b2) -> b1 && b2).get();
		return isGroupFinished;
	}
	
	
	/**
	 * Returns a {@link GroupTeam} from the provided {@code groupTeams} list, 
	 * where the result team is on {@code positionInGroup} position in its group.
	 * 
	 * @param groupTeams - list of {@link GroupTeam} instances in a group
	 * @param positionInGroup - position of the wanted team in the group started from 1
	 * @return team on {@code positionInGroup} position from a team list of a group 
	 */
	public GroupTeam getGroupTeamByGroupPosition(List<GroupTeam> groupTeams, int positionInGroup) {
		checkArgument(groupTeams != null && !groupTeams.isEmpty(), "Argument \"groupTeams\" list must be neither null nor empty.");
		
		List<GroupTeam> teams = groupTeams.stream()
				.filter(groupTeam->groupTeam.getPositionInGroup() == positionInGroup)
				.toList();
		if (teams.size() != 1) {
			return null; // unambiguous rank
		}
		return teams.get(0);
	}
	
	/**
	 * Sorts teams in the given {@link GroupTeam> instances based on the tie-breaking 
	 * criteria of the organizer of the event. Usually it is called to sort the best teams
	 * among the 3rd positions in all groups.
	 * 
	 * @param groupTeams - list of teams in a group
	 * @return {@code true} if after sorting there are equal teams in the group
	 */
	public boolean sortGroupTeamsOnPosition(List<GroupTeam> groupTeams) {
		boolean hasEqualRankings = false;
		checkArgument(groupTeams != null && !groupTeams.isEmpty(), "Argument \"groupTeams\" list must be neither null nor empty.");
		String eventShortDescWithYear = groupTeams.get(0).getEventShortDescWithYear();
		checkNotNull(eventShortDescWithYear);
		EventShortDescWithYearEnum eventShortDescWithYearEnum = EventShortDescWithYearEnum.valueOf(eventShortDescWithYear); // may throw IllegalArgumentException

		Comparator<GroupTeam> comparator = null;
		
		if (Arrays.asList(
				EventShortDescWithYearEnum.EC2016,
				EventShortDescWithYearEnum.AFC2019,
				EventShortDescWithYearEnum.CA2019,
				EventShortDescWithYearEnum.CAF2019,
				EventShortDescWithYearEnum.EC2020,
				EventShortDescWithYearEnum.CA2021
				).contains(eventShortDescWithYearEnum)) {
			comparator = groupTeamComparatorPGdGf;
		}
		else {
			throw new IllegalArgumentException(String.format("Unsupported function on %s event.", eventShortDescWithYear));
		}

		Collections.sort(groupTeams, comparator);
		
		GroupTeam groupTeamPrev = null;
		int positionInGroup = 1;
		int twins = 1;
		for (GroupTeam groupTeam : groupTeams) {
			if (groupTeamPrev == null) {
				groupTeam.setPositionInGroup(positionInGroup);
			}
			else {
				if (comparator.compare(groupTeamPrev, groupTeam) == 0) {
					twins++;
					groupTeam.setPositionInGroup(positionInGroup);
					hasEqualRankings = true;
				}
				else {
					positionInGroup += twins;
					twins = 1;
					groupTeam.setPositionInGroup(positionInGroup);
				}
			}
			groupTeamPrev = groupTeam;
		}
		
		return hasEqualRankings;
	}

	/**
	 * Returns converted participantRule as a pair of {@link GroupPosition} elements.
	 * It processes all kind of participantRules, both group and knock-out stages.
	 * 
	 * @param participantRule
	 * @return pair of {@link GroupPosition} elements
	 */
	public Pair<GroupPosition> convertParticipantRuleToGroupPositionPair(String participantRule) {
		final String PARTICIPANT_RULE_REGEX = "^([A-Z]+)([0-9]+)-([A-Z]+)([0-9]+)$";
		Pair<GroupPosition> groupPositionPair = new Pair<>();
		
		checkArgument(!Strings.isNullOrEmpty(participantRule), "Parameter \"participantRule\" must not be empty.");
		
		Matcher matcher = Pattern.compile(PARTICIPANT_RULE_REGEX).matcher(participantRule);
		if (!matcher.find()) {
			throw new IllegalArgumentException(
					String.format("Parameter \"participantRule\" = \"%s\" is in incorrect format.", participantRule));
		}

		for (int i=0; i<2; i++) {
			String groupName = matcher.group(i*2+1);
			Integer position = Integer.valueOf(matcher.group(i*2+2));
			GroupPosition groupPosition = new GroupPosition(groupName, position);
			groupPositionPair.setValueN(i, groupPosition);
		}

		return groupPositionPair;
	}
}
