package com.zematix.jworldcup.backend.controller;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.ws.rs.core.Response;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.zematix.jworldcup.backend.dto.GenericResponse;
import com.zematix.jworldcup.backend.dto.UserDto;
import com.zematix.jworldcup.backend.dto.UserExtendedDto;
import com.zematix.jworldcup.backend.entity.User;
import com.zematix.jworldcup.backend.entity.model.UserExtended;
import com.zematix.jworldcup.backend.mapper.UserExtendedMapper;
import com.zematix.jworldcup.backend.mapper.UserMapper;
import com.zematix.jworldcup.backend.service.ParametrizedMessage;
import com.zematix.jworldcup.backend.service.ServiceBase;
import com.zematix.jworldcup.backend.service.ServiceException;
import com.zematix.jworldcup.backend.service.UserService;

import io.swagger.v3.oas.annotations.Operation;

/**
 * WS Rest wrapper class of {@link UserService}.
 * Only the necessary public methods of its associated class are in play. 
 */
@RestController
@RequestMapping("users")
public class UserController extends ServiceBase {

	@Inject
	private UserService userService;
	
	@Inject
	private UserMapper userMapper;

	@Inject
	private UserExtendedMapper userExtendedMapper;

//	@Inject
//	//@Context // although it works without an own producer, but it does not support mocking
//	private UriInfo uriInfo;
	
	@Value("${app.key}")
	private String serverAppKey;

	/**
	 * Return an User instance wrapped in Response if the given user is authenticated.
	 * Otherwise it throws ServiceException. It is a simple wrapper to the same
	 * method in {@link UserService}.
	 *  
	 * @param appKey - key belongs to the application
	 * @param loginName - login name of the user to be authenticated
	 * @param loginPassword - login password of the user to be authenticated
	 * @return authenticated {@link com.zematix.jworldcup.server.entity.User} instance wrapped
	 *         in {@link Response}
	 * @throws ServiceException if the user cannot be authenticated based on the given login data 
	 */
	@Operation(summary = "Login a user", description = "Login a user with the given authentication data")
	@GetMapping(value = "/login")
	public ResponseEntity<GenericResponse<UserDto>> login(//@RequestParam("appKey") String appKey,
			@RequestParam("loginName") String loginName, 
			@RequestParam("loginPassword") String loginPassword ) throws ServiceException  {

		//logger.info("uriInfo="+uriInfo.getAbsolutePath().getPath());

		List<ParametrizedMessage> errMsgs = new ArrayList<>();
//
//		if (Strings.isNullOrEmpty(appKey) || !appKey.equals(serverAppKey)) {
//			errMsgs.add(ParametrizedMessage.create("DISALLOWED_TO_CALL_WS"));
//		}

		if (!errMsgs.isEmpty()) {
			throw new ServiceException(errMsgs);
		}
		
		User user = userService.login(loginName, loginPassword);

		//return Response.status(Response.Status.OK).entity(user).build();
		return new ResponseEntity<>(new GenericResponse<>(userMapper.entityToDto(user)), HttpStatus.OK);
	}

	/**
	 * Sign up a new user with USER role, store her personal data into database.
	 * It is a simple wrapper to the same method in {@link UserService}.
	 * 
	 * @param appKey - key belongs to the application
	 * @param loginName
	 * @param loginPassword
	 * @param loginPasswordAgain - repeated login password for confirmation
	 * @param fullName
	 * @param emailAddr - email address
	 * @param languageTag - well-formed IETF BCP 47 language tag representing a locale
	 * @return stored new {@link User} entity instance wrapped in a {@link Response}
	 * @throws ServiceException if the user cannot be signed up, its login data is not valid 
	 *         or the user already exists in the database
	 */
	@Operation(summary = "Sign up a user", description = "Sign up a user with the given data")
	@PostMapping(value = "/signup")
//	public Response signUp(@RequestParam("appKey") String appKey, 
//			@RequestParam("loginName") String loginName, 
//			@RequestParam("loginPassword") String loginPassword,
//			@RequestParam("loginPasswordAgain") String loginPasswordAgain,
//			@RequestParam("fullName") String fullName,
//			@RequestParam("emailAddr") String emailAddr,
//			@RequestParam("zoneId") String zoneId,
//			@RequestParam("languageTag") String languageTag) throws ServiceException {
	public ResponseEntity<GenericResponse<UserDto>> signUp(
			@RequestBody UserExtendedDto userExtendedDto) throws ServiceException {

		List<ParametrizedMessage> errMsgs = new ArrayList<>();

//		if (Strings.isNullOrEmpty(appKey) || !appKey.equals(serverAppKey)) {
//			errMsgs.add(ParametrizedMessage.create("DISALLOWED_TO_CALL_WS"));
//		}

		if (!errMsgs.isEmpty()) {
			throw new ServiceException(errMsgs);
		}

//		Locale locale = null;
//		if (!Strings.isNullOrEmpty(languageTag)) {
//			locale = Locale.forLanguageTag(languageTag);
//		}

		UserExtended userExtended = userExtendedMapper.dtoToEntity(userExtendedDto);
		
//		User user = userService.signUp(loginName, loginPassword, loginPasswordAgain, 
//				fullName, emailAddr, zoneId, locale);
		User user = userService.signUp(userExtended.getLoginName(), userExtended.getLoginPassword(), 
				userExtended.getLoginPasswordAgain(), userExtended.getFullName(), 
				userExtended.getEmailAddr(), userExtended.getZoneId(), userExtended.getLocale());
		
		//return Response.status(Response.Status.OK).entity(user).build();
		return new ResponseEntity<>(new GenericResponse<>(userMapper.entityToDto(user)), HttpStatus.OK);
	}

	/**
	 * Modify registration data of an existing user. It is a simple wrapper to
	 * the same method in {@link UserService}.
	 * 
	 * @param appKey - key belongs to the application
	 * @param loginName
	 * @param loginPasswordActual - actual login password
	 * @param loginPasswordNew - new login password
	 * @param loginPasswordAgain - new repeated login password for confirmation
	 * @param fullName
	 * @param emailNew - new email address
	 * @param emailNewAgain - new repeated email address for confirmation
	 * @param zoneId - time zone id
	 * @param languageTag - well-formed IETF BCP 47 language tag representing a locale
	 * @return {@link User} entity instance belongs to the modified user wrapped in a {@link Response} 
	 * @throws ServiceException if the user cannot be identified or modified
	 */
//	@PreAuthorize("hasAnyRole('USER', 'ADMIN')")
	@Operation(summary = "Modify a user", description = "Modify a user with the given data")
	@PutMapping(value = "/modify-user")
//	public Response modifyUser(@RequestParam("appKey") String appKey, 
//			@RequestParam("loginName") String loginName, 
//			@RequestParam("loginPasswordActual") String loginPasswordActual,
//			@RequestParam("loginPasswordNew") String loginPasswordNew,
//			@RequestParam("loginPasswordAgain") String loginPasswordAgain,
//			@RequestParam("fullName") String fullName,
//			@RequestParam("emailNew") String emailNew,
//			@RequestParam("emailNewAgain") String emailNewAgain,
//			@RequestParam("zoneId") String zoneId,
//			@RequestParam("languageTag") String languageTag) throws ServiceException {
	public ResponseEntity<GenericResponse<UserDto>> modifyUser(
			@RequestBody UserExtendedDto userExtendedDto) throws ServiceException {
	
		List<ParametrizedMessage> errMsgs = new ArrayList<>();

//		if (Strings.isNullOrEmpty(appKey) || !appKey.equals(serverAppKey)) {
//			errMsgs.add(ParametrizedMessage.create("DISALLOWED_TO_CALL_WS"));
//		}

		if (!errMsgs.isEmpty()) {
			throw new ServiceException(errMsgs);
		}
		
//		Locale locale = null;
//		if (!Strings.isNullOrEmpty(userExtendedDto.getLanguageTag())) {
//			locale = Locale.forLanguageTag(userExtendedDto.getLanguageTag());
//		}
		
		UserExtended userExtended = userExtendedMapper.dtoToEntity(userExtendedDto);
		
		User user = userService.modifyUser(userExtended.getLoginName(), userExtended.getLoginPassword(), 
				userExtended.getLoginPasswordNew(), userExtended.getLoginPasswordAgain(), 
				userExtended.getFullName(), userExtended.getEmailNew(), 
				userExtended.getEmailNewAgain(), userExtended.getZoneId(), 
				userExtended.getLocale());

		//return Response.status(Response.Status.OK).entity(user).build();
		return new ResponseEntity<>(new GenericResponse<>(userMapper.entityToDto(user)), HttpStatus.OK);
	}
}
