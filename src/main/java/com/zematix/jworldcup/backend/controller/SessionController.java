package com.zematix.jworldcup.backend.controller;

import javax.inject.Inject;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.zematix.jworldcup.backend.dto.GenericResponse;
import com.zematix.jworldcup.backend.dto.SessionInfoDto;
import com.zematix.jworldcup.backend.mapper.SessionInfoMapper;
import com.zematix.jworldcup.backend.service.ServiceBase;
import com.zematix.jworldcup.backend.service.SessionService;

import io.swagger.v3.oas.annotations.Operation;

/**
 * WS Rest wrapper class of {@link SessionService}.
 * Only the necessary public methods of its associated service class are in play.
 */
@RestController
@RequestMapping("session")
public class SessionController extends ServiceBase implements ResponseEntityHelper {

	@Inject
	private SessionService sessionService;

	@Inject
	private SessionInfoMapper sessionInfoMapper;


//	/**
//	 * Modifies registration data of an existing user. It is a simple wrapper to
//	 * the same method in {@link UserService}.
//	 * 
//	 * @param userExtendedDto - user to be modified
//	 * @return modified user wrapped in 
//	 * @throws ServiceException if the user cannot be identified or modified
//	 */
//	@PreAuthorize("hasAnyRole('USER', 'ADMIN')")
//	@Operation(summary = "Modify a user", description = "Modify a user with the given data")
//	@PutMapping(value = "/modify-user")
//	public ResponseEntity<GenericResponse<UserDto>> modifyUser(
//			@RequestBody UserExtendedDto userExtendedDto) throws ServiceException {
//
//		UserExtended userExtended = userExtendedMapper.dtoToEntity(userExtendedDto);
//		
//		User user = userService.modifyUser(userExtended.getLoginName(), userExtended.getLoginPassword(), 
//				userExtended.getLoginPasswordNew(), userExtended.getLoginPasswordAgain(), 
//				userExtended.getFullName(), userExtended.getEmailNew(), 
//				userExtended.getEmailNewAgain(), userExtended.getZoneId(), 
//				userExtended.getLocale());
//
//		return buildResponseEntityWithOK(new GenericResponse<>(userMapper.entityToDto(user)));
//	}
	
	/**
	 * Returns a wrapped list of strings containing user loginName values matched by the given 
	 * loginNamePrefix.
	 * 
	 * @param loginNamePrefix
	 * @return list of strings containing user loginName values matched by the given 
	 *         loginNamePrefix
	 * @throws IllegalArgumentException if any of the given parameters is invalid
	 */
	@PreAuthorize("hasAnyRole('USER', 'ADMIN')")
	@Operation(summary = "Find session info", description = "Find session info")
	@GetMapping(value = "/find-session-info")
	public ResponseEntity<GenericResponse<SessionInfoDto>> findSessionInfo() {
		var sessionInfo = sessionService.findSessionInfo();
		return buildResponseEntityWithOK(new GenericResponse<>(sessionInfoMapper.entityToDto(sessionInfo)));
	}
	
}
