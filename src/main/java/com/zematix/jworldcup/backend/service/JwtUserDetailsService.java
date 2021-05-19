package com.zematix.jworldcup.backend.service;

import java.util.ArrayList;

import javax.inject.Inject;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.zematix.jworldcup.backend.dao.UserDao;
import com.zematix.jworldcup.backend.entity.User;
import com.zematix.jworldcup.backend.exception.ServiceException;
import com.zematix.jworldcup.backend.model.UserExtended;

import lombok.NonNull;

@Service
public class JwtUserDetailsService implements UserDetailsService {

	@Inject
	private UserDao userDao;

	@Inject
	private UserService userService;
	
	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		User user = userDao.findUserByLoginName(username);
		if (user == null) {
			throw new UsernameNotFoundException("User not found with username: " + username);
		}
		return new org.springframework.security.core.userdetails.User(user.getLoginName(), user.getLoginPassword(),
				new ArrayList<>());
	}

	public UserDetails loadUserDetailsByUser(@NonNull User user) throws UsernameNotFoundException {
		return new org.springframework.security.core.userdetails.User(user.getLoginName(), user.getLoginPassword(),
				new ArrayList<>());
	}

	public User login(String username, String password) throws ServiceException {
		return userService.login(username, password);
	}
	
	public User signup(UserExtended userExtended) throws ServiceException {
		return userService.signUp(userExtended.getLoginName(), userExtended.getLoginPasswordNew(), 
				userExtended.getLoginPasswordAgain(), userExtended.getFullName(), userExtended.getEmailAddr(), 
				userExtended.getZoneId(), userExtended.getLocale());
	}

}