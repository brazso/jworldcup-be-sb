package com.zematix.jworldcup.backend.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import java.util.HashSet;
import java.util.List;

import javax.inject.Inject;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import com.zematix.jworldcup.backend.entity.Event;
import com.zematix.jworldcup.backend.entity.User;
import com.zematix.jworldcup.backend.entity.UserOfEvent;

/**
 * Contains test functions of {@link UserStatusDao} class.
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles({"development", "test"})
@Transactional
public class UserOfEventDaoTest {

	@Inject
	private UserOfEventDao userOfEventDao;

	@Inject
	private CommonDao commonDao;

	/**
	 * Initialization before all test cases.
	 * Because UserOfEvent table is empty, we insert some elements for test purpose.
	 */
	@Before
	public void init() {
		// UserOfEvent table is empty, so we insert some test elements
		List<UserOfEvent> userOfEvents = commonDao.findAllEntities(UserOfEvent.class);
		if (!userOfEvents.isEmpty()) {
			throw new IllegalStateException("UserOfEvent entities should not exist in database.");
		}
		
		// inserting WC2014-admin
		UserOfEvent userOfEvent = new UserOfEvent();
		Event event = commonDao.findEntityById(Event.class, 1L); // WC2014
		User user = commonDao.findEntityById(User.class, 1L); // admin
		userOfEvent.setEvent(event);
		userOfEvent.setUser(user);
		commonDao.persistEntity(userOfEvent);
		
		// inserting WC2014-normal
		/*UserOfEvent*/ userOfEvent = new UserOfEvent();
		/*Event*/ event = commonDao.findEntityById(Event.class, 2L); // WC2014
		/*User*/ user = commonDao.findEntityById(User.class, 2L); // normal
		userOfEvent.setEvent(event);
		userOfEvent.setUser(user);
		commonDao.persistEntity(userOfEvent);
		
		// inserting EC2016-admin
		/*UserOfEvent*/ userOfEvent = new UserOfEvent();
		/*Event*/ event = commonDao.findEntityById(Event.class, 2L); // EC2016
		/*User*/ user = commonDao.findEntityById(User.class, 1L); // admin
		userOfEvent.setEvent(event);
		userOfEvent.setUser(user);
		commonDao.persistEntity(userOfEvent);

		commonDao.flushEntityManager();
	}
	
	/**
	 * Test {@link UserOfEventDao#getAllUserOfEvents()} method.
	 * Scenario: successfully retrieves a list of all {@link UserOfEvent} entities
	 */
	@Test
	public void /*List<UserOfEvent>*/ getAllUserOfEvents() {
		List<UserOfEvent> allExpectedUserOfEvents = commonDao.findAllEntities(UserOfEvent.class);
		List<UserOfEvent> allUserOfEvents = userOfEventDao.getAllUserOfEvents();
		
		// order does not matter
		assertEquals(new HashSet<>(allExpectedUserOfEvents), new HashSet<>(allUserOfEvents));
	}
	
	/**
	 * Test {@link UserOfEventDao#deleteUserOfEventsByUser(Long)} method.
	 * Scenario: successfully deletes elements of {@link UserOfEvent} belong to the given userId
	 */
	@Test
	public void deleteUserOfEventsByUser(/*Long userId*/) {
		Long userId = 1L; // admin
		userOfEventDao.deleteUserOfEventsByUser(userId);
		
		List<UserOfEvent> userOfEvents = commonDao.findAllEntities(UserOfEvent.class);
		assertEquals(1, userOfEvents.size());
		assertNotEquals("The remaining only UserOfEntity element should not belong to the given user.", 
				userId, userOfEvents.get(0).getUser().getUserId());
	}

	/**
	 * Test {@link UserOfEventDao#deleteUserOfEventsByUser(Long)} method.
	 * Scenario: Because the given userId is {@code null} {@link NullPointerException} is thrown.
	 */
	@Test(expected = NullPointerException.class)
	public void deleteUserOfEventsByUserNull(/*Long userId*/) {
		Long userId = null;

		userOfEventDao.deleteUserOfEventsByUser(userId);
	}

	/**
	 * Test {@link UserOfEventDao#deleteUserOfEventsByUser(Long)} method.
	 * Scenario: Because given userId is unknown it does not delete any row.
	 */
	@Test
	public void deleteUserOfEventsByUserUnknown(/*Long userId*/) {
		Long userId = -1L;
		
		List<UserOfEvent> userOfEventsExpected = commonDao.findAllEntities(UserOfEvent.class);
		userOfEventDao.deleteUserOfEventsByUser(userId);
		commonDao.flushEntityManager();
		
		List<UserOfEvent> userOfEvents = commonDao.findAllEntities(UserOfEvent.class);
		assertEquals(new HashSet<>(userOfEventsExpected), new HashSet<>(userOfEvents));
		
	}
}
