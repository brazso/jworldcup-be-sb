package com.zematix.jworldcup.backend.service;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.base.Strings;
import com.zematix.jworldcup.backend.configuration.CachingConfig;
import com.zematix.jworldcup.backend.dao.TeamDao;
import com.zematix.jworldcup.backend.entity.Group;
import com.zematix.jworldcup.backend.entity.Team;
import com.zematix.jworldcup.backend.exception.ServiceException;

/**
 * Operations around {@link Team} elements. 
 * Usually it injects at least its DAO class belongs to the same entities. 
 * It may also inject other service and DAO classes.
 */
@Service
@Transactional
public class TeamService extends ServiceBase {

	@Inject 
	private TeamDao teamDao;
	
	@Inject
	private GroupService groupService;

//	@Inject 
//	private CommonDao commonDao;

	/**
	 * Retrieves all teams of an event belongs to the given {@code eventId}.
	 * 
	 * @param eventId
	 * @return all teams of an event
	 */
	@Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
	@Cacheable(cacheNames = CachingConfig.CACHE_FAVOURITE_GROUP_TEAMS, key = "{#eventId}")
	public List<Team> retrieveFavouriteGroupTeams(Long eventId) {
		checkNotNull(eventId);
		
		var teams = teamDao.retrieveFavouriteGroupTeams(eventId);

		// load lazy associations
		teams.stream().forEach(e -> {
			e.getGroup().getName();
		});
		
		return teams;
	}
	
	/**
	 * Retrieves all teams of the knockout phase of an event belongs to the given 
	 * {@code eventId}.
	 * 
	 * @param eventId
	 * @return all teams qualified to knockout phase of an event
	 */
	@Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
	@Cacheable(cacheNames = CachingConfig.CACHE_FAVOURITE_KNOCKOUT_TEAMS, key = "{#eventId}")
	public List<Team> retrieveFavouriteKnockoutTeams(Long eventId) {
		checkNotNull(eventId);

		var teams = teamDao.retrieveFavouriteKnockoutTeams(eventId);
		
		// load lazy associations
		teams.stream().forEach(e -> {
			e.getGroup().getName();
		});
		

		return teams;
	}

	/**
	 * Retrieves a team of an event belongs to the given 
	 * {@code eventId} and with the given {@link Team#wsId}.
	 * 
	 * @param eventId
	 * @param wsId - webService id of the team
	 * @return a team given by the parameters, {@code null} if not found
	 */
	@Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
	public Team retrieveTeamByWsId(Long eventId, Long wsId) {
		checkNotNull(eventId);
		checkNotNull(wsId);

		return teamDao.retrieveTeamByWsId(eventId, wsId);
	}

	/**
	 * Retrieves all teams of an event belongs to the given {@code eventId}.
	 * 
	 * @param eventId
	 * @return all teams of an event
	 */
	@Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
	public List<Team> retrieveTeamsByGroupName(Long eventId, String groupName) throws ServiceException {
		List<Team> teams = new ArrayList<>();
		
		checkNotNull(eventId);
		checkArgument(!Strings.isNullOrEmpty(groupName), "Parameter \"groupName\" must not be null nor empty.");
		
		// groupName may contain more groups, eg. "ABC"
		for (char c : groupName.toCharArray()) {
			Group group = groupService.retrieveGroupByName(eventId, String.valueOf(c));
			checkArgument(group != null, String.format("Parameter \"groupName\"=\"%s\" of eventId=%d contains not existing group.", groupName, eventId));
			teams.addAll(group.getTeams());
		}
		
		return teams;
	}
	
}
