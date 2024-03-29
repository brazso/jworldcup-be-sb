package com.zematix.jworldcup.backend.service;

import static com.google.common.base.Preconditions.checkNotNull;

import javax.inject.Inject;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.session.SessionInformation;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.stereotype.Service;

import com.zematix.jworldcup.backend.configuration.JwtTokenUtil;

@Service
public class SessionLogoutHandler implements LogoutHandler {
	
	@Inject
	private Logger logger;
	
	@Inject
	private ApplicationService applicationService;
	
	@Inject
	private SessionService sessionService;
	
	@Inject
	private JwtTokenUtil jwtTokenUtil;
	
	@Override
	public void logout(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
		// authentication variable is null here, cannot be used
		logger.info("logout id: {}, username: {}", sessionService.getId(), sessionService.getUsername());
		if (sessionService.getUsername() != null) {
			expireUserSessions(sessionService.getUsername()); // getUser() cannot be used, no authentication yet here
			applicationService.getLastAppearancebyUserCache().invalidate(sessionService.getUsername());
		}

		// invalidate refreshToken cookie
		ResponseCookie responseCookie = jwtTokenUtil.generateDeletedRefreshTokenCookie();
		Cookie cookie = new Cookie(responseCookie.getName(), responseCookie.getValue());
		cookie.setPath(responseCookie.getPath());
		cookie.setHttpOnly(responseCookie.isHttpOnly());
		cookie.setMaxAge(responseCookie.getMaxAge().toSecondsPart());
		response.addCookie(cookie);
	}
    
	/**
	 * Expires authenticated user sessions belongs to the given {@code username}
	 * @param username
	 */
	private void expireUserSessions(String username) {
		checkNotNull(username);
		applicationService.getAllAuthenticatedPrincipals().stream().filter(User.class::isInstance)
				.filter(e -> username.equals(e.getUsername())).forEach(user -> {
					applicationService.getAllAuthenticatedSessions(user).stream()
							.forEach(SessionInformation::expireNow);
				});
	}
}