package com.zematix.jworldcup.backend.dao;

import java.util.List;

import javax.persistence.TypedQuery;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.querydsl.jpa.impl.JPAQuery;
import com.zematix.jworldcup.backend.entity.Event;
import com.zematix.jworldcup.backend.entity.QRound;
import com.zematix.jworldcup.backend.entity.Round;

/**
 * Database operations around {@link Round} entities.
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
		return query.getResultList();
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
		QRound qRound = QRound.round;
		JPAQuery<Round> query = new JPAQuery<>(getEntityManager());
		return query.from(qRound)
			.where(qRound.event.eventId.eq(eventId))
			.orderBy(qRound.roundId.asc())
			.fetch();
	}
	
}
