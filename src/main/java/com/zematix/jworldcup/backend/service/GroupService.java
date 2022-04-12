package com.zematix.jworldcup.backend.service;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Strings;
import com.zematix.jworldcup.backend.dao.CommonDao;
import com.zematix.jworldcup.backend.dao.GroupDao;
import com.zematix.jworldcup.backend.dao.MatchDao;
import com.zematix.jworldcup.backend.entity.Event;
import com.zematix.jworldcup.backend.entity.Group;
import com.zematix.jworldcup.backend.entity.Match;
import com.zematix.jworldcup.backend.entity.Team;
import com.zematix.jworldcup.backend.exception.ServiceException;
import com.zematix.jworldcup.backend.model.GroupPosition;
import com.zematix.jworldcup.backend.model.GroupTeam;
import com.zematix.jworldcup.backend.model.Pair;

/**
 * Operations around {@link Group} elements. 
 * Usually it injects at least its DAO class belongs to the same entities. 
 * It may also inject other service and DAO classes.
 */
@Service
@Transactional
public class GroupService extends ServiceBase {

	@Inject 
	private GroupDao groupDao;

	@Inject 
	private MatchDao matchDao;

	@Inject 
	private GroupTeamService groupTeamService;

	@Inject 
	private CommonDao commonDao;

	@Value("${app.shortName}")
	private String appShortName;
		
	/**
	 * Return a list of {@link Group} instances belongs to the given {@link Event#eventId} 
	 * parameter. The retrieved elements are ordered by {@link Group#name}.
	 *  
	 * @param eventId - event belongs to the rounds to be retrieved
	 * @throws ServiceException if the matches cannot be retrieved 
	 * @throws IllegalArgumentException if given {@link Event#eventId} is {@code null}
	 */
	@Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
	public List<Group> retrieveGroupsByEvent(Long eventId) throws ServiceException {
		checkNotNull(eventId);
		
		List<Group> groups = groupDao.retrieveGroupsByEvent(eventId);
		return groups; 
	}

	/**
	 * Returns a list of {@link GroupTeam} instances belongs to the provided
	 * {@link Group#getGroupId() group. The list is sorted, starts with first position.
	 *
	 * @param groupId
	 * @return a list of ranked teams belongs to the provided group
	 */
	@Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
	public List<GroupTeam> getRankedGroupTeamsByGroup(Long groupId) throws ServiceException{
		checkNotNull(groupId);
		
		Group group = commonDao.findEntityById(Group.class, groupId);
		List<GroupTeam> groupTeams = new ArrayList<>();
		for (Team team : group.getTeams()) {
			team.getEvent().getEventId();
			List<Match> playedMatches = matchDao.retrieveFinishedGroupMatchesByTeam(team.getTeamId());
			GroupTeam groupTeam = new GroupTeam(team, playedMatches);
			groupTeams.add(groupTeam);
		}
		/*boolean hasEqualRankings =*/ groupTeamService.sortGroupTeams(groupTeams);
		return groupTeams;
	}
	
	/**
	 * Returns a list of {@link GroupTeam} instances belongs to the provided
	 * {@link Event#getEventId() event. The list is sorted by its group and position inside its group.
	 *
	 * @param groupId
	 * @return a list of ranked teams belongs to the provided group
	 */
	@Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
	public List<GroupTeam> getRankedGroupTeamsByEvent(Long eventId) throws ServiceException{
		checkNotNull(eventId);
		List<GroupTeam> result = new ArrayList<>();
		
		List<Group> groups = groupDao.retrieveGroupsByEvent(eventId);
		for (Group group: groups) {
			List<GroupTeam> groupTeams = new ArrayList<>();
			for (Team team : group.getTeams()) {
				team.getEvent().getEventId();
				List<Match> playedMatches = matchDao.retrieveFinishedGroupMatchesByTeam(team.getTeamId());
				GroupTeam groupTeam = new GroupTeam(team, playedMatches);
				groupTeams.add(groupTeam);
			}
			groupTeamService.sortGroupTeams(groupTeams);
			result.addAll(groupTeams);
		}
		return result;
	}
	
	/**
	 * Returns a constructed map containing all {@link Team} instances of the provided 
	 * {@link Event#getEventId()} where the key are {@link GroupPosition} instances.
	 * If on a position there are more teams, those teams are not included in the map.
	 *
	 * @param eventId 
	 * @return a map containing group position as key to team as value
	 */
	@Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
	public Map<GroupPosition, Team> getTeamByGroupPositionMap(Long eventId) throws ServiceException {
		checkNotNull(eventId);
		
		Map<GroupPosition, Team> teamByGroupPositionMap = new HashMap<>();
		
		List<Group> groups = retrieveGroupsByEvent(eventId);
		for (Group group : groups) {
			List<GroupTeam> rankedGroupTeams = getRankedGroupTeamsByGroup(group.getGroupId());
			if (!groupTeamService.isGroupFinished(rankedGroupTeams)) {
				continue;
			}
			List<GroupTeam> bestGroupTeams = new ArrayList<>();
			for (GroupPosition groupPosition : retrieveGroupPositionsOfParticipantRules(eventId)) {
				if (groupPosition.getGroupName().equals(group.getName())) {
					// single group name
					GroupTeam groupTeam = groupTeamService.getGroupTeamByGroupPosition(rankedGroupTeams, groupPosition.getPosition());
					if (groupTeam != null) {
						teamByGroupPositionMap.put(groupPosition, groupTeam.getTeam());
					}
				}
				else if (groupPosition.getGroupName().length() > 1) {
					// multiple group name, only some events (tournaments) support it
					if (bestGroupTeams.isEmpty()) {
						bestGroupTeams = retrieveBestTeamsOnGroupPosition(eventId, groupPosition.getPosition(), groups.size()-groupPosition.getGroupName().length()+1);
					}
					for (GroupTeam groupTeam : bestGroupTeams) {
						if (groupPosition.getGroupName().contains(groupTeam.getTeam().getGroup().getName())) {
							if (!teamByGroupPositionMap.values().contains(groupTeam.getTeam())) {
								teamByGroupPositionMap.put(groupPosition, groupTeam.getTeam());
								break;
							}
						}
					}
				}
			}
		}
		return teamByGroupPositionMap;
	}
	
	/**
	 * Returns {@link GlobalTeam} instances, the best given {@code numberOfTeams} number of
	 * teams on the provided {@code positionInGroup} position of all groups belongs to
	 * {@link Event#getEventId()} event. Result is an empty list if any of the groups is not
	 * terminated yet, there are still matches to be played.
	 * Normally this method is called only from those events (tournaments) where match 
	 * participant rules contain multiple groupName, e.g. from EC2016.
	 *
	 * @param eventId
	 * @param positionInGroup
	 * @param numberOfTeams
	 * @return a list of best teams with maximum provided size on given position of all groups of the given event
	 */
	@VisibleForTesting
	/*private*/ List<GroupTeam> retrieveBestTeamsOnGroupPosition(Long eventId, int positionInGroup, int numberOfTeams) throws ServiceException {
		checkNotNull(eventId);
		
		List<GroupTeam> groupTeams = new ArrayList<>();
		List<Group> groups = retrieveGroupsByEvent(eventId);
		boolean isAllGroupsFinished = true;
		for (Group group : groups) {
			List<GroupTeam> rankedGroupTeams = getRankedGroupTeamsByGroup(group.getGroupId());
			isAllGroupsFinished &= groupTeamService.isGroupFinished(rankedGroupTeams);
			if (!isAllGroupsFinished) {
				break;
			}
			GroupTeam groupTeam = groupTeamService.getGroupTeamByGroupPosition(rankedGroupTeams, positionInGroup);
			if (groupTeam != null) {
				groupTeams.add(groupTeam);
			}
		}
		
		if (!isAllGroupsFinished || groupTeams.isEmpty()) {
			groupTeams.clear();
			return groupTeams;
		}
		
		groupTeamService.sortGroupTeamsOnPosition(groupTeams);
		return groupTeams.subList(0, Math.min(numberOfTeams, groupTeams.size()));
	}

	/**
	 * Returns a list of {@link GroupPosition} instances belongs to all matches of the
	 * round after group rounds.
	 *  
	 * @param eventId
	 * @return
	 */
	@VisibleForTesting
	/*private*/ List<GroupPosition> retrieveGroupPositionsOfParticipantRules(Long eventId) {
		checkNotNull(eventId);
		
		final String PARTICIPANT_RULE_REGEX = "^([WL])([0-9]+)-([WL])([0-9]+)$";
		List<GroupPosition> groupPositions = new ArrayList<>();

		// retrieve participant rules, e.g. "A1-B2", "A2-BCD3", "W34-W35", "L32-L33", ...
		List<String> participantRules = matchDao.retrieveParticipantRulesOfMatchesByEvent(eventId);
		
		// filter out non group rules, e.g. "W34-W35", "L32-L33"
		participantRules = participantRules.stream()
				.filter(rule->!rule.matches(PARTICIPANT_RULE_REGEX))
				.toList();
		
		for (String participantRule : participantRules) {
			Pair<GroupPosition> groupPositionPair = groupTeamService.convertParticipantRuleToGroupPositionPair(participantRule);
			groupPositions.addAll(groupPositionPair.getList());
		}
		
		return groupPositions;
	}

	/**
	 * Retrieves {@link Group} instance belongs to the given {@code eventId} and
	 *  group {@code name}.
	 * 
	 * @param eventId
	 * @param name - group name, for example "A"
	 * @return found {Group} instance or {@code null} if not found
	 * @throws IllegalArgumentException if any of the parameters is {@code null} or
	 *         parameter {@code name} is empty.
	 */
	@Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
	public Group retrieveGroupByName(Long eventId, String name) throws ServiceException {
		checkNotNull(eventId);
		checkArgument(!Strings.isNullOrEmpty(name), "Argument \"name\" cannot be null nor empty.");
		
		Group group = groupDao.retrieveGroupByName(eventId, name);
		return group;
		
	}
}
