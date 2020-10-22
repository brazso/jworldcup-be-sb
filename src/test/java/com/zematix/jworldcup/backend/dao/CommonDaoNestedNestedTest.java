package com.zematix.jworldcup.backend.dao;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import javax.inject.Inject;
import javax.persistence.PersistenceException;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.zematix.jworldcup.backend.entity.Event;
import com.zematix.jworldcup.backend.entity.User;

/**
 * Helper class containing test methods called by {@link CommonDaoNestedTest} class
 */
@Component
@Transactional
public class CommonDaoNestedNestedTest {

	@Inject
	private CommonDao commonDao;

	/**
	 * Test {@link CommonDao#persistEntity(Object)} method.
	 * Scenario: unsuccessful insertion of 2 new entity instances because the
	 *           2nd one causes constraint violation
	 */
	@Transactional(propagation = Propagation.REQUIRES_NEW)	
	public void persistEntityRequiresNew() throws PersistenceException {
		Event event = new Event();
		event.setLocation("London");
		event.setYear((short) 2020);
		event.setDescription("Euro Cup");
		event.setShortDesc("EC");
		event.setOrganizer("UEFA");
		commonDao.persistEntity(event);
		assertNotNull(event.getEventId());

		/*Event*/ event = new Event();
		event.setLocation(/* "Unknown" */ null); //  // causes not null constraint violation later
		event.setYear((short) 2022);
		event.setDescription("World Cup");
		event.setShortDesc("WC");
		event.setOrganizer("FIFA");
		commonDao.persistEntity(event);
		fail("Should not be persisted because of not null constraint violation.");
	}

	/**
	 * Modifies the fullName property of a User entity belongs to admin in a new transaction.
	 */
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public void modifyUserWithFullName(Long userId, String fullName) {
		User user = commonDao.findEntityById(User.class, userId);
		assertNotNull(user);
		
		user.setFullName(fullName);
	}
}
