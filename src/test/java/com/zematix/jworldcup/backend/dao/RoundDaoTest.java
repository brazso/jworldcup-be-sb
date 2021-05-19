package com.zematix.jworldcup.backend.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;

import javax.inject.Inject;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.collect.Ordering;
import com.zematix.jworldcup.backend.entity.Event;
import com.zematix.jworldcup.backend.entity.Round;
import com.zematix.jworldcup.backend.model.EventShortDescWithYearEnum;

/**
 * Contains test functions of {@link RoundDao} class.
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles({"development", "test"})
@Transactional
public class RoundDaoTest {

	@Inject
	private RoundDao roundDao;

	@Inject
	private EventDao eventDao;

	/**
	 * Test {@link MatchDao#retrieveRoundsByEvent(Long)} method.
	 * Scenario: successfully retrieves a list of all {@link Round} entities belongs
	 *           to the {@link Event} specified by the given {@link Event#eventId}.
	 */
	@Test
	public void /*List<Round>*/ retrieveRoundsByEvent(/*Long eventId*/) {
		Event event = eventDao.findEventByShortDescWithYear(EventShortDescWithYearEnum.WC2014.name());
		Long eventId = event.getEventId();
		int EXPECTED_ROUNDS_SIZE = 8;
		
		List<Round> rounds = roundDao.retrieveRoundsByEvent(eventId);

		assertEquals(EXPECTED_ROUNDS_SIZE, rounds.size());

		boolean isAllRoundsBelongToEvent = rounds.stream().allMatch(m -> m.getEvent().getEventId().equals(eventId));
		assertTrue(isAllRoundsBelongToEvent);

		// check that the list is sorted
		Ordering<Round> byRoundIdOrdering = new Ordering<Round>() {
			public int compare(Round left, Round right) {
				return Long.compare(left.getRoundId(), right.getRoundId());
			}
		};
		assertTrue(byRoundIdOrdering.isOrdered(rounds));
	}

	/**
	 * Test {@link MatchDao#retrieveRoundsByEvent(Long)} method.
	 * Scenario: unsuccessfully retrieves a list of all {@link Round} entities because
	 *           of null parameter, it throws an exception.
	 */
	@Test(expected = IllegalArgumentException.class)
	public void /*List<Round>*/ retrieveRoundsByEventNull(/*Long eventId*/) {
		Long eventId = null;
		
		/*List<Round> rounds =*/ roundDao.retrieveRoundsByEvent(eventId);
	}

	/**
	 * Test {@link MatchDao#retrieveRoundsByEvent(Long)} method.
	 * Scenario: unsuccessfully retrieves a list of all {@link Round} entities because
	 *           of null parameter, it throws an exception.
	 */
	@Test
	public void /*List<Round>*/ retrieveRoundsByEventUnknown(/*Long eventId*/) {
		Long eventId = -1L;

		List<Round> rounds = roundDao.retrieveRoundsByEvent(eventId);
		assertTrue(rounds.isEmpty());
	}
}
