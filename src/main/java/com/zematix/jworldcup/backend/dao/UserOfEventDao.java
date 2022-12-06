package com.zematix.jworldcup.backend.dao;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.TypedQuery;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.querydsl.jpa.impl.JPADeleteClause;
import com.querydsl.jpa.impl.JPAQuery;
import com.zematix.jworldcup.backend.entity.Bet;
import com.zematix.jworldcup.backend.entity.QUserOfEvent;
import com.zematix.jworldcup.backend.entity.User;
import com.zematix.jworldcup.backend.entity.UserOfEvent;

/**
 * Database operations around {@link Bet} entities.
 */
@Component
@Transactional
public class UserOfEventDao extends DaoBase {

	/**
	 * Returns a list of all {@link UserOfEvent} entities from database.
	 * 
	 * @return list of all {@link UserOfEvent} entities
	 */
	@Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
	public List<UserOfEvent> getAllUserOfEvents() {
		TypedQuery<UserOfEvent> query = getEntityManager().createNamedQuery("UserOfEvent.findAll", UserOfEvent.class);
		List<UserOfEvent> userOfEvents = query.getResultList();
		return userOfEvents;
	}
	
	/**
	 * Delete all userOfEvents of the given user.
	 * 
	 * @param userId - belongs to an {@link User} entity
	 * @throws IllegalArgumentException if any of the given parameters is {@code null}
	 */
	public void deleteUserOfEventsByUser(Long userId) {
		checkNotNull(userId);
		
		QUserOfEvent qUserOfEvent = QUserOfEvent.userOfEvent;
		new JPADeleteClause(getEntityManager(), qUserOfEvent)
				.where(qUserOfEvent.user.userId.eq(userId)).execute();
	}

	/**
	 * Returns found {@link UserOfEvent} instance which matches the given
	 * {@code userId} and {@code eventId}. Otherwise {@code null} is returned.
	 * 
	 * @param - eventId
	 * @param - userId
	 * @return found userOfEvent with the given {@code userId} and {@code eventId}
	 * @throws IllegalArgumentException if any of the given parameters is invalid
	 */
	@Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
	public UserOfEvent retrieveUserOfEvent(Long eventId, Long userId) {
		UserOfEvent userOfEvent = null;
		checkNotNull(eventId);
		checkNotNull(userId);

		QUserOfEvent qUserOfEvent = QUserOfEvent.userOfEvent;
		JPAQuery<UserOfEvent> query = new JPAQuery<>(getEntityManager());
		userOfEvent = query.from(qUserOfEvent)
				.where(qUserOfEvent.user.userId.eq(userId).and(qUserOfEvent.event.eventId.eq(eventId))).fetchOne();

		return userOfEvent;
	}	
	
	/**
	 * Returns found {@link UserOfEvent} instances which matches the given {@code eventId}. 
	 * Otherwise empty list is returned.
	 * 
	 * @param - eventId
	 * @return found userOfEvent with the given {@code eventId}
	 * @throws IllegalArgumentException if any of the given parameters is invalid
	 */
	@Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
	public List<UserOfEvent> retrieveUserOfEventByEvent(Long eventId) {
		List<UserOfEvent> userOfEvents = new ArrayList<>(0);
		checkNotNull(eventId);

		QUserOfEvent qUserOfEvent = QUserOfEvent.userOfEvent;
		JPAQuery<UserOfEvent> query = new JPAQuery<>(getEntityManager());
		userOfEvents = query.from(qUserOfEvent)
				.where(qUserOfEvent.event.eventId.eq(eventId)).fetch();

		return userOfEvents;
	}


}
