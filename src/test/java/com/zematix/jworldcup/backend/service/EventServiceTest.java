package com.zematix.jworldcup.backend.service;

import static org.junit.Assert.assertEquals;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import com.zematix.jworldcup.backend.dao.CommonDao;
import com.zematix.jworldcup.backend.dao.EventDao;
import com.zematix.jworldcup.backend.entity.Event;
import com.zematix.jworldcup.backend.model.EventShortDescWithYearEnum;

/**
 * Contains test functions of {@link EventService} class.
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles({"develop", "test"})
@Transactional
public class EventServiceTest {
	
	@Inject
	private EventService eventService;
	
	@Inject
	private CommonDao commonDao;
	
	@MockBean
	private EventDao eventDao; // used by methods of EventService
	
	@SpyBean
	private EventService eventServicePartial; // partial mock
	
	@MockBean
	private ApplicationService applicationService;

	/**
	 * Test {@link EventService#findAllEvents()} method.
	 * Scenario: successfully retrieves all events
	 */
	@Test
	public void /*List<Event>*/findAllEvents() {
		Event event1 = commonDao.findEntityById(Event.class, 1L);
		Event event2 = commonDao.findEntityById(Event.class, 2L);
		List<Event> event12 = Arrays.asList(event1, event2);
		Mockito.when(eventDao.findAllEvents()).thenReturn(event12);
		
		LocalDateTime someDate = LocalDateTime.now();
		Mockito.when(eventDao.getStartTime(event1.getEventId())).thenReturn(someDate);
		Mockito.when(eventDao.getEndTime(event1.getEventId())).thenReturn(someDate);
		Mockito.when(eventDao.getStartTime(event2.getEventId())).thenReturn(someDate);
		Mockito.when(eventDao.getEndTime(event2.getEventId())).thenReturn(someDate);
		
		Mockito.doNothing().when(eventServicePartial).initEvent(ArgumentMatchers.any(Event.class));
		
		List<Event> events = eventServicePartial.findAllEvents();
		assertEquals("Result event list should be equal to the expected one.", event12, events);
	}
	
	/**
	 * Test {@link EventService#initEvent(Event)} method.
	 * Scenario: throws {@link NullPointerException} because of the given {@code event} parameter is {@code null}.
	 */
	@Test(expected=NullPointerException.class)
	public void initEvent_Null(/*Event event*/) {
		Event expectedEvent = null;
		eventService.initEvent(expectedEvent);
	}
	/**
	 * Test {@link EventService#initEvent(Event)} method.
	 * Scenario: successfully initializes the given {@code event} object
	 */
	@Test
	public void initEvent(/*Event event*/) {
		Event event = commonDao.findEntityById(Event.class, 1L); // WC2014
		
		LocalDateTime someDate = LocalDateTime.now();
		Mockito.when(eventDao.getStartTime(event.getEventId())).thenReturn(someDate);
		Mockito.when(eventDao.getEndTime(event.getEventId())).thenReturn(someDate);

		eventService.initEvent(event);
		
		// check that mocked methods were really called
		Mockito.verify(eventDao).getStartTime(event.getEventId());
		Mockito.verify(eventDao).getEndTime(event.getEventId());
	}

	/**
	 * Test {@link EventService#findEventByEventId(Long)} method.
	 * Scenario: throws {@link NullPointerException} because of the given {@code eventId} parameter is {@code null}.
	 */
	@Test(expected=NullPointerException.class)
	public void /*Event*/ findEventByEventId_NullEventId(/*Long eventId*/) {
		Long eventId = null;
		eventService.findEventByEventId(eventId);
	}
	
	/**
	 * Test {@link EventService#findEventByEventId(Long)} method.
	 * Scenario: throws {@link IllegalStateException} because of the given 
	 *           {@code eventId} parameter does not exist in database
	 */
	@Test(expected=IllegalStateException.class)
	public void /*Event*/ findEventByEventId_UnknownEventId(/*Long eventId*/) {
		Long eventId = -1L;
		eventService.findEventByEventId(eventId);
	}
	
	/**
	 * Test {@link EventService#findEventByEventId(Long)} method.
	 * Scenario: successfully retrieves the event
	 */
	@Test
	public void /*Event*/ findEventByEventId(/*Long eventId*/) {
		Long eventId = 1L; // WC2014
		Event expectedEvent = commonDao.findEntityById(Event.class, eventId);
		
		Mockito.doNothing().when(eventServicePartial).initEvent(expectedEvent);

		Event event = eventServicePartial.findEventByEventId(eventId);
		assertEquals("Retrieved event should be equal to the expected one.", expectedEvent, event);
	}
	
	/**
	 * Test {@link EventService#findLastEvent()} method.
	 * Scenario: successfully retrieves the last event
	 */
	@Test
	public void /*Event*/ findLastEvent() {
		Event expectedEvent = commonDao.findEntityById(Event.class, 1L);
		Mockito.when(eventDao.findLastEvent()).thenReturn(expectedEvent);
		
		Mockito.doNothing().when(eventServicePartial).initEvent(expectedEvent);

		Event event = eventServicePartial.findLastEvent();
		assertEquals("Result event should be equal to the expected one.", expectedEvent, event);
	}	

	/**
	 * Test {@link EventService#findEventByShortDescWithYear(String)} method.
	 * Scenario: throws IllegalArgumentException because of the given {@code null} 
	 *           {@code shortDescWithYear} parameter.
	 */
	@Test(expected=IllegalArgumentException.class)
	public void /*Event*/ findEventByShortDescWithYear_Null(/*String shortDescWithYear*/) {
		String shortDescWithYear = null;
		//Mockito.when(eventDao.getEventByShortDescWithYear(shortDescWithYear)).thenThrow(new IllegalArgumentException());

		eventService.findEventByShortDescWithYear(shortDescWithYear);
	}	

	/**
	 * Test {@link EventService#findEventByShortDescWithYear(String)} method.
	 * Scenario: successfully retrieves the event
	 */
	@Test
	public void /*Event*/ findEventByShortDescWithYear(/*String shortDescWithYear*/) {
		String shortDescWithYear = EventShortDescWithYearEnum.WC2014.name();
		Event expectedEvent = commonDao.findEntityById(Event.class, 1L); // WC2014
		Mockito.when(eventDao.findEventByShortDescWithYear(shortDescWithYear)).thenReturn(expectedEvent);
		
		EventService eventServicePartial = Mockito.spy(eventService); // partial mock
		Mockito.doNothing().when(eventServicePartial).initEvent(expectedEvent);

		Event event = eventServicePartial.findEventByShortDescWithYear(shortDescWithYear);
		assertEquals("Result event should be equal to the expected one.", expectedEvent, event);
	}
	
	/**
	 * Test {@link EventService#findCompletedEvents()} method.
	 * Scenario: successfully retrieves the event list
	 */
	public void /*List<Event>*/ findCompletedEvents() {
	}
}
