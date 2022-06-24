package com.zematix.jworldcup.backend.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import javax.inject.Inject;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import com.zematix.jworldcup.backend.entity.Bet;
import com.zematix.jworldcup.backend.entity.Event;
import com.zematix.jworldcup.backend.entity.Match;
import com.zematix.jworldcup.backend.entity.User;
import com.zematix.jworldcup.backend.entity.UserGroup;
import com.zematix.jworldcup.backend.entity.UserStatus;

/**
 * Contains test functions of {@link UserGroupDao} class.
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles({"development", "test"})
@Transactional
public class UserGroupDaoTest {
	
	@Inject
	private UserGroupDao userGroupDao;

	@Inject
	private CommonDao commonDao;

	/**
	 * Test {@link UserDao#getAllUserGroups()} method.
	 * Scenario: successfully retrieves a list of all {@link UserGroup} entities
	 */
	@Test
	public void /*List<UserGroup>*/ getAllUserGroups() {
		List<UserGroup> expectedUserGroups = commonDao.findAllEntities(UserGroup.class);
		List<UserGroup> userGroups = userGroupDao.getAllUserGroups();
		
		// order does not matter
		assertEquals(new HashSet<>(expectedUserGroups), new HashSet<>(userGroups));
	}
	
	/**
	 * Test {@link UserDao#createVirtualEverybodyUserGroup_NullEventId(Long, Long)} method.
	 * Scenario: throws {@link NullPointerException} because of the given {@code null} 
	 *           {@code eventId} parameter
	 */
	@Test(expected = NullPointerException.class)
	public void /*UserGroup*/ createVirtualEverybodyUserGroup_NullEventId(/*Long eventId, Long userId*/) {
		Long eventId = null;
		Long userId = 2L; // normal

		userGroupDao.createVirtualEverybodyUserGroup(eventId, userId);
	}
	
	/**
	 * Test {@link UserDao#createVirtualEverybodyUserGroup_NullEventId(Long, Long)} method.
	 * Scenario: throws {@link NullPointerException} because of the given {@code null} 
	 *           {@code userId} parameter
	 */
	@Test(expected = NullPointerException.class)
	public void /*UserGroup*/ createVirtualEverybodyUserGroup_NullUserId(/*Long eventId, Long userId*/) {
		Long eventId = 1L; // WC2014
		Long userId = null;
		
		userGroupDao.createVirtualEverybodyUserGroup(eventId, userId);
	}
	
	/**
	 * Test {@link UserDao#createVirtualEverybodyUserGroup_NullEventId(Long, Long)} method.
	 * Scenario: throws {@link IllegalStateException} because there is no {@link Event} object 
	 *           belongs to the given {@code eventId} parameter in the database
	 */
	@Test(expected = IllegalStateException.class)
	public void /*UserGroup*/ createVirtualEverybodyUserGroup_UnknownEventId(/*Long eventId, Long userId*/) {
		Long eventId = -1L;
		Long userId = 2L; // normal

		userGroupDao.createVirtualEverybodyUserGroup(eventId, userId);
}
	
	/**
	 * Test {@link UserDao#createVirtualEverybodyUserGroup_NullEventId(Long, Long)} method.
	 * Scenario: throws {@link IllegalStateException} because there is no {@link User} object 
	 *           belongs to the given {@code userId} parameter in the database
	 */
	@Test(expected = IllegalStateException.class)
	public void /*UserGroup*/ createVirtualEverybodyUserGroup_UnknownUserId(/*Long eventId, Long userId*/) {
		Long eventId = 1L; // WC2014
		Long userId = -1L;

		userGroupDao.createVirtualEverybodyUserGroup(eventId, userId);
	}
	
	/**
	 * Test {@link UserDao#getAllUserGroups()} method.
	 * Scenario: successfully creates virtual Everybody userGroup
	 */
	@Test
	public void /*UserGroup*/ createVirtualEverybodyUserGroup(/*Long eventId, Long userId*/) {
		Long eventId = 1L; // WC2014
		Long userId = 2L; // normal
		UserGroup userGroup = userGroupDao.createVirtualEverybodyUserGroup(eventId, userId);
		assertNotNull(userGroup);
	}

	/**
	 * Test {@link UserDao#retrieveUserGroups(Long, Long)} method. 
	 * Scenario: successful retrieval of a list of more {@link UserGroup} instances
	 *           belongs to a new dummy user
	 */
	@Test
	public void /*List<UserGroup>*/ retrieveUserGroups(/*Long eventId, Long userId*/) {
		Long eventId = 1L; // WC2014
		Event event = commonDao.findEntityById(Event.class, eventId);
		User adminUser = commonDao.findEntityById(User.class, 1L); // admin
		
		List<UserGroup> expectedUserGroups = new ArrayList<>();

		// create a new dummy user
		User user = new User();
		user.setFullName("Dummy Kid");
		user.setLoginName("dummy");
		user.setLoginPassword("dummyPassword");
		user.setEmailAddr("dummy@zematix.hu");
		UserStatus userStatus = commonDao.findAllEntities(UserStatus.class).get(0);
		user.setUserStatus(userStatus);
		user.setToken("dummyToken");
		user.setZoneId("CET");
		user.setModificationTime(LocalDateTime.now());
		user.setUserGroups(new HashSet<UserGroup>());
		commonDao.persistEntity(user);
		Long userId = user.getUserId();
		
		// create a new dummyGroup user group with self owner and add our dummy user to it
		UserGroup userGroup = new UserGroup();
		userGroup.setName("dummyGroup-withUser-owner");
		userGroup.setEvent(event);
		userGroup.setOwner(user);
		userGroup.setPublicEditableAsBoolean(false);
		userGroup.setPublicVisibleAsBoolean(true);
		userGroup.getUsers().add(user);
		user.getUserGroups().add(userGroup);
		commonDao.persistEntity(userGroup);
		expectedUserGroups.add(userGroup);

		// create a new dummyGroup2 user group with other owner and add our dummy user to it
		/*UserGroup*/ userGroup = new UserGroup();
		userGroup.setName("dummyGroup2-withUser-otherOwner");
		userGroup.setEvent(event);
		userGroup.setOwner(adminUser);
		userGroup.setPublicEditableAsBoolean(false);
		userGroup.setPublicVisibleAsBoolean(true);
		userGroup.getUsers().add(user);
		user.getUserGroups().add(userGroup);
		commonDao.persistEntity(userGroup);
		expectedUserGroups.add(userGroup);
		
		// create a new dummyGroup3 user group with self owner owner but without adding user to it
		/*UserGroup*/ userGroup = new UserGroup();
		userGroup.setName("dummyGroup3-withoutUser-owner");
		userGroup.setEvent(event);
		userGroup.setOwner(user);
		userGroup.setPublicEditableAsBoolean(false);
		userGroup.setPublicVisibleAsBoolean(true);
		commonDao.persistEntity(userGroup);
		expectedUserGroups.add(userGroup);

		// create a new dummyGroup4 user group with other owner owner but without adding user to it
		/*UserGroup*/ userGroup = new UserGroup();
		userGroup.setName("dummyGroup3-withoutUser-otherOwner");
		userGroup.setEvent(event);
		userGroup.setOwner(adminUser);
		userGroup.setPublicEditableAsBoolean(false);
		userGroup.setPublicVisibleAsBoolean(true);
		commonDao.persistEntity(userGroup);
		//expectedUserGroups.add(userGroup); // must be in comment because it should not be retrieved

		List<UserGroup> userGroups = userGroupDao.retrieveUserGroups(eventId, userId);
		assertEquals(expectedUserGroups, userGroups);
	}
	
	/**
	 * Test {@link UserDao#retrieveUserGroups(Long, Long)} method. 
	 * Scenario: throws {@link NullPointerException} because of the given {@code null} eventId parameter
	 */
	@Test(expected = NullPointerException.class)
	public void /*List<UserGroup>*/ retrieveUserGroups_NullEventId(/*Long eventId, Long userId*/) {
		Long eventId = null;
		Long userId = 1L; // admin
		
		userGroupDao.retrieveUserGroups(eventId, userId);
	}
	
	/**
	 * Test {@link UserDao#retrieveUserGroups(Long, Long)} method. 
	 * Scenario: throws {@link NullPointerException} because of the given {@code null} userId parameter
	 */
	@Test(expected = NullPointerException.class)
	public void /*List<UserGroup>*/ retrieveUserGroups_NullUserId(/*Long eventId, Long userId*/) {
		Long eventId = 1L; // WC2014; 
		Long userId = null;
		
		userGroupDao.retrieveUserGroups(eventId, userId);
	}
	
	/**
	 * Test {@link UserDao#retrieveUserGroups(Long, Long)} method. 
	 * Scenario: throws {@link IllegalStateException} because there is no {@link User} object 
	 *           belongs to the given userId parameter in the database
	 */
	@Test(expected = IllegalStateException.class)
	public void /*List<UserGroup>*/ retrieveUserGroups_InvalidUserId(/*Long eventId, Long userId*/) {
		Long eventId = 1L; // WC2014; 
		Long userId = -1L;
		
		userGroupDao.retrieveUserGroups(eventId, userId);
	}
	
	/**
	 * Test {@link UserDao#retrieveUsersByUserGroup(Long)} method. 
	 * Scenario: successful retrieval of a list of a {@link User} instance
	 *           belongs to a new userGroup
	 */
	@Test
	public void /*List<User>*/ retrieveUsersByUserGroup_NewUserGroup(/*Long userGroupId*/) {
		Event event = commonDao.findEntityById(Event.class, 1L); // WC2014
		User user = commonDao.findEntityById(User.class, 2L); // normal
		
		// create a new dummyGroup userGroup and add normal user to it
		UserGroup userGroup = new UserGroup();
		userGroup.setName("dummyGroup");
		userGroup.setEvent(event);
		userGroup.setOwner(user);
		userGroup.setPublicEditableAsBoolean(false);
		userGroup.setPublicVisibleAsBoolean(true);
		userGroup.getUsers().add(user);
		user.setUserGroups(new HashSet<UserGroup>());
		user.getUserGroups().add(userGroup);
		commonDao.persistEntity(userGroup);
		Long userGroupId = userGroup.getUserGroupId();
		
		List<User> expectedUserList = Arrays.asList(user);
		List<User> userList = userGroupDao.retrieveUsersByUserGroup(userGroupId);
		assertEquals(expectedUserList, userList);
	}

	/**
	 * Test {@link UserDao#retrieveUsersByUserGroup(Long)} method.
	 * Scenario: throws {@link NullPointerException} because of the given {@code null}
	 *           {@code userGroupId} parameter
	 */
	@Test(expected = NullPointerException.class)
	public /*List<User>*/ void retrieveUsersByUserGroup_NullUserGroupId(/*Long userGroupId*/) {
		Long userGroupId = null;

		/*List<User> userList =*/ userGroupDao.retrieveUsersByUserGroup(userGroupId);
	}

	/**
	 * Test {@link UserDao#retrieveUsersByUserGroup(Long)} method.
	 * Scenario: throws {@link illegalArgumentException} because of the given 
	 *           {@code userGroupId} parameter does not exist in database
	 */
	@Test(expected = IllegalArgumentException.class)
	public /*List<User>*/ void retrieveUsersByUserGroup_UnknownUserGroupId(/*Long userGroupId*/) {
		Long userGroupId = -1L;
		
		/*List<User> userList =*/ userGroupDao.retrieveUsersByUserGroup(userGroupId);
	}

	/**
	 * Test {@link UserDao#retrieveUsersWithBetsByUserGroup(Long)} method. 
	 * Scenario: successful retrieval of a list of a {@link User} instance
	 *           belongs to a new userGroup
	 */
	@Test
	public void /*List<User>*/ retrieveUsersWithBetsByUserGroup_NewUserGroup(/*Long userGroupId, Long eventId*/) {
		Event event = commonDao.findEntityById(Event.class, 1L); // WC2014
		Long eventId = null; // not used in fact
		User user = commonDao.findEntityById(User.class, 2L); // normal
		
		// create a new dummyGroup userGroup and add normal user to it
		UserGroup userGroup = new UserGroup();
		userGroup.setName("dummyGroup");
		userGroup.setEvent(event);
		userGroup.setOwner(user);
		userGroup.setPublicEditableAsBoolean(false);
		userGroup.setPublicVisibleAsBoolean(true);
		userGroup.getUsers().add(user);
		user.setUserGroups(new HashSet<UserGroup>());
		user.getUserGroups().add(userGroup);
		commonDao.persistEntity(userGroup);
		Long userGroupId = userGroup.getUserGroupId();
		
		Bet bet = new Bet();
		bet.setEvent(event);
		Match match = commonDao.findEntityById(Match.class, 1L);
		bet.setMatch(match);
		match.getBets().add(bet);
		bet.setGoalNormalByTeam1((byte)1);
		bet.setGoalNormalByTeam2((byte)0);
		bet.setUser(user);
		user.getBets().add(bet);
		commonDao.persistEntity(bet);
		
		List<User> expectedUserList = Arrays.asList(user);
		List<User> userList = userGroupDao.retrieveUsersWithBetsByUserGroup(userGroupId, eventId);
		assertEquals(expectedUserList, userList);
	}

	/**
	 * Test {@link UserDao#retrieveUsersWithBetsByUserGroup(Long)} method. 
	 * Scenario: successful retrieval of a list of a {@link User} instance
	 *           belongs to virtual Everybody userGroup
	 */
	@Test
	public void /*List<User>*/ retrieveUsersWithBetsByUserGroup_EverbodyUserGroup(/*Long userGroupId, Long eventId*/) {
		Long userGroupId = UserGroup.EVERYBODY_USER_GROUP_ID;
		Long eventId = 1L; // WC2014
		
		Event event = commonDao.findEntityById(Event.class, eventId); // WC2014
		User user = commonDao.findEntityById(User.class, 2L); // normal
		
		Bet bet = new Bet();
		bet.setEvent(event);
		Match match = commonDao.findEntityById(Match.class, 1L);
		bet.setMatch(match);
		match.getBets().add(bet);
		bet.setGoalNormalByTeam1((byte)1);
		bet.setGoalNormalByTeam2((byte)0);
		bet.setUser(user);
		user.getBets().add(bet);
		commonDao.persistEntity(bet);
		
		List<User> expectedUserList = Arrays.asList(user);
		List<User> userList = userGroupDao.retrieveUsersWithBetsByUserGroup(userGroupId, eventId);
		assertEquals(expectedUserList, userList);
	}

	/**
	 * Test {@link UserDao#retrieveUsersWithBetsByUserGroup(Long, Long)} method.
	 * Scenario: throws {@link NullPointerException} because of the given {@code null}
	 *           {@code userGroupId} parameter
	 */
	@Test(expected = NullPointerException.class)
	public /*List<User>*/ void retrieveUsersWithBetsByUserGroup_NullUserGroupId(/*Long userGroupId, Long eventId*/) {
		Long userGroupId = null;
		Long eventId = 1L; // WC2014
		
		/*List<User> userList =*/ userGroupDao.retrieveUsersWithBetsByUserGroup(userGroupId, eventId);
	}

	/**
	 * Test {@link UserDao#retrieveUsersWithBetsByUserGroup(Long, Long)} method.
	 * Scenario: throws {@link illegalArgumentException} because of the given 
	 *           {@code userGroupId} parameter does not exist in database
	 */
	@Test(expected = IllegalArgumentException.class)
	public /*List<User>*/ void retrieveUsersWithBetsByUserGroup_UnknownUserGroupId(/*Long userGroupId, Long eventId*/) {
		Long userGroupId = -1L;
		Long eventId = 1L; // WC2014
		
		/*List<User> userList =*/ userGroupDao.retrieveUsersWithBetsByUserGroup(userGroupId, eventId);
	}

	/**
	 * Test {@link UserDao#retrieveUsersWithBetsByUserGroup(Long, Long)} method.
	 * Scenario: throws {@link NullPointerException} because of the given {@code null}
	 *           {@code eventId} parameter in case of virtual Everybody {@code userGroupId}
	 */
	@Test(expected = NullPointerException.class)
	public /*List<User>*/ void retrieveUsersWithBetsByUserGroup_EverybodyUserGroupId_NullEventId(/*Long userGroupId, Long eventId*/) {
		Long userGroupId = UserGroup.EVERYBODY_USER_GROUP_ID;
		Long eventId = null;
		
		/*List<User> userList =*/ userGroupDao.retrieveUsersWithBetsByUserGroup(userGroupId, eventId);
	}
	
	/**
	 * Test {@link UserDao#retrieveNumberOfUsersWithBetsByUserGroup(Long)} method. 
	 * Scenario: successful retrieval of number of users with bets belongs to a 
	 *           new userGroup
	 */
	@Test
	public void /*int*/ retrieveNumberOfUsersWithBetsByUserGroup_NewUserGroup(/*Long userGroupId, Long eventId*/) {
		Event event = commonDao.findEntityById(Event.class, 1L); // WC2014
		Long eventId = null; // not used in fact
		User user = commonDao.findEntityById(User.class, 2L); // normal
		
		// create a new dummyGroup userGroup and add normal user to it
		UserGroup userGroup = new UserGroup();
		userGroup.setName("dummyGroup");
		userGroup.setEvent(event);
		userGroup.setOwner(user);
		userGroup.setPublicEditableAsBoolean(false);
		userGroup.setPublicVisibleAsBoolean(true);
		userGroup.getUsers().add(user);
		user.setUserGroups(new HashSet<UserGroup>());
		user.getUserGroups().add(userGroup);
		commonDao.persistEntity(userGroup);
		Long userGroupId = userGroup.getUserGroupId();
		
		Bet bet = new Bet();
		bet.setEvent(event);
		Match match = commonDao.findEntityById(Match.class, 1L);
		bet.setMatch(match);
		match.getBets().add(bet);
		bet.setGoalNormalByTeam1((byte)1);
		bet.setGoalNormalByTeam2((byte)0);
		bet.setUser(user);
		user.getBets().add(bet);
		commonDao.persistEntity(bet);
		
		int expectedNumberOfUsersWithBets = 1;
		int numberOfUsersWithBets = userGroupDao.retrieveNumberOfUsersWithBetsByUserGroup(userGroupId, eventId);
		assertEquals(expectedNumberOfUsersWithBets, numberOfUsersWithBets);
	}

	/**
	 * Test {@link UserDao#retrieveNumberOfUsersWithBetsByUserGroup(Long)} method. 
	 * Scenario: successful retrieval of number of users with bets belongs to 
	 *           virtual Everybody userGroup
	 */
	@Test
	public void /*List<User>*/ retrieveNumberOfUsersWithBetsByUserGroup_EverbodyUserGroup(/*Long userGroupId, Long eventId*/) {
		Long userGroupId = UserGroup.EVERYBODY_USER_GROUP_ID;
		Long eventId = 1L; // WC2014
		
		Event event = commonDao.findEntityById(Event.class, eventId); // WC2014
		User user = commonDao.findEntityById(User.class, 2L); // normal
		
		Bet bet = new Bet();
		bet.setEvent(event);
		Match match = commonDao.findEntityById(Match.class, 1L);
		bet.setMatch(match);
		match.getBets().add(bet);
		bet.setGoalNormalByTeam1((byte)1);
		bet.setGoalNormalByTeam2((byte)0);
		bet.setUser(user);
		user.getBets().add(bet);
		commonDao.persistEntity(bet);
		
		int expectedNumberOfUsersWithBets = 1;
		int numberOfUsersWithBets =  userGroupDao.retrieveNumberOfUsersWithBetsByUserGroup(userGroupId, eventId);
		assertEquals(expectedNumberOfUsersWithBets, numberOfUsersWithBets);
	}

	/**
	 * Test {@link UserDao#retrieveNumberOfUsersWithBetsByUserGroup(Long, Long)} method.
	 * Scenario: throws {@link NullPointerException} because of the given {@code null}
	 *           {@code userGroupId} parameter
	 */
	@Test(expected = NullPointerException.class)
	public /*List<User>*/ void retrieveNumberOfUsersWithBetsByUserGroup_NullUserGroupId(/*Long userGroupId, Long eventId*/) {
		Long userGroupId = null;
		Long eventId = 1L; // WC2014

		/*List<User> userList =*/ userGroupDao.retrieveNumberOfUsersWithBetsByUserGroup(userGroupId, eventId);
	}

	/**
	 * Test {@link UserDao#retrieveNumberOfUsersWithBetsByUserGroup(Long, Long)} method.
	 * Scenario: throws {@link IllegalArgumentException} because of the given 
	 *           {@code userGroupId} parameter does not exist in database
	 */
	@Test(expected = IllegalArgumentException.class)
	public /*List<User>*/ void retrieveNumberOfUsersWithBetsByUserGroup_UnknownUserGroupId(/*Long userGroupId, Long eventId*/) {
		Long userGroupId = -1L;
		Long eventId = 1L; // WC2014

		/*List<User> userList =*/ userGroupDao.retrieveNumberOfUsersWithBetsByUserGroup(userGroupId, eventId);
	}

	/**
	 * Test {@link UserDao#retrieveNumberOfUsersWithBetsByUserGroup(Long, Long)} method.
	 * Scenario: throws {@link NullPointerException} because of the given {@code null}
	 *           {@code eventId} parameter in case of virtual Everybody {@code userGroupId}
	 */
	@Test(expected = NullPointerException.class)
	public /*List<User>*/ void retrieveNumberOfUsersWithBetsByUserGroup_EverybodyUserGroupId_NullEventId(/*Long userGroupId, Long eventId*/) {
		Long userGroupId = UserGroup.EVERYBODY_USER_GROUP_ID;
		Long eventId = null;

		/*List<User> userList =*/ userGroupDao.retrieveNumberOfUsersWithBetsByUserGroup(userGroupId, eventId);
	}
	
	/**
	 * Test {@link UserDao#retrieveUsersByUserGroup(Long)} method.
	 * Scenario: throws {@link NullPointerException} because of the given {@code null} eventId parameter
	 */
	@Test(expected = NullPointerException.class)
	public void /*UserGroup*/ findUserGroupByName_NullEventId(/*Long eventId, String name*/) {
		Long eventId = null;
		String name = "DummyUserGroup";

		/*UserGroup userGroup =*/ userGroupDao.findUserGroupByName(eventId, name);
	}

	/**
	 * Test {@link UserDao#findUserGroupByName(Long, String)} method.
	 * Scenario: throws {@link IllegalArgumentException} because of the given {@code null} 
	 *           {@code name} parameter
	 */
	@Test(expected = IllegalArgumentException.class)
	public void /*UserGroup*/ findUserGroupByName_NullName(/*Long eventId, String name*/) {
		Long eventId = 1L; // WC2014
		String name = null;

		/*UserGroup userGroup =*/ userGroupDao.findUserGroupByName(eventId, name);
	}

	/**
	 * Test {@link UserDao#findUserGroupByName(Long, String)} method.
	 * Scenario: throws IllegalArgumentException because of the given empty 
	 *           @{code name} parameter
	 */
	@Test(expected = IllegalArgumentException.class)
	public void /*UserGroup*/ findUserGroupByName_EmptyName(/*Long eventId, String name*/) {
		Long eventId = 1L; // WC2014
		String name = null;

		/*UserGroup userGroup =*/ userGroupDao.findUserGroupByName(eventId, name);
	}

	/**
	 * Test {@link UserDao#findUserGroupByName(Long, String)} method.
	 * Scenario: returns {@code null} because of non existing input {@code eventId} parameter
	 */
	@Test
	public void /*UserGroup*/ findUserGroupByName_UnknownEventId(/*Long eventId, String name*/) {
		Long eventId = -1L;
		String name = "DummyUserGroup";
		UserGroup userGroup = userGroupDao.findUserGroupByName(eventId, name);
		assertNull(userGroup);
	}

	/**
	 * Test {@link UserDao#findUserGroupByName(Long, String)} method.
	 * Scenario: returns {@code null} because of non existing input userGroup {@code name}
	 */
	@Test
	public void /*UserGroup*/ findUserGroupByName_UnknownName(/*Long eventId, String name*/) {
		Long eventId = 1L;
		String name = "dummyGroup2";

		Event event = commonDao.findEntityById(Event.class, eventId);
		User user = commonDao.findEntityById(User.class, 2L); // normal

		// create a new dummyGroup userGroup and add normal user to it
		UserGroup userGroup = new UserGroup();
		userGroup.setName("dummyGroup");
		userGroup.setEvent(event);
		userGroup.setOwner(user);
		userGroup.setPublicEditableAsBoolean(false);
		userGroup.setPublicVisibleAsBoolean(true);
		userGroup.getUsers().add(user);
		user.setUserGroups(new HashSet<UserGroup>());
		user.getUserGroups().add(userGroup);
		commonDao.persistEntity(userGroup);
		
		/*UserGroup*/ userGroup = userGroupDao.findUserGroupByName(eventId, name);
		assertNull(userGroup);
	}

	/**
	 * Test {@link UserDao#findUserGroupByName(Long, String)} method.
	 * Scenario: successful retrieval of "dummyGroup2" userGroup
	 */
	@Test
	public void /*UserGroup*/ findUserGroupByName(/*Long eventId, String name*/) {
		Long eventId = 1L; // WC2014
		String name = "dummyGroup2";

		Event event = commonDao.findEntityById(Event.class, eventId);
		User user = commonDao.findEntityById(User.class, 2L); // normal

		// create a new dummyGroup userGroup and add normal user to it
		UserGroup userGroup = new UserGroup();
		userGroup.setName("dummyGroup");
		userGroup.setEvent(event);
		userGroup.setOwner(user);
		userGroup.setPublicEditableAsBoolean(false);
		userGroup.setPublicVisibleAsBoolean(true);
		userGroup.getUsers().add(user);
		user.setUserGroups(new HashSet<UserGroup>());
		user.getUserGroups().add(userGroup);
		commonDao.persistEntity(userGroup);

		// create a new dummyGroup2 userGroup and add normal user to it
		/*UserGroup*/ userGroup = new UserGroup();
		userGroup.setName("dummyGroup2");
		userGroup.setEvent(event);
		userGroup.setOwner(user);
		userGroup.setPublicEditableAsBoolean(false);
		userGroup.setPublicVisibleAsBoolean(true);
		userGroup.getUsers().add(user);
		user.setUserGroups(new HashSet<UserGroup>());
		user.getUserGroups().add(userGroup);
		commonDao.persistEntity(userGroup);
		UserGroup expectedUserGroup = userGroup;
		
		/*UserGroup*/ userGroup = userGroupDao.findUserGroupByName(eventId, name);
		assertEquals(expectedUserGroup, userGroup);
	}
	
	/**
	 * Test {@link UserDao#findLastUserGroupByName(Long, String)} method.
	 * Scenario: throws {@link NullPointerException} because of the given {@code null} eventId parameter
	 */
	@Test(expected = NullPointerException.class)
	public void /*UserGroup*/ findLastUserGroupByName_NullEventId(/*Long eventId, String name*/) {
		Long eventId = null;
		String name = "DummyUserGroup";

		/*UserGroup userGroup =*/ userGroupDao.findLastUserGroupByName(eventId, name);
	}

	/**
	 * Test {@link UserDao#findLastUserGroupByName(Long, String)} method.
	 * Scenario: throws {@link IllegalArgumentException} because of the given {@code null} name parameter
	 */
	@Test(expected = IllegalArgumentException.class)
	public void /*UserGroup*/ findLastUserGroupByName_NullName(/*Long eventId, String name*/) {
		Long eventId = 1L; // WC2014
		String name = null;

		/*UserGroup userGroup =*/ userGroupDao.findLastUserGroupByName(eventId, name);
	}

	/**
	 * Test {@link UserDao#findLastUserGroupByName(Long, String)} method.
	 * Scenario: returns {@code null} because of non existing input {@code eventId} parameter
	 */
	@Test
	public void /*UserGroup*/ findLastUserGroupByName_UnknownEventId(/*Long eventId, String name*/) {
		Long eventId = -1L;
		String name = "DummyUserGroup";
		
		UserGroup userGroup = userGroupDao.findLastUserGroupByName(eventId, name);
		assertNull(userGroup);
	}

	/**
	 * Test {@link UserDao#findLastUserGroupByName(Long, String)} method.
	 * Scenario: returns {@code null} because of non existing input userGroup {@code name}
	 */
	@Test
	public void /*UserGroup*/ findLastUserGroupByName_UnknownName(/*Long eventId, String name*/) {
		Long eventId = 1L;
		String name = "dummyGroup2";

		Event event = commonDao.findEntityById(Event.class, eventId);
		User user = commonDao.findEntityById(User.class, 2L); // normal

		// create a new dummyGroup userGroup and add normal user to it
		UserGroup userGroup = new UserGroup();
		userGroup.setName("dummyGroup");
		userGroup.setEvent(event);
		userGroup.setOwner(user);
		userGroup.setPublicEditableAsBoolean(false);
		userGroup.setPublicVisibleAsBoolean(true);
		userGroup.getUsers().add(user);
		user.setUserGroups(new HashSet<UserGroup>());
		user.getUserGroups().add(userGroup);
		commonDao.persistEntity(userGroup);
		
		/*UserGroup*/ userGroup = userGroupDao.findLastUserGroupByName(eventId, name);
		assertNull(userGroup);
	}

	/**
	 * Test {@link UserDao#findLastUserGroupByName(Long, String)} method.
	 * Scenario: successful retrieval of an userGroup of an earlier event
	 */
	@Test
	public void /*UserGroup*/ findLastUserGroupByName_Earlier(/*Long eventId, String name*/) {
		Long eventId = 2L; // EC2016
		String name = "dummyGroup";
		
		Long lastEventId = 1L; // WC2014
		Event lastEvent = commonDao.findEntityById(Event.class, lastEventId);
		//Event event = commonDao.findEntityById(Event.class, eventId);
		User user = commonDao.findEntityById(User.class, 2L); // normal

		// create a new dummyGroup userGroup and add normal user to it
		UserGroup expectedUserGroup = new UserGroup();
		expectedUserGroup.setName("dummyGroup");
		expectedUserGroup.setEvent(lastEvent);
		expectedUserGroup.setOwner(user);
		expectedUserGroup.setPublicEditableAsBoolean(false);
		expectedUserGroup.setPublicVisibleAsBoolean(true);
		expectedUserGroup.getUsers().add(user);
		user.setUserGroups(new HashSet<UserGroup>());
		user.getUserGroups().add(expectedUserGroup);
		commonDao.persistEntity(expectedUserGroup);
		
		UserGroup userGroup = userGroupDao.findLastUserGroupByName(eventId, name);
		assertEquals(expectedUserGroup, userGroup);
	}
	
	/**
	 * Test {@link UserDao#findLastUserGroupByName(Long, String)} method.
	 * Scenario: successful retrieval of an userGroup of the given {@code eventId}
	 *           parameter
	 */
	@Test
	public void /*UserGroup*/ findLastUserGroupByName(/*Long eventId, String name*/) {
		Long eventId = 2L; // EC2016
		String name = "dummyGroup";
		
		Long lastEventId = 1L; // WC2014
		Event lastEvent = commonDao.findEntityById(Event.class, lastEventId);
		Event event = commonDao.findEntityById(Event.class, eventId);
		User user = commonDao.findEntityById(User.class, 2L); // normal

		// create a new dummyGroup userGroup to the given event
		UserGroup userGroup = new UserGroup();
		userGroup.setName("dummyGroup");
		userGroup.setEvent(event);
		userGroup.setOwner(user);
		userGroup.setPublicEditableAsBoolean(false);
		userGroup.setPublicVisibleAsBoolean(true);
		userGroup.getUsers().add(user);
		user.setUserGroups(new HashSet<UserGroup>());
		user.getUserGroups().add(userGroup);
		commonDao.persistEntity(userGroup);

		// create a new dummyGroup userGroup to an earlier event
		UserGroup expectedUserGroup = new UserGroup();
		expectedUserGroup.setName("dummyGroup");
		expectedUserGroup.setEvent(lastEvent);
		expectedUserGroup.setOwner(user);
		expectedUserGroup.setPublicEditableAsBoolean(false);
		expectedUserGroup.setPublicVisibleAsBoolean(true);
		expectedUserGroup.getUsers().add(user);
		user.setUserGroups(new HashSet<UserGroup>());
		user.getUserGroups().add(expectedUserGroup);
		commonDao.persistEntity(expectedUserGroup);
		
		/*UserGroup*/ userGroup = userGroupDao.findLastUserGroupByName(eventId, name);
		assertEquals(expectedUserGroup, userGroup);
	}
	
	/**
	 * Test {@link UserDao#insertUserGroup(Long, Long, String)} method.
	 * Scenario: successful insertion of a new user group named to the given name of
	 *           the given event and user.
	 */
	@Test
	public void /*UserGroup*/ insertUserGroup(/*Long eventId, Long userId, String name*/) {
		Long eventId = 1L; //WC2014
		Long userId = 2L; // normal
		String name = "newUserGroup";
		
		UserGroup userGroup = userGroupDao.insertUserGroup(eventId, userId, name);
		assertNotNull(userGroup);
		
		assertEquals(eventId, userGroup.getEvent().getEventId());
		assertEquals(name, userGroup.getName());
		assertEquals(userId, userGroup.getOwner().getUserId());
		
		UserGroup insertedUserGroup = userGroupDao.findUserGroupByName(eventId, name);
		assertNotNull(userGroup);
		
		assertEquals(userGroup, insertedUserGroup);
		
	}

	/**
	 * Test {@link UserDao#insertUserGroup(Long, Long, String)} method.
	 * Scenario: throws {@link NullPointerException} because of the given {@code null} {@code eventId} parameter.
	 */
	@Test(expected = NullPointerException.class)
	public void /*UserGroup*/ insertUserGroup_NullEventId(/*Long eventId, Long userId, String name*/) {
		Long eventId = null; // WC2014
		Long userId = 1L; // admin 
		String name = "newUserGroup";
		
		/*UserGroup userGroup =*/ userGroupDao.insertUserGroup(eventId, userId, name);
	}

	/**
	 * Test {@link UserDao#insertUserGroup(Long, Long, String)} method.
	 * Scenario: throws {@link NullPointerException} because of the given {@code null} {@code userId} parameter.
	 */
	@Test(expected = NullPointerException.class)
	public void /*UserGroup*/ insertUserGroup_NullUserId(/*Long eventId, Long userId, String name*/) {
		Long eventId = 1L; // WC2014
		Long userId = null; 
		String name = "newUserGroup";
		
		/*UserGroup userGroup =*/ userGroupDao.insertUserGroup(eventId, userId, name);
	}

	/**
	 * Test {@link UserDao#insertUserGroup(Long, Long, String)} method.
	 * Scenario: throws {@link IllegalArgumentException} because of the given {@code null} {@code name} parameter.
	 */
	@Test(expected = IllegalArgumentException.class)
	public void /*UserGroup*/ insertUserGroup_NullName(/*Long eventId, Long userId, String name*/) {
		Long eventId = 1L; // WC2014
		Long userId = 1L; // admin 
		String name = null;
		
		/*UserGroup userGroup =*/ userGroupDao.insertUserGroup(eventId, userId, name);
	}

	/**
	 * Test {@link UserDao#insertUserGroup(Long, Long, String)} method.
	 * Scenario: throws {@link IllegalArgumentException} because of the given non existing {@code eventId} parameter. 
	 */
	@Test(expected = IllegalArgumentException.class)
	public void /*UserGroup*/ insertUserGroup_UnknownEventId(/*Long eventId, Long userId, String name*/) {
		Long eventId = -1L;
		Long userId = 1L; // admin 
		String name = "newUserGroup";
		
		/*UserGroup userGroup =*/ userGroupDao.insertUserGroup(eventId, userId, name);
	}
	
	/**
	 * Test {@link UserDao#insertUserGroup(Long, Long, String)} method.
	 * Scenario: throws {@link IllegalArgumentException} because of the given non existing {@code userId} parameter. 
	 */
	@Test(expected = IllegalArgumentException.class)
	public void /*UserGroup*/ insertUserGroup_UnknownUserId(/*Long eventId, Long userId, String name*/) {
		Long eventId = 1L; // WC2014
		Long userId = -1L; 
		String name = "newUserGroup";
		
		/*UserGroup userGroup =*/ userGroupDao.insertUserGroup(eventId, userId, name);
	}
	
	/**
	 * Test {@link UserDao#deleteUserGroup(Long)} method.
	 * Scenario: throws {@link NullPointerException} because of the given {@code null} 
	 *           {@code userGroupId} parameter
	 */
	@Test(expected = NullPointerException.class)
	public void deleteUserGroup_NullUserGroupId(/*Long userGroupId*/) {
		Long userGroupId = null;

		userGroupDao.deleteUserGroup(userGroupId);
	}

	/**
	 * Test {@link UserDao#deleteUserGroup(Long)} method.
	 * Scenario: throws {@link IllegalStateException} because the given {@code userGroupId} parameter
	 *           does not exist in database
	 */
	@Test(expected = IllegalStateException.class)
	public void deleteUserGroup_UnknownUserGroupId(/*Long userGroupId*/) {
		Long userGroupId = -1L;

		userGroupDao.deleteUserGroup(userGroupId);
	}

	/**
	 * Test {@link UserDao#deleteUserGroup(Long)} method.
	 * Scenario: successful deletion of a new userGroup named dummyGroup
	 */
	@Test
	public void deleteUserGroup(/*Long userGroupId*/) {
		Long userGroupId; // initialized later

		Event event = commonDao.findEntityById(Event.class, 1L); // WC2014
		User user = commonDao.findEntityById(User.class, 2L); // normal

		// create a new dummyGroup userGroup and add normal user to it
		UserGroup userGroup = new UserGroup();
		userGroup.setName("dummyGroup");
		userGroup.setEvent(event);
		userGroup.setOwner(user);
		userGroup.setPublicEditableAsBoolean(false);
		userGroup.setPublicVisibleAsBoolean(true);
		userGroup.getUsers().add(user);
		user.setUserGroups(new HashSet<UserGroup>());
		user.getUserGroups().add(userGroup);
		commonDao.persistEntity(userGroup);
		userGroupId = userGroup.getUserGroupId();
		assertNotNull(userGroupId);

		userGroupDao.deleteUserGroup(userGroupId);
		
		UserGroup deletedUserGroup = commonDao.findEntityById(UserGroup.class, userGroupId);		
		assertNull(deletedUserGroup);
	}

	/**
	 * Test {@link UserDao#deleteUserGroup(Long)} method.
	 * Scenario: throws {@link NullPointerException} because of null userGroupId argument.
	 */
	@Test(expected = NullPointerException.class)
	public void deleteUserGroupNull(/*Long userGroupId*/) {
		Long userGroupId = null; 

		userGroupDao.deleteUserGroup(userGroupId);
	}

	/**
	 * Test {@link UserDao#deleteUserGroup(Long)} method.
	 * Scenario: throws {@link IllegalStateException} because of non existing userGroupId argument.
	 */
	@Test(expected = IllegalStateException.class)
	public void deleteUserGroupUnknown(/*Long userGroupId*/) {
		Long userGroupId = -1L;
		
		userGroupDao.deleteUserGroup(userGroupId);
	}
}