package com.zematix.jworldcup.backend.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;

import javax.inject.Inject;
import javax.persistence.PersistenceException;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import com.zematix.jworldcup.backend.entity.Event;
import com.zematix.jworldcup.backend.entity.Role;
import com.zematix.jworldcup.backend.entity.Team;
import com.zematix.jworldcup.backend.entity.User;
import com.zematix.jworldcup.backend.entity.UserOfEvent;
import com.zematix.jworldcup.backend.entity.UserStatus;
import com.zematix.jworldcup.backend.util.CommonUtil;

/**
 * Contains test functions of {@link UserDao} class. Although a lot of
 * methods of {@link UserDao} depends on methods of {@link CommonDao} or
 * other Dao classes used in the application, none of them is mocked  
 * on purpose. We give the unit(!) tests up for the sake of simplicity
 * in our Dao classes. 
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles({"development", "test"})
@Transactional
public class UserDaoTest {
	
	@Inject
	private UserDao userDao;

	@Inject
	private CommonDao commonDao;

	@Inject
	private RoleDao roleDao;

	/**
	 * Test {@link UserDao#getAllUsers()} method.
	 * Scenario: successfully retrieves a list of all User entities
	 */
	@Test
	public void /*List<User>*/ getAllUsers() {
		List<User> allExpectedUsers = commonDao.findAllEntities(User.class);
		List<User> allUsers = userDao.getAllUsers();

		// order does not matter
		assertEquals(new HashSet<>(allExpectedUsers), new HashSet<>(allUsers));
	}
	
	/**
	 * Test {@link UserDao#stripUser(User)} method.
	 * Scenario: runs the method on admin user and successfully strips it down
	 */
	@Test
	public void stripUser(/*User user*/) {
		User user = commonDao.findAllEntities(User.class).get(0);
		userDao.stripUser(user);
		assertTrue(user.getBets() == null && user.getRoles() == null 
				&& user.getUserGroups() == null && user.getOwnerUserGroups() == null);
		assertFalse("User instance must be detached.", commonDao.containsEntity(user));
	}

	/**
	 * Test {@link UserDao#stripUser(User)} method.
	 * Scenario: fails because of the given {@code null} user parameter
	 */
	@Test(expected = IllegalArgumentException.class)
	public void stripUserNull(/*User user*/) {
		User user = null;

		userDao.stripUser(user);
	}

	/**
	 * Test {@link UserDao#findUserByLoginName(String)} method. 
	 * Scenario: successfully retrieves user named admin.
	 */
	@Test
	public void /*User*/ findUserByLoginName(/*String loginName*/) {
		String loginName = "admin";
		User user = userDao.findUserByLoginName(loginName);
		assertTrue(user!=null && loginName.equals(user.getLoginName()));
	}
	
	/**
	 * Test {@link UserDao#findUserByLoginName(String)} method. 
	 * Scenario: unsuccessfully retrieves non existing user named admin2.
	 */
	@Test
	public void /*User*/ findUserByLoginNameUnknownUser(/*String loginName*/) {
		String loginName = "admin2";
		User user = userDao.findUserByLoginName(loginName);
		assertNull(user);
	}
	
	/**
	 * Test {@link UserDao#findUserByLoginName(String)} method. 
	 * Scenario: due to null input loginName, {@link IllegalArgumentException} must be thrown.
	 */
	@Test(expected = IllegalArgumentException.class)
	public void /*User*/ findUserByLoginNameNull(/*String loginName*/) {
		String loginName = null;
		
		/*User user =*/ userDao.findUserByLoginName(loginName);
	}
	
	/**
	 * Test {@link UserDao#findUserByLoginNameAndEncryptedLoginPassword(String, String)} method. 
	 * Scenario: successfully retrieves user named admin.
	 */
	@Test
	public void /*User*/ findUserByLoginNameAndEncryptedLoginPassword(/*String loginName, String encryptedLoginPassword*/) {
		String loginName = "admin";
		String encryptedLoginPassword = "87326fa89d072eb3f575e62350184dc70131fae2645728b278e2c683799a951327fa3bcb06ec7d045c7543edf1c79ab1dc1ec3bacf054e2c1eae39148b9a5c29";
		User user = userDao.findUserByLoginNameAndEncryptedLoginPassword(loginName, encryptedLoginPassword);
		assertTrue(user!=null && loginName.equals(user.getLoginName()));
	}

	/**
	 * Test {@link UserDao#findUserByLoginNameAndEncryptedLoginPassword(String, String)} method. 
	 * Scenario: unsuccessfully retrieves non existing user named admin2.
	 */
	@Test
	public void /*User*/ findUserByLoginNameAndEncryptedLoginPasswordUnknownUser(/*String loginName, String encryptedLoginPassword*/) {
		String loginName = "admin2";
		String encryptedLoginPassword = "somePassword";
		User user = userDao.findUserByLoginNameAndEncryptedLoginPassword(loginName, encryptedLoginPassword);
		assertNull(user);
	}

	/**
	 * Test {@link UserDao#findUserByLoginNameOrEmailAddress(String, String)} method. 
	 * Scenario: successfully retrieves user named admin.
	 */
	@Test
	public void /*User*/ findUserByLoginNameOrEmailAddress(/*String loginName, String emailAddr*/) {
		String loginName = "admin";
		String emailAddr = "worldcup@zematix.hu";
		User user = userDao.findUserByLoginNameOrEmailAddress(loginName, emailAddr);
		assertTrue(user!=null && loginName.equals(user.getLoginName()));
	}
	
	/**
	 * Test {@link UserDao#findUserByLoginNameOrEmailAddress(String, String)} method. 
	 * Scenario: due to null input loginName, {@link IllegalArgumentException} must be thrown.
	 */
	@Test(expected = IllegalArgumentException.class)
	public void /*User*/ findUserByLoginNameOrEmailAddressByLoginNameNull(/*String loginName, String emailAddr*/) {
		String loginName = null;
		String emailAddr = "worldcup@zematix.hu";

		/*User user =*/ userDao.findUserByLoginNameOrEmailAddress(loginName, emailAddr);
	}
	
	/**
	 * Test {@link UserDao#findUserByLoginNameOrEmailAddress(String, String)} method. 
	 * Scenario: due to null input loginName, {@link IllegalArgumentException} must be thrown.
	 */
	@Test(expected = IllegalArgumentException.class)
	public void /*User*/ findUserByLoginNameOrEmailAddressByEmailAddrNull(/*String loginName, String emailAddr*/) {
		String loginName = "admin";
		String emailAddr = null;

		/*User user =*/ userDao.findUserByLoginNameOrEmailAddress(loginName, emailAddr);
	}

	/**
	 * Test {@link UserDao#existUserByEmailAddrExceptUser(String, String)} method. 
	 * Scenario: successfully returns {@code false} because there is no other user with dummy@zematix.hu email address.
	 */
	@Test
	public void /*boolean*/ existUserByEmailAddrExceptUserTrue(/*String loginName, String emailAddr*/) {
		String loginName = "dummy";
		String emailAddr = "dummy@zematix.hu";
		
//		User user = new User();
//		user.setFullName("Dummy Kid");
//		user.setLoginName("dummy");
//		user.setLoginPassword("dummyPassword");
//		user.setEmailAddr("dummy@zematix.hu");
//		UserStatus userStatus = commonDao.findAllEntities(UserStatus.class).get(0);
//		user.setUserStatus(userStatus);
//		user.setToken("dummyToken");
//		user.setZoneId("CET");
//		user.setModificationTime(new Date());
//		commonDao.persistEntity(user);
		
		boolean isExist = userDao.existUserByEmailAddrExceptUser(loginName, emailAddr);
		assertFalse("Result should be false because there is no other user with the same email address.", 
				isExist);
	}

	/**
	 * Test {@link UserDao#existUserByEmailAddrExceptUser(String, String)} method. 
	 * Scenario: successfully returns {@code true} because there is other user with dummy@zematix.hu email address.
	 */
	@Test
	public void /*boolean*/ existUserByEmailAddrExceptUserFalse(/*String loginName, String emailAddr*/) {
		String loginName = "dummy";
		String emailAddr = "dummy@zematix.hu";
		String otherLoginName = "admin";

		// change email address of user named admin to the same
		User otherUser = userDao.findUserByLoginName(otherLoginName);
		otherUser.setEmailAddr(emailAddr);

		boolean isExist = userDao.existUserByEmailAddrExceptUser(loginName, emailAddr);
		assertTrue("Result should be true because there is other user with the same email address.", isExist);
	}

	/**
	 * Test {@link UserDao#existUserByEmailAddrExceptUser(String, String)} method. 
	 * Scenario: due to null input loginName, {@link IllegalArgumentException} must be thrown.
	 */
	@Test(expected = IllegalArgumentException.class)
	public void /*boolean*/ existUserByEmailAddrExceptUserByLoginNameNull(/*String loginName, String emailAddr*/) {
		String loginName = null;
		String emailAddr = "dummy@zematix.hu";

		/*boolean isExist =*/ userDao.existUserByEmailAddrExceptUser(loginName, emailAddr);
	}

	/**
	 * Test {@link UserDao#existUserByEmailAddrExceptUser(String, String)} method. 
	 * Scenario: due to null input loginName, {@link IllegalArgumentException} must be thrown.
	 */
	@Test(expected = IllegalArgumentException.class)
	public void /*boolean*/ existUserByEmailAddrExceptUserByLoginEmailAddr(/*String loginName, String emailAddr*/) {
		String loginName = "dummy";
		String emailAddr = null;

		/*boolean isExist =*/ userDao.existUserByEmailAddrExceptUser(loginName, emailAddr);
	}

	/**
	 * Test {@link UserDao#saveUser()} method. 
	 * Scenario: successfully saves of a user given by numerous login/personal data and a role.
	 */
	@Test
	public void saveUserSuccessful() {
		String loginName = "brazso";
		String loginPassword = "yell0wsubmarine";
		String fullName = "Zsolt Branyiczky";
		String emailAddr = "zbranyiczky@dummy987.com";
		String encryptedLoginPassword = CommonUtil.getEncryptedLoginPassword(loginName, loginPassword);
		String sRole = "USER";
		String sUserstatus = "CANDIDATE";
		String token = "Y54Fd1fjjegzB0yymkoz";
		String zoneId = "CET";
		LocalDateTime modificationTime = LocalDateTime.now();
		
		int expectedUsersSize = userDao.getAllUsers().size() + 1;
		
		User user = userDao.saveUser(loginName, encryptedLoginPassword, fullName, 
				emailAddr, sRole, sUserstatus, token, zoneId, modificationTime);
		assertNotNull(user.getUserId());
		
		List<User> users = userDao.getAllUsers();
		assertEquals(expectedUsersSize, users.size());
	}

	/**
	 * Test {@link UserDao#saveUser()} method. 
	 * Scenario: saving the same user twice causes unique constraint violation at 2nd persist.
	 *           Due to transaction handler no database change happens.
	 */
	@Test(expected = PersistenceException.class)
	public void saveUserUniqueConstraintViolation() {
		String loginName = "brazso";
		String loginPassword = "yell0wsubmarine";
		String fullName = "Zsolt Branyiczky";
		String emailAddr = "zbranyiczky@dummy987.com";
		String encryptedLoginPassword = CommonUtil.getEncryptedLoginPassword(loginName, loginPassword);
		String sRole = "USER";
		String sUserStatus = "CANDIDATE";
		String token = "Y54Fd1fjjegzB0yymkoz";
		String zoneId = "CET";
		LocalDateTime modificationTime = LocalDateTime.now();
		
		User user = userDao.saveUser(loginName, encryptedLoginPassword, fullName, 
				emailAddr, sRole, sUserStatus, token, zoneId, modificationTime);
		assertNotNull(user.getUserId());

		// expected behaviour: next saveUser throws PersistenceException
		userDao.saveUser(loginName, encryptedLoginPassword, fullName,
				emailAddr, sRole, sUserStatus, token, zoneId, modificationTime);
	}

	/**
	 * Test {@link UserDao#modifyUser()} method. 
	 * Scenario: successful modification of a user
	 */
	@Test
	public void modifyUserSuccessful() {
		String loginName = "admin";
		String loginPasswordNew = "admin_!!";
		String fullName = "Mr Zsolt Branyiczky";
		String emailNew = "zbranyiczky@dummy988.com";
		String zoneId = "CET";
		String encryptedNewLoginPassword = CommonUtil.getEncryptedLoginPassword(loginName, loginPasswordNew);
		LocalDateTime modificationTime = LocalDateTime.now();
		
		User user = userDao.findUserByLoginName(loginName);
		assertNotNull(user);
		
		User user2 = userDao.modifyUser(user, fullName, emailNew, encryptedNewLoginPassword, zoneId, modificationTime);
		assertNotNull(user2);
		assertEquals("Original and modified users must be equal", user, user2);
		assertTrue("Modified user must have the modified field values", 
				fullName.equals(user2.getFullName()) && emailNew.equals(user2.getEmailNew())
				&& encryptedNewLoginPassword.equals(user2.getLoginPassword()));
	}

	/**
	 * Test {@link UserDao#modifyUser()} method. 
	 * Scenario: unsuccessful modification of a user due detached given user parameter
	 */
	@Test(expected = IllegalArgumentException.class)
	public void modifyUserFailsWithUnmanagedUser() {
		String loginName = "admin";
		String loginPasswordNew = "admin_!!";
		String fullName = "Mr Zsolt Branyiczky";
		String emailNew = "zbranyiczky@dummy988.com";
		String zoneId = "CET";
		String encryptedNewLoginPassword = CommonUtil.getEncryptedLoginPassword(loginName, loginPasswordNew);
		LocalDateTime modificationTime = LocalDateTime.now();
		
		User user = userDao.findUserByLoginName(loginName);
		assertNotNull(user);
		commonDao.detachEntity(user); // make user unmanaged

		userDao.modifyUser(user, fullName, emailNew, encryptedNewLoginPassword, zoneId, modificationTime);
	}

	/**
	 * Test {@link UserDao#findUserByToken()} method. 
	 * Scenario: successful retrieval of a user named admin
	 */
	@Test
	public void findUserByTokenSuccessful() {
		String token = "IF2YCcPnNulH8UEEkAIP";
		
		User user = userDao.findUserByToken(token);
		assertNotNull(user);
	}

	/**
	 * Test {@link UserDao#findUserByToken()} method. 
	 * Scenario: unsuccessful retrieval of a user because of token unknown
	 */
	@Test
	public void findUserByTokenUnsuccessful() {
		String token = "dummy";
		
		User user = userDao.findUserByToken(token);
		assertNull(user);
	}

	/**
	 * Test {@link UserDao#findUserByToken()} method. 
	 * Scenario: unsuccessful retrieval of a user because of null input token
	 */
	@Test(expected = IllegalArgumentException.class)
	public void findUserByTokenNull() {
		String token = null;
		
		userDao.findUserByToken(token);
	}
	
	/**
	 * test {@link UserDao#modifyUserStatusToken(User, String)} method.
	 * Scenario: successfully saves admin user with LOCKED status
	 */
	@Test
	public /*User*/ void modifyUserStatusTokenSucccesful(/*User user, String status, Date modificationTime*/) {
		String loginName = "admin";
		User user = userDao.findUserByLoginName(loginName);
		assertNotNull(user);
		LocalDateTime modificationTime = LocalDateTime.now();

		String status = "LOCKED";
		userDao.modifyUserStatusToken(user, status, modificationTime);

		User user2 = userDao.findUserByLoginName(loginName);
		assertNotNull(user2);
		
		assertEquals(String.format("saved userStatus must be %s", status), status, user2.getUserStatus().getStatus()); 
	}

	/**
	 * Test {@link UserDao#modifyUserStatusToken(User, String)} method.
	 * Scenario: unsuccessfully saves admin user with mistyped LOCED status
	 */
	@Test(expected = IllegalArgumentException.class)
	public /*User*/ void modifyUserStatusTokenWithMistypedStatus(/*User user, String status*/) {
		String loginName = "admin";
		User user = userDao.findUserByLoginName(loginName);
		assertNotNull(user);
		LocalDateTime modificationTime = LocalDateTime.now();
		String status = "LOCED";

		userDao.modifyUserStatusToken(user, status, modificationTime);
	}

	/**
	 * Test {@link UserDao#modifyUserStatusToken(User, String)} method.
	 * Scenario: unsuccessfully saves null user with LOCKED status
	 */
	@Test(expected = IllegalArgumentException.class)
	public /*User*/ void modifyUserStatusTokenWithNullUser(/*User user, String status*/) {
		User user = null;
		String status = "LOCKED";
		LocalDateTime modificationTime = LocalDateTime.now();
		
		userDao.modifyUserStatusToken(user, status, modificationTime);
	}
	
	/**
	 * Test {@link UserDao#retrieveUserOfEvent(Long, Long)} method. 
	 * Scenario: successful retrieval of a {@link UserOfEvent} instance
	 */
	@Test
	public void /*UserOfEvent*/ retrieveUserOfEvent(/*Long eventId, Long userId*/) {
		Event event = commonDao.findAllEntities(Event.class).get(0);
		Long eventId = event.getEventId();
		User user = commonDao.findAllEntities(User.class).get(0);
		Long userId = user.getUserId();
		
		// new UserOfEvent instance must be created
		Team team = commonDao.findAllEntities(Team.class).get(0);
		UserOfEvent expectedUserOfEvent = new UserOfEvent();
		expectedUserOfEvent.setEvent(event);
		expectedUserOfEvent.setUser(user);
		expectedUserOfEvent.setFavouriteGroupTeam(team);
		expectedUserOfEvent.setFavouriteKnockoutTeam(team);
		commonDao.persistEntity(expectedUserOfEvent);
		
		UserOfEvent userOfEvent = userDao.retrieveUserOfEvent(eventId, userId);
		assertEquals(expectedUserOfEvent, userOfEvent);
	}

	/**
	 * Test {@link UserDao#retrieveUserOfEvent(Long, Long)} method. 
	 * Scenario: unsuccessful retrieval of a non existing {@link UserOfEvent} instance
	 */
	@Test
	public void /*UserOfEvent*/ retrieveUserOfEventUnsuccessful(/*Long eventId, Long userId*/) {
		Event event = commonDao.findAllEntities(Event.class).get(0);
		Long eventId = event.getEventId();
		User user = commonDao.findAllEntities(User.class).get(0);
		Long userId = user.getUserId();

		UserOfEvent userOfEvent = userDao.retrieveUserOfEvent(eventId, userId);
		assertNull(userOfEvent);
	}
	
	/**
	 * Test {@link UserDao#findUserByLoginNameOrFullName(String, String)} method.
	 * Scenario: successful retrieval of a user by its loginName
	 */
	@Test
	public void /*User*/ findUserByLoginNameOrFullNameLoginName(/*String loginName, String fullName*/) {
		String loginName = "admin";
		String fullName = null;
		
		User user = userDao.findUserByLoginNameOrFullName(loginName, fullName);
		assertNotNull(user);
	}

	/**
	 * Test {@link UserDao#findUserByLoginNameOrFullName(String, String)} method.
	 * Scenario: successful retrieval of a user by its fullName
	 */
	@Test
	public void /*User*/ findUserByLoginNameOrFullNameFullName(/*String loginName, String fullName*/) {
		String loginName = null;
		String fullName = "ADmInIsTrAtOr";
		
		User user = userDao.findUserByLoginNameOrFullName(loginName, fullName);
		assertNotNull(user);
	}
	
	/**
	 * Test {@link UserDao#findUserByLoginNameOrFullName(String, String)} method.
	 * Scenario: unsuccessful retrieval of a user because both input parameters are empty/null
	 */
	@Test(expected = IllegalArgumentException.class)
	public void /*User*/ findUserByLoginNameOrFullNameEmptyNull(/*String loginName, String fullName*/) {
		String loginName = "";
		String fullName = null;
		
		/*User user =*/ userDao.findUserByLoginNameOrFullName(loginName, fullName);
	}
	
	/**
	 * Test {@link UserDao#findUsersByLoginNamePrefix(String)} method.
	 * Scenario: successful retrieval of a list of users where each user matches the given prefix
	 */
	@Test
	public void /*List<User>*/ findUsersByLoginNamePrefix(/*String loginNamePrefix*/) {
		String loginNamePrefix = "aDm";
		List<User> users = userDao.findUsersByLoginNamePrefix(loginNamePrefix);
		assertTrue(users.size() == 1);				
	}

	/**
	 * Test {@link UserDao#findUsersByLoginNamePrefix(String)} method.
	 * Scenario: successful retrieval of a list of users where each user matches the given prefix
	 */
	@Test
	public void /*List<User>*/ findUsersByLoginNamePrefixDouble(/*String loginNamePrefix*/) {
		String loginNamePrefix = "aDm";

		// create a new dummy user with admDummy loginName
		User user = new User();
		user.setFullName("Dummy Kid");
		user.setLoginName("admDummy");
		user.setLoginPassword("dummyPassword");
		user.setEmailAddr("dummy@zematix.hu");
		UserStatus userStatus = commonDao.findAllEntities(UserStatus.class).get(0);
		user.setUserStatus(userStatus);
		user.setToken("dummyToken");
		user.setZoneId("CET");
		user.setModificationTime(LocalDateTime.now());
		commonDao.persistEntity(user);

		List<User> users = userDao.findUsersByLoginNamePrefix(loginNamePrefix);
		assertEquals(2, users.size());				
	}

	/**
	 * Test {@link UserDao#findUsersByLoginNamePrefix(String)} method.
	 * Scenario: unsuccessful retrieval of a list of users because the given prefix is null
	 */
	@Test(expected = IllegalArgumentException.class)
	public void /*List<User>*/ findUsersByLoginNamePrefixNull(/*String loginNamePrefix*/) {
		String loginNamePrefix = null;

		/*List<User> users =*/ userDao.findUsersByLoginNamePrefix(loginNamePrefix);
	}
	
	/**
	 * Test {@link UserDao#findUsersByLoginNamePrefix(String)} method.
	 * Scenario: unsuccessful retrieval of a list of users because the given prefix is empty
	 */
	@Test(expected = IllegalArgumentException.class)
	public void /*List<User>*/ findUsersByLoginNamePrefixEmpty(/*String loginNamePrefix*/) {
		String loginNamePrefix = "";

		/*List<User> users =*/ userDao.findUsersByLoginNamePrefix(loginNamePrefix);
	}
	
	/**
	 * Test {@link UserDao#findUsersByLoginNamePrefix(String)} method.
	 * Scenario: successful retrieval of a list of users where each user matches the given prefix
	 */
	@Test
	public void /*List<User>*/ findUsersByFullNameContain(/*String fullNameContain*/) {
		String fullNameContain = "mInIsT";
		List<User> users = userDao.findUsersByFullNameContain(fullNameContain);
		assertEquals(1, users.size());				
	}

	/**
	 * Test {@link UserDao#findUsersByLoginNamePrefix(String)} method.
	 * Scenario: unsuccessful retrieval of a list of users because given fullNameContain is null
	 */
	@Test(expected = IllegalArgumentException.class)
	public void /*List<User>*/ findUsersByFullNameContainNull(/*String fullNameContain*/) {
		String fullNameContain = null;

		/*List<User> users =*/ userDao.findUsersByFullNameContain(fullNameContain);
	}

	/**
	 * Test {@link UserDao#modifyUserEmailAddr(User)} method.
	 * Scenario: successful modification of the given user
	 */
	@Test
	public void /*boolean*/ modifyUserEmailAddr(/*User user*/) {
		String loginName = "dummy";
		String emailNew = "dummyNew@zematix.hu";
		LocalDateTime modificationTime = LocalDateTime.now();
		
		// create a new dummy user
		User user = new User();
		user.setFullName("Dummy Kid");
		user.setLoginName(loginName);
		user.setLoginPassword("dummyPassword");
		user.setEmailAddr("dummyActual@zematix.hu");
		user.setEmailNew(emailNew);
		UserStatus userStatus = commonDao.findAllEntities(UserStatus.class).get(0);
		user.setUserStatus(userStatus);
		user.setToken("dummyToken");
		user.setZoneId("CET");
		user.setModificationTime(modificationTime);
		user.setRoles(new HashSet<Role>());
		Role userRole = roleDao.findRoleByRole("USER");
		user.getRoles().add(userRole);
		userRole.getUsers().add(user);
		commonDao.persistEntity(user);
		
		boolean isModified = userDao.modifyUserEmailAddr(user, modificationTime);
		assertTrue(isModified);
		
		User modifiedUser = userDao.findUserByLoginName(loginName);
		assertNull("Modified user's field named emailNew should contain null value.", modifiedUser.getEmailNew());
		assertEquals("Modified user's field named emailAddr should contain the same value as original's emailNew", 
				emailNew, modifiedUser.getEmailAddr());
	}

	/**
	 * Test {@link UserDao#modifyUserEmailAddr(User)} method.
	 * Scenario: unsuccessful modification of the given user
	 */
	@Test(expected = IllegalArgumentException.class)
	public void /*boolean*/ modifyUserEmailAddrNull(/*User user*/) {
		User user = null;
		LocalDateTime modificationTime = LocalDateTime.now();
		
		/*boolean isModified =*/ userDao.modifyUserEmailAddr(user, modificationTime);
	}

	/**
	 * Test {@link UserDao#modifyUserEmailAddr(User)} method.
	 * Scenario: successful modification of the given user because her emailNew field value is null
	 */
	@Test
	public void /*boolean*/ modifyUserEmailAddrEmailNewNull(/*User user*/) {
		User user = commonDao.findAllEntities(User.class).get(0);
		LocalDateTime modificationTime = LocalDateTime.now();
		
		boolean isModified = userDao.modifyUserEmailAddr(user, modificationTime);
		assertFalse(isModified);
	}

	/**
	 * Test {@link UserDao#findFirstAdminUser()} method.
	 * Scenario: throws {@link IllegalStateException} because there is no user with ADMIN role
	 *           in the database.
	 */
	@Test(expected = IllegalStateException.class)
	public void /*boolean*/ findFirstAdminUser_DeletedAdmin(/*User user*/) {
		User expectedUser = commonDao.findEntityById(User.class, 1L); // admin
		
		// remove ADMIN role from database
		Role userRole = roleDao.findRoleByRole("ADMIN");
		userRole.getUsers().remove(expectedUser);
		expectedUser.getRoles().remove(userRole);
		commonDao.removeEntity(userRole);
		
		/*User user =*/ userDao.findFirstAdminUser();
	}

	/**
	 * Test {@link UserDao#findFirstAdminUser()} method.
	 * Scenario: successful retrieves an admin user
	 */
	@Test
	public void /*boolean*/ findFirstAdminUser(/*User user*/) {
		User expectedUser = commonDao.findEntityById(User.class, 1L); // admin
		
		User user = userDao.findFirstAdminUser();
		assertEquals(expectedUser, user);
	}
}