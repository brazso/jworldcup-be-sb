package com.zematix.jworldcup.backend.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.List;

import javax.inject.Inject;
import javax.persistence.PersistenceException;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.zematix.jworldcup.backend.entity.Event;
import com.zematix.jworldcup.backend.entity.Role;
import com.zematix.jworldcup.backend.entity.User;
import com.zematix.jworldcup.backend.entity.WebService;

/**
 * Contains test functions mostly of {@link CommonDao} class.
 * 
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles({"develop", "test"})
@Transactional
public class CommonDaoTest {

	@Inject
	private Logger logger;
	
	/**
	 * {@link CommonDao} instance on {@link PersistenceUnitNameEnum.JWORLDCUP}.
	 * Since default persistence unit is jworldcup, {@link @Dao} annotation is unnecessary. 
	 */
	@Inject
	private CommonDao commonDao;

	@Inject
	private CommonDaoNestedTest commonDaoNestedTest;

	/**
	 * Test {@link CommonDao#persistEntity(Object)} method.
	 * Scenario: successful insertion of a new entity instance.
	 */
	@Test
	public void persistEntity() {
		int eventsSizeBefore = commonDao.findAllEntities(Event.class).size();
		
		Event event = new Event();
		event.setLocation("Argentina");
		event.setYear((short) 1978);
		event.setDescription("World Cup");
		event.setShortDesc("WC");
		event.setOrganizer("FIFA");
		commonDao.persistEntity(event);
		logger.info("eventId=" + event.getEventId());
		assertNotNull(event.getEventId());

		int eventsSizeAfter = commonDao.findAllEntities(Event.class).size();
		assertEquals(1, eventsSizeAfter - eventsSizeBefore);
	}

	/**
	 * Test {@link CommonDao#persistEntity(Object)} method.
	 * Scenario: unsuccessful insertion of a new entity instance because of not null constraint violation.
	 */
	@Test(expected = PersistenceException.class)
	public void persistEntityNotNull() {
		Event event = new Event();
		event.setLocation(/* "Russia" */ null); // causes not null constraint violation later
		event.setYear((short) 2018);
		event.setDescription("World Cup");
		event.setShortDesc("WC");
		event.setOrganizer("FIFA");
		
		commonDao.persistEntity(event);
		fail("Should not be persisted because of not null constraint violation.");
	}

	/**
	 * Test {@link CommonDao#persistEntity(Object)} method.
	 * Scenario: unsuccessful insertion of a new entity instance because of not null constraint violation.
	 */
	@Test
	@Transactional(propagation = Propagation.NEVER)
	public void persistEntityRequiresNew() {
		int eventsSizeBefore = commonDao.findAllEntities(Event.class).size();
		
		// start a method which runs in transaction
		Long insertedEventId = commonDaoNestedTest.persistEntityRequiresNew();

		int eventsSizeAfter = commonDao.findAllEntities(Event.class).size();
		assertEquals(1, eventsSizeAfter -  eventsSizeBefore);

		// remove inserted event during new transaction because it will be not rolled back aster test
		Event insertedEvent = commonDao.findEntityById(Event.class, insertedEventId);
		commonDao.removeEntity(insertedEvent);
	}

	/**
	 * Test {@link CommonDao#mergeEntity(Object)} method.
	 * Scenario: successfully manages an entity instance
	 */
	@Test
	public void /*<ENTITYCLASS> ENTITYCLASS*/ mergeEntity(/*ENTITYCLASS entity*/) {
		String newFullName = "New Administrator";
		User user = commonDao.findEntityById(User.class, 1L);
		commonDao.detachEntity(user);
		user.setFullName(newFullName);
		
		User mergedUser = commonDao.mergeEntity(user);
		
		assertTrue("After merge the entity should be in the persistence context.", 
				commonDao.containsEntity(mergedUser));

		/*User*/ user = commonDao.findEntityById(User.class, 1L);
		assertEquals("After merge the User entity should have the modified fullname.", 
				newFullName, user.getFullName());
	}
	
	/**
	 * Test {@link CommonDao#removeEntity(Object)} method.
	 * Scenario: successful remove an entity instance
	 */
	@Test
	public void /*<ENTITYCLASS> void*/ removeEntity(/*ENTITYCLASS entity*/) {
		int webServicesSizeBefore = commonDao.findAllEntities(WebService.class).size();
		WebService webService = commonDao.findEntityById(WebService.class, 1L);

		commonDao.removeEntity(webService);
		
		int webServicesSizeAfter = commonDao.findAllEntities(WebService.class).size();
		assertEquals(1, webServicesSizeBefore - webServicesSizeAfter);
		
		webService = commonDao.findEntityById(WebService.class, 1L);
		assertNull(webService);
	}

	/**
	 * Test {@link CommonDao#removeEntity(Object)} method.
	 * Scenario: throws {@link PersistenceException} because of constraint violation
	 */
	@Test(expected = PersistenceException.class)
	public void /*<ENTITYCLASS> void*/ removeEntityForeginKey(/*ENTITYCLASS entity*/) {
		User user = commonDao.findEntityById(User.class, 1L);

		commonDao.removeEntity(user); // still works because db operation would run later
		commonDao.flushEntityManager();
	}

	/**
	 * Test {@link CommonDao#refreshEntity(Object)} method.
	 * Scenario: successfully refreshes the state of an entity instance
	 * This function should be run outside transaction (propagation = NOT_SUPPORTED) but 
	 * spring entity refresh throws IllegalArgumentException because of unmanaged entity.
	 * However using default REQUIRED propagation and at most READ_COMMITTED isolation level, 
	 * refresh entity can detect the modified fullName property changed by another transaction.
	 */
	@Test
	public /*<ENTITYCLASS>*/ void refreshEntity(/*ENTITYCLASS entity*/) {
		String newFullName = "New Administrator";
		User user = commonDao.findEntityById(User.class, 1L);
		String fullName = user.getFullName();
		
		// start a method which runs in transaction
		commonDaoNestedTest.modifyUserWithFullName(user.getUserId(), newFullName);
		assertEquals("Before refresh the User entity should contain the old fullName value.", 
				fullName, user.getFullName());

		commonDao.refreshEntity(user);
		assertEquals("After refresh the User entity should contain the new fullName value.", 
				newFullName, user.getFullName());
		
		// restore original value because it will be not rolled back at the end of the test (new transaction)
		commonDaoNestedTest.modifyUserWithFullName(user.getUserId(), fullName);
	}
	
	/**
	 * Test {@link CommonDao#refreshEntity(Object)} method.
	 * Scenario: unsuccessfully refreshes an entity instance with detached status
	 */
	@Test(expected = IllegalArgumentException.class)
	public /*<ENTITYCLASS>*/ void refreshEntityDetached(/*ENTITYCLASS entity*/) {
		User user = commonDao.findEntityById(User.class, 1L);
		commonDao.detachEntity(user);
		
		commonDao.refreshEntity(user); // should throw IllegalArgumentException if the entity is not managed
	}
	
	/**
	 * Test {@link CommonDao#refreshEntity(Object)} method.
	 * Scenario: unsuccessfully refreshes the given object because it belongs to not an entity.
	 */
	@Test(expected = IllegalArgumentException.class)
	public /*<ENTITYCLASS>*/ void refreshEntityNotEntity(/*ENTITYCLASS entity*/) {
		String s = "I'm not an entity";
		commonDao.refreshEntity(s); // should throw IllegalArgumentException if the object is not entity
	}
	
	/**
	 * Test {@link CommonDao#containsEntity(Object)} method.
	 * Scenario: successfully determines that the given entity is in the persistence context,
	 *           i.e. it is not detached.
	 */
	@Test
	public void /*<ENTITYCLASS> boolean*/ containsEntity(/*ENTITYCLASS entity*/) {
		User user = commonDao.findEntityById(User.class, 1L);

		assertTrue("Given entity should be in the persistence context, i.e. it should not be detached.", 
				commonDao.containsEntity(user));
	}

	/**
	 * Test {@link CommonDao#containsEntity(Object)} method.
	 * Scenario: successfully determines that the given entity is not in the persistence context,
	 *           i.e. it is detached.
	 */
	@Test
	public void /*<ENTITYCLASS> boolean*/ containsEntityDetached(/*ENTITYCLASS entity*/) {
		User user = commonDao.findEntityById(User.class, 1L);
		commonDao.detachEntity(user);
		
		assertFalse("Given entity should not be in the persistence context, i.e. it should be detached.", 
				commonDao.containsEntity(user));
	}

	/**
	 * Test {@link CommonDao#detachEntity(Object)} method.
	 * Scenario: successfully detaches the given entity from the persistence context.
	 */
	@Test
	public void /*<ENTITYCLASS> void*/ detachEntity(/*ENTITYCLASS entity*/) {
		User user = commonDao.findEntityById(User.class, 1L);
		// Before detaching the given entity it should be in the persistence context.
		assertTrue(commonDao.containsEntity(user));

		commonDao.detachEntity(user);
		
		assertFalse("After detaching the given entity it should not be in the persistence context, i.e. it should be detached.", 
				commonDao.containsEntity(user));
	}
	
	/**
	 * Test {@link CommonDao#detachEntity(Object)} method.
	 * Scenario: unsuccessfully detaches the given entity because it is already detached.
	 */
	@Test
	public void /*<ENTITYCLASS> void*/ detachEntityDetached(/*ENTITYCLASS entity*/) {
		User user = commonDao.findEntityById(User.class, 1L);

		commonDao.detachEntity(user);
		commonDao.detachEntity(user);

		assertFalse("Still after more detach operations the given entity should not be in the persistence context, i.e. it should be detached.", 
				commonDao.containsEntity(user));
	}

	/**
	 * Test {@link CommonDao#detachEntity(Object)} method.
	 * Scenario: unsuccessfully detaches the given object because it belongs to not an entity.
	 */
	@Test(expected = IllegalArgumentException.class)
	public /*<ENTITYCLASS>*/ void detachEntityNotEntity(/*ENTITYCLASS entity*/) {
		String s = "I'm not an entity";
		commonDao.detachEntity(s);
	}
	
	/**
	 * Test {@link CommonDao#flushEntityManager()} method.
	 * Scenario: successfully flushes the entity manager.
	 */
	@Test
	public void flushEntityManager() {
		String fullName = "newAdministrator";
		User user = commonDao.findEntityById(User.class, 1L);
		user.setFullName(fullName);

		// our field modification is lost due to the retrieval of the entity with refresh flag
		User modifiedUser = commonDao.findEntityById(User.class, 1L, true);
		assertNotEquals(fullName, modifiedUser.getFullName());

		// never mind, modifies the field again
		modifiedUser.setFullName(fullName);

		// but now uses the tested function which updates the database
		commonDao.flushEntityManager();

		// our field modification is now done
		User modifiedUser2 = commonDao.findEntityById(User.class, 1L, true);
		assertEquals(fullName, modifiedUser2.getFullName());
	}
	
	/**
	 * Test {@link CommonDao#findAllEntities(Class)} method.
	 * Scenario: successfully retrieves a list of all entities from a database table.
	 */
	@Test
	public void /*<ENTITYCLASS> List<ENTITYCLASS>*/ findAllEntities(/*Class<ENTITYCLASS> entityClass*/) {
		List<Role> roles = commonDao.findAllEntities(Role.class);
		assertEquals(2, roles.size());
	}
	
	/**
	 * Test {@link CommonDao#findEntityById(Class, Object)} method.
	 * Scenario: successful retrieval of a user entity
	 */
	@Test
	public void /*<ENTITYCLASS> ENTITYCLASS*/ findEntityById(/*Class<ENTITYCLASS> entityClass, Object entityId*/) {
		String loginName = "admin";
		User user = commonDao.findEntityById(User.class, 1L);
		
		assertNotNull(user);
		assertEquals(loginName, user.getLoginName());
	}

	/**
	 * Test {@link CommonDao#findEntityById(Class, Object)} method.
	 * Scenario: unsuccessful retrieval of a user entity because of non existing given id argument
	 */
	@Test
	public void /*<ENTITYCLASS> ENTITYCLASS*/ findEntityByIdUnknownEntityId(/*Class<ENTITYCLASS> entityClass, Object entityId*/) {
		User user = commonDao.findEntityById(User.class, -1L);
		
		assertNull(user);
	}

	/**
	 * Test {@link CommonDao#findEntityById(Class, Object, boolean)} method.
	 * Scenario: successful retrieval of a user entity
	 */
	@Test
	@Transactional(propagation = Propagation.REQUIRED)
	public void /*<ENTITYCLASS> ENTITYCLASS*/ findEntityByIdRefresh(/*Class<ENTITYCLASS> entityClass, Object entityId, boolean refresh*/) {
		String fullName = "newAdministrator";
		User user = commonDao.findEntityById(User.class, 1L); // should work, covered by findEntityById test method
		user.setFullName(fullName);

		// our field modification is now done
		User modifiedUser2 = commonDao.findEntityById(User.class, 1L, false);
		assertEquals(fullName, modifiedUser2.getFullName());
		
		// our field modification is lost due to the retrieval of the entity with refresh flag
		User modifiedUser = commonDao.findEntityById(User.class, 1L, true);
		assertNotEquals(fullName, modifiedUser.getFullName());
	}
	
	/**
	 * Test {@link CommonDao#findEntityById(Class, Object, boolean)} method.
	 * Scenario: successful retrieval of the id value of an user entity
	 */
	@Test
	public void /*<ENTITYCLASS> Object*/ getEntityIdentifierUser(/*ENTITYCLASS entity*/) {
		User user = commonDao.findEntityById(User.class, 1L);
		Long expectedUserId = user.getUserId();
		Long userId = (Long)commonDao.getEntityIdentifier(user);
		assertEquals(expectedUserId, userId);
	}
	
	/**
	 * Test {@link CommonDao#findEntityById(Class, Object, boolean)} method.
	 * Scenario: successful retrieval of the name of the id field of the user entity
	 */
	@Test
	public void /*<ENTITYCLASS> String*/ getEntityIdentifierName(/*Class<ENTITYCLASS> entityClass*/) {
		String expectedEntityIdentifierName = "userId";
		String entityIdentifierName = commonDao.getEntityIdentifierName(User.class);
		assertEquals(expectedEntityIdentifierName, entityIdentifierName);
	}
	
	/**
	 * Test {@link CommonDao#findEntityById(Class, Object, boolean)} method.
	 * Scenario: successful retrieval of the type of the id field of the user entity
	 */
	@Test
	public void /*Class<?>*/ getEntityIdentifierType(/*Class<?> entityType*/) {
		String expectedUserIdTypeName = "java.lang.Long";
		Class<?> userIdType = commonDao.getEntityIdentifierType(User.class);
		
		assertEquals(expectedUserIdTypeName, userIdType.getName());
	}
}
