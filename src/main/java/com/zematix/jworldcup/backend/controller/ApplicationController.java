package com.zematix.jworldcup.backend.controller;

import javax.inject.Inject;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.zematix.jworldcup.backend.dto.ChatDto;
import com.zematix.jworldcup.backend.dto.GenericListResponse;
import com.zematix.jworldcup.backend.dto.UserCertificateDto;
import com.zematix.jworldcup.backend.entity.Chat;
import com.zematix.jworldcup.backend.entity.Event;
import com.zematix.jworldcup.backend.entity.UserGroup;
import com.zematix.jworldcup.backend.exception.ServiceException;
import com.zematix.jworldcup.backend.mapper.ChatMapper;
import com.zematix.jworldcup.backend.mapper.UserCertificateMapper;
import com.zematix.jworldcup.backend.model.UserCertificate;
import com.zematix.jworldcup.backend.service.ApplicationService;
import com.zematix.jworldcup.backend.service.ServiceBase;

import io.swagger.v3.oas.annotations.Operation;

/**
 * WS Rest wrapper class of {@link ApplicatgionService}.
 * Only the necessary public methods of its associated service class are in play.
 */
@RestController
@RequestMapping("application")
public class ApplicationController extends ServiceBase implements ResponseEntityHelper {

	@Inject
	private ApplicationService applicationService;

	@Inject
	private UserCertificateMapper userCertificateMapper;

	@Inject
	private ChatMapper chatMapper;

	/**
	 * Returns a sorted list of {@link UserCertificate} instances from all events.
	 * 
	 * @return list of sorted userCertificate objects from all events
	 */
	@PreAuthorize("hasAnyRole('USER', 'ADMIN')")
	@Operation(summary = "Retrive sorted userCertificate list from all events", description = "Retrive sorted userCertificate list from all events")
	@GetMapping(value = "/retrieve-top-users")
	public ResponseEntity<GenericListResponse<UserCertificateDto>> retrieveTopUsers() throws ServiceException {
		var topUsers = applicationService.getTopUsersCache();
		return buildResponseEntityWithOK(new GenericListResponse<>(userCertificateMapper.entityListToDtoList(topUsers)));
	}
	
	/**
	 * Returns a list of {@link Chat} instances which belongs to the 
	 * given {@link Event} and {@link UserGroup} instances.
	 * 
	 * @param userGroup - filter
	 * @return list of chats which belongs to the eventId and userGroupId
	 */
	@PreAuthorize("hasAnyRole('USER', 'ADMIN')")
	@Operation(summary = "Retrieves chat records", description = "Retrieves chat records belong to the given event and userGroup")
	@GetMapping(value = "/retrieve-chats")
	public ResponseEntity<GenericListResponse<ChatDto>> retrieveChats(@RequestParam Long eventId, @RequestParam Long userGroupId) throws ServiceException {
		UserGroup userGroup = new UserGroup();
		userGroup.setUserGroupId(userGroupId);
		Event event = new Event();
		event.setEventId(eventId);
		event.addUserGroup(userGroup);
		
		var chats = applicationService.getChatsByUserGroupCache().getUnchecked(userGroup);
		return buildResponseEntityWithOK(new GenericListResponse<>(chatMapper.entityListToDtoList(chats)));
	}
}
