package com.zematix.jworldcup.backend.configuration;

import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.servlet.annotation.WebListener;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Listener to store active sessions in a map. Because of used
 * ChangeSessionIdAuthenticationStrategy, the session ids are being altered at
 * each session, so it must be updated also from outside.
 */
@WebListener
public class SessionListener implements HttpSessionListener {
	
	private static final Logger logger = LoggerFactory.getLogger(SessionListener.class);

	private static final ConcurrentMap<String, HttpSession> sessionMap = new ConcurrentHashMap<>();

	@Override
	public void sessionCreated(HttpSessionEvent event) {
        String id = event.getSession().getId();
        sessionMap.put(id, event.getSession());
        logger.debug("session created: {} (map size: {})", id, sessionMap.size());
    }

	public static void sessionCreated(HttpSession session) {
        String id = session.getId();
        sessionMap.put(id, session);
        logger.debug("session created: {} (map size: {})", id, sessionMap.size());
    }

	@Override
	public void sessionDestroyed(HttpSessionEvent event) {
		String id = event.getSession().getId();
		sessionMap.remove(id);
		logger.debug("session destroyed: {} (map size: {})", id, sessionMap.size());
	}

	public static void sessionDestroyed(String id) {
		sessionMap.remove(id);
		logger.debug("session destroyed: {} (map size: {})", id, sessionMap.size());
	}

	public static HttpSession getSession(String sessionID) {
        return sessionMap.get(sessionID);
    }

	public static Collection<HttpSession> getSessions() {
		return sessionMap.values();
	}
	
	public static ConcurrentMap<String, HttpSession> getSessionMap() {
		return sessionMap;
	}
}
