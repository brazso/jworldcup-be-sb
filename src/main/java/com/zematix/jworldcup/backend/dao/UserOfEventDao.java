package com.zematix.jworldcup.backend.dao;

import static com.google.common.base.Preconditions.checkArgument;

import java.util.List;

import javax.persistence.TypedQuery;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.querydsl.jpa.impl.JPADeleteClause;
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
		checkArgument(userId != null, "Argument \"userId\" cannot be null.");
		
		QUserOfEvent qUserOfEvent = QUserOfEvent.userOfEvent;
		new JPADeleteClause(getEntityManager(), qUserOfEvent)
				.where(qUserOfEvent.user.userId.eq(userId)).execute();
	}
}
