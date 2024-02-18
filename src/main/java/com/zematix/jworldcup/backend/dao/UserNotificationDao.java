package com.zematix.jworldcup.backend.dao;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import java.time.LocalDateTime;
import java.util.List;

import javax.inject.Inject;
import javax.persistence.TypedQuery;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.querydsl.jpa.impl.JPADeleteClause;
import com.querydsl.jpa.impl.JPAQuery;
import com.zematix.jworldcup.backend.emun.DictionaryEnum;
import com.zematix.jworldcup.backend.emun.UserNotificationEnum;
import com.zematix.jworldcup.backend.entity.Dictionary;
import com.zematix.jworldcup.backend.entity.QUserNotification;
import com.zematix.jworldcup.backend.entity.User;
import com.zematix.jworldcup.backend.entity.UserNotification;

/**
 * Database operations around {@link UserNotification} entities.
 */
@Component
@Transactional
public class UserNotificationDao extends DaoBase {

	@Inject
	private CommonDao commonDao;

	@Inject
	private DictionaryDao dictionaryDao;

	/**
	 * Returns a list of all {@link UserNotification} entities from database.
	 * 
	 * @return list of all {@link UserNorification} entities
	 */
	@Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
	public List<UserNotification> getAllUserNotifications() {
		TypedQuery<UserNotification> query = getEntityManager().createNamedQuery("UserNotification.findAll", UserNotification.class);
		return query.getResultList();
	}
	
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
	public UserNotification findByUserAndKey(Long userId, UserNotificationEnum key) {
		UserNotification userNotification = null;
		checkNotNull(userId);
		checkNotNull(key);

		QUserNotification qUserNotification = QUserNotification.userNotification;
		JPAQuery<UserNotification> query = new JPAQuery<>(getEntityManager());
		userNotification = query.from(qUserNotification).where(qUserNotification.user.userId.eq(userId)
				.and(qUserNotification.userNotificationType.key.eq(DictionaryEnum.USER_NOTIFICATION.name()))
				.and(qUserNotification.userNotificationType.value.eq(key.name()))).fetchOne();

		return userNotification;
	}	
	
	/**
	 * Persists the given userNotification into database.
	 * 
	 * @param userId
	 * @param key
	 * @param creationTime
	 * @param modificationTime
	 * @param value
	 * @return persisted UserNotification entity instance
	 * @throws IllegalArgumentException if no {@link User} or {@link Dictionary}
	 *                                  instances belong to the given parameters
	 */
	public UserNotification insert(Long userId, UserNotificationEnum key, LocalDateTime creationTime,
			LocalDateTime modificationTime, String value) {
		checkNotNull(userId);
		checkNotNull(key);
		User user = commonDao.findEntityById(User.class, userId);
		checkArgument(user != null, String.format("No \"User\" entity belongs to \"userId\"=%d in database.", userId));			

		UserNotification userNotification = new UserNotification();
		userNotification.setUser(user);
		
		Dictionary userNotificationKey = dictionaryDao.findDictionaryByKeyAndValue(DictionaryEnum.USER_NOTIFICATION.name(), key.name());
		checkArgument(userNotificationKey != null,
				String.format("UserNotificationKey named \"%s\" cannot be found in database.", key.name()));
		userNotification.setUserNotificationType(userNotificationKey);

		userNotification.setCreationTime(creationTime);
		userNotification.setModificationTime(modificationTime);
		userNotification.setValue(value);

		commonDao.persistEntity(userNotification);

		user.getUserNotifications().add(userNotification);

		return userNotification;
	}

	/**
	 * Updates the given userNotification in database.
	 * 
	 * @param userNotificationId
	 * @param modificationTime
	 * @return persisted UserNotification entity instance
	 * @throws IllegalArgumentException if no {@link UserNotification}
	 *                                  instance belong to the given parameters
	 */
	public UserNotification update(Long userNotificationId, LocalDateTime modificationTime, String value) {
		checkNotNull(userNotificationId);

		UserNotification userNotification = commonDao.findEntityById(UserNotification.class, userNotificationId);
		checkArgument(userNotification != null,
				String.format("UserNotification given by userNotificationId=\"%d\" cannot be found in database.", userNotificationId));

		userNotification.setModificationTime(modificationTime);
		userNotification.setValue(value);

		commonDao.persistEntity(userNotification);

		return userNotification;
	}

	/**
	 * Delete all userNotifications belongs to the given user.
	 * 
	 * @param userId - belongs to an {@link User} entity
	 * @throws IllegalArgumentException if any of the given parameters is {@code null}
	 */
	public void deleteUserNotificationsByUser(Long userId) {
		checkNotNull(userId);
		
		QUserNotification qUserNotification = QUserNotification.userNotification;
		new JPADeleteClause(getEntityManager(), qUserNotification)
				.where(qUserNotification.user.userId.eq(userId)).execute();
	}
}
