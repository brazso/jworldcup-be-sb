package com.zematix.jworldcup.backend.service;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.stereotype.Service;

@Service
public class SessionLogoutHandler implements LogoutHandler {
	
	@Autowired
	private Logger logger;
	
	@Autowired
	private SessionService sessionService;

    @Override
    public void logout(HttpServletRequest request, HttpServletResponse response, 
      Authentication authentication) {
    	logger.info("logout id: " + sessionService.getId());
    }
}