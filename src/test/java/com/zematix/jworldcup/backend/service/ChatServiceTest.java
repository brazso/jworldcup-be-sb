package com.zematix.jworldcup.backend.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import com.zematix.jworldcup.backend.dao.ChatDao;
import com.zematix.jworldcup.backend.dao.CommonDao;
import com.zematix.jworldcup.backend.entity.Chat;
import com.zematix.jworldcup.backend.entity.Event;
import com.zematix.jworldcup.backend.entity.UserGroup;
import com.zematix.jworldcup.backend.exception.ServiceException;

/**
 * Contains test functions of {@link EventService} class.
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles({"development", "test"})
@Transactional
public class ChatServiceTest {
	
	@Inject
	private ChatService chatService;
	
	@Inject
	private CommonDao commonDao;
	
	@MockBean
	private ChatDao chatDao; // used by methods of EventService
	
	@MockBean
	private ApplicationService applicationService;

	/**
	 * Test {@link ChatService#retrieveChats(UserGroup)} method.
	 * Scenario: throws {@link NullPointerException} because of the given {@code null} 
	 *           {@code userGroupId} parameter
	 */
	@Test(expected=NullPointerException.class)
	public void /*List<Chat>*/ retrieveChats_NullUserGroup(/*UserGroup userGroup*/) throws ServiceException {
		UserGroup userGroup = null;
		
		chatService.retrieveChats(userGroup);
	}

	/**
	 * Test {@link ChatService#retrieveChats(UserGroup)} method.
	 * Scenario: successfully retrieves all chats belongs the given
	 *           userGroup parameter
	 */
	@Test
	public void /*List<Chat>*/ retrieveChats(/*UserGroup userGroup*/) throws ServiceException {
		UserGroup userGroup = new UserGroup();
		userGroup.setUserGroupId(1L);
		Event event = commonDao.findEntityById(Event.class, 1L); // WC2014
		userGroup.setEvent(event);
		
		List<Chat> expectedChats = new ArrayList<>();
		Mockito.when(chatDao.retrieveChats(userGroup.getEvent().getEventId(), userGroup.getUserGroupId())).thenReturn(expectedChats);
		
		List<Chat> chats = chatService.retrieveChats(userGroup);
		assertEquals("Retrieved list of Chat entitites must be the same as the expected one.",
				expectedChats, chats);
	}
	
	/**
	 * Test {@link ChatService#sendChatMessage(UserGroup, Long, String)} method.
	 * Scenario: throws {@link NullPointerException} because of the given {@code null} 
	 *           {@code userGroup} parameter
	 */
	@Test(expected=NullPointerException.class)
	public void sendChatMessage_NullUserGroup(/*UserGroup userGroup, Long userId, String message*/) throws ServiceException {
		UserGroup userGroup = null;
		Long userId = 2L; // normal
		String message = "Dummy message";
		
		chatService.sendChatMessage(userGroup, userId, message);
	}

	/**
	 * Test {@link ChatService#sendChatMessage(UserGroup, Long, String)} method.
	 * Scenario: throws {@link NullPointerException} because of the given {@code null} 
	 *           {@code userId} parameter
	 */
	@Test(expected=NullPointerException.class)
	public void sendChatMessage_NullUserId(/*UserGroup userGroup, Long userId, String message*/) throws ServiceException {
		UserGroup userGroup = new UserGroup();
		Long userId = null;
		String message = "Dummy message";
		
		chatService.sendChatMessage(userGroup, userId, message);
	}

	/**
	 * Test {@link ChatService#sendChatMessage(UserGroup, Long, String)} method.
	 * Scenario: throws {@link ServiceException} because of the given {@code null} 
	 *           {@code message} parameter
	 */
	@Test(expected=ServiceException.class)
	public void sendChatMessage_NullMessage(/*UserGroup userGroup, Long userId, String message*/) throws ServiceException {
		UserGroup userGroup = new UserGroup();
		Long userId = 2L; // normal
		String message = null;
		
		try {
			chatService.sendChatMessage(userGroup, userId, message);
		}
		catch (ServiceException e) {
			String msgCode = "MISSING_MESSAGE";
			assertTrue("There must be a single message in ServiceException named " + msgCode, 
					e.getMessages().size()==1 && e.getMessages().get(0).getMsgCode().equals(msgCode));
			throw e;
		}
	}

	/**
	 * Test {@link ChatService#sendChatMessage(UserGroup, Long, String)} method.
	 * Scenario: throws {@link ServiceException} because of the given empty 
	 *           {@code message} parameter
	 */
	@Test(expected=ServiceException.class)
	public void sendChatMessage_EmptyMessage(/*UserGroup userGroup, Long userId, String message*/) throws ServiceException {
		UserGroup userGroup = new UserGroup();
		Long userId = 2L; // normal
		String message = "";
		
		try {
			chatService.sendChatMessage(userGroup, userId, message);
		}
		catch (ServiceException e) {
			String msgCode = "MISSING_MESSAGE";
			assertTrue("There must be a single message in ServiceException named " + msgCode, 
					e.getMessages().size()==1 && e.getMessages().get(0).getMsgCode().equals(msgCode));
			throw e;
		}
	}

	/**
	 * Test {@link ChatService#sendChatMessage(UserGroup, Long, String)} method.
	 * Scenario: throws {@link IllegalStateException} because of the given userGroup
	 *           has no event. 
	 */
	@Test(expected=IllegalStateException.class)
	public void sendChatMessage_UserGroupWithoutEvent(/*UserGroup userGroup, Long userId, String message*/) throws ServiceException {
		UserGroup userGroup = new UserGroup();
		Long userId = 2L; // normal
		String message = "Dummy message";
		
		chatService.sendChatMessage(userGroup, userId, message);
	}

	/**
	 * Test {@link ChatService#sendChatMessage(UserGroup, Long, String)} method.
	 * Scenario: throws {@link IllegalStateException} because there is no user
	 *           belongs to the given {@code userId} parameter in the database 
	 */
	@Test(expected=IllegalStateException.class)
	public void sendChatMessage_InvalidUser(/*UserGroup userGroup, Long userId, String message*/) throws ServiceException {
		UserGroup userGroup = new UserGroup();
		Long userId = -1L; // unknown
		String message = "Dummy message";
	
		Event event = commonDao.findEntityById(Event.class, 1L); // WC2014
		userGroup.setEvent(event);
		
		chatService.sendChatMessage(userGroup, userId, message);
	}

	/**
	 * Test {@link ChatService#sendChatMessage(UserGroup, Long, String)} method.
	 * Scenario: successfully sends chat message to everybody user group from a user
	 */
	@Test
	public void sendChatMessage_EverybodyUserGroup(/*UserGroup userGroup, Long userId, String message*/) throws ServiceException {
		UserGroup userGroup = new UserGroup();
		Long userId = 2L; // normal
		String message = "Dummy message";
	
		Event event = commonDao.findEntityById(Event.class, 1L); // WC2014
		userGroup.setEvent(event);
		userGroup.setUserGroupId(UserGroup.EVERYBODY_USER_GROUP_ID);
		
		LocalDateTime modificationTime = LocalDateTime.now();
		Mockito.when(applicationService.getActualDateTime()).thenReturn(modificationTime);

		chatService.sendChatMessage(userGroup, userId, message);
		
		Chat chat = commonDao.findEntityById(Chat.class, 1L);
		assertTrue(String.format("Retrieved chat result should have \"%s\" message but it is \"%s\"", message, chat.getMessage()), chat != null && chat.getMessage().equals(message));
	}
}
