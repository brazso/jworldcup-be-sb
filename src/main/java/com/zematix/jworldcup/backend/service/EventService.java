package com.zematix.jworldcup.backend.service;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

import java.util.List;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.base.Strings;
import com.zematix.jworldcup.backend.dao.CommonDao;
import com.zematix.jworldcup.backend.dao.EventDao;
import com.zematix.jworldcup.backend.entity.Event;

/**
 * Operations around {@link Event} elements. 
 * Usually it injects at least its DAO class belongs to the same entities. 
 * It may also inject other service and DAO classes.
 */
@Service
@Transactional
public class EventService extends ServiceBase {

	@Inject
	private EventDao eventDao;
	
	@Inject
	private CommonDao commonDao;
	
	@Inject
	private ApplicationService applicationService;

	/**
	 * Same as {@link EventDao#findAllEvents()} but it also loads all 
	 * transitive fields of {@link Event} instances.
	 * @return list of all Events entities
	 */
	@Transactional(readOnly = true)
	public List<Event> findAllEvents() {
		List<Event> events = eventDao.findAllEvents();
		events.stream().forEach(e -> initEvent(e));
		return events;
	}

	/**
	 * Loads transient fields of the given Event instance
	 * @throws {@link IllegalArgumentException} if the given {@code event} parameter is {@code null}
	 */
	@Transactional(readOnly = true)
	public void initEvent(Event event) {
		checkNotNull(event);
		event.setStartTime(eventDao.getStartTime(event.getEventId()));
		event.setEndTime(eventDao.getEndTime(event.getEventId()));
	}
	
	/**
	 * It loads the {@link Event} instance belongs to the given {@code eventId} parameter 
	 * moreover it also loads its all transitive fields
	 * @return an {@link Event} instance belongs to the given {@code eventId} parameter
	 */
	@Transactional(readOnly = true)
	public Event findEventByEventId(Long eventId) {
		checkNotNull(eventId);
		Event event = commonDao.findEntityById(Event.class, eventId);
		checkState(event != null, "No \"Event\" instance belongs to \"eventId\"=%d in database.", eventId);
		initEvent(event);
		return event;
	}
	
	/**
	 * Same as {@link EventDao#findLastEvent()} but it also loads all 
	 * transitive fields of the found {@link Event} instance
	 * @return found last {@link Event} instance
	 */
	@Transactional(readOnly = true)
	public Event findLastEvent() {
		Event event = eventDao.findLastEvent();
		initEvent(event);
		return event;
	}
	
	/**
	 * Same as {@link EventDao#findEventByShortDescWithYear(String)} 
	 * but it also loads all transitive fields of the found 
	 * {@link Event} instance.
	 * 
	 * @param shortDescWithYear - event short description concatenated by its year
	 * @return found {@link Event} instance
	 * @throws IllegalArgumentException if the given {@code shortDescWithYear} parameter is {@code null}
	 * 
	 */
	@Transactional(readOnly = true)
	public Event findEventByShortDescWithYear(String shortDescWithYear) {
		checkArgument(!Strings.isNullOrEmpty(shortDescWithYear), "Argument \"shortDescWithYear\" cannot be null nor empty.");

		Event event = eventDao.findEventByShortDescWithYear(shortDescWithYear);
		initEvent(event);
		return event;
	}
	
	/**
	 * Retrieves completed event and also loads all their transitive field.
	 * @return list of all completed Event entities
	 */
	@Transactional(readOnly = true)
	public List<Event> findCompletedEvents() {
		List<Event> events = eventDao.findAllEvents().stream()
				.filter(e -> applicationService.getEventCompletionPercentCache(e.getEventId()) == 100)
				.collect(Collectors.toList());
		events.stream().forEach(e -> initEvent(e));
		return events;
	}
}
