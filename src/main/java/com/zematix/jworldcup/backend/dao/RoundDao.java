package com.zematix.jworldcup.backend.dao;

import java.util.List;

import javax.persistence.TypedQuery;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.querydsl.jpa.impl.JPAQuery;
import com.zematix.jworldcup.backend.entity.Event;
import com.zematix.jworldcup.backend.entity.QRole;
import com.zematix.jworldcup.backend.entity.QRound;
import com.zematix.jworldcup.backend.entity.Role;
import com.zematix.jworldcup.backend.entity.Round;

/**
 * Database operations around {@link Role} entities.
 */
@Component
@Transactional
public class RoundDao extends DaoBase {
	
	/**
	 * @return list of all Role entities from database
	 */
	@Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
	public List<Round> getAllRounds() {
		TypedQuery<Round> query = getEntityManager().createNamedQuery("Round.findAll", Round.class);
		List<Round> roles = query.getResultList();
		return roles;
	}

	/**
	 * Returns a list of {@link Round} instances belongs to the given {@code eventId} parameter.
	 * The result list is ordered by {@link Round#roundId}.
	 * 
	 * @param eventId
	 * @return list of {@link Round} instances belongs to the given {@link Event#eventId} parameter
	 * @throws IllegalArgumentException if any of the arguments is null 
	 */
	@Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
	public List<Round> retrieveRoundsByEvent(Long eventId) {
		List<Round> rounds;
		
		QRound qRound = QRound.round;
		JPAQuery<Round> query = new JPAQuery<>(getEntityManager());
		rounds = query.from(qRound)
			.where(qRound.event.eventId.eq(eventId))
			.orderBy(qRound.roundId.asc())
			.fetch();
		
		return rounds;
	}
	
	/**
	 * Return found {@link Role} instance which matches the given role string value. 
	 * Otherwise {@code null} is returned.
	 * 
	 * @param - sRole - searched role string
	 * @return found {@link Role} entity instance or {@code null} if not found 
	 */
	@Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
	public Role findRoleByRole(String sRole) {
		Role role = null;
		
		QRole qRole = QRole.role1;
		JPAQuery<Role> query = new JPAQuery<>(getEntityManager());
		role = query.from(qRole)
			.where(qRole.role.eq(sRole))
		  .fetchOne();
		
		return role;
	}
	
}
