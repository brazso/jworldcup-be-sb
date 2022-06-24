package com.zematix.jworldcup.backend.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Properties;

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

import com.google.common.collect.Lists;
import com.zematix.jworldcup.backend.dao.CommonDao;
import com.zematix.jworldcup.backend.dao.UserDao;
import com.zematix.jworldcup.backend.dao.UserGroupDao;
import com.zematix.jworldcup.backend.emun.TemplateId;
import com.zematix.jworldcup.backend.entity.Event;
import com.zematix.jworldcup.backend.entity.User;
import com.zematix.jworldcup.backend.entity.UserGroup;
import com.zematix.jworldcup.backend.exception.ServiceException;
import com.zematix.jworldcup.backend.model.ParameterizedMessage;
import com.zematix.jworldcup.backend.model.UserCertificate;
import com.zematix.jworldcup.backend.model.UserPosition;

/**
 * Contains test functions of {@link UserGroupService} class.
 */

@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles({"development", "test"})
@Transactional
public class UserGroupServiceTest {

	@Inject
	private UserGroupService userGroupService;
	
	@MockBean
	private UserDao userDao; // used by some methods inside UserService

	@MockBean
	private UserGroupDao userGroupDao;

	@Inject 
	private CommonDao commonDao;

	@MockBean
	private ApplicationService applicationService;

	@MockBean
	private BetService betService;

	@MockBean
	private EventService eventService;

	@MockBean
	private TemplateService templateService;
	
	@SpyBean
	private UserGroupService userGroupServicePartial; // partial mock

	/**
	 * Test {@link UserGroupService#retrieveUserGroups(Long, Long, boolean)} method.
	 * Scenario: successfully retrieves result, an list containing only 
	 * a virtual Everybody userGroup.
	 */
	@Test
	public void /*List<UserGroup>*/ retrieveUserGroups_WithEverybody_Empty(/*Long event_id, Long userId, boolean isEverybodyIncluded*/) throws ServiceException {
		Long eventId = 1L; // WC2014
		Long userId = 2L; // normal
		boolean isEverybodyIncluded = true; 
		
		List<UserGroup> retrievedUserGroups = new ArrayList<>();
		Mockito.when(userGroupDao.retrieveUserGroups(eventId, userId)).thenReturn(retrievedUserGroups);
		
		UserGroup userGroup = new UserGroup(); // virtual Everybody
		userGroup.setUserGroupId(UserGroup.EVERYBODY_USER_GROUP_ID);
		Mockito.when(userGroupDao.createVirtualEverybodyUserGroup(eventId, userId)).thenReturn(userGroup);
		List<UserGroup> expectedUserGroups = Arrays.asList(userGroup);
		
		List<UserGroup> userGroups = userGroupService.retrieveUserGroups(eventId, userId, isEverybodyIncluded);
		assertEquals("Retrieved list should be equal to the expected one.", expectedUserGroups, userGroups);
	}

	/**
	 * Test {@link UserGroupService#retrieveUserGroups(Long, Long)} method.
	 * Scenario: successfully retrieves result, an list containing a userGroup object and
	 * another one with virtual Everybody.
	 */
	@Test
	public void /*List<UserGroup>*/ retrieveUserGroups_WithEverybody(/*Long event_id, Long userId*/) throws ServiceException {
		Long eventId = 1L; // WC2014
		Long userId = 2L; // normal
		boolean isEverybodyIncluded = true;
		
		List<UserGroup> retrievedUserGroups = new ArrayList<>();
		UserGroup userGroup = new UserGroup();
		userGroup.setUserGroupId(1L);
		retrievedUserGroups.add(userGroup);
		List<UserGroup> expectedUserGroups = Lists.newArrayList(userGroup); // mutable list
		Mockito.when(userGroupDao.retrieveUserGroups(eventId, userId)).thenReturn(retrievedUserGroups);
		
		/*UserGroup*/ userGroup = new UserGroup(); // virtual Everybody
		userGroup.setUserGroupId(UserGroup.EVERYBODY_USER_GROUP_ID);
		Mockito.when(userGroupDao.createVirtualEverybodyUserGroup(eventId, userId)).thenReturn(userGroup);
		expectedUserGroups.add(userGroup);

		List<UserGroup> userGroups = userGroupService.retrieveUserGroups(eventId, userId, isEverybodyIncluded);
		assertEquals("Retrieved list should be equal to the expected one.", expectedUserGroups, userGroups);
	}

	/**
	 * Test {@link UserGroupService#retrieveUserGroups(Long, Long, boolean)} method.
	 * Scenario: successfully retrieves result, an empty list.
	 */
	@Test
	public void /*List<UserGroup>*/ retrieveUserGroups_WithoutEverybody_Empty(/*Long event_id, Long userId, boolean isEverybodyIncluded*/) throws ServiceException {
		Long eventId = 1L; // WC2014
		Long userId = 2L; // normal
		boolean isEverybodyIncluded = false; 
		
		List<UserGroup> retrievedUserGroups = new ArrayList<>();
		Mockito.when(userGroupDao.retrieveUserGroups(eventId, userId)).thenReturn(retrievedUserGroups);
		List<UserGroup> expectedUserGroups = new ArrayList<>();
		
		List<UserGroup> userGroups = userGroupService.retrieveUserGroups(eventId, userId, isEverybodyIncluded);
		assertEquals("Retrieved list should be equal to the expected one.", expectedUserGroups, userGroups);
	}

	/**
	 * Test {@link UserGroupService#retrieveUserGroups(Long, Long)} method.
	 * Scenario: successfully retrieves result, an list containing a new userGroup object
	 */
	@Test
	public void /*List<UserGroup>*/ retrieveUserGroups_WithoutEverybody(/*Long event_id, Long userId*/) throws ServiceException {
		Long eventId = 1L; // WC2014
		Long userId = 2L; // normal
		boolean isEverybodyIncluded = false;
		
		List<UserGroup> retrievedUserGroups = new ArrayList<>();
		UserGroup userGroup = new UserGroup();
		userGroup.setUserGroupId(1L);
		retrievedUserGroups.add(userGroup);
		List<UserGroup> expectedUserGroups = Arrays.asList(userGroup);
		Mockito.when(userGroupDao.retrieveUserGroups(eventId, userId)).thenReturn(retrievedUserGroups);

		User user = new User();
		user.setUserId(1L);
		List<User> expectedUsers = Arrays.asList(user);
		
		retrievedUserGroups.stream().forEach(e -> {Mockito.when(userGroupDao.retrieveUsersByUserGroup(e.getUserGroupId())).thenReturn(expectedUsers);});
		
		List<UserGroup> userGroups = userGroupService.retrieveUserGroups(eventId, userId, isEverybodyIncluded);
		assertEquals("Retrieved list should be equal to the expected one.", expectedUserGroups, userGroups);
		assertEquals("Retrieved user list should be equal to the expected one.", expectedUserGroups.get(0).getUsers(), expectedUsers);
	}

	/**
	 * Test {@link UserGroupService#retrieveUsersByUserGroup(Long)} method.
	 * Scenario: successfully retrieves users belongs to the given {@code userGroupId} parameter
	 */
	@Test
	public void /*List<UserPosition>*/ retrieveUsersByUserGroup(/*Long userGroupId*/) throws ServiceException {
		Long userGroupId = 1L; // Everybody
		//Mockito.doReturn(null).when(userGroupDao).retrieveUsersByUserGroup(userGroupId);
		
		userGroupService.retrieveUsersByUserGroup(userGroupId);
	}
	
	/**
	 * Test {@link UserGroupService#retrieveUserPositions(Long, Long)} method.
	 * Scenario: throws {@link NullPointerException} because of the given {@code null}
	 *           {@code eventId} parameter
	 */
	@Test(expected=NullPointerException.class)
	public void /*List<UserPosition>*/ retrieveUserPositions_NullEventId(/*Long eventId, Long userGroupId*/) throws ServiceException {
		Long eventId = null;
		Long userGroupId = 1L; // Everybody
		
		userGroupService.retrieveUserPositions(eventId, userGroupId);
	}
	
	/**
	 * Test {@link UserGroupService#retrieveUserPositions(Long, Long)} method.
	 * Scenario: throws {@link NullPointerException} because of the given {@code null}
	 *           {@code userGroupId} parameter
	 */
	@Test(expected=NullPointerException.class)
	public void /*List<UserPosition>*/ retrieveUserPositions_NullUserGroupId(/*Long eventId, Long userGroupId*/) throws ServiceException {
		Long eventId = 1L; // WC2014
		Long userGroupId = null;
		
		userGroupService.retrieveUserPositions(eventId, userGroupId);
	}
	
	/**
	 * Test {@link UserGroupService#retrieveUserPositions(Long, Long)} method.
	 * Scenario: successfully retrieves result of the given Everybody userGroup 
	 *           after some users were inserted into the group
	 */
	@Test
	public void /*List<UserPosition>*/ retrieveUserPositions(/*Long eventId, Long userGroupId*/) throws ServiceException {
		Long eventId = 1L; // WC2014
		Long userGroupId = 1L; // WC2014/Everybody
		
		User normalUser = commonDao.findEntityById(User.class, 2L);
		
		// create a new dummy user with normal2 loginName
		User normal2User = new User();
		normal2User.setLoginName("normal2");
		normal2User.setUserId(-1L); // unique value
		
		// create a new dummy user with normal3 loginName
		User normal3User = new User();
		normal3User.setLoginName("normal3");
		normal3User.setUserId(-2L); // unique value
		
		List<User> retrievedUsers = Arrays.asList(normalUser, normal2User, normal3User);
		//Mockito.when(userGroupDao.retrieveUsersByUserGroup(userGroupId)).thenReturn(retrievedUsers);
		Mockito.when(userGroupDao.retrieveUsersWithBetsByUserGroup(userGroupId, eventId)).thenReturn(retrievedUsers);
		
		// mocked retrieved scores of users
		Mockito.when(betService.retrieveScoreByEventAndUser(eventId, normalUser.getUserId())).thenReturn(32);
		Mockito.when(betService.retrieveScoreByEventAndUser(eventId, normal2User.getUserId())).thenReturn(41);
		Mockito.when(betService.retrieveScoreByEventAndUser(eventId, normal3User.getUserId())).thenReturn(37);
		List<Long> expectedUserIds = Arrays.asList(normal2User.getUserId(), normal3User.getUserId(), normalUser.getUserId()); // sorted by score
		
		List<UserPosition> userPositions = userGroupService.retrieveUserPositions(eventId, userGroupId);
		assertEquals("Retrieved list should be equal to the expected one.", 
				expectedUserIds, userPositions.stream().map(e -> e.getUserId()).toList());
		assertEquals("Retrieved position list should be equal to the expected one.", 
				Arrays.asList(1, 2, 3), 
				userPositions.stream().map(e -> e.getPosition()).toList());
	}
	
	/**
	 * Test {@link UserGroupService#insertUserGroup(Long, Long, String, boolean)} method.
	 * Scenario: throws {@link ServiceException} because of the given {@code null}
	 *           {@code name} parameter
	 */
	@Test(expected=ServiceException.class)
	public void /*UserGroup*/ insertUserGroup_NullName(/*Long eventId, Long userId, String name, boolean isInsertConfirmed*/) throws ServiceException {
		Long eventId = 1L; // WC2014
		Long userId = 2L; // normal
		String name = null;
		boolean isInsertConfirmed = false;
		
		try {
			userGroupService.insertUserGroup(eventId, userId, name, isInsertConfirmed);
		}
		catch (ServiceException e) {
			assertTrue("There must be a single message in ServiceException named USER_GROUP_NAME_EMPTY", 
					e.getMessages().size()==1 && e.getMessages().get(0).getMsgCode().equals("USER_GROUP_NAME_EMPTY"));
			throw e;
		}
	}
	
	/**
	 * Test {@link UserGroupService#insertUserGroup(Long, Long, String, boolean)} method.
	 * Scenario: throws {@link ServiceException} because of the given empty
	 *           {@code name} parameter
	 */
	@Test(expected=ServiceException.class)
	public void /*UserGroup*/ insertUserGroup_NullEmpty(/*Long eventId, Long userId, String name, boolean isInsertConfirmed*/) throws ServiceException {
		Long eventId = 1L; // WC2014
		Long userId = 2L; // normal
		String name = "";
		boolean isInsertConfirmed = false;
		
		try {
			userGroupService.insertUserGroup(eventId, userId, name, isInsertConfirmed);
		}
		catch (ServiceException e) {
			assertTrue("There must be a single message in ServiceException named USER_GROUP_NAME_EMPTY", 
					e.getMessages().size()==1 && e.getMessages().get(0).getMsgCode().equals("USER_GROUP_NAME_EMPTY"));
			throw e;
		}
	}
	
	/**
	 * Test {@link UserGroupService#insertUserGroup(Long, Long, String, boolean)} method.
	 * Scenario: throws {@link ServiceException} because of the userGroup specified by
	 *           the given {@code eventId} and {@code name} parameters is already
	 *           exits only on an earlier event.
	 */
	@Test(expected=ServiceException.class)
	public void /*UserGroup*/ insertUserGroup_FoundEarlier(/*Long eventId, Long userId, String name, boolean isInsertConfirmed*/) throws ServiceException {
		Long eventId = 2L; // EC2016
		Long userId = 2L; // normal
		String name = "DummyUserGroup";
		boolean isInsertConfirmed = false;
		
		User user = commonDao.findEntityById(User.class, userId);
		Event lastEvent = commonDao.findEntityById(Event.class, 1L); // WC2014 
		
		UserGroup expectedUserGroup = new UserGroup();
		expectedUserGroup.setUserGroupId(-1L);
		expectedUserGroup.setOwner(user);
		expectedUserGroup.setEvent(lastEvent);
		
		Mockito.when(userGroupDao.findLastUserGroupByName(eventId, name)).thenReturn(expectedUserGroup);

		try {
			userGroupService.insertUserGroup(eventId, userId, name, isInsertConfirmed);
		}
		catch (ServiceException e) {
			assertTrue("There must be a single message in ServiceException named USER_GROUP_NAME_OCCUPIED_ON_EARLIER_EVENT", 
					e.getMessages().size()==1 && e.getMessages().get(0).getMsgCode().equals("USER_GROUP_NAME_OCCUPIED_ON_EARLIER_EVENT"));
			throw e;
		}
	}
	
	/**
	 * Test {@link UserGroupService#insertUserGroup(Long, Long, String, boolean)} method.
	 * Scenario: successfully inserts a new userGroup. Although the userGroup 
	 *           specified by the given {@code eventId} and {@code name} parameters 
	 *           is already exits on an earlier event and not on the given one,
	 *           but the insert is confirmed by the given {@code isInsertConfirmed} 
	 *           parameter.
	 */
	@Test
	public void /*UserGroup*/ insertUserGroup_FoundEarlierInsertConfirmed(/*Long eventId, Long userId, String name, boolean isInsertConfirmed*/) throws ServiceException {
		Long eventId = 2L; // EC2016
		Long userId = 2L; // normal
		String name = "DummyUserGroup";
		boolean isInsertConfirmed = true;
		
		User user = commonDao.findEntityById(User.class, userId);
		Event lastEvent = commonDao.findEntityById(Event.class, 1L); // WC2014 
		
		UserGroup expectedUserGroup = new UserGroup();
		expectedUserGroup.setUserGroupId(-1L);
		expectedUserGroup.setOwner(user);
		expectedUserGroup.setEvent(lastEvent);

		List<User> expectedUsers = Arrays.asList(user);
		
		Mockito.when(userGroupDao.findLastUserGroupByName(eventId, name)).thenReturn(expectedUserGroup);
		Mockito.when(userGroupDao.insertUserGroup(eventId, userId, name)).thenReturn(expectedUserGroup);
		Mockito.when(userGroupDao.retrieveUsersByUserGroup(expectedUserGroup.getUserGroupId())).thenReturn(expectedUsers);

		UserGroup userGroup = userGroupService.insertUserGroup(eventId, userId, name, isInsertConfirmed);
		assertEquals("Inserted userGroup must be the same as the expected one", 
				expectedUserGroup, userGroup);
		assertEquals("Retrieved user list should be equal to the expected one.", userGroup.getUsers(), expectedUsers);
	}
	
	/**
	 * Test {@link UserGroupService#insertUserGroup(Long, Long, String, boolean)} method.
	 * Scenario: throws {@link ServiceException} because of the userGroup specified by
	 *           the given {@code eventId} and {@code name} parameters is already
	 *           exits, moreover its owner is {@code userId}.
	 */
	@Test(expected=ServiceException.class)
	public void /*UserGroup*/ insertUserGroup_FoundOwner(/*Long eventId, Long userId, String name, boolean isInsertConfirmed*/) throws ServiceException {
		Long eventId = 1L; // WC2014
		Long userId = 2L; // normal
		
		Event event = commonDao.findEntityById(Event.class, eventId);
		User user = commonDao.findEntityById(User.class, userId);
		String name = "DummyUserGroup";
		boolean isInsertConfirmed = false;
		
		UserGroup expectedUserGroup = new UserGroup();
		expectedUserGroup.setUserGroupId(-1L);
		expectedUserGroup.setOwner(user);
		expectedUserGroup.setEvent(event);
		
		Mockito.when(userGroupDao.findLastUserGroupByName(eventId, name)).thenReturn(expectedUserGroup);

		try {
			userGroupService.insertUserGroup(eventId, userId, name, isInsertConfirmed);
		}
		catch (ServiceException e) {
			assertTrue("There must be a single message in ServiceException named USER_GROUP_NAME_ALREADY_EXIST", 
					e.getMessages().size()==1 && e.getMessages().get(0).getMsgCode().equals("USER_GROUP_NAME_ALREADY_EXIST"));
			throw e;
		}
	}
	
	/**
	 * Test {@link UserGroupService#insertUserGroup(Long, Long, String, boolean)} method.
	 * Scenario: throws {@link ServiceException} because of the userGroup specified by
	 *           the given {@code eventId} and {@code name} parameters is already
	 *           exits, moreover its owner is not {@code userId}.
	 */
	@Test(expected=ServiceException.class)
	public void /*UserGroup*/ insertUserGroup_FoundNotOwner(/*Long eventId, Long userId, String name, boolean isInsertConfirmed*/) throws ServiceException {
		Long eventId = 1L; // WC2014
		Long userId = 2L; // normal
		String name = "DummyUserGroup";
		boolean isInsertConfirmed = false;

		Event event = commonDao.findEntityById(Event.class, eventId);
		User user = commonDao.findEntityById(User.class, 1L); // another user, admin
		
		UserGroup expectedUserGroup = new UserGroup();
		expectedUserGroup.setUserGroupId(-1L);
		expectedUserGroup.setOwner(user);
		expectedUserGroup.setEvent(event);
		
		Mockito.when(userGroupDao.findLastUserGroupByName(eventId, name)).thenReturn(expectedUserGroup);

		try {
			userGroupService.insertUserGroup(eventId, userId, name, isInsertConfirmed);
		}
		catch (ServiceException e) {
			assertTrue("There must be a single message in ServiceException named USER_GROUP_NAME_OCCUPIED", 
					e.getMessages().size()==1 && e.getMessages().get(0).getMsgCode().equals("USER_GROUP_NAME_OCCUPIED"));
			throw e;
		}
	}
	
	/**
	 * Test {@link UserGroupService#insertUserGroup(Long, Long, String, boolean)} method.
	 * Scenario: successfully inserts a new userGroup
	 */
	@Test
	public void /*UserGroup*/ insertUserGroup(/*Long eventId, Long userId, String name, boolean isInsertConfirmed*/) throws ServiceException {
		Long eventId = 1L; // WC2014
		Long userId = 2L; // normal
		String name = "DummyUserGroup";
		boolean isInsertConfirmed = false;
		
		Event event = commonDao.findEntityById(Event.class, eventId);
		User user = commonDao.findEntityById(User.class, 2L);
		
		UserGroup expectedUserGroup = new UserGroup();
		expectedUserGroup.setUserGroupId(-1L);
		expectedUserGroup.setOwner(user);
		expectedUserGroup.setEvent(event);
		
		List<User> expectedUsers = Arrays.asList(user);
		
		Mockito.when(userGroupDao.findLastUserGroupByName(eventId, name)).thenReturn(null);
		Mockito.when(userGroupDao.insertUserGroup(eventId, userId, name)).thenReturn(expectedUserGroup);
		Mockito.when(userGroupDao.retrieveUsersByUserGroup(expectedUserGroup.getUserGroupId())).thenReturn(expectedUsers);

		UserGroup userGroup = userGroupService.insertUserGroup(eventId, userId, name, isInsertConfirmed);
		assertEquals("Inserted userGroup must be the same as the expected one", 
				expectedUserGroup, userGroup);
		assertEquals("Retrieved user list should be equal to the expected one.", userGroup.getUsers(), expectedUsers);
	}
	
	/**
	 * Test {@link UserGroupService#deleteUserGroup(Long)} method.
	 * Scenario: successfully deletes the given userGroup
	 */
	@Test
	public void deleteUserGroup(/*Long userGroupId*/) throws ServiceException {
		Long userGroupId = 1L; // WC2014/Everybody
		Mockito.doNothing().when(userGroupDao).deleteUserGroup(userGroupId);
		
		userGroupService.deleteUserGroup(userGroupId);
	}
	
	/**
	 * Test {@link UserGroupService#findAndAddUserToUserGroup(Long, String, String)} method.
	 * Scenario: throws {@link NullPointerException} because of the given {@code null}
	 *           {@code userGroupId} parameter
	 */
	@Test(expected=NullPointerException.class)
	public void /*User*/ findAndAddUserToUserGroup_NullUserGroupId(/*Long userGroupId, String loginName, String fullName*/) throws ServiceException {
		Long userGroupId = null;
		String loginName = "normal";
		String fullName = "Normal Dummy";
		
		userGroupService.findAndAddUserToUserGroup(userGroupId, loginName, fullName);
	}
	
	/**
	 * Test {@link UserGroupService#findAndAddUserToUserGroup(Long, String, String)} method.
	 * Scenario: throws {@link IllegalStateException} because of the given {@code userGroupId} 
	 *           parameter does not exist in database
	 */
	@Test(expected=IllegalStateException.class)
	public void /*User*/ findAndAddUserToUserGroup_UnknownUserGroupId(/*Long userGroupId, String loginName, String fullName*/) throws ServiceException {
		Long userGroupId = -1L;
		String loginName = "normal";
		String fullName = "Normal Dummy";
		
		userGroupService.findAndAddUserToUserGroup(userGroupId, loginName, fullName);
	}

	/**
	 * Test {@link UserGroupService#findAndAddUserToUserGroup(Long, String, String)} method.
	 * Scenario: throws {@link ServiceException} because of the given {@code loginName} 
	 *           and {@code fulName} parameters are empty
	 */
	@Test(expected=ServiceException.class)
	public void /*User*/ findAndAddUserToUserGroup_EmptyLoginNameAndFullName(/*Long userGroupId, String loginName, String fullName*/) throws ServiceException {
		Long userGroupId; // new dummy userGroup initialized later
		String loginName = "";
		String fullName = "";

		// create a new dummy userGroup 
		Event event = commonDao.findEntityById(Event.class, 1L); // WC2014
		User user = commonDao.findEntityById(User.class, 2L); // normal
		UserGroup userGroup = new UserGroup();
		userGroup.setName("dummyGroup");
		userGroup.setEvent(event);
		userGroup.setPublicEditableAsBoolean(false);
		userGroup.setPublicVisibleAsBoolean(true);
		userGroup.setOwner(user);
		commonDao.persistEntity(userGroup);
		userGroupId = userGroup.getUserGroupId();

		try {
			userGroupService.findAndAddUserToUserGroup(userGroupId, loginName, fullName);
		}
		catch (ServiceException e) {
			assertTrue("There must be a single message in ServiceException named USER_GROUP_FIELDS_EMPTY", 
					e.getMessages().size()==1 && e.getMessages().get(0).getMsgCode().equals("USER_GROUP_FIELDS_EMPTY"));
			throw e;
		}
	}

	/**
	 * Test {@link UserGroupService#findAndAddUserToUserGroup(Long, String, String)} method.
	 * Scenario: throws {@link ServiceException} because of the given {@code loginName} 
	 *           and {@code fulName} parameters do not specify any user in database
	 */
	@Test(expected=ServiceException.class)
	public void /*User*/ findAndAddUserToUserGroup_UnknownUser(/*Long userGroupId, String loginName, String fullName*/) throws ServiceException {
		Long userGroupId; // new dummy userGroup initialized later
		String loginName = "normal";
		String fullName = "Normal Dummy";

		// create a new dummy userGroup 
		Event event = commonDao.findEntityById(Event.class, 1L); // WC2014
		User user = commonDao.findEntityById(User.class, 2L); // normal
		UserGroup userGroup = new UserGroup();
		userGroup.setName("dummyGroup");
		userGroup.setEvent(event);
		userGroup.setPublicEditableAsBoolean(false);
		userGroup.setPublicVisibleAsBoolean(true);
		userGroup.setOwner(user);
		commonDao.persistEntity(userGroup);
		userGroupId = userGroup.getUserGroupId();

		Mockito.when(userDao.findUserByLoginNameOrFullName(loginName, fullName)).thenReturn(null);
		
		try {
			userGroupService.findAndAddUserToUserGroup(userGroupId, loginName, fullName);
		}
		catch (ServiceException e) {
			assertTrue("There must be a single message in ServiceException named NO_USER_BELONGS_TO_USER_GROUP", 
					e.getMessages().size()==1 && e.getMessages().get(0).getMsgCode().equals("NO_USER_BELONGS_TO_USER_GROUP"));
			throw e;
		}
	}

	/**
	 * Test {@link UserGroupService#findAndAddUserToUserGroup(Long, String, String)} method.
	 * Scenario: throws {@link ServiceException} because of the given {@code loginName} 
	 *           and {@code fulName} parameters specify a user in database
	 *           who is already member of the given {@code userGroupId}
	 */
	@Test(expected=ServiceException.class)
	public void /*User*/ findAndAddUserToUserGroup_UserIsInUserGroup(/*Long userGroupId, String loginName, String fullName*/) throws ServiceException {
		Long userGroupId; // new dummy userGroup initialized later
		String loginName = "normal";
		String fullName = "Normal Dummy";

		// create a new dummy userGroup and add normal user to it
		Event event = commonDao.findEntityById(Event.class, 1L); // WC2014
		User user = commonDao.findEntityById(User.class, 2L); // normal
		UserGroup userGroup = new UserGroup();
		userGroup.setName("dummyGroup");
		userGroup.setEvent(event);
		userGroup.setPublicEditableAsBoolean(false);
		userGroup.setPublicVisibleAsBoolean(true);
		userGroup.setOwner(user);
		userGroup.addUser(user);
		commonDao.persistEntity(userGroup);
		userGroupId = userGroup.getUserGroupId();

		Mockito.when(userDao.findUserByLoginNameOrFullName(loginName, fullName)).thenReturn(user);
		
		try {
			userGroupService.findAndAddUserToUserGroup(userGroupId, loginName, fullName);
		}
		catch (ServiceException e) {
			assertTrue("There must be a single message in ServiceException named USER_IS_ALREADY_IN_USER_GROUP", 
					e.getMessages().size()==1 && e.getMessages().get(0).getMsgCode().equals("USER_IS_ALREADY_IN_USER_GROUP"));
			throw e;
		}
	}

	/**
	 * Test {@link UserGroupService#findAndAddUserToUserGroup(Long, String, String)} method.
	 * Scenario: inserts given user to the given userGroup
	 */
	@Test
	public void /*User*/ findAndAddUserToUserGroup(/*Long userGroupId, String loginName, String fullName*/) throws ServiceException {
		Long userGroupId; // new dummy userGroup initialized later
		String loginName = "normal";
		String fullName = "Normal Dummy";

		// create a new dummy userGroup
		Event event = commonDao.findEntityById(Event.class, 1L); // WC2014
		User user = commonDao.findEntityById(User.class, 2L); // normal
		UserGroup userGroup = new UserGroup();
		userGroup.setName("dummyGroup");
		userGroup.setEvent(event);
		userGroup.setPublicEditableAsBoolean(false);
		userGroup.setPublicVisibleAsBoolean(true);
		userGroup.setOwner(user);
		commonDao.persistEntity(userGroup);
		userGroupId = userGroup.getUserGroupId();
		commonDao.refreshEntity(userGroup); // updates .getUsers() otherwise it remains null

		Mockito.when(userDao.findUserByLoginNameOrFullName(loginName, fullName)).thenReturn(user);
		
		User insertedUser =	userGroupService.findAndAddUserToUserGroup(userGroupId, loginName, fullName);
		assertEquals("Inserted user must be the same as the given one", user, insertedUser);
	}

	/**
	 * Test {@link UserGroupService#removeUserFromUserGroup(Long, Long)} method.
	 * Scenario: throws {@link NullPointerException} because of the given {@code null}
	 *           {@code userGroupId} parameter
	 */
	@Test(expected=NullPointerException.class)
	public void removeUserFromUserGroup_NullUserGroupId(/*Long userGroupId, Long userId*/) throws ServiceException {
		Long userGroupId = null;
		Long userId = 2L; // normal
		
		userGroupService.removeUserFromUserGroup(userGroupId, userId);
	}
	
	/**
	 * Test {@link UserGroupService#removeUserFromUserGroup(Long, Long)} method.
	 * Scenario: throws {@link NullPointerException} because of the given {@code null}
	 *           {@code userId} parameter
	 */
	@Test(expected=NullPointerException.class)
	public void removeUserFromUserGroup_NullUserId(/*Long userGroupId, Long userId*/) throws ServiceException {
		Long userGroupId = 1L; // WC2014/Everybody
		Long userId = null;
		
		userGroupService.removeUserFromUserGroup(userGroupId, userId);
	}
	
	/**
	 * Test {@link UserGroupService#removeUserFromUserGroup(Long, Long)} method.
	 * Scenario: throws {@link IllegalStateException} because of the given
	 *           {@code userGroupId} parameter does not exist in database
	 */
	@Test(expected=IllegalStateException.class)
	public void removeUserFromUserGroup_UnknownUserGroupId(/*Long userGroupId, Long userId*/) throws ServiceException {
		Long userGroupId = -1L;
		Long userId = 2L; // normal
		
		userGroupService.removeUserFromUserGroup(userGroupId, userId);
	}
	
	/**
	 * Test {@link UserGroupService#removeUserFromUserGroup(Long, Long)} method.
	 * Scenario: throws {@link IllegalStateException} because of the given
	 *           {@code userId} parameter does not exist in database
	 */
	@Test(expected=IllegalStateException.class)
	public void removeUserFromUserGroup_UnknownUserId(/*Long userGroupId, Long userId*/) throws ServiceException {
		Long userGroupId; // new dummy userGroup initialized later
		Long userId = -1L;
		
		// create a new dummy userGroup 
		Event event = commonDao.findEntityById(Event.class, 1L); // WC2014
		User user = commonDao.findEntityById(User.class, 2L); // normal
		UserGroup userGroup = new UserGroup();
		userGroup.setName("dummyGroup");
		userGroup.setEvent(event);
		userGroup.setPublicEditableAsBoolean(false);
		userGroup.setPublicVisibleAsBoolean(true);
		userGroup.setOwner(user);
		commonDao.persistEntity(userGroup);
		userGroupId = userGroup.getUserGroupId();

		userGroupService.removeUserFromUserGroup(userGroupId, userId);
	}
	
	/**
	 * Test {@link UserGroupService#removeUserFromUserGroup(Long, Long)} method.
	 * Scenario: successfully removes given user from the given userGroup in database
	 */
	@Test
	public void removeUserFromUserGroup(/*Long userGroupId, Long userId*/) throws ServiceException {
		Long userGroupId; // new dummy userGroup initialized later
		Long userId = 2L; // normal
		
		// create a new dummy userGroup and add normal user to it
		Event event = commonDao.findEntityById(Event.class, 1L); // WC2014
		User user = commonDao.findEntityById(User.class, userId); // normal
		UserGroup userGroup = new UserGroup();
		userGroup.setName("dummyGroup");
		userGroup.setEvent(event);
		userGroup.setPublicEditableAsBoolean(false);
		userGroup.setPublicVisibleAsBoolean(true);
		userGroup.setOwner(user);
		userGroup.addUser(user);
		commonDao.persistEntity(userGroup);
		userGroupId = userGroup.getUserGroupId();
		
		userGroupService.removeUserFromUserGroup(userGroupId, userId);
		
		assertTrue("UserGroup still should be in the persistence context.", commonDao.containsEntity(userGroup));
		commonDao.refreshEntity(userGroup);
		assertTrue("UserGroup should not contain user after removal.", !userGroup.getUsers().contains(user));
	}
	
	/**
	 * Test {@link UserGroupService#retrieveUserCertificates(Long, Long)} method.
	 * Scenario: throws {@link NullPointerException} because of the given {@code null}
	 *           {@code eventId} parameter
	 */
	@Test(expected=NullPointerException.class)
	public void /*List<UserCertificate>*/ retrieveUserCertificates_NullEventId(/*Long eventId, Long userId*/) throws ServiceException {
		Long eventId = null;
		Long userId = 2L; // normal
		
		userGroupService.retrieveUserCertificates(eventId, userId);
	}

	/**
	 * Test {@link UserGroupService#retrieveUserCertificates(Long, Long)} method.
	 * Scenario: throws {@link NullPointerException} because of the given {@code null}
	 *           {@code userId} parameter
	 */
	@Test(expected=NullPointerException.class)
	public void /*List<UserCertificate>*/ retrieveUserCertificates_NullUserId(/*Long eventId, Long userId*/) throws ServiceException {
		Long eventId = 1L; // WC2014
		Long userId = null;
		
		userGroupService.retrieveUserCertificates(eventId, userId);
	}

	/**
	 * Test {@link UserGroupService#retrieveUserCertificates(Long, Long)} method.
	 * Scenario: throws {@link IllegalStateException} because of the given unknown
	 *           {@code userId} parameter
	 */
	@Test(expected=IllegalStateException.class)
	public void /*List<UserCertificate>*/ retrieveUserCertificates_InvalidUserId(/*Long eventId, Long userId*/) throws ServiceException {
		Long eventId = 1L; // WC2014
		Long userId = -1L; // unknown
		
		userGroupService.retrieveUserCertificates(eventId, userId);
	}
	
	/**
	 * Test {@link UserGroupService#retrieveUserCertificates(Long, Long)} method.
	 * Scenario: throws {@link ServiceException} because of content generation
	 *           error
	 */
	@Test(expected=ServiceException.class)
	public void /*ByteArrayOutputStream*/ printUserCertificate_generationContentError(/*UserCertificate userCertificate, Locale locale*/) throws ServiceException {
		UserCertificate userCertificate = new UserCertificate();
		userCertificate.setUserGroupId(1L);
		Locale locale = new Locale("en");
//		Properties properties = new Properties();
//		properties.put("userGroupId", 1L);
//		properties.put("eventShortDescWithYear", "");
//		properties.put("userLoginName", "");
//		properties.put("userFullName", "");
//		properties.put("userGroupName", 0);
//		properties.put("maximumScoreByEvent", 0);
//		properties.put("firstUserScore", 0);
//		properties.put("userScore", 0);
//		properties.put("numberOfMembers", 0);
//		properties.put("numberOfEverybodyMembers", 0);
//		properties.put("userGroupPosition", 1);
//		properties.put("userGroupLastPosition",  1);
//		properties.put("everybody", false);
//		properties.put("score", 0.1);

		List<ParameterizedMessage> errMsgs = new ArrayList<>();
		errMsgs.add(ParameterizedMessage.create("TEMPLATE_GENERATION_FAILED", TemplateId.USER_CERTIFICATE_PDF));
		
		//Mockito.when(templateService.generatePDFContent(TemplateId.USER_CERTIFICATE_PDF, properties, locale)).thenThrow(new ServiceException(null));
		Mockito.when(templateService.generatePDFContent(ArgumentMatchers.eq(TemplateId.USER_CERTIFICATE_PDF), ArgumentMatchers.any(Properties.class), ArgumentMatchers.eq(locale))).thenThrow(new ServiceException(errMsgs));
		
		userGroupService.printUserCertificate(userCertificate, locale);
	}

	/**
	 * Test {@link UserGroupService#retrieveUserCertificates(Long, Long)} method.
	 * Scenario: successfully generates PDF content
	 */
	@Test
	public void /*ByteArrayOutputStream*/ printUserCertificate(/*UserCertificate userCertificate, Locale locale*/) throws ServiceException {
		UserCertificate userCertificate = new UserCertificate();
		userCertificate.setUserGroupId(1L);
		Locale locale = new Locale("en");
//		Properties properties = new Properties();
//		properties.put("userGroupId", 1L);
//		properties.put("eventShortDescWithYear", "");
//		properties.put("userLoginName", "");
//		properties.put("userFullName", "");
//		properties.put("userGroupName", 0);
//		properties.put("maximumScoreByEvent", 0);
//		properties.put("firstUserScore", 0);
//		properties.put("userScore", 0);
//		properties.put("numberOfMembers", 0);
//		properties.put("numberOfEverybodyMembers", 0);
//		properties.put("userGroupPosition", 1);
//		properties.put("userGroupLastPosition",  1);
//		properties.put("everybody", false);
//		properties.put("score", 0.1);

		ByteArrayOutputStream expectedContent = null;
		
		//Mockito.when(templateService.generatePDFContent(TemplateId.USER_CERTIFICATE_PDF, properties, locale)).thenThrow(new ServiceException(null));
		Mockito.when(templateService.generatePDFContent(ArgumentMatchers.eq(TemplateId.USER_CERTIFICATE_PDF), ArgumentMatchers.any(Properties.class), ArgumentMatchers.eq(locale))).thenReturn(expectedContent);
		
		ByteArrayOutputStream content = userGroupService.printUserCertificate(userCertificate, locale);
		assertEquals("Retrieved content should be equal to the expected one.", expectedContent, content);
		
		Mockito.verify(templateService).generatePDFContent(ArgumentMatchers.eq(TemplateId.USER_CERTIFICATE_PDF), ArgumentMatchers.any(Properties.class), ArgumentMatchers.eq(locale));
	}
	
//	/**
//	 * Test {@link UserGroupService#retrieveTopUsers()} method.
//	 * Scenario: successfully retrieves list from cache
//	 */
//	@Test
//	public void /*List<UserCertificate>*/ retrieveTopUsers_fromCache() throws ServiceException {
//		UserCertificate userCertificate = new UserCertificate();
//		userCertificate.setUserGroupId(-1L);
//		userCertificate.setUserLoginName("dummy");
//		List<UserCertificate> expectedUserCertificates = Arrays.asList(userCertificate);
//		
//		Mockito.when(applicationService.getCachedTopUsers()).thenReturn(expectedUserCertificates);
//		
//		List<UserCertificate> userCertificates = userGroupService.retrieveTopUsers();
//		assertEquals("Retrieved list should be equal to the expected one.", expectedUserCertificates, userCertificates);
//	}

	/**
	 * Test {@link UserGroupService#retrieveTopUsers()} method.
	 * Scenario: successfully retrieves list
	 */
	@Test
	public void /*List<UserCertificate>*/ retrieveTopUsers() throws ServiceException {
		UserCertificate userCertificate = new UserCertificate();
		userCertificate.setUserGroupId(-1L);
		userCertificate.setUserLoginName("dummy");
		List<UserCertificate> expectedUserCertificates = Arrays.asList(userCertificate);

//		List<UserCertificate> emptyUserCertificates = new ArrayList<>();
//		Mockito.when(applicationService.getCachedTopUsers()).thenReturn(emptyUserCertificates);
		
		Event event = commonDao.findEntityById(Event.class, 1L); // WC2014
		List<Event> events = Arrays.asList(event);
		Mockito.when(eventService.findCompletedEvents()).thenReturn(events);
		
		Integer maxNumberOfEverybodyMembers = 1;
		Mockito.when(userGroupDao.retrieveNumberOfUsersWithBetsByUserGroup(UserGroup.EVERYBODY_USER_GROUP_ID, event.getEventId())).thenReturn(maxNumberOfEverybodyMembers);
		
		//Mockito.when(userGroupServicePartial.retrieveTopUsersByEvent(event.getEventId(), maxNumberOfEverybodyMembers)).thenReturn(expectedUserCertificates); // this calls the real method at once
		Mockito.doReturn(expectedUserCertificates).when(userGroupServicePartial).retrieveTopUsersByEvent(event.getEventId(), maxNumberOfEverybodyMembers); // use this instead of the previous line in case of partial mock 
		
		List<UserCertificate> userCertificates = userGroupServicePartial.retrieveTopUsers();
		assertEquals("Retrieved list should be equal to the expected one.", expectedUserCertificates, userCertificates);
	}
	
	/**
	 * Test {@link UserGroupService#retrieveTopUsersByEvent(Long, Integer)} method.
	 * Scenario: throws {@link NullPointerException} because of the given {@code null}
	 *           {@code eventId} parameter

	 */
	@Test(expected=NullPointerException.class)
	public void /*List<UserCertificate>*/ retrieveTopUsersByEvent_NullEventId(/*Long eventId, Integer maxNumberOfEverybodyMembers*/) throws ServiceException {
		Long eventId = null;
		Integer maxNumberOfEverybodyMembers = 100;
		
		userGroupService.retrieveTopUsersByEvent(eventId, maxNumberOfEverybodyMembers);
	}
	
	/**
	 * Test {@link UserGroupService#retrieveTopUsersByEvent(Long, Integer)} method.
	 * Scenario: throws {@link IllegalStateException} because of the given unknown
	 *           {@code eventId} parameter

	 */
	@Test(expected=IllegalStateException.class)
	public void /*List<UserCertificate>*/ retrieveTopUsersByEvent_InvalidEventId(/*Long eventId, Integer maxNumberOfEverybodyMembers*/) throws ServiceException {
		Long eventId = -1L;
		Integer maxNumberOfEverybodyMembers = 100;
		
		userGroupService.retrieveTopUsersByEvent(eventId, maxNumberOfEverybodyMembers);
	}
	
	/**
	 * Test {@link UserGroupService#retrieveTopUsersByEvent(Long, Integer)} method.
	 * Scenario: throws {@link IllegalStateException} because of the event belongs
	 *           to the given {@code eventId} parameter is incomplete

	 */
	@Test(expected=IllegalStateException.class)
	public void /*List<UserCertificate>*/ retrieveTopUsersByEvent_IncompleteEventId(/*Long eventId, Integer maxNumberOfEverybodyMembers*/) throws ServiceException {
		Long eventId = 1L; // WC2014
		Integer maxNumberOfEverybodyMembers = 100;
		
		Mockito.when(applicationService.getEventCompletionPercentCache(eventId)).thenReturn(99); // incomplete event
		
		userGroupService.retrieveTopUsersByEvent(eventId, maxNumberOfEverybodyMembers);
	}
	
	/**
	 * Test {@link UserGroupService#retrieveTopUsersByEvent(Long, Integer)} method.
	 * Scenario: successfully retrieves list
	 */
	@Test
	public void /*List<UserCertificate>*/ retrieveTopUsersByEvent(/*Long eventId, Integer maxNumberOfEverybodyMembers*/) throws ServiceException {
		Long eventId = 1L; // WC2014
		Integer maxNumberOfEverybodyMembers = 100;
		
		Mockito.when(applicationService.getEventCompletionPercentCache(eventId)).thenReturn(100);; // complete event
		
		int maximumScoreByEvent = 213;
		Mockito.when(betService.retrieveMaximumScoreByEvent(eventId)).thenReturn(maximumScoreByEvent);
		
		Long adminUserId = 1L; // admin
		User adminUser = commonDao.findEntityById(User.class, adminUserId);
		Mockito.when(userDao.findFirstAdminUser()).thenReturn(adminUser);
		
		UserGroup userGroup = new UserGroup();
		Event event = commonDao.findEntityById(Event.class, eventId);
		userGroup.setEvent(event);
		userGroup.setName(UserGroup.EVERYBODY_NAME);
		userGroup.setOwner(adminUser);
		userGroup.setPublicVisibleAsBoolean(true);
		userGroup.setPublicEditableAsBoolean(false);
		userGroup.setUserGroupId(UserGroup.EVERYBODY_USER_GROUP_ID);
		Mockito.when(userGroupDao.createVirtualEverybodyUserGroup(eventId, adminUser.getUserId())).thenReturn(userGroup);

		UserPosition userPosition1 = new UserPosition();
		userPosition1.setFullName("Dummy1 Dum");
		userPosition1.setLoginName("dummy1");
		userPosition1.setUserId(-1L);
		userPosition1.setPosition(1);
		userPosition1.setScore(50);
		UserPosition userPosition2 = new UserPosition();
		userPosition2.setFullName("Dummy2 Dum");
		userPosition2.setLoginName("dummy2");
		userPosition2.setUserId(-2L);
		userPosition2.setPosition(2);
		userPosition2.setScore(48);
		UserPosition userPosition3 = new UserPosition();
		userPosition3.setFullName("Dummy3 Dum");
		userPosition3.setLoginName("dummy3");
		userPosition3.setUserId(-3L);
		userPosition3.setPosition(3);
		userPosition3.setScore(42);
		List<UserPosition> userPositions = Arrays.asList(userPosition1, userPosition2, userPosition3);
		Mockito.doReturn(userPositions).when(userGroupServicePartial).retrieveUserPositions(eventId, userGroup.getUserGroupId());
		
		List<UserCertificate> userCertificates = userGroupServicePartial.retrieveTopUsersByEvent(eventId, maxNumberOfEverybodyMembers);
		assertTrue("Result list should have expected size", userCertificates!=null && userPositions.size() == userCertificates.size());
	}
}
