package com.zematix.jworldcup.backend.service;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import java.time.LocalDateTime;

import javax.inject.Inject;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.zematix.jworldcup.backend.dao.CommonDao;
import com.zematix.jworldcup.backend.dao.UserNotificationDao;
import com.zematix.jworldcup.backend.emun.UserNotificationEnum;
import com.zematix.jworldcup.backend.entity.Dictionary;
import com.zematix.jworldcup.backend.entity.User;
import com.zematix.jworldcup.backend.entity.UserNotification;
import com.zematix.jworldcup.backend.exception.ServiceException;

/**
 * Operations around {@link UserNotification} elements. 
 * Usually it injects at least its DAO class belongs to the same entities. 
 * It may also inject other service and DAO classes.
 */
@Service
@Transactional
public class UserNotificationService extends ServiceBase {

	@Inject
	private ApplicationService applicationService;

	@Inject 
	private UserNotificationDao userNotificationDao;
	
	@Inject
	private CommonDao commonDao;

	/**
	 * Returns found {@link UserNotification} instance which matches the given
	 * {@code userId} and {@code key}. Otherwise {@code null} is returned.
	 * 
	 * @param - userId
	 * @param - key
	 * @return found UserNotification with the given {@code userId} and {@code key}
	 * @throws IllegalArgumentException if any of the given parameters is invalid
	 */
	@Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
	public UserNotification findByUserAndKey(Long userId, UserNotificationEnum key) throws ServiceException {
		checkNotNull(userId);
		checkNotNull(key);

		UserNotification userNotification = userNotificationDao.findByUserAndKey(userId, key);
		if (userNotification == null) {
			return null;
		}
		
		// load lazy associations
		userNotification.getUser().getRoles().size();
		userNotification.getUserNotificationType().getKey();
		
		commonDao.detachEntity(userNotification);
		return userNotification;
	}	
	
	/**
	 * Persists the given userNotification into database.
	 * 
	 * @param userId
	 * @param key
	 * @param value
	 * @return persisted UserNotification entity instance
	 * @throws IllegalArgumentException if no {@link User} or {@link Dictionary}
	 *                                  instances belong to the given parameters
	 */
	public UserNotification insert(Long userId, UserNotificationEnum key, String value, boolean hasModificationTime) throws ServiceException {
		checkNotNull(userId);
		checkNotNull(key);
		User user = commonDao.findEntityById(User.class, userId);
		checkArgument(user != null, String.format("No \"User\" entity belongs to \"userId\"=%d in database.", userId));			

		LocalDateTime creationTime = applicationService.getActualDateTime();
		LocalDateTime modificationTime = hasModificationTime ? creationTime : null;
		
		return userNotificationDao.insert(userId, key, creationTime, modificationTime, value);
	}

	/**
	 * Updates the given userNotification in database.
	 * 
	 * @param userNotificationId
	 * @param value
	 * @return persisted UserNotification entity instance
	 * @throws IllegalArgumentException if no {@link UserNotification}
	 *                                  instance belong to the given parameters
	 */
	public UserNotification update(Long userNotificationId, String value) throws ServiceException {
		checkNotNull(userNotificationId);

		LocalDateTime modificationTime = applicationService.getActualDateTime();
		
		UserNotification userNotification = userNotificationDao.update(userNotificationId, modificationTime, value);

		return userNotification;
	}
}

