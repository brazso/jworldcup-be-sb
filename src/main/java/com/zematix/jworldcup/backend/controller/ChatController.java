package com.zematix.jworldcup.backend.controller;

import javax.inject.Inject;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.zematix.jworldcup.backend.dto.ChatDto;
import com.zematix.jworldcup.backend.dto.CommonResponse;
import com.zematix.jworldcup.backend.dto.GenericResponse;
import com.zematix.jworldcup.backend.entity.Chat;
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
	public ResponseEntity<CommonResponse> sendChat(@RequestBody ChatDto chatDto) throws ServiceException {
		chatService.sendChat(chatMapper.dtoToEntity(chatDto));
		return buildResponseEntityWithOK(new CommonResponse());
	}
}
