package com.zematix.jworldcup.backend.controller;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import javax.inject.Inject;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.Sql.ExecutionPhase;
import org.springframework.test.context.junit4.SpringRunner;

import com.zematix.jworldcup.backend.dao.CommonDao;
import com.zematix.jworldcup.backend.dto.ChatDto;
import com.zematix.jworldcup.backend.dto.EventDto;
import com.zematix.jworldcup.backend.dto.UserDto;
import com.zematix.jworldcup.backend.dto.UserGroupDto;
import com.zematix.jworldcup.backend.entity.Chat;
import com.zematix.jworldcup.backend.exception.ServiceException;

@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles({"develop", "test"})
@Sql(scripts = { "/database/controller/chat-controller-before.sql" }, executionPhase = ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(scripts = { "/database/controller/chat-controller-after.sql" }, executionPhase = ExecutionPhase.AFTER_TEST_METHOD)
public class ChatControllerIT {

	@Inject
	private ChatController chatController;
	
	@Inject
	private CommonDao commonDao;
	
	@Test
	@WithMockUser(username = "normal", roles = {"USER"})
	public void sendPrivateChat() throws ServiceException {
		// given
		Long eventId = 1L; // WC2014
		Long userId = 2L; // normal
		Long targetUserId = 1L; // admin
		ChatDto chatDao = new ChatDto();
		chatDao.setMessage("Hello");
		EventDto eventDto = new EventDto();
		eventDto.setEventId(eventId);
		chatDao.setEvent(eventDto);
		UserDto userDto = new UserDto();
		userDto.setUserId(userId);
		chatDao.setUser(userDto);
		UserDto targetUserDto = new UserDto();
		targetUserDto.setUserId(targetUserId);
		chatDao.setTargetUser(targetUserDto);
		// when
		var result = chatController.sendChat(chatDao);
		// then
		assertEquals(HttpStatus.OK, result.getStatusCode());
		assertTrue(result.getBody().getSuccessful());
		Chat chat = commonDao.findEntityById(Chat.class, result.getBody().getData().getChatId().longValue());
		assertNotNull(chat);
	}

	@Test
	@WithMockUser(username = "normal", roles = {"USER"})
	public void sendGroupChat() throws ServiceException {
		// given
		Long eventId = 1L; // WC2014
		Long userId = 2L; // normal
		Long userGroupId = 1L;
		ChatDto chatDao = new ChatDto();
		chatDao.setMessage("Hello");
		EventDto eventDto = new EventDto();
		eventDto.setEventId(eventId);
		chatDao.setEvent(eventDto);
		UserDto userDto = new UserDto();
		userDto.setUserId(userId);
		chatDao.setUser(userDto);
		UserGroupDto userGroupDto = new UserGroupDto();
		userGroupDto.setUserGroupId(userGroupId);
		userGroupDto.setEvent(eventDto);
		chatDao.setUserGroup(userGroupDto);
		// when
		var result = chatController.sendChat(chatDao);
		// then
		assertEquals(HttpStatus.OK, result.getStatusCode());
		assertTrue(result.getBody().getSuccessful());
		Chat chat = commonDao.findEntityById(Chat.class, result.getBody().getData().getChatId().longValue());
		assertNotNull(chat);
	}

	@Test
	@WithMockUser(username = "normal", roles = {"USER"})
	public void retrievePrivateChats() throws ServiceException {
		// given
		Long eventId = 1L; // WC2014
		Long sourceUserId = 2L;
		Long targetUserId = 1L;
		// when
		var result = chatController.retrievePrivateChats(eventId, sourceUserId, targetUserId);
		// then
		assertEquals(HttpStatus.OK, result.getStatusCode());
		assertTrue(result.getBody().getSuccessful());
	}}
