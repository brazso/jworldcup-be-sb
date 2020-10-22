package com.zematix.jworldcup.backend.dao;

import static com.google.common.base.Preconditions.checkArgument;

import java.time.LocalDateTime;
import java.util.List;

import javax.persistence.TypedQuery;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.querydsl.jpa.impl.JPAQuery;
import com.zematix.jworldcup.backend.entity.Event;
import com.zematix.jworldcup.backend.entity.Match;
import com.zematix.jworldcup.backend.entity.QEvent;
import com.zematix.jworldcup.backend.entity.QMatch;

/**
 * Database operations around {@link Event} entities.
 */
@Component
@Transactional
public class EventDao extends DaoBase {

	/**
	 * Retrieves all {@link Event} entities from database.
	 * 
	 * @return list of all {@link Event} entities
	 */
	@Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
	public List<Event> findAllEvents() {
		TypedQuery<Event> query = getEntityManager().createNamedQuery("Event.findAll", Event.class);
		List<Event> events = query.getResultList();
		return events;
	}
	
	/**
	 * Retrieves the last {@link Event} instance ordered by its year. Returns
	 * {@code null} if not found.
	 *  
	 * @return found last {@link Event} instance or {@code null} if not found 
	 */
	@Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
	public Event findLastEvent() {
		Event event = null;
		
		QEvent qEvent = QEvent.event;
		JPAQuery<Event> query = new JPAQuery<>(getEntityManager());
		event = query.from(qEvent)
			.orderBy(qEvent.year.desc())
			.fetchFirst();
		
		return event;
	}
	
	/**
	 * Retrieves an {@link Event} instance belongs to the given 
	 * {@code shortDescWithYear} parameter.
	 * Returns {@code null} if not found.
	 * 
	 * @param shortDescWithYear - event short description concatenated by its year
	 * @return found {@link Event} instance or {@code null} if not found
	 * @throws IllegalArgumentException if any of the input parameters is invalid
	 */
	@Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
	public Event findEventByShortDescWithYear(String shortDescWithYear) {
		checkArgument(shortDescWithYear != null && shortDescWithYear.length()>4, 
				"Argument \"shortDescWithYear\" cannot be null and its length must be at least 4 characters.");
		String shortDesc = shortDescWithYear.substring(0, shortDescWithYear.length()-4);
		short year = Short.parseShort(shortDescWithYear.substring(shortDescWithYear.length()-4));
		
		Event event = null;
		
		QEvent qEvent = QEvent.event;
		JPAQuery<Event> query = new JPAQuery<>(getEntityManager());
		event = query.from(qEvent)
				.where(qEvent.shortDesc.eq(shortDesc), 
						qEvent.year.eq(year))
				.fetchOne();
		
		return event;
	}
	
	/**
	 * Retrieves start time of the {@link Event} belongs to the given {@code eventId} parameter.
	 * It means the start time of its first match.
	 * 
	 * @param eventId
	 * @return start time of the given event
	 * @throws IllegalArgumentException if the given {@code eventId} is null
	 */
	@Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
	public LocalDateTime getStartTime(Long eventId) {
		Match match = null;
		
		QMatch qMatch = QMatch.match;
		JPAQuery<Match> query = new JPAQuery<>(getEntityManager());
		match = query.from(qMatch)
				.where(qMatch.event.eventId.eq(eventId))
				.orderBy(qMatch.startTime.asc())
				.fetchFirst();
		
		return match!=null ? match.getStartTime() : null;
	}

	/**
	 * Retrieves end time of the {@link Event} belongs to the given {@code eventId} parameter.
	 * It means the start time of its last match.
	 * 
	 * @param eventId
	 * @return end time of the given event
	 * @throws IllegalArgumentException if the given eventId is null 
	 */
	@Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
	public LocalDateTime getEndTime(Long eventId) {
		Match match = null;
		
		QMatch qMatch = QMatch.match;
		JPAQuery<Match> query = new JPAQuery<>(getEntityManager());
		match = query.from(qMatch)
				.where(qMatch.event.eventId.eq(eventId))
				.orderBy(qMatch.startTime.desc())
				.fetchFirst();
		
		return match!=null ? match.getStartTime() : null;
	}

	/**
	 * Retrieve start time of the knockout stage of the {@link Event}
	 * belongs to the given {@code eventId} parameter. 
	 * It means the start time of the first knockout match belongs to
	 * the given event.
	 * 
	 * @param eventId
	 * @return start time of the knockout stage of the given event
	 * @throws IllegalArgumentException if the given {@code eventId} is null
	 */
	@Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
	public LocalDateTime getKnockoutStartTime(Long eventId) {
		Match match = null;
		
		QMatch qMatch = QMatch.match;
		JPAQuery<Match> query = new JPAQuery<>(getEntityManager());
		match = query.from(qMatch)
				.where(qMatch.event.eventId.eq(eventId)
						.and(qMatch.round.isGroupmatch.eq((byte) 0)))
				.orderBy(qMatch.startTime.asc())
				.fetchFirst();
		
		return match!=null ? match.getStartTime() : null;
	}
}
