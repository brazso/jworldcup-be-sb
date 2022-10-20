package com.zematix.jworldcup.backend.service;

import static org.junit.Assert.assertEquals;

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
@ActiveProfiles({"develop", "test"})
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
}
