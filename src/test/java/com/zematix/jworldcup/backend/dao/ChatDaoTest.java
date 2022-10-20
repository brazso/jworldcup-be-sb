package com.zematix.jworldcup.backend.dao;

import static com.google.common.base.Preconditions.checkState;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import javax.inject.Inject;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import com.zematix.jworldcup.backend.entity.Chat;
import com.zematix.jworldcup.backend.entity.Event;
import com.zematix.jworldcup.backend.entity.User;
import com.zematix.jworldcup.backend.entity.UserGroup;
import com.zematix.jworldcup.backend.exception.ServiceException;
import com.zematix.jworldcup.backend.util.CommonUtil;

/**
 * Contains test functions of {@link EventDao} class.
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles({"develop", "test"})
@Transactional
public class ChatDaoTest {

	@Inject
	private ChatDao chatDao;

	@Inject
	private CommonDao commonDao;

	/**
	 * Test {@link ChatDao#findAllChats()} method.
	 * Scenario: successfully retrieves a list of all Chat entities
	 */
	@Test
	public void /*List<Event>*/ findAllChats() {
		List<Chat> allExpectedChats = commonDao.findAllEntities(Chat.class);
		List<Chat> allChats = chatDao.findAllChats();
		
		assertEquals(new HashSet<>(allExpectedChats), new HashSet<>(allChats));
	}
	
	/**
	 * Test {@link ChatDao#retrieveChats(Long, Long)} method.
	 * Scenario: throws {@link NullPointerException} because of the given {@code null} 
	 *           {@code userGroupId} parameter
	 */
	@Test(expected = NullPointerException.class)
	public void /*List<Chat>*/ retrieveChats_NullUserGroupId(/*Long eventId, Long userGroupId*/) throws ServiceException {
		Long eventId = 1L; // WC2014
		Long userGroupId = null;

		chatDao.retrieveChats(eventId, userGroupId);
	}
	
	/**
	 * Test {@link ChatDao#retrieveChats(Long, Long)} method.
	 * Scenario: throws {@link IllegalArgumentException} because of the given {@code null} 
	 *           {@code eventId} parameter and virtual everybody {@code userGroupId} parameter
	 */
	@Test(expected = IllegalArgumentException.class)
	public void /*List<Chat>*/ retrieveChats_NullEventId(/*Long eventId, Long userGroupId*/) throws ServiceException {
		Long eventId = null;
		Long userGroupId = UserGroup.EVERYBODY_USER_GROUP_ID;
		
		chatDao.retrieveChats(eventId, userGroupId);
	}
	
	/**
	 * Test {@link ChatDao#retrieveChats(Long, Long)} method.
	 * Scenario: successfully retrieves a list of all Chat entities belongs
	 *           to the given event and virtual everybody userGroup
	 */
	@Test
	public void /*List<Chat>*/ retrieveChats_EverybodyUserGroupId(/*Long eventId, Long userGroupId*/) throws ServiceException {
		Long eventId = 1L; // WC2014
		Long userGroupId = UserGroup.EVERYBODY_USER_GROUP_ID;
		
		// inserts test chat messages into database
		Event event = commonDao.findEntityById(Event.class, eventId);
		User user = commonDao.findEntityById(User.class, 2L); // normal
		List<Chat> expectedChats = new ArrayList<>();
		for (int i=0; i<ChatDao.MAX_USERGROUP_CHAT_MESSAGES+1; i++) {
			Chat chat = new Chat();
			chat.setEvent(event);
			LocalDateTime actualDateTime = LocalDateTime.now();
			chat.setModificationTime(actualDateTime);
			chat.setUser(user);
			chat.setUserGroup(null); // virtual everybody userGroup
			chat.setMessage("Dummy"+ i);
			commonDao.persistEntity(chat);
			if (i < ChatDao.MAX_USERGROUP_CHAT_MESSAGES) {
				expectedChats.add(chat);
			}
		}
		commonDao.flushEntityManager();
		
		List<Chat> chats = chatDao.retrieveChats(eventId, userGroupId);
		assertEquals(expectedChats, chats);
	}

	/**
	 * Test {@link ChatDao#retrieveChats(Long, Long)} method.
	 * Scenario: successfully retrieves a list of all Chat entities belongs
	 *           to the given userGroup
	 */
	@Test
	public void /*List<Chat>*/ retrieveChats(/*Long eventId, Long userGroupId*/) throws ServiceException {
		Long eventId = null; // does not matter
		
		UserGroup userGroup = new UserGroup();
		eventId = 1L;
		Event event = commonDao.findEntityById(Event.class, eventId); // WC2014
		checkState(event != null, String.format("No \"Event\" entity belongs to \"eventId\"=%d in database.", eventId));
		userGroup.setEvent(event);
		userGroup.setName("Dummy user group");
		Long userId = 2L; // normal
		User user = commonDao.findEntityById(User.class, userId);
		checkState(user != null, String.format("No \"User\" entity belongs to \"userId\"=%d in database.", userId));
		userGroup.setOwner(user);
		userGroup.setPublicVisibleAsBoolean(true);
		userGroup.setPublicEditableAsBoolean(false);
		commonDao.persistEntity(userGroup);
		Long userGroupId = userGroup.getUserGroupId(); // generated value
				
		// inserts test chat messages into database
		List<Chat> expectedChats = new ArrayList<>();
		for (int i=0; i<ChatDao.MAX_USERGROUP_CHAT_MESSAGES+1; i++) {
			Chat chat = new Chat();
			chat.setEvent(event);
			LocalDateTime actualDateTime = LocalDateTime.now();
			chat.setModificationTime(actualDateTime);
			chat.setUser(user);
			chat.setUserGroup(userGroup);
			chat.setMessage("Dummy"+ i);
			commonDao.persistEntity(chat);
			if (i < ChatDao.MAX_USERGROUP_CHAT_MESSAGES) {
				expectedChats.add(chat);
			}
		}
		commonDao.flushEntityManager();
		
		List<Chat> chats = chatDao.retrieveChats(eventId, userGroupId);
		assertEquals(expectedChats, chats);
	}

	/**
	 * Test {@link ChatDao#truncateChats(Long, Long)} method.
	 * Scenario: throws {@link NullPointerException} because of the given {@code null} 
	 *           {@code userGroupId} parameter
	 */
	@Test(expected = NullPointerException.class)
	public void /*List<Chat>*/ truncateChats_NullUserGroupId(/*Long eventId, Long userGroupId*/) throws ServiceException {
		Long eventId = 1L; // WC2014
		Long userGroupId = null;

		chatDao.truncateChats(eventId, userGroupId);
	}
	
	/**
	 * Test {@link ChatDao#truncateChats(Long, Long)} method.
	 * Scenario: throws {@link IllegalArgumentException} because of the given {@code null} 
	 *           {@code eventId} parameter and virtual everybody {@code userGroupId} parameter
	 */
	@Test(expected = IllegalArgumentException.class)
	public void /*List<Chat>*/ truncateChats_NullEventId(/*Long eventId, Long userGroupId*/) throws ServiceException {
		Long eventId = null;
		Long userGroupId = UserGroup.EVERYBODY_USER_GROUP_ID;

		chatDao.truncateChats(eventId, userGroupId);
	}
	
	/**
	 * Test {@link ChatDao#truncateChats(Long, Long)} method.
	 * Scenario: successfully truncates the first element of the list 
	 *           of all Chat entities belongs to the given event and 
	 *           virtual everybody userGroup.
	 */
	@Test
	public void /*List<Chat>*/ truncateChats_EverybodyUserGroupId(/*Long eventId, Long userGroupId*/) throws ServiceException {
		Long eventId = 1L; // WC2014
		Long userGroupId = UserGroup.EVERYBODY_USER_GROUP_ID;
		
		// inserts test chat messages into database
		Event event = commonDao.findEntityById(Event.class, eventId);
		User user = commonDao.findEntityById(User.class, 2L); // normal
		List<Chat> expectedChats = new ArrayList<>();
		for (int i=0; i<ChatDao.MAX_USERGROUP_CHAT_MESSAGES+1; i++) {
			Chat chat = new Chat();
			chat.setEvent(event);
			LocalDateTime actualDateTime = LocalDateTime.now();
			chat.setModificationTime(actualDateTime);
			chat.setUser(user);
			chat.setMessage("Dummy"+ i);
			commonDao.persistEntity(chat);
			if (i != 0) {
				expectedChats.add(chat); // first one will be truncated
			}
		}
		commonDao.flushEntityManager();
		
		boolean truncateChats = chatDao.truncateChats(eventId, userGroupId);
		assertTrue(truncateChats);
		List<Chat> chats = commonDao.findAllEntities(Chat.class);
		assertEquals(expectedChats, chats);
	}

	/**
	 * Test {@link ChatDao#truncateChats(Long, Long)} method.
	 * Scenario: successfully truncates the first element of the list 
	 *           of all Chat entities belongs to the given userGroup.
	 */
	@Test
	public void /*List<Chat>*/ truncateChats(/*Long eventId, Long userGroupId*/) throws ServiceException {
		Long eventId = null; // does not matter
		
		UserGroup userGroup = new UserGroup();
		eventId = 1L;
		Event event = commonDao.findEntityById(Event.class, eventId); // WC2014
		checkState(event != null, String.format("No \"Event\" entity belongs to \"eventId\"=%d in database.", eventId));
		userGroup.setEvent(event);
		userGroup.setName("Dummy user group");
		Long userId = 2L; // normal
		User user = commonDao.findEntityById(User.class, userId);
		checkState(user != null, String.format("No \"User\" entity belongs to \"userId\"=%d in database.", userId));
		userGroup.setOwner(user);
		userGroup.setPublicVisibleAsBoolean(true);
		userGroup.setPublicEditableAsBoolean(false);
		commonDao.persistEntity(userGroup);
		Long userGroupId = userGroup.getUserGroupId(); // generated value

		// inserts test chat messages into database
		List<Chat> expectedChats = new ArrayList<>();
		for (int i=0; i<ChatDao.MAX_USERGROUP_CHAT_MESSAGES+1; i++) {
			Chat chat = new Chat();
			chat.setEvent(event);
			LocalDateTime actualDateTime = LocalDateTime.now();
			chat.setModificationTime(actualDateTime);
			chat.setUser(user);
			chat.setUserGroup(userGroup);
			chat.setMessage("Dummy"+ i);
			commonDao.persistEntity(chat);
			if (i != 0) {
				expectedChats.add(chat); // first one will be truncated
			}
		}
		commonDao.flushEntityManager();
		
		boolean truncateChats = chatDao.truncateChats(eventId, userGroupId);
		assertTrue(truncateChats);
		List<Chat> chats = commonDao.findAllEntities(Chat.class);
		assertEquals(expectedChats, chats);
	}

	/**
	 * Test {@link ChatDao#retrieveLatestChat(Long, Long)} method.
	 * Scenario: successfully retrieves no chat message because of unknown eventId parameter
	 */
	@Test
	public void /*List<UserGroup>*/ retrieveLatestChat_UnknownEventId(/*Long eventId, Long userId*/) throws ServiceException {
		Long eventId = -1L; // unknown
		Long userId = 2L; // normal
		
		Chat chat = chatDao.retrieveLatestChat(eventId, userId);
		assertNull(chat);
	}

	/**
	 * Test {@link ChatDao#retrieveLatestChat(Long, Long)} method.
	 * Scenario: successfully retrieves the latest chat message.
	 */
	@Test
	public void /*List<UserGroup>*/ retrieveLatestChat(/*Long eventId, Long userId*/) throws ServiceException {
		Long eventId = 1L; // WC2014
		Long userId = 2L; // normal

		// persists a new userGroup which belongs to our user (by userId)
		UserGroup userGroup = new UserGroup();
		Event event = commonDao.findEntityById(Event.class, eventId); // WC2014
		checkState(event != null, String.format("No \"Event\" entity belongs to \"eventId\"=%d in database.", eventId));
		userGroup.setEvent(event);
		userGroup.setName("Dummy user group");
		User user = commonDao.findEntityById(User.class, userId);
		checkState(user != null, String.format("No \"User\" entity belongs to \"userId\"=%d in database.", userId));
		userGroup.setOwner(user);
		userGroup.setPublicVisibleAsBoolean(true);
		userGroup.setPublicEditableAsBoolean(false);
		userGroup.getUsers().add(user);
		user.getUserGroups().add(userGroup);
		commonDao.persistEntity(userGroup);

		// persists some test chat messages into our new userGroup, where
		// the first one has the latest modificationTime
		List<Chat> chats = new ArrayList<>();
		for (int i=0; i<5; i++) {
			Chat chat = new Chat();
			chat.setEvent(event);
			LocalDateTime actualDateTime = LocalDateTime.now();
			chat.setModificationTime(CommonUtil.plusDays(actualDateTime, -i));
			chat.setUser(user);
			chat.setUserGroup(userGroup);
			chat.setMessage("Dummy"+ i);
			commonDao.persistEntity(chat);
			if (i < ChatDao.MAX_USERGROUP_CHAT_MESSAGES) {
				chats.add(chat);
			}
		}
		commonDao.flushEntityManager();
		Chat expectedChat = chats.get(0);

		Chat chat = chatDao.retrieveLatestChat(eventId, userId);
		assertEquals(expectedChat, chat);
	}

	/**
	 * Test {@link ChatDao#retrieveLatestChat(Long, Long)} method.
	 * Scenario: successfully retrieves the latest chat message of more userGroups.
	 */
	@Test
	public void /*List<UserGroup>*/ retrieveLatestChatWithMoreUserGroups(/*Long eventId, Long userId*/) throws ServiceException {
		Long eventId = 1L; // WC2014
		Long userId = 2L; // normal
		Long otherEventId = 2L; // EC2016
		Long otherUserId = 1L; // admin

		// persists a new userGroup which belongs to our user (by userId)
		UserGroup userGroup1 = new UserGroup();
		Event event = commonDao.findEntityById(Event.class, eventId); // WC2014
		checkState(event != null, String.format("No \"Event\" entity belongs to \"eventId\"=%d in database.", eventId));
		userGroup1.setEvent(event);
		userGroup1.setName("Dummy user group1");
		User user = commonDao.findEntityById(User.class, userId);
		checkState(user != null, String.format("No \"User\" entity belongs to \"userId\"=%d in database.", userId));
		userGroup1.setOwner(user);
		userGroup1.setPublicVisibleAsBoolean(true);
		userGroup1.setPublicEditableAsBoolean(false);
		user.getUserGroups().add(userGroup1);
		commonDao.persistEntity(userGroup1);
		
		Chat chat = new Chat();
		chat.setEvent(event);
		LocalDateTime actualDateTime = LocalDateTime.now();
		chat.setModificationTime(CommonUtil.plusDays(actualDateTime, -3));
		chat.setUser(user);
		chat.setUserGroup(userGroup1);
		chat.setMessage("Dummy");
		commonDao.persistEntity(chat);
		Chat expectedChat = chat;

		// persists a new userGroup which belongs to our user (by userId) but on other event
		UserGroup userGroup2 = new UserGroup();
		Event otherEvent = commonDao.findEntityById(Event.class, otherEventId); // EC2016
		checkState(otherEvent != null, String.format("No \"Event\" entity belongs to \"eventId\"=%d in database.", otherEventId));
		userGroup2.setEvent(otherEvent);
		userGroup2.setName("Dummy user group2");
		userGroup2.setOwner(user);
		userGroup2.setPublicVisibleAsBoolean(true);
		userGroup2.setPublicEditableAsBoolean(false);
		userGroup2.getUsers().add(user);
		user.getUserGroups().add(userGroup2);
		commonDao.persistEntity(userGroup2);

		/*Chat*/ chat = new Chat();
		chat.setEvent(otherEvent);
		chat.setModificationTime(CommonUtil.plusDays(actualDateTime, -2));
		chat.setUser(user);
		chat.setUserGroup(userGroup2);
		chat.setMessage("Dummy");
		commonDao.persistEntity(chat);

		// persists a new userGroup which does not belongs to our user (by userId)
		UserGroup userGroup3 = new UserGroup();
		userGroup3.setEvent(event);
		userGroup3.setName("Dummy user group3");
		User otherUser = commonDao.findEntityById(User.class, otherUserId);
		checkState(otherUser != null, String.format("No \"User\" entity belongs to \"userId\"=%d in database.", otherUserId));
		userGroup3.setOwner(otherUser);
		userGroup3.setPublicVisibleAsBoolean(true);
		userGroup3.setPublicEditableAsBoolean(false);
		userGroup3.getUsers().add(otherUser);
		otherUser.getUserGroups().add(userGroup3);
		commonDao.persistEntity(userGroup3);
		
		/*Chat*/ chat = new Chat();
		chat.setEvent(event);
		chat.setModificationTime(CommonUtil.plusDays(actualDateTime, -1));
		chat.setUser(user);
		chat.setUserGroup(userGroup3);
		chat.setMessage("Dummy");
		commonDao.persistEntity(chat);

		commonDao.flushEntityManager();

		chat = chatDao.retrieveLatestChat(eventId, userId);
		assertEquals(expectedChat, chat);
	}

	/**
	 * Test {@link ChatDao#retrieveLatestChat(Long, Long)} method.
	 * Scenario: successfully retrieves the latest chat message of more userGroups.
	 */
	@Test
	public void /*List<UserGroup>*/ retrieveLatestChatWithMoreUserGroupsIncludedVirtual(/*Long eventId, Long userId*/) throws ServiceException {
		Long eventId = 1L; // WC2014
		Long userId = 2L; // normal
		Long otherEventId = 2L; // EC2016
		Long otherUserId = 1L; // admin

		// persists a new userGroup which belongs to our user (by userId)
		UserGroup userGroup1 = new UserGroup();
		Event event = commonDao.findEntityById(Event.class, eventId); // WC2014
		checkState(event != null, String.format("No \"Event\" entity belongs to \"eventId\"=%d in database.", eventId));
		userGroup1.setEvent(event);
		userGroup1.setName("Dummy user group1");
		User user = commonDao.findEntityById(User.class, userId);
		checkState(user != null, String.format("No \"User\" entity belongs to \"userId\"=%d in database.", userId));
		userGroup1.setOwner(user);
		userGroup1.setPublicVisibleAsBoolean(true);
		userGroup1.setPublicEditableAsBoolean(false);
		userGroup1.getUsers().add(user);
		user.getUserGroups().add(userGroup1);
		commonDao.persistEntity(userGroup1);
		
		Chat chat = new Chat();
		chat.setEvent(event);
		LocalDateTime actualDateTime = LocalDateTime.now();
		chat.setModificationTime(CommonUtil.plusDays(actualDateTime, -3));
		chat.setUser(user);
		chat.setUserGroup(userGroup1);
		chat.setMessage("Dummy");
		commonDao.persistEntity(chat);

		// persists a new userGroup which belongs to our user (by userId) but on other event
		UserGroup userGroup2 = new UserGroup();
		Event otherEvent = commonDao.findEntityById(Event.class, otherEventId); // EC2016
		checkState(otherEvent != null, String.format("No \"Event\" entity belongs to \"eventId\"=%d in database.", otherEventId));
		userGroup2.setEvent(otherEvent);
		userGroup2.setName("Dummy user group2");
		userGroup2.setOwner(user);
		userGroup2.setPublicVisibleAsBoolean(true);
		userGroup2.setPublicEditableAsBoolean(false);
		userGroup2.getUsers().add(user);
		user.getUserGroups().add(userGroup2);
		commonDao.persistEntity(userGroup2);

		/*Chat*/ chat = new Chat();
		chat.setEvent(otherEvent);
		chat.setModificationTime(CommonUtil.plusDays(actualDateTime, -2));
		chat.setUser(user);
		chat.setUserGroup(userGroup2);
		chat.setMessage("Dummy");
		commonDao.persistEntity(chat);

		// persists a new userGroup which does not belongs to our user (by userId)
		UserGroup userGroup3 = new UserGroup();
		userGroup3.setEvent(event);
		userGroup3.setName("Dummy user group3");
		User otherUser = commonDao.findEntityById(User.class, otherUserId);
		checkState(otherUser != null, String.format("No \"User\" entity belongs to \"userId\"=%d in database.", otherUserId));
		userGroup3.setOwner(otherUser);
		userGroup3.setPublicVisibleAsBoolean(true);
		userGroup3.setPublicEditableAsBoolean(false);
		userGroup3.getUsers().add(otherUser);
		otherUser.getUserGroups().add(userGroup3);
		commonDao.persistEntity(userGroup3);
		
		/*Chat*/ chat = new Chat();
		chat.setEvent(event);
		chat.setModificationTime(CommonUtil.plusDays(actualDateTime, -1));
		chat.setUser(user);
		chat.setUserGroup(userGroup3);
		chat.setMessage("Dummy");
		commonDao.persistEntity(chat);

		// persists a new chat message belongs to virtual everybody userGroup on this event
		/*Chat*/ chat = new Chat();
		chat.setEvent(event);
		chat.setModificationTime(CommonUtil.plusDays(actualDateTime, 0));
		chat.setUser(user);
		chat.setUserGroup(null); // virtual everybody userGroup
		chat.setMessage("Dummy");
		commonDao.persistEntity(chat);
		Chat expectedChat = chat;

		commonDao.flushEntityManager();

		chat = chatDao.retrieveLatestChat(eventId, userId);
		assertEquals(expectedChat, chat);
	}
}
