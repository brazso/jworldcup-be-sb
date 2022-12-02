package com.zematix.jworldcup.backend.configuration;

import java.io.IOException;
import java.io.Serializable;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zematix.jworldcup.backend.exception.ApiError;
import com.zematix.jworldcup.backend.exception.ApiErrorHelper;
import com.zematix.jworldcup.backend.exception.UnauthorizedException;

/**
 * This class extends Spring's AuthenticationEntryPoint class and override its method 
 * commence. It rejects every unauthenticated request and send error code 401 as Unauthorized. 
 */
@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint, Serializable, ApiErrorHelper {

	@Inject
	ObjectMapper mapper;
	
	private static final long serialVersionUID = 1L;

	@Override
	public void commence(HttpServletRequest request, HttpServletResponse response,
			AuthenticationException authException) throws IOException {

		ResponseEntity<Object> responseEntity = buildResponseEntity(
				new ApiError(HttpStatus.UNAUTHORIZED, new UnauthorizedException(null)));

		response.setStatus(responseEntity.getStatusCodeValue());

		String json = mapper.writeValueAsString(responseEntity.getBody());
		response.getWriter().write(json);
	}
}