package com.zematix.jworldcup.backend.configuration;

import java.io.IOException;

import javax.inject.Inject;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.zematix.jworldcup.backend.service.ApplicationService;
import com.zematix.jworldcup.backend.service.JwtUserDetailsService;

import io.jsonwebtoken.JwtException;

/**
 * The JwtRequestFilter extends the Spring Web Filter OncePerRequestFilter class. 
 * For any incoming request this Filter class gets executed. It checks if the request 
 * has a valid JWT token. If it has a valid JWT Token then it sets the Authentication 
 * in the context, to specify that the current user is authenticated. 
 */
@Component
public class JwtRequestFilter extends OncePerRequestFilter {

	@Inject
	private JwtUserDetailsService jwtUserDetailsService;

	@Inject
	private JwtTokenUtil jwtTokenUtil;
	
	@Inject
	private ApplicationService applicationService;

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
			throws ServletException, IOException {

		final String TOKEN_PREFIX = "Bearer ";
	    final String HEADER_STRING = "Authorization";
		final String requestTokenHeader = request.getHeader(HEADER_STRING);

		logger.info("request.uri: " + request.getRequestURI());
		String username = null;
		String jwtToken = null;
		// JWT Token is in the form "Bearer token". Remove Bearer word and get only the Token
		if (requestTokenHeader != null) {
			if (requestTokenHeader.startsWith(TOKEN_PREFIX)) {
				jwtToken = requestTokenHeader.substring(TOKEN_PREFIX.length());
				try {
					username = jwtTokenUtil.getUsernameFromToken(jwtToken);
				}
				catch (JwtException e) {
					logger.error(e.getMessage());
				}
			} else {
				logger.warn("JWT Token does not begin with Bearer String");
			}
		}

		if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
			// configure Spring Security to manually set authentication
			UserDetails userDetails = this.jwtUserDetailsService.loadUserByUsername(username);
			UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(
					userDetails, null, userDetails.getAuthorities());
			usernamePasswordAuthenticationToken
					.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
			// After setting the Authentication in the context, we specify
			// that the current user is authenticated. So it passes the
			// Spring Security Configurations successfully.
			SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);
		}
		
		if (username != null && !request.getRequestURI().endsWith("session/refresh-session-data")) { 
			applicationService.refreshLastAppearanceByUserCache(username);
		}
		
		chain.doFilter(request, response);
	
		SecurityContextHolder.getContext().setAuthentication(null); // reset authentication after request
	}

}