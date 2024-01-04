package com.zematix.jworldcup.backend.service;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

import java.util.List;

import javax.inject.Inject;

import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.interceptor.SimpleKeyGenerator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.zematix.jworldcup.backend.configuration.CachingConfig;
import com.zematix.jworldcup.backend.dao.CommonDao;
import com.zematix.jworldcup.backend.dao.UserOfEventDao;
import com.zematix.jworldcup.backend.entity.Event;
import com.zematix.jworldcup.backend.entity.Team;
import com.zematix.jworldcup.backend.entity.User;
import com.zematix.jworldcup.backend.entity.UserOfEvent;
import com.zematix.jworldcup.backend.exception.ServiceException;

/**
 * Operations around {@link User} elements. 
 * Usually it injects at least its DAO class belongs to the same entities. 
 * It may also inject other service and DAO classes.
 */
@Service
@Transactional
public class UserOfEventService extends ServiceBase {

	@Inject 
	private UserOfEventDao userOfEventDao;
	
	@Inject
	private CommonDao commonDao;

	@Inject
	private CacheManager cacheManager;

	/**
	 * Retrieves {@link UserOfEvent} instance by its given eventId and userId or {@code null}
	 * unless found. Returned entity is detached from PU.
	 * 
	 * @param eventId
	 * @param userId
	 * @return found {@link UserOfEvent} detached object or {@code null}
	 */
	@Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
	@Cacheable(cacheNames = CachingConfig.CACHE_USER_OF_EVENT, key = "{#eventId, #userId}")
	public UserOfEvent retrieveUserOfEvent(Long eventId, Long userId) throws ServiceException {
		checkNotNull(eventId);
		checkNotNull(userId);
		
		UserOfEvent userOfEvent = userOfEventDao.retrieveUserOfEvent(eventId, userId);
		if (userOfEvent == null) {
			return null;
		}
		
		// load lazy associations
		userOfEvent.getEvent().getEventId();
		userOfEvent.getUser().getUserId();
		if (userOfEvent.getFavouriteGroupTeam() != null) {
//			userOfEvent.getFavouriteGroupTeam().getTeamId(); // not enough, it does not fetch Team entity...
			userOfEvent.getFavouriteGroupTeam().getName(); // so we choose another field
			userOfEvent.getFavouriteGroupTeam().getGroup().getName();
		}
		if (userOfEvent.getFavouriteKnockoutTeam() != null) {
			userOfEvent.getFavouriteKnockoutTeam().getName();
			userOfEvent.getFavouriteKnockoutTeam().getGroup().getName();
		}
		
		commonDao.detachEntity(userOfEvent);
		return userOfEvent;
	}

	/**
	 * Saves given favourite teams of {@link UserOfEvent} instance by its given 
	 * userId and eventId. It creates a new database row or it just modifies that.
	 * 
	 * @param eventId
	 * @param userId
	 * @param favouriteGroupTeamId - favourite group team id
	 * @param favouriteKnockoutTeamId - favourite knockout team id 
	 * @return saved userOfEvent
	 */
	@CachePut(cacheNames = CachingConfig.CACHE_USER_OF_EVENT, key = "{#eventId, #userId}")
	public UserOfEvent saveUserOfEvent(Long eventId, Long userId, Long favouriteGroupTeamId, Long favouriteKnockoutTeamId) throws ServiceException {
		checkNotNull(userId);
		checkNotNull(eventId);
		
		UserOfEvent userOfEvent = userOfEventDao.retrieveUserOfEvent(eventId, userId);
		if (userOfEvent == null) {
			userOfEvent = new UserOfEvent();
			Event event = commonDao.findEntityById(Event.class, eventId);
			if (event == null) {
				throw new IllegalStateException(String.format("No \"Event\" entity belongs to \"eventId\"=%d in database.", eventId));
			}
			userOfEvent.setEvent(event);
			User user = commonDao.findEntityById(User.class, userId);
			if (user == null) {
				throw new IllegalStateException(String.format("No \"User\" entity belongs to \"userId\"=%d in database.", userId));
			}
			userOfEvent.setUser(user);
		}
		
		Team favouriteGroupTeam = null;
		if (favouriteGroupTeamId != null) {
			favouriteGroupTeam = commonDao.findEntityById(Team.class, favouriteGroupTeamId);
			if (favouriteGroupTeam == null) {
				throw new IllegalStateException(String.format("No \"Team\" entity belongs to \"favouriteGroupTeamId\"=%d, cannot be found in database.", favouriteGroupTeamId));
			}
		}
		Team favouriteKnockoutTeam = null;
		if (favouriteKnockoutTeamId != null) {
			favouriteKnockoutTeam = commonDao.findEntityById(Team.class, favouriteKnockoutTeamId);
			if (favouriteKnockoutTeam == null) {
				throw new IllegalStateException(String.format("No \"Team\" entity belongs to \"favouriteKnockoutTeamId\"=%d, cannot be found in database.", favouriteKnockoutTeamId));
			}
		}
		
		userOfEvent.setFavouriteGroupTeam(favouriteGroupTeam);
		userOfEvent.setFavouriteKnockoutTeam(favouriteKnockoutTeam);
		
		if (userOfEvent.getUserOfEventId() == null) {
			commonDao.persistEntity(userOfEvent);
		}
		
		commonDao.flushEntityManager();

		return userOfEvent;
	}

	/**
	 * Updates favouriteKnockoutTeams of all userOfEvent entity which belongs to given {@code eventId} and
	 * existing userOfEvent has the same favouriteGroupTeam than the given {@code teamId} and its favouriteKnockoutTeam
	 * is still null. 
	 * @param eventId
	 * @param teamId
	 */
	void prolongFavouriteTeamOfEventUsers(Long eventId, Long teamId) {
		checkNotNull(eventId);
		checkNotNull(teamId);
		
		List<UserOfEvent> userOfEvents = userOfEventDao.retrieveUserOfEventByEvent(eventId);
		userOfEvents.stream().forEach(e -> {
			if (e.getFavouriteGroupTeam() != null && teamId.equals(e.getFavouriteGroupTeam().getTeamId())
					&& e.getFavouriteKnockoutTeam() == null) {
				Team team = commonDao.findEntityById(Team.class, teamId);
				checkState(team != null, String.format("No \"Team\" entity belongs to \"teamsId\"=%d in database.", teamId));
				e.setFavouriteKnockoutTeam(team); // update knockout team in UserOfEvent/database
				// invalidate dependent cache(s)
				Cache cache = cacheManager.getCache(CachingConfig.CACHE_USER_OF_EVENT);
				if (cache != null) {
					cache.evictIfPresent(SimpleKeyGenerator.generateKey(eventId, e.getUser().getUserId()));
				}
			}
		});
	}
}

