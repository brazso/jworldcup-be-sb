package com.zematix.jworldcup.backend.dao;

import static org.junit.Assert.fail;

import javax.inject.Inject;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.zematix.jworldcup.backend.entity.Event;

/**
 * Helper class containing test methods called by {@link CommonDaoTest} class
 */
@Component
@Transactional
public class CommonDaoNestedTest {

	@Inject
	private CommonDao commonDao;
	
	@Inject
	private CommonDaoNestedNestedTest commonDaoNestedNestedTest;

	/**
	 * Test {@link CommonDao#persistEntity(Object)} method.
	 * Scenario: successful insertion of a new entity instance and 
	 *           unsuccessful insertion of another one. The later does not bothers
	 *           the first one, because the later runs in another transaction.
	 */
	@Transactional(propagation = Propagation.REQUIRED)	
	public Long persistEntityRequiresNew() {
		Event event = new Event();
		event.setLocation("Argentina");
		event.setYear((short) 1978);
		event.setDescription("World Cup");
		event.setShortDesc("WC");
		event.setOrganizer("FIFA");
		commonDao.persistEntity(event);
		
		try {
			// start a method which runs in a new transaction
			commonDaoNestedNestedTest.persistEntityRequiresNew();
			fail("Should not be executed because of not null constraint violation.");
		}
		catch (Exception e) {
		}
		
		return event.getEventId();
	}

	/**
	 * Modifies the fullName property of a User entity belongs to admin in a new transaction.
	 */
	@Transactional(propagation = Propagation.REQUIRED)
	public void modifyUserWithFullName(Long userId, String fullName) {
		// start a method which runs in a new transaction
		commonDaoNestedNestedTest.modifyUserWithFullName(userId, fullName);
	}

}
