package com.zematix.jworldcup.backend.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.HashSet;
import java.util.List;

import javax.inject.Inject;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import com.zematix.jworldcup.backend.entity.Event;

/**
 * Contains test functions of {@link EventDao} class.
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles({"development", "test"})
@Transactional
public class EventDaoTest {

	@Inject
	private EventDao eventDao;

	@Inject
	private CommonDao commonDao;

	/**
	 * Test {@link EventDao#findAllEvents()} method.
	 * Scenario: successfully retrieves a list of all Event entities
	 */
	@Test
	public void /*List<Event>*/ findAllEvents() {
		List<Event> allExpectedEvents = commonDao.findAllEntities(Event.class);
		List<Event> allEvents = eventDao.findAllEvents();
		
		// order does not matter
		assertEquals(new HashSet<>(allExpectedEvents), new HashSet<>(allEvents));
	}
	
	/**
	 * Test {@link EventDao#findLastEvent()} method.
	 * Scenario: successfully retrieves the last event in time
	 */
	@Test
	public void /*Event*/ findLastEvent() {
		Event expectedLastEvent = commonDao.findEntityById(Event.class, 7L);
		Event lastEvent = eventDao.findLastEvent();
		
		assertEquals(expectedLastEvent, lastEvent);
	}
	
	/**
	 * Test {@link EventDao#findEventByShortDescWithYear(String)} method.
	 * Scenario: successfully retrieves the proper {@link Event} instance.
	 */
	@Test
	public void /*Event*/ findEventByShortDescWithYear(/*String shortDescWithYear*/) {
		Event expectedEvent = commonDao.findEntityById(Event.class, 2L); // EC2016
		String shortDescWithYear = expectedEvent.getShortDescWithYear();
		Event event = eventDao.findEventByShortDescWithYear(shortDescWithYear);
		
		assertEquals(expectedEvent, event);
	}

	/**
	 * Test {@link EventDao#findEventByShortDescWithYear(String)} method.
	 * Scenario: unsuccessfully retrieves {@link Event} instance because the given parameter is null.
	 */
	@Test(expected = IllegalArgumentException.class)
	public void /*Event*/ findEventByShortDescWithYearNull(/*String shortDescWithYear*/) {
		String shortDescWithYear = null;
		
		/*Event event =*/ eventDao.findEventByShortDescWithYear(shortDescWithYear);
	}

	/**
	 * Test {@link EventDao#findEventByShortDescWithYear(String)} method.
	 * Scenario: unsuccessfully retrieves {@link Event} instance because the given parameter is too short.
	 */
	@Test(expected = IllegalArgumentException.class)
	public void /*Event*/ findEventByShortDescWithYearShort(/*String shortDescWithYear*/) {
		String shortDescWithYear = "2015";
		
		/*Event event =*/ eventDao.findEventByShortDescWithYear(shortDescWithYear);
	}

	/**
	 * Test {@link EventDao#findEventByShortDescWithYear(String)} method.
	 * Scenario: unsuccessfully retrieves {@link Event} instance because it does not exists.
	 */
	@Test
	public void /*Event*/ findEventByShortDescWithYearUnknown(/*String shortDescWithYear*/) {
		String shortDescWithYear = "WC2015"; // non existing
		Event event = eventDao.findEventByShortDescWithYear(shortDescWithYear);
		
		assertNull(event);
	}
	
	/**
	 * Test {@link EventDao#getStartTime(Long)} method.
	 * Scenario: successfully retrieves {@link Date} instance of the given {@link Event}.
	 */
	@Test
	public void /*Date*/ getStartTime(/*Long eventId*/) {
		Long eventId = 2L; // EC2016
		LocalDateTime expectedStartTime = LocalDateTime.parse("2016-06-10 19:00", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
		LocalDateTime startTime = eventDao.getStartTime(eventId);
		
		assertEquals(expectedStartTime, startTime);
	}

	/**
	 * Test {@link EventDao#getStartTime(Long)} method.
	 * Scenario: unsuccessfully retrieves {@link Date} instance of the given null parameter,
	 *           instead it throws exception.
	 */
	@Test(expected = IllegalArgumentException.class)
	public void /*Date*/ getStartTimeNull(/*Long eventId*/) {
		Long eventId = null;
		
		/*Date startTime =*/ eventDao.getStartTime(eventId);
	}

	/**
	 * Test {@link EventDao#getStartTime(Long)} method.
	 * Scenario: successfully retrieves {@code null} because the given eventId is unknown.
	 */
	@Test
	public void /*Date*/ getStartTimeUnknown(/*Long eventId*/) {
		Long eventId = -1L; // non existing
		LocalDateTime startTime = eventDao.getStartTime(eventId);
		
		assertNull(startTime);
	}

	/**
	 * Test {@link EventDao#getEndTime(Long)} method.
	 * Scenario: successfully retrieves {@link Date} instance of the given {@link Event}.
	 */
	@Test
	public void /*Date*/ getEndTime(/*Long eventId*/) {
		Long eventId = 2L; // EC2016
		LocalDateTime expectedEndTime = LocalDateTime.parse("2016-07-10 19:00", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
		LocalDateTime endTime = eventDao.getEndTime(eventId);
		
		assertEquals(expectedEndTime, endTime);
	}

	/**
	 * Test {@link EventDao#getEndTime(Long)} method.
	 * Scenario: unsuccessfully retrieves {@link Date} instance of the given null parameter,
	 *           instead it throws exception.
	 */
	@Test(expected = IllegalArgumentException.class)
	public void /*Date*/ getEndTimeNull(/*Long eventId*/) {
		Long eventId = null;
		
		/*Date endTime =*/ eventDao.getEndTime(eventId);
	}

	/**
	 * Test {@link EventDao#getEndTime(Long)} method.
	 * Scenario: successfully retrieves {@code null} because the given eventId is unknown.
	 */
	@Test
	public void /*Date*/ getEndTimeUnknown(/*Long eventId*/) {
		Long eventId = -1L; // non existing
		LocalDateTime endTime = eventDao.getEndTime(eventId);
		
		assertNull(endTime);
	}

	/**
	 * Test {@link EventDao#getKnockoutStartTime(Long)} method.
	 * Scenario: successfully retrieves {@link Date} instance of the given {@link Event}.
	 */
	@Test
	public void /*Date*/ getKnockoutStartTime(/*Long eventId*/) {
		Long eventId = 2L; // EC2016
		LocalDateTime expectedKnockoutStartTime = LocalDateTime.parse("2016-06-25 13:00", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
		LocalDateTime knockoutStartTime = eventDao.getKnockoutStartTime(eventId);
		
		assertEquals(expectedKnockoutStartTime, knockoutStartTime);
	}

	/**
	 * Test {@link EventDao#getKnockoutStartTime(Long)} method.
	 * Scenario: unsuccessfully retrieves {@link Date} instance of the given null parameter,
	 *           instead it throws exception.
	 */
	@Test(expected = IllegalArgumentException.class)
	public void /*Date*/ getKnockoutStartTimeNull(/*Long eventId*/) {
		Long eventId = null;
		
		/*Date knockoutStartTime =*/ eventDao.getKnockoutStartTime(eventId);
	}

	/**
	 * Test {@link EventDao#getKnockoutStartTime(Long)} method.
	 * Scenario: successfully retrieves {@code null} because the given eventId is unknown.
	 */
	@Test
	public void /*Date*/ getKnockoutStartTimeUnknown(/*Long eventId*/) {
		Long eventId = -1L; // non existing
		LocalDateTime knockoutStartTime = eventDao.getKnockoutStartTime(eventId);
		
		assertNull(knockoutStartTime);
	}
}
