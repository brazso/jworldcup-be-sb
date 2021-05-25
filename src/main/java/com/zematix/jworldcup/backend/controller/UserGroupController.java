package com.zematix.jworldcup.backend.controller;

import java.io.ByteArrayOutputStream;
import java.util.Locale;

import javax.inject.Inject;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.zematix.jworldcup.backend.dto.CommonResponse;
import com.zematix.jworldcup.backend.dto.GenericListResponse;
import com.zematix.jworldcup.backend.dto.GenericResponse;
import com.zematix.jworldcup.backend.dto.UserCertificateDto;
import com.zematix.jworldcup.backend.dto.UserCertificateExtendedDto;
import com.zematix.jworldcup.backend.dto.UserDto;
import com.zematix.jworldcup.backend.dto.UserGroupDto;
import com.zematix.jworldcup.backend.dto.UserPositionDto;
import com.zematix.jworldcup.backend.entity.Event;
import com.zematix.jworldcup.backend.entity.Role;
import com.zematix.jworldcup.backend.entity.User;
import com.zematix.jworldcup.backend.entity.UserGroup;
import com.zematix.jworldcup.backend.entity.UserStatus;
import com.zematix.jworldcup.backend.exception.ServiceException;
import com.zematix.jworldcup.backend.mapper.UserCertificateExtendedMapper;
import com.zematix.jworldcup.backend.mapper.UserCertificateMapper;
import com.zematix.jworldcup.backend.mapper.UserGroupMapper;
import com.zematix.jworldcup.backend.mapper.UserMapper;
import com.zematix.jworldcup.backend.mapper.UserPositionMapper;
import com.zematix.jworldcup.backend.model.UserCertificate;
import com.zematix.jworldcup.backend.model.UserPosition;
import com.zematix.jworldcup.backend.service.ServiceBase;
import com.zematix.jworldcup.backend.service.UserGroupService;

import io.swagger.v3.oas.annotations.Operation;

/**
 * WS Rest wrapper class of {@link UserGroupService}.
 * Only the necessary public methods of its associated service class are in play.
 */
@RestController
@RequestMapping("user-groups")
public class UserGroupController extends ServiceBase implements ResponseEntityHelper {

	@Inject
	private UserGroupService userGroupService;
	
	@Inject
	private UserGroupMapper userGroupMapper;

	@Inject
	private UserMapper userMapper;

	@Inject
	private UserPositionMapper userPositionMapper;

	@Inject
	private UserCertificateMapper userCertificateMapper;

	@Inject
	private UserCertificateExtendedMapper userCertificateExtendedMapper;

	/**
	 * Returns a list of found {@link UserGrup} instances filtered with the given parameters
	 * 
	 * @param eventId
	 * @param userId
	 * @param isEverybodyIncluded
	 * @return list of found {UserGrup} instances
	 * @throws IllegalArgumentException if any of the given parameters is {@code null}
	 */
	@PreAuthorize("hasAnyRole('USER', 'ADMIN')")	
	@Operation(summary = "Retrieve bets of an event and user", description = "Retrieve all bets of the given event and user")
	@GetMapping(value = "/user-groups-by-event-and-user")
	public ResponseEntity<GenericListResponse<UserGroupDto>> retrieveUserGroups(@RequestParam Long eventId, @RequestParam Long userId, 
			@RequestParam boolean isEverybodyIncluded) throws ServiceException {
		var userGroups = userGroupService.retrieveUserGroups(eventId, userId, isEverybodyIncluded);
		return buildResponseEntityWithOK(new GenericListResponse<>(userGroupMapper.entityListToDtoList(userGroups)));
	}
	
	/**
	 * Returns a list of found {@link User} instance with "USER" {@link Role#getRole()} 
	 * and with "NORMAL" {@link UserStatus#getStatus()} which belongs to the given 
	 * {@code userGroupId}. If the given userGroup is virtual Everybody then all users 
	 * are retrieved.
	 * 
	 * @param userGroupId - filter
	 * @return list of users which belongs to the given userGroupId
	 * @throws IllegalArgumentException if any of the given parameters is invalid
	 */
	@PreAuthorize("hasAnyRole('USER', 'ADMIN')")	
	@Operation(summary = "Retrieve users belongs to the given user group", description = "Retrieve normal users belongs to the given user group")
	@GetMapping(value = "/users-by-user-group")
	public ResponseEntity<GenericListResponse<UserDto>> retrieveUsersByUserGroup(Long userGroupId) throws ServiceException {
		var users = userGroupService.retrieveUsersByUserGroup(userGroupId);
		return buildResponseEntityWithOK(new GenericListResponse<>(userMapper.entityListToDtoList(users)));
	}

	/**
	 * Returns a sorted list of {@link UserPosition} instances which belong to the given {@link Event#eventId}
	 * and {@link UserGroup#userGroupId}. The returned elements are sorted by their scores. 
	 * @param eventId - filter for {@link Event}
	 * @param userGroupId - filter for {@link UserGroup}
	 * @return list of sorted userPositions which belongs to the given eventId and userGroupId
	 */
	@PreAuthorize("hasAnyRole('USER', 'ADMIN')")	
	@Operation(summary = "Retrieve user positionsbets of an event and user", description = "Retrieve all bets of the given event and user")
	@GetMapping(value = "/user-positions-by-event-and-user-group")
	public ResponseEntity<GenericListResponse<UserPositionDto>> retrieveUserPositions(@RequestParam Long eventId, @RequestParam Long userGroupId) throws ServiceException {
		var userPositions = userGroupService.retrieveUserPositions(eventId, userGroupId);
		return buildResponseEntityWithOK(new GenericListResponse<>(userPositionMapper.entityListToDtoList(userPositions)));
	}
	
	/**
	 * Persists the given user group into database.
	 * 
	 * @param eventId
	 * @param userId - creator/owner
	 * @param name - user group name
	 * @param isInsertConfirmed
	 * @return wrapped persisted UserGroup entity instance
	 */
	@PreAuthorize("hasAnyRole('USER', 'ADMIN')")	
	@Operation(summary = "Create given user group", description = "Insert given user group into database")
	@PostMapping(value = "/insert-user-group")
	public ResponseEntity<GenericResponse<UserGroupDto>> insertUserGroup(@RequestParam Long eventId, 
			@RequestParam Long userId, @RequestParam String name, @RequestParam boolean isInserConfirmed) throws ServiceException {
		var userGroup = userGroupService.insertUserGroup(eventId, userId, name, isInserConfirmed);
		return buildResponseEntityWithOK(new GenericResponse<>(userGroupMapper.entityToDto(userGroup)));
	}

	/**
	 * Persists a new user group into database by importing the given user group.
	 * 
	 * @param eventId
	 * @param userId - creator/owner
	 * @param name - name of the user group to be imported
	 * @return wrapped persisted UserGroup entity instance
	 */
	@PreAuthorize("hasAnyRole('USER', 'ADMIN')")	
	@Operation(summary = "Import given user group", description = "Import given user group into database")
	@PostMapping(value = "/import-user-group")
	public ResponseEntity<GenericResponse<UserGroupDto>> importUserGroup(@RequestParam Long eventId, @RequestParam Long userId, @RequestParam String name) throws ServiceException {
		var userGroup = userGroupService.importUserGroup(eventId, userId, name);
		return buildResponseEntityWithOK(new GenericResponse<>(userGroupMapper.entityToDto(userGroup)));
	}
	
	/**
	 * Removes the given user group from database.
	 * 
	 * @param userGroupId
	 */
	@PreAuthorize("hasAnyRole('USER', 'ADMIN')")	
	@Operation(summary = "Delete given user group", description = "Delete given user group from database")
	@DeleteMapping(value = "/delete-user-group/{id}")
	public ResponseEntity<CommonResponse> deleteUserGroup(@PathVariable("id") Long userGroupId) throws ServiceException {
		userGroupService.deleteUserGroup(userGroupId);
		return buildResponseEntityWithOK(new CommonResponse());
	}
	
	/**
	 * Adds a User instance given by her login or full name to the given UserGroup instance. 
	 * As a precondition, both entity must exist in the database.
	 * If given user is already associated to given user group, the function does 
	 * nothing.
	 * 
	 * @param userGroupId - this user group should be expanded
	 * @param loginName - filter belongs to the user to be added
	 * @param fullName - filter belongs to the user to be added
	 * @throws ServiceException
	 * @throws IllegalArgumentException
	 * @throws IllegalStateException
	 */
	@PreAuthorize("hasAnyRole('USER', 'ADMIN')")	
	@Operation(summary = "Add user to user group", description = "Add user to user group in database")
	@PostMapping(value = "/find-and-add-user-to-user-group")
	public ResponseEntity<GenericResponse<UserDto>> findAndAddUserToUserGroup(@RequestParam Long userGroupId, 
			@RequestParam String loginName, @RequestParam String fullName) throws ServiceException {
		var user = userGroupService.findAndAddUserToUserGroup(userGroupId, loginName, fullName);
		return buildResponseEntityWithOK(new GenericResponse<>(userMapper.entityToDto(user)));
	}
	
	/**
	 * Removes given User from the given UserGroup. Both entity must exist in the database.
	 * If given user is not associated to given user group, the function does nothing.
	 * 
	 * @param userGroupId - this user group should be changed
	 * @param userId - this user should be removed from the given user group
	 * @throws ServiceException
	 * @throws IllegalArgumentException
	 * @throws IllegalStateException
	 */
	@PreAuthorize("hasAnyRole('USER', 'ADMIN')")	
	@Operation(summary = "Delete user from user group", description = "Delete given user from the given user group in database")
	@DeleteMapping(value = "/delete-user-from-user-group")
	public ResponseEntity<CommonResponse> removeUserFromUserGroup(@RequestParam Long userGroupId, @RequestParam Long userId) throws ServiceException {
		userGroupService.removeUserFromUserGroup(userGroupId, userId);
		return buildResponseEntityWithOK(new CommonResponse());
	}
	
	/**
	 * Returns a sorted list of {@link UserCertificate} instances which belong to the given {@link Event#eventId}
	 * and {@link User#userId}.
	 * 
	 * @param eventId - filter for {@link Event}
	 * @param userId - filter for {@link User}
	 * @return list of sorted userCertificate objects which belongs to the given eventId and userId
	 */
	@PreAuthorize("hasAnyRole('USER', 'ADMIN')")	
	@Operation(summary = "Retrieve user certificates by event and user", description = "Retrieve user certificates by event and user")
	@GetMapping(value = "/find-user-certificates-by-event-and-user")
	public ResponseEntity<GenericListResponse<UserCertificateDto>> retrieveUserCertificates(Long eventId, Long userId) throws ServiceException {
		var userCertificates = userGroupService.retrieveUserCertificates(eventId, userId);
		return buildResponseEntityWithOK(new GenericListResponse<>(userCertificateMapper.entityListToDtoList(userCertificates)));
	}
	
	/**
	 * Generates user certificate document in PDF format. 
	 * 
	 * @param userCertificate - template data
	 * @param locale - document language
	 * @return OutputStream object containing generated PDF in a byte array 
	 * @throws ServiceException if something goes wrong 
	 */
	@PreAuthorize("hasAnyRole('USER', 'ADMIN')")	
	@Operation(summary = "Print user certificate", description = "Print user certificate")
	@GetMapping(value = "/print-user-certificate")
	public ResponseEntity<GenericResponse<ByteArrayOutputStream>> printUserCertificate(@RequestBody UserCertificateExtendedDto userCertificateExtendedDto) throws ServiceException {
		Locale locale = userCertificateExtendedDto.getLanguageTag() == null ? Locale.getDefault() : Locale.forLanguageTag( userCertificateExtendedDto.getLanguageTag() );
		ByteArrayOutputStream stream = userGroupService.printUserCertificate(userCertificateExtendedMapper.dtoToEntity(userCertificateExtendedDto), locale);
		return buildResponseEntityWithOK(new GenericResponse<>(stream));
	}
}
