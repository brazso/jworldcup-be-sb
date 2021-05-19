package com.zematix.jworldcup.backend.controller;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.zematix.jworldcup.backend.configuration.JwtTokenUtil;
import com.zematix.jworldcup.backend.dto.GenericResponse;
import com.zematix.jworldcup.backend.dto.UserDto;
import com.zematix.jworldcup.backend.dto.UserExtendedDto;
import com.zematix.jworldcup.backend.entity.User;
import com.zematix.jworldcup.backend.exception.ServiceException;
import com.zematix.jworldcup.backend.mapper.UserExtendedMapper;
import com.zematix.jworldcup.backend.mapper.UserMapper;
import com.zematix.jworldcup.backend.model.JwtRequest;
import com.zematix.jworldcup.backend.model.JwtResponse;
import com.zematix.jworldcup.backend.model.ParameterizedMessage;
import com.zematix.jworldcup.backend.model.UserExtended;
import com.zematix.jworldcup.backend.service.JwtUserDetailsService;

@RestController
//@CrossOrigin
public class JwtAuthenticationController {

	@Inject
	private AuthenticationManager authenticationManager;

	@Inject
	private JwtTokenUtil jwtTokenUtil;

	@Inject
	private JwtUserDetailsService userDetailsService;

	@Inject
	private UserExtendedMapper userExtendedMapper;

	@Inject
	private UserMapper userMapper;

	@PostMapping(value = "/authenticate")
	public ResponseEntity<JwtResponse> createAuthenticationToken(@RequestBody JwtRequest authenticationRequest) throws ServiceException {
		User user = userDetailsService.login(authenticationRequest.getUsername(), authenticationRequest.getPassword());
		authenticate(authenticationRequest.getUsername(), authenticationRequest.getPassword()); // TODO - needed?
		//final UserDetails userDetails = userDetailsService.loadUserByUsername(authenticationRequest.getUsername());
		final UserDetails userDetails = userDetailsService.loadUserDetailsByUser(user);
		final String token = jwtTokenUtil.generateToken(userDetails);
		return ResponseEntity.ok(new JwtResponse(token));
	}

	@PostMapping(value = "/register")
	public ResponseEntity<GenericResponse<UserDto>> saveUser(@RequestBody UserExtendedDto userExtendedDto) throws ServiceException {
		UserExtended userExtended = userExtendedMapper.dtoToEntity(userExtendedDto);
		User newUser = userDetailsService.signup(userExtended);
		return new ResponseEntity<>(new GenericResponse<>(userMapper.entityToDto(newUser)), HttpStatus.OK);
	}

	private void authenticate(String username, String password) throws ServiceException {
		List<ParameterizedMessage> errMsgs = new ArrayList<>();
		
		try {
			authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, password));
		} catch (DisabledException e) {
			errMsgs.add(ParameterizedMessage.create("USER_DISABLED")); // TODO
//			throw new ServiceException("USER_DISABLED", e);
		} catch (BadCredentialsException e) {
			errMsgs.add(ParameterizedMessage.create("INVALID_CREDENTIALS")); // TODO
//			throw new Exception("INVALID_CREDENTIALS", e);
		}
		
		if (!errMsgs.isEmpty()) {
			throw new ServiceException(errMsgs);
		}
	}
}