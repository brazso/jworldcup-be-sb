package com.zematix.jworldcup.backend.controller;

import javax.inject.Inject;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.zematix.jworldcup.backend.dto.GenericResponse;
import com.zematix.jworldcup.backend.dto.UserNotificationDto;
import com.zematix.jworldcup.backend.emun.UserNotificationEnum;
import com.zematix.jworldcup.backend.entity.UserNotification;
import com.zematix.jworldcup.backend.exception.ServiceException;
import com.zematix.jworldcup.backend.mapper.UserNotificationMapper;
import com.zematix.jworldcup.backend.service.ServiceBase;
import com.zematix.jworldcup.backend.service.UserNotificationService;

import io.swagger.v3.oas.annotations.Operation;

/**
 * WS Rest wrapper class of {@link UserNotificationService}.
 */
@RestController
@RequestMapping("user-notifications")
public class UserNotificationController extends ServiceBase implements ResponseEntityHelper {

	private final UserNotificationService userNotificationService;
	
	private final UserNotificationMapper userNotificationMapper;

	@Inject
	public UserNotificationController(UserNotificationService userNotificationService,
			UserNotificationMapper userNotificationMapper) {
		this.userNotificationService = userNotificationService;
		this.userNotificationMapper = userNotificationMapper;
	}
	
	/**
	 * Returns found {@link UserNotification} instance which matches the given
	 * {@code userId} and {@code key}.
	 * 
	 * @param userId
	 * @param key
	 * @return found userNotification
	 * @throws ServiceException
	 */
	@PreAuthorize("hasAnyRole('USER', 'ADMIN')")
	@Operation(summary = "Find userNotification by its userId and key", description = "Find userNotification by its userId and key")
	@GetMapping(value = "/find-by-user-and-key")
	public ResponseEntity<GenericResponse<UserNotificationDto>> findByUserAndKey(@RequestParam Long userId, @RequestParam String key) throws ServiceException {
		var userNotification = userNotificationService.findByUserAndKey(userId, UserNotificationEnum.valueOf(key));
		return buildResponseEntityWithOK(new GenericResponse<>(userNotificationMapper.entityToDto(userNotification)));
	}

	/**
	 * Persists the given userNotification into database.
	 * 
	 * @param userId
	 * @param key
	 * @param value
	 * @return created UserNotification
	 * @throws ServiceException
	 */
	@Operation(summary = "Creates a new userNotification", description = "Creates a new userNotification")
	@PostMapping(value = "/insert")
	public ResponseEntity<GenericResponse<UserNotificationDto>> insert(@RequestParam Long userId, @RequestParam String key, @RequestParam(required = false) String value, @RequestParam boolean hasModificationTime) throws ServiceException {
		var userNotification = userNotificationService.insert(userId, UserNotificationEnum.valueOf(key), value, hasModificationTime);
		return buildResponseEntityWithOK(new GenericResponse<>(userNotificationMapper.entityToDto(userNotification)));
	}
	
	/**
	 * Modifies an existing userNotification.
	 * 
	 * Updates the given userNotification in database.
	 * 
	 * @param userNotificationId
	 * @return persisted UserNotification entity instance
	 * @throws IllegalArgumentException if no {@link UserNotification}
	 *                                  instance belong to the given parameters
	 */

	@PreAuthorize("hasAnyRole('USER', 'ADMIN')")
	@Operation(summary = "Modifies an userNotification", description = "Modifies an userNotification with the given data")
	@PutMapping(value = "/update")
	public ResponseEntity<GenericResponse<UserNotificationDto>> update(
			@RequestParam Long userNotificationId, @RequestParam(required = false) String value) throws ServiceException {

		var userNotification = userNotificationService.update(userNotificationId, value); 
		return buildResponseEntityWithOK(new GenericResponse<>(userNotificationMapper.entityToDto(userNotification)));
	}
}
