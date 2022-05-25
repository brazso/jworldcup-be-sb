package com.zematix.jworldcup.backend.service;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.session.SessionInformation;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.stereotype.Service;

@Service
public class SessionLogoutHandler implements LogoutHandler {
	
	@Autowired
	private Logger logger;
	
	@Autowired
	private ApplicationService applicationService;
	
	@Autowired
	private SessionService sessionService;
	
	@Override
	public void logout(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
		// authentication variable is null here, cannot be used
		logger.info("logout id: {}, username: {}", sessionService.getId(), sessionService.getUsername());
		expireUserSessions(sessionService.getUsername()); // getUser() cannot be used, no authentication yet here
	}
    
	/**
	 * Expires authenticated user sessions belongs to the given {@code username}
	 * @param username
	 */
	private void expireUserSessions(String username) {
		applicationService.getAllAuthenticatedPrincipals().stream().filter(User.class::isInstance)
				.filter(e -> username.equals(e.getUsername())).forEach(user -> {
					applicationService.getAllAuthenticatedSessions(user).stream()
							.forEach(SessionInformation::expireNow);
				});
	}
}