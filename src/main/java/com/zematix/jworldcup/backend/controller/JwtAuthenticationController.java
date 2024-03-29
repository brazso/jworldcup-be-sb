package com.zematix.jworldcup.backend.controller;

import java.util.List;

import javax.inject.Inject;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.zematix.jworldcup.backend.configuration.JwtTokenUtil;
import com.zematix.jworldcup.backend.dto.CommonResponse;
import com.zematix.jworldcup.backend.dto.GenericResponse;
import com.zematix.jworldcup.backend.dto.JwtRequest;
import com.zematix.jworldcup.backend.dto.JwtResponse;
import com.zematix.jworldcup.backend.dto.ReCaptchaDto;
import com.zematix.jworldcup.backend.dto.UserDto;
import com.zematix.jworldcup.backend.dto.UserExtendedDto;
import com.zematix.jworldcup.backend.emun.ParameterizedMessageType;
import com.zematix.jworldcup.backend.entity.User;
import com.zematix.jworldcup.backend.exception.GoogleException;
import com.zematix.jworldcup.backend.exception.ServiceException;
import com.zematix.jworldcup.backend.mapper.UserExtendedMapper;
import com.zematix.jworldcup.backend.mapper.UserMapper;
import com.zematix.jworldcup.backend.model.ParameterizedMessage;
import com.zematix.jworldcup.backend.model.UserExtended;
import com.zematix.jworldcup.backend.service.ApplicationService;
import com.zematix.jworldcup.backend.service.GoogleService;
import com.zematix.jworldcup.backend.service.JwtUserDetailsService;
import com.zematix.jworldcup.backend.service.ServiceBase;

import io.jsonwebtoken.JwtException;
import io.swagger.v3.oas.annotations.Operation;

@RestController
//@CrossOrigin
public class JwtAuthenticationController extends ServiceBase implements ResponseEntityHelper {

//	@Inject
//	private AuthenticationManager authenticationManager;

	@Inject
	private JwtTokenUtil jwtTokenUtil;

	@Inject
	private JwtUserDetailsService userDetailsService;
	
	@Inject
	private UserExtendedMapper userExtendedMapper;

	@Inject
	private UserMapper userMapper;
	
	@Inject
	private ApplicationService applicationService;
	
	@Inject
	private GoogleService googleService;
	
	@Value("${app.reCaptcha.secretKey}")
	private String captchaSecret;

	/**
	 * 
	 * @param authenticationRequest
	 * @return
	 * @throws ServiceException
	 */
	@PostMapping(value = "/login")
	@Operation(summary = "Authenticate a user", description = "Authenticate a user")
	public ResponseEntity<JwtResponse> createAuthenticationToken(@RequestBody JwtRequest authenticationRequest) throws ServiceException {
//		authenticate(authenticationRequest.getUsername(), authenticationRequest.getPassword()); // authentication is already executed in every request of {@link JwtRequestFilter}
//		UserDetails userDetails = userDetailsService.loadUserByUsername(authenticationRequest.getUsername());

		User user = userDetailsService.login(authenticationRequest.getUsername(), authenticationRequest.getPassword());
		UserDetails userDetails = userDetailsService.loadUserDetailsByUser(user);
		
		String accessToken = jwtTokenUtil.generateAccessToken(userDetails);
		ResponseCookie responseCookie = jwtTokenUtil.generateRefreshTokenCookie(userDetails);

		// because login does not go through JwtRequestFilter, lastAppearanceByUserCache must be filled here
		applicationService.refreshLastAppearanceByUserCache(user.getLoginName());
		
		return ResponseEntity.ok()
				.header(HttpHeaders.SET_COOKIE, responseCookie.toString())
				.body(new JwtResponse(accessToken));
	}

	/**
	 * 
	 * @param request
	 * @return
	 * @throws ServiceException
	 */
	@PostMapping(value = "/refresh")
	@Operation(summary = "Refresh authentication token", description = "Refresh authentication token")
	public ResponseEntity<JwtResponse> refreshAuthenticationToken(@CookieValue(value = "refreshToken", required = false) String refreshToken) throws ServiceException {
		if (refreshToken == null) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
		}
		
		String username = null;
		try {
			username = jwtTokenUtil.getUsernameFromToken(refreshToken);
		}
		catch (JwtException e) {
			logger.error(e.getMessage());
		}
		if (username == null) { // incoming refresh token is invalid
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();			
		}
		
		UserDetails userDetails = userDetailsService.loadUserByUsername(username);
		String accessToken = jwtTokenUtil.generateAccessToken(userDetails);
		ResponseCookie responseCookie = jwtTokenUtil.generateRefreshTokenCookie(userDetails);
		
		return ResponseEntity.ok()
				.header(HttpHeaders.SET_COOKIE, responseCookie.toString())
				.body(new JwtResponse(accessToken));
	}
	
	@PostMapping(value = "/signup")
	@Operation(summary = "Register a new user", description = "Register a new user")
	public ResponseEntity<GenericResponse<UserDto>> saveUser(@RequestBody UserExtendedDto userExtendedDto) throws ServiceException {
		UserExtended userExtended = userExtendedMapper.dtoToEntity(userExtendedDto);
		User newUser = userDetailsService.signup(userExtended);
		return buildResponseEntityWithOK(new GenericResponse<>(userMapper.entityToDto(newUser)));
	}

	/**
	 * Returns actual backend version and its release date
	 * 
	 * @return actual backend version and its release date
	 */
	@Operation(summary = "Retrieve actual backend version", description = "Retrieve actual backend version")
	@GetMapping(value = "/backend-version")
	public ResponseEntity<GenericResponse<String>> whoami() {
		var version = String.format("%s (%s)", applicationService.getAppVersionNumber(), applicationService.getAppVersionDate());
		return buildResponseEntityWithOK(new GenericResponse<>(version));
	}
	
//	private void authenticate(String username, String password) throws ServiceException {
//		List<ParameterizedMessage> errMsgs = new ArrayList<>();
//		
//		try {
//			authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, password));
//		} catch (DisabledException e) {
//			errMsgs.add(ParameterizedMessage.create("USER_DISABLED"));
////			throw new ServiceException("USER_DISABLED", e);
//		} catch (LockedException e) {
//			errMsgs.add(ParameterizedMessage.create("USER_LOCKED"));
////			throw new ServiceException("USER_LOCKED", e);
//		} catch (BadCredentialsException e) {
//			errMsgs.add(ParameterizedMessage.create("INVALID_CREDENTIALS"));
////			throw new Exception("INVALID_CREDENTIALS", e);
//		}
//		
//		if (!errMsgs.isEmpty()) {
//			throw new ServiceException(errMsgs);
//		}
//	}

	/**
	 * Verifies the given captcha reponse
	 * 
	 * @return success status
	 */
	@Operation(summary = "Verify captcha", description = "Verify captcha response received from client")
	@PostMapping(value = "/verify-captcha")
	public ResponseEntity<CommonResponse> verifyCaptcha(@RequestParam String response) throws ServiceException {
		CommonResponse commonResponse = new CommonResponse();
		
		ReCaptchaDto reCaptchaDto = null;
		try {
			reCaptchaDto = googleService.siteVerify(captchaSecret, response, /*remoteip*/ null);
		} catch (GoogleException e) {
			throw new ServiceException(List.of(ParameterizedMessage.create("general.error.captcha", ParameterizedMessageType.ERROR, e.getMessage())));
		}
		if (reCaptchaDto != null) {
			commonResponse.setSuccessful(reCaptchaDto.getSuccess());
		}
		return buildResponseEntityWithOK(commonResponse);
	}
}