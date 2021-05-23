package com.zematix.jworldcup.backend.service;

import java.util.Set;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.zematix.jworldcup.backend.entity.User;
import com.zematix.jworldcup.backend.exception.ServiceException;
import com.zematix.jworldcup.backend.model.UserExtended;

import lombok.NonNull;

@Service
public class JwtUserDetailsService implements UserDetailsService {

	@Inject
	private UserService userService;
	
	@Override
//	@Cacheable(cacheNames = CachingConfig.USER_DETAILS_CACHE, key = "#username") // wo caching remove eraseCredentials at WebSecurityConfig
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		User user = userService.findUserByLoginName(username);
		if (user == null) {
			throw new UsernameNotFoundException("User not found with username: " + username);
		}
		return loadUserDetailsByUser(user);
	}

	public UserDetails loadUserDetailsByUser(@NonNull User user) {
		Set<SimpleGrantedAuthority> authorities = user.getRoles().stream()
				.map(e -> new SimpleGrantedAuthority("ROLE_" + e.getRole())).collect(Collectors.toSet());
		return new org.springframework.security.core.userdetails.User(user.getLoginName(), user.getLoginPassword(),
				authorities);
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