package com.zematix.jworldcup.backend.controller;

import javax.inject.Inject;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.zematix.jworldcup.backend.dto.ChatDto;
import com.zematix.jworldcup.backend.dto.GenericListResponse;
import com.zematix.jworldcup.backend.dto.GenericResponse;
import com.zematix.jworldcup.backend.entity.Chat;
import com.zematix.jworldcup.backend.entity.User;
import com.zematix.jworldcup.backend.exception.ServiceException;
import com.zematix.jworldcup.backend.mapper.ChatMapper;
import com.zematix.jworldcup.backend.service.ChatService;
import com.zematix.jworldcup.backend.service.ServiceBase;

import io.swagger.v3.oas.annotations.Operation;

/**
 * WS Rest wrapper class of {@link ChatService}.
 * Only the necessary public methods of its associated service class are in play. 
 */
@RestController
@RequestMapping("chats")
public class ChatController extends ServiceBase implements ResponseEntityHelper {

//	@Inject
//	private Logger logger;

	@Inject
	private ChatService chatService;
	
	@Inject
	private ChatMapper chatMapper;
	
	/**
	 * Sends given chat (with message) and persists it as new chat entity.
	 * 
	 * @param chatDto
	 * @throws ServiceException
	 */
	@PreAuthorize("hasAnyRole('USER', 'ADMIN')")	
	@Operation(summary = "Sends a chat message", description = "Sends a chat message and inserts that into database")
	@PostMapping(value = "/send-chat")
	public ResponseEntity<GenericResponse<ChatDto>> sendChat(@RequestBody ChatDto chatDto) throws ServiceException {
		var chat = chatService.sendChat(chatMapper.dtoToEntity(chatDto));
		return buildResponseEntityWithOK(new GenericResponse<>(chatMapper.entityToDto(chat)));
	}

	/**
	 * Returns a list of private {@link Chat} instances which belongs to the 
	 * given event (optional, might be null) source and target {@link User} instances.
	 * 
	 * @param eventId - filter (optional)
	 * @param sourceUserId - filter
	 * @param targetUserId - filter
	 * @return list of private chats which belongs to the given event, source and target users
	 */
	@PreAuthorize("hasAnyRole('USER', 'ADMIN')")
	@Operation(summary = "Retrieves private chat records", description = "Retrieves private chat records belong to the given event, source and target users")
	@GetMapping(value = "/retrieve-private-chats")
	public ResponseEntity<GenericListResponse<ChatDto>> retrievePrivateChats(@RequestParam(required = false) Long eventId, @RequestParam Long sourceUserId, @RequestParam Long targetUserId) throws ServiceException {
		var chats = chatService.retrievePrivateChats(eventId, sourceUserId, targetUserId);
		return buildResponseEntityWithOK(new GenericListResponse<>(chatMapper.entityListToDtoList(chats)));
	}}
