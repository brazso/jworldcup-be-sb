package com.zematix.jworldcup.backend.controller;

import javax.inject.Inject;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.zematix.jworldcup.backend.dto.GenericResponse;
import com.zematix.jworldcup.backend.dto.SessionDataDto;
import com.zematix.jworldcup.backend.exception.ServiceException;
import com.zematix.jworldcup.backend.mapper.SessionDataMapper;
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
	private SessionDataMapper sessionDataMapper;
	
	/**
	 * Refreshes session data storing locale, user and event.
	 * 
	 * @param sessionDataDto
	 * @return refreshed session data
	 * @throws ServiceException if the session data cannot be refreshed
	 */
	@PreAuthorize("hasAnyRole('USER', 'ADMIN')")
	@Operation(summary = "Refreah session info", description = "Refresh session info")
	@PutMapping(value = "/refresh-session-data")
	public ResponseEntity<GenericResponse<SessionDataDto>> refreshSessionData(
			@RequestBody SessionDataDto sessionDataDto) throws ServiceException {
		var sessionData = sessionService.refreshSessionData(sessionDataMapper.dtoToEntity(sessionDataDto));
		return buildResponseEntityWithOK(new GenericResponse<>(sessionDataMapper.entityToDto(sessionData)));
	}
	
}
