package com.zematix.jworldcup.backend.controller;

import javax.inject.Inject;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.zematix.jworldcup.backend.dto.CommonResponse;
import com.zematix.jworldcup.backend.dto.UserGroupExtendedDto;
import com.zematix.jworldcup.backend.exception.ServiceException;
import com.zematix.jworldcup.backend.mapper.ChatMapper;
import com.zematix.jworldcup.backend.mapper.UserGroupExtendedMapper;
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
	
	@Inject
	private UserGroupExtendedMapper userGroupExtendedMapper;
	
	/**
	 * Persists new chat entity.
	 * 
	 * @param userGroup
	 * @param userId
	 * @param message
	 * @return {@code true} if persist was successful, {@code false} otherwise
	 */
	@PreAuthorize("hasAnyRole('USER', 'ADMIN')")	
	@Operation(summary = "Create a chat entity", description = "Create a chat entity into database")
	@PostMapping(value = "/insert-chat")
	public ResponseEntity<CommonResponse> sendChatMessage(@RequestBody UserGroupExtendedDto userGroupExtendedDto) throws ServiceException {
		chatService.sendChatMessage(userGroupExtendedMapper.dtoToEntity(userGroupExtendedDto), userGroupExtendedDto.getUserId(), 
				userGroupExtendedDto.getMessage());
		return buildResponseEntityWithOK(new CommonResponse());
	}
}
