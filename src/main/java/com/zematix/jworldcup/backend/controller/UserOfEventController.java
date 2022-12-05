package com.zematix.jworldcup.backend.controller;

import javax.inject.Inject;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.zematix.jworldcup.backend.dto.GenericResponse;
import com.zematix.jworldcup.backend.dto.UserOfEventDto;
import com.zematix.jworldcup.backend.entity.UserOfEvent;
import com.zematix.jworldcup.backend.exception.ServiceException;
import com.zematix.jworldcup.backend.mapper.UserOfEventMapper;
import com.zematix.jworldcup.backend.service.ServiceBase;
import com.zematix.jworldcup.backend.service.UserOfEventService;
import com.zematix.jworldcup.backend.service.UserService;

import io.swagger.v3.oas.annotations.Operation;

/**
 * WS Rest wrapper class of {@link UserService}.
 * Only the necessary public methods of its associated service class are in play.
 * Both login and signup methods were moved to {@link JwtAuthenticationController}.
 */
@RestController
@RequestMapping("user-of-events")
public class UserOfEventController extends ServiceBase implements ResponseEntityHelper {

	@Inject
	private UserOfEventService userOfEventService;
	
	@Inject
	private UserOfEventMapper userOfEventMapper;

	/**
	 * Retrieves {@link UserOfEvent} instance by its given eventId and userId or {@code null}
	 * unless found. Returned entity is detached from PU.
	 * 
	 * @param eventId
	 * @param userId
	 * @return found {@link UserOfEvent} detached object or {@code null}
	 */
	@PreAuthorize("hasAnyRole('USER', 'ADMIN')")
	@Operation(summary = "Find user-of-event belongs to event and user", description = "Find user-of-event belongs to the given event and user")
	@GetMapping(value = "/find-user-of-event-by-event-and-user")
	public ResponseEntity<GenericResponse<UserOfEventDto>> retrieveUserOfEvent(@RequestParam Long eventId, @RequestParam Long userId) throws ServiceException {
		var userOfEvent = userOfEventService.retrieveUserOfEvent(eventId, userId); // cached method
		return buildResponseEntityWithOK(new GenericResponse<>(userOfEventMapper.entityToDto(userOfEvent)));
	}
	
	/**
	 * Saves given favourite teams of {@link UserOfEvent} instance by its given 
	 * userId and eventId. It creates a new database row or it just modifies that.
	 * 
	 * @param eventId
	 * @param userId
	 * @param favouriteGroupTeamId - favourite group team id
	 * @param favouriteKnockoutTeamId - favourite knockout team id 
	 * @return saved userOfEvent
	 */
	@PreAuthorize("hasAnyRole('USER', 'ADMIN')")
	@Operation(summary = "Save favourite teams of user-of-event by event and user", description = "Save favourite teams of user-of-event by event and user")
	@PostMapping(value = "/save-user-of-event")	
	public ResponseEntity<GenericResponse<UserOfEventDto>> saveUserOfEvent(@RequestParam Long eventId, @RequestParam Long userId, 
			@RequestParam(required = false) Long favouriteGroupTeamId, @RequestParam(required = false) Long favouriteKnockoutTeamId) throws ServiceException {
		var userOfEvent = userOfEventService.saveUserOfEvent(eventId, userId, favouriteGroupTeamId, favouriteKnockoutTeamId);
		return buildResponseEntityWithOK(new GenericResponse<>(userOfEventMapper.entityToDto(userOfEvent)));
	}
}
