package com.zematix.jworldcup.backend.controller;

import java.util.Locale;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.zematix.jworldcup.backend.dto.CommonResponse;
import com.zematix.jworldcup.backend.dto.GenericListResponse;
import com.zematix.jworldcup.backend.dto.GenericMapResponse;
import com.zematix.jworldcup.backend.dto.GenericResponse;
import com.zematix.jworldcup.backend.dto.UserDto;
import com.zematix.jworldcup.backend.dto.UserExtendedDto;
import com.zematix.jworldcup.backend.dto.UserOfEventDto;
import com.zematix.jworldcup.backend.entity.User;
import com.zematix.jworldcup.backend.entity.UserOfEvent;
import com.zematix.jworldcup.backend.exception.ServiceException;
import com.zematix.jworldcup.backend.mapper.UserExtendedMapper;
import com.zematix.jworldcup.backend.mapper.UserMapper;
import com.zematix.jworldcup.backend.mapper.UserOfEventMapper;
import com.zematix.jworldcup.backend.model.UserExtended;
import com.zematix.jworldcup.backend.service.JwtUserDetailsService;
import com.zematix.jworldcup.backend.service.ServiceBase;
import com.zematix.jworldcup.backend.service.UserService;

import io.swagger.v3.oas.annotations.Operation;

/**
 * WS Rest wrapper class of {@link UserService}.
 * Only the necessary public methods of its associated service class are in play.
 * Both login and signup methods were moved to {@link JwtAuthenticationController}.
 */
@RestController
@RequestMapping("users")
public class UserController extends ServiceBase implements ResponseEntityHelper {

	@Inject
	private UserService userService;
	
	@Inject
	private JwtUserDetailsService userDetailsService;
	
	@Inject
	private UserMapper userMapper;

	@Inject
	private UserExtendedMapper userExtendedMapper;

	@Inject
	private UserOfEventMapper userOfEventMapper;

	/**
	 * Modifies registration data of an existing user. It is a simple wrapper to
	 * the same method in {@link UserService}.
	 * 
	 * @param userExtendedDto - user to be modified
	 * @return modified user wrapped in 
	 * @throws ServiceException if the user cannot be identified or modified
	 */
	@PreAuthorize("hasAnyRole('USER', 'ADMIN')")
	@Operation(summary = "Modify a user", description = "Modify a user with the given data")
	@PutMapping(value = "/modify-user")
	public ResponseEntity<GenericResponse<UserDto>> modifyUser(
			@RequestBody UserExtendedDto userExtendedDto) throws ServiceException {

		UserExtended userExtended = userExtendedMapper.dtoToEntity(userExtendedDto);
		
		User user = userService.modifyUser(userExtended.getLoginName(), userExtended.getLoginPassword(), 
				userExtended.getLoginPasswordNew(), userExtended.getLoginPasswordAgain(), 
				userExtended.getFullName(), userExtended.getEmailNew(), 
				userExtended.getEmailNewAgain(), userExtended.getZoneId(), 
				userExtended.getLocale());

		return buildResponseEntityWithOK(new GenericResponse<>(userMapper.entityToDto(user)));
	}
	
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
	@Operation(summary = "Find user loginNames by prefix", description = "Find user loginNames by loginName prefix")
	@GetMapping(value = "/find-user-login-names-by-prefix")
	public ResponseEntity<GenericListResponse<String>> findUserLoginNamesByLoginNamePrefix(@RequestParam String loginNamePrefix) {
		var loginNames = userService.findUserLoginNamesByLoginNamePrefix(loginNamePrefix);
		return buildResponseEntityWithOK(new GenericListResponse<>(loginNames));
	}
	
	/**
	 * Returns a list of strings containing user fullName values matched by the given 
	 * fullNameContain.
	 * 
	 * @param fullNameContain
	 * @return Returns a list of strings containing user fullName values matched by the given 
	 *         fullNameContain.
	 */
	@PreAuthorize("hasAnyRole('USER', 'ADMIN')")
	@Operation(summary = "Find user fullNames by contain", description = "Find user fullNames by fullName contain")
	@GetMapping(value = "/find-user-full-names-by-contain")
	public ResponseEntity<GenericListResponse<String>> findUserFullNamesByFullNameContain(@RequestParam String fullNameContain) {
		var fullNames = userService.findUserFullNamesByFullNameContain(fullNameContain);
		return buildResponseEntityWithOK(new GenericListResponse<>(fullNames));
	}

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
		var userOfEvent = userService.retrieveUserOfEvent(eventId, userId);
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
		var userOfEvent = userService.saveUserOfEvent(eventId, userId, favouriteGroupTeamId, favouriteKnockoutTeamId);
		return buildResponseEntityWithOK(new GenericResponse<>(userOfEventMapper.entityToDto(userOfEvent)));
	}
	
	/**
	 * Resets user password by the given email address. 
	 * After verification of the email address, if it belongs to
	 * an existing user, an email is being sent there containing 
	 * an url with a new temporary password and a token linked 
	 * to the user. 
	 * 
	 * @param emailAddr - belongs to the user with forgotten password or username
	 * @param languageTag
	 * @throws ServiceException if the given email address belongs to no user 
	 */
	@PreAuthorize("hasAnyRole('USER', 'ADMIN')")
	@Operation(summary = "Reset user password by email address", description = "Reset user password by the given email address")
	@PutMapping(value = "/reset-password")
	public ResponseEntity<CommonResponse> resetPassword(@RequestParam String emailAddr, @RequestParam String languageTag) throws ServiceException {
		Locale locale = languageTag == null ? Locale.getDefault() : Locale.forLanguageTag( languageTag );
		userService.resetPassword(emailAddr, locale);
		return buildResponseEntityWithOK(new CommonResponse());
	}
	
	/**
	 * Returns all possible time zone key/value pairs in a map, for example
	 * [<"Europe/Berlin", "+02:00">, <"Africa/Algiers", "+01:00">, ...].
	 * The result map is sorted by its key.
	 * 
	 * @return a map containing all supported time zone key/value pairs
	 */
	@PreAuthorize("hasAnyRole('USER', 'ADMIN')")
	@Operation(summary = "Find supported timezone ids", description = "Find supported timezone ids")
	@GetMapping(value = "/find-time-zone-ids")
	public ResponseEntity<GenericMapResponse<String, String>> getAllSupportedTimeZoneIds() {
		var timeZoneIds = userService.getAllSupportedTimeZoneIds();
		return buildResponseEntityWithOK(new GenericMapResponse<>(timeZoneIds));
	}
	
	/**
	 * Returns detached {@link User} instance with the provided {@link User#userId}.
	 * 
	 * @param userId
	 * @return found user
	 */
	@PreAuthorize("hasAnyRole('USER', 'ADMIN')")
	@Operation(summary = "Find user by her id", description = "Find user by her id")
	@GetMapping(value = "/user/{id}")
	public ResponseEntity<GenericResponse<UserDto>> findUser(@PathVariable("id") Long userId) throws ServiceException {
		var user = userService.retrieveUser(userId);
		return buildResponseEntityWithOK(new GenericResponse<>(userMapper.entityToDto(user)));
	}
	
	/**
	 * Deletes a user given by loginName parameter.
	 * 
	 * @param loginName
	 */
	@PreAuthorize("hasAnyRole('USER', 'ADMIN')")
	@Operation(summary = "Delete a user by her login name", description = "Delete a user by the given login name")
	@DeleteMapping(value = "/delete-user-by-login-name")
	public ResponseEntity<CommonResponse> deleteUser(@RequestParam String loginName) throws ServiceException {
		userService.deleteUser(loginName);
		return buildResponseEntityWithOK(new CommonResponse());
	}

	/**
	 * Returns detached {@link User} instance with the provided {@link User#userId}.
	 * 
	 * @param userId
	 * @return found user
	 */
	@PreAuthorize("hasAnyRole('USER', 'ADMIN')")
	@Operation(summary = "Find authenticated user", description = "Find authenticated user")
	@GetMapping(value = "/whoami")
	public ResponseEntity<GenericResponse<UserDto>> whoami() throws ServiceException {
		var authenticatedUser = userDetailsService.getAuthenticatedUser();
		var user = userService.findUserByLoginName(authenticatedUser.getUsername()); // user.getRoles() also fetched
		var authorities = authenticatedUser.getAuthorities().stream().map(e -> e.getAuthority()).collect(Collectors.toSet());
		return buildResponseEntityWithOK(new GenericResponse<>(userMapper.entityToDto(user, authorities)));
	}

	/**
	 * Processes user registration token. If the input registrationToken specifies 
	 * an user and this user has CANDIDATE userStatus, then alter her userStatus
	 * to NORMAL.
	 *  
	 * @param userToken - token for first login after registration
	 */
	@Operation(summary = "Processes user registration token", description = "Processes user registration token")
	@PutMapping(value = "/process-registration-token")
	public ResponseEntity<CommonResponse> processRegistrationToken(@RequestParam String userToken) throws ServiceException {
		userService.processRegistrationToken(userToken);
		return buildResponseEntityWithOK(new CommonResponse());
	}

	/**
	 * Processes token from a user initiated email change request. If the input 
	 * userToken specifies an user and this user has NORMAL userStatus, 
	 * then this function changes her email address.
	 *  
	 * @param userToken - token linked to the user
	 */
	@Operation(summary = "Processes token from a user initiated email change request", description = "Processes token from a user initiated email change request")
	@PutMapping(value = "/process-change-email-token")
	public ResponseEntity<CommonResponse> processChangeEmailToken(@RequestParam String userToken) throws ServiceException {
		userService.processChangeEmailToken(userToken);
		return buildResponseEntityWithOK(new CommonResponse());
	}

	/**
	 * Processes token from a user initiated password reset request. If the input 
	 * userToken specifies an user, this user has NORMAL userStatus and there is
	 * a reset password of the user then this function updates the reset password 
	 * for the user.
	 *  
	 * @param userToken - token linked to the user
	 */
	@Operation(summary = "Processes token from a user initiated password reset request", description = "Processes token from a user initiated password reset request")
	@PutMapping(value = "/process-reset-password-token")
	public ResponseEntity<CommonResponse> processResetPasswordToken(@RequestParam String userToken) throws ServiceException {
		userService.processResetPasswordToken(userToken);
		return buildResponseEntityWithOK(new CommonResponse());
	}

}
