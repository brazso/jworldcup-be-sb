package com.zematix.jworldcup.backend.dao;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

import java.util.List;

import javax.inject.Inject;
import javax.persistence.TypedQuery;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.base.Strings;
import com.querydsl.jpa.impl.JPADeleteClause;
import com.querydsl.jpa.impl.JPAQuery;
import com.zematix.jworldcup.backend.emun.DictionaryEnum;
import com.zematix.jworldcup.backend.emun.RoleEnum;
import com.zematix.jworldcup.backend.emun.UserStatusEnum;
import com.zematix.jworldcup.backend.entity.Dictionary;
import com.zematix.jworldcup.backend.entity.Event;
import com.zematix.jworldcup.backend.entity.QUser;
import com.zematix.jworldcup.backend.entity.QUserGroup;
import com.zematix.jworldcup.backend.entity.User;
import com.zematix.jworldcup.backend.entity.UserGroup;

/**
 * Database operations around {@link UserGroup} entities.
 */
@Component
@Transactional
public class UserGroupDao extends DaoBase {

	@Inject
	private CommonDao commonDao;
	
	@Inject
	private DictionaryDao dictionaryDao;
	
	/**
	 * Returns a list of all {@link UserGroup} entities from database.
	 * 
	 * @return list of all {@link UserGroup} entities
	 */
	@Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
	public List<UserGroup> getAllUserGroups() {
		TypedQuery<UserGroup> query = getEntityManager().createNamedQuery("UserGroup.findAll", UserGroup.class);
		return query.getResultList();
	}
	
	/**
	 * Returns a new virtual Everybody userGroup object belongs to the given 
	 * {@code eventId} event and {@code userId} user. Returned object is not 
	 * linked to the database at all.
	 * 
	 * @param eventId
	 * @param userId
	 * @return new virtual Everybody userGroup object
	 */
	@Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
	public UserGroup createVirtualEverybodyUserGroup(Long eventId, Long userId) {
		checkNotNull(eventId);
		checkNotNull(userId);

		// create virtual Everybody userGroup
		UserGroup userGroup = new UserGroup();
		Event event = commonDao.findEntityById(Event.class, eventId);
		checkState(event != null, String.format("No \"Event\" entity belongs to \"eventId\"=%d in database.", eventId));
		userGroup.setEvent(event);
		userGroup.setName(UserGroup.EVERYBODY_NAME);
		User user = commonDao.findEntityById(User.class, userId);
		checkState(user != null, String.format("No \"User\" entity belongs to \"userId\"=%d in database.", userId));
		userGroup.setOwner(user);
		userGroup.setPublicVisibleAsBoolean(true);
		userGroup.setPublicEditableAsBoolean(false);
		userGroup.setUserGroupId(UserGroup.EVERYBODY_USER_GROUP_ID);
				
		return userGroup;
	}

	/**
	 * Returns a list of {@link UserGroup} instances which belongs to the given 
	 * {@link Event#eventId} and given {@link User#userId}. The latter means that 
	 * userGroup contains the given user as a member of it or the user is its owner.
	 *    
	 * @param userId - filter
	 * @return list of userGroups which belongs to the given userId
	 * @throws IllegalArgumentException if any of the given parameters is invalid 
	 */
	@Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
	public List<UserGroup> retrieveUserGroups(Long eventId, Long userId) {
		List<UserGroup> userGroups;
		checkNotNull(eventId);
		checkNotNull(userId);
		User user = commonDao.findEntityById(User.class, userId);
		checkState(user != null, String.format("No \"User\" entity belongs to \"userId\"=%d in database.", userId));
		
		QUserGroup qUserGroup = QUserGroup.userGroup;
		JPAQuery<UserGroup> query = new JPAQuery<>(getEntityManager());
		userGroups = query.from(qUserGroup)
		  .where((qUserGroup.users.contains(user).or(qUserGroup.owner.eq(user)))
				  .and(qUserGroup.event.eventId.eq(eventId)))
		  .orderBy(qUserGroup.name.asc())
		  .fetch();
		
		return userGroups;
	}
	
	/**
	 * Returns a list of found {@link User} instance with "USER" role 
	 * and with "NORMAL" user status which belongs to the given 
	 * {@code userGroupId}. If the given userGroup is virtual Everybody then all users 
	 * are retrieved.
	 * 
	 * @param userGroupId - filter
	 * @return list of users which belongs to the given userGroupId
	 * @throws IllegalArgumentException if any of the given parameters is invalid
	 */
	@Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
	public List<User> retrieveUsersByUserGroup(Long userGroupId) {
		List<User> users;
		
		checkNotNull(userGroupId);
		UserGroup userGroup = commonDao.findEntityById(UserGroup.class, userGroupId);
		checkArgument(userGroup != null || userGroupId == UserGroup.EVERYBODY_USER_GROUP_ID, 
				String.format("No \"UserGroup\" instance belongs to \"userGroupId\"=%d in database.", userGroupId));
		
		Dictionary userRole = dictionaryDao.findDictionaryByKeyAndValue(DictionaryEnum.ROLE.name(), RoleEnum.USER.name());
		checkState(userRole != null, "No \"Role\" instance belongs to \"role\"=%s in database.", RoleEnum.USER.name());

		Dictionary normalUserStatus = dictionaryDao.findDictionaryByKeyAndValue(DictionaryEnum.USER_STATUS.name(), UserStatusEnum.NORMAL.name());
		checkState(normalUserStatus != null, "No \"UserStatus\" instance belongs to \"userStatus\"=%s in database.", UserStatusEnum.NORMAL.name());
		
		QUser qUser = QUser.user;
		JPAQuery<User> query = new JPAQuery<>(getEntityManager());
		
		if (userGroupId == UserGroup.EVERYBODY_USER_GROUP_ID) {
			users = query.from(qUser)
					  .where(qUser.roles.contains(userRole)
							  .and(qUser.userStatus.eq(normalUserStatus)))
					  .orderBy(qUser.loginName.asc())
					  .fetch();
		}
		else {
			users = query.from(qUser)
					  .where(qUser.userGroups.contains(userGroup)
							  .and(qUser.userStatus.eq(normalUserStatus))
							  .and(qUser.roles.contains(userRole)))
					  .orderBy(qUser.loginName.asc())
					  .fetch();
		}
		
		return users;
	}

	/**
	 * Returns a list of found {@link User} instance with "USER" role 
	 * and with "NORMAL" user status which belongs to the given 
	 * {@code userGroupId} and each user has at least one bet belongs to the event of
	 * the given userGroup.
	 * If the given {@code userGroupId} belongs to the virtual Everybody then the given
	 * {@code eventId} is mandatory. Retrieval of elements is similar but instead of 
	 * filtering on the userGroup, the event is used. Each retrieved user must have 
	 * at least one bet belongs to the given event. 
	 * 
	 * @param userGroupId - filter
	 * @param eventId - only in case of virtual Everybody userGroupId
	 * @return list of users which belongs to the given userGroupId
	 * @throws IllegalArgumentException if any of the given parameters is invalid
	 */
	@Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
	public List<User> retrieveUsersWithBetsByUserGroup(Long userGroupId, Long eventId) {
		List<User> users;
		
		checkNotNull(userGroupId);
		UserGroup userGroup = commonDao.findEntityById(UserGroup.class, userGroupId);
		checkArgument(userGroup != null || userGroupId == UserGroup.EVERYBODY_USER_GROUP_ID, 
				String.format("No \"UserGroup\" instance belongs to \"userGroupId\"=%d in database.", userGroupId));
		
		if (userGroupId == UserGroup.EVERYBODY_USER_GROUP_ID) {
			checkNotNull(eventId, 
					"Argument \"eventId\" cannot be null if \"userGroupId\" belongs to virtual Everybody userGroup.");
		}
		
		Dictionary userRole = dictionaryDao.findDictionaryByKeyAndValue(DictionaryEnum.ROLE.name(), RoleEnum.USER.name());
		checkState(userRole != null, "No \"Role\" instance belongs to \"role\"=%s in database.", RoleEnum.USER.name());

		Dictionary normalUserStatus = dictionaryDao.findDictionaryByKeyAndValue(DictionaryEnum.USER_STATUS.name(), UserStatusEnum.NORMAL.name());
		checkState(normalUserStatus != null, "No \"UserStatus\" instance belongs to \"userStatus\"=%s in database.", UserStatusEnum.NORMAL.name());
		
		QUser qUser = QUser.user;
		JPAQuery<User> query = new JPAQuery<>(getEntityManager());
		
		if (userGroupId == UserGroup.EVERYBODY_USER_GROUP_ID) {
			users = query.from(qUser)
					  .where(qUser.roles.contains(userRole)
							  .and(qUser.userStatus.eq(normalUserStatus))
					  		  .and(qUser.bets.any().event.eventId.eq(eventId)))
					  .orderBy(qUser.loginName.asc())
					  .fetch();
		}
		else {
			users = query.from(qUser)
					  .where(qUser.roles.contains(userRole)
							  .and(qUser.userStatus.eq(normalUserStatus))
							  .and(qUser.userGroups.contains(userGroup))
							  .and(qUser.bets.any().event.eventId.eq(userGroup.getEvent().getEventId())))
					  .orderBy(qUser.loginName.asc())
					  .fetch();
		}
		
		return users;
	}

	/**
	 * Returns the number of the list of found {@link User} instance with "USER" 
	 * role and with "NORMAL" user status which 
	 * belongs to the given {@code userGroupId} and each user has at least one bet 
	 * belongs to the event of the given userGroup.
	 * If the given {@code userGroupId} belongs to the virtual Everybody then the given
	 * {@code eventId} is mandatory. Retrieval of the list elements is similar but 
	 * instead of filtering on the userGroup, the event is used. Each retrieved user 
	 * must have at least one bet belongs to the given event. 
	 * 
	 * @param userGroupId - filter
	 * @param eventId - only in case of virtual Everybody userGroupId
	 * @return list of users which belongs to the given userGroupId
	 * @throws IllegalArgumentException if any of the given parameters is invalid
	 */
	@Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
	public int retrieveNumberOfUsersWithBetsByUserGroup(Long userGroupId, Long eventId) {
		int numberOfUsersWithBetsByUserGroup = 0;
		
		checkNotNull(userGroupId);
		UserGroup userGroup = commonDao.findEntityById(UserGroup.class, userGroupId);
		checkArgument(userGroup != null || userGroupId == UserGroup.EVERYBODY_USER_GROUP_ID, 
				String.format("No \"UserGroup\" instance belongs to \"userGroupId\"=%d in database.", userGroupId));
		
		if (userGroupId == UserGroup.EVERYBODY_USER_GROUP_ID) {
			checkNotNull(eventId, 
					"Argument \"eventId\" cannot be null if \"userGroupId\" belongs to virtual Everybody userGroup.");
		}
		
		Dictionary userRole = dictionaryDao.findDictionaryByKeyAndValue(DictionaryEnum.ROLE.name(), RoleEnum.USER.name());
		checkState(userRole != null, "No \"Role\" instance belongs to \"role\"=%s in database.", RoleEnum.USER.name());

		Dictionary normalUserStatus = dictionaryDao.findDictionaryByKeyAndValue(DictionaryEnum.USER_STATUS.name(), UserStatusEnum.NORMAL.name());
		checkState(normalUserStatus != null, "No \"UserStatus\" instance belongs to \"userStatus\"=%s in database.", UserStatusEnum.NORMAL.name());
		
		QUser qUser = QUser.user;
		JPAQuery<User> query = new JPAQuery<>(getEntityManager());
		
		if (userGroupId == 0L) {
			numberOfUsersWithBetsByUserGroup = query.from(qUser)
					  .where(qUser.roles.contains(userRole)
							  .and(qUser.userStatus.eq(normalUserStatus))
					  		  .and(qUser.bets.any().event.eventId.eq(eventId)))
					  .orderBy(qUser.loginName.asc())
					  .fetch()
					  .size();
		}
		else {
			numberOfUsersWithBetsByUserGroup = query.from(qUser)
					  .where(qUser.roles.contains(userRole)
							  .and(qUser.userStatus.eq(normalUserStatus))
							  .and(qUser.userGroups.contains(userGroup))
							  .and(qUser.bets.any().event.eventId.eq(userGroup.getEvent().getEventId())))
					  .orderBy(qUser.loginName.asc())
					  .fetch()
					  .size();
		}
		
		return numberOfUsersWithBetsByUserGroup;
	}

	/**
	 * Returns found {@link UserGroup} instance which matches the given {@code eventId}
	 * event and user group {@code name}. Otherwise {@code null} is returned.
	 * 
	 * @param name - user group name to be searched
	 * @return found {@link UserGroup} instance
	 * @throws IllegalArgumentException if any of the given parameters is invalid
	 */
	@Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
	public UserGroup findUserGroupByName(Long eventId, String name) {
		checkNotNull(eventId);
		checkArgument(!Strings.isNullOrEmpty(name), "Argument \"name\" cannot be null nor empty.");

		UserGroup userGroup = null;
		
		QUserGroup qUserGroup = QUserGroup.userGroup;
		JPAQuery<UserGroup> query = new JPAQuery<>(getEntityManager());
		userGroup = query.from(qUserGroup)
		  .where(qUserGroup.name.equalsIgnoreCase(name)
				  .and(qUserGroup.event.eventId.eq(eventId)))
		  .fetchOne();
		
		return userGroup;
	}

	/**
	 * Returns the latest {@link UserGroup} instance which matches the earlier events of
	 * the given {@code eventId} event, the latter one is also included, and does match the 
	 * given user group {@code name}. 
	 * Otherwise {@code null} returns.
	 * 
	 * @param name - user group name to be searched
	 * @return found {@link UserGroup} instance
	 * @throws IllegalArgumentException if any of the given parameters is invalid
	 */
	@Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
	public UserGroup findLastUserGroupByName(Long eventId, String name) {
		checkNotNull(eventId);
		checkArgument(!Strings.isNullOrEmpty(name), "Argument \"name\" cannot be null nor empty.");

		UserGroup userGroup = null;
		
		QUserGroup qUserGroup = QUserGroup.userGroup;
		JPAQuery<UserGroup> query = new JPAQuery<>(getEntityManager());
		userGroup = query.from(qUserGroup)
		  .where(qUserGroup.name.equalsIgnoreCase(name)
				  .and(qUserGroup.event.eventId.loe(eventId)))
		  .orderBy(qUserGroup.userGroupId.desc())
		  .fetchFirst();
		
		return userGroup;
	}

	/**
	 * Persists the given user group into database.
	 * 
	 * @param eventId
	 * @param userId - creator/owner
	 * @param name - user group name
	 * @return persisted {@link UserGroup} entity instance
	 * @throws IllegalArgumentException if any of the given parameters is invalid
	 */
	public UserGroup insertUserGroup(Long eventId, Long userId, String name) {
		checkNotNull(eventId);
		checkNotNull(userId);
		checkArgument(!Strings.isNullOrEmpty(name), "Argument \"name\" cannot be null nor empty.");
		Event event = commonDao.findEntityById(Event.class, eventId);
		checkArgument(event != null, String.format("No \"Event\" entity belongs to \"eventId\"=%d in database.", eventId));			
		User user = commonDao.findEntityById(User.class, userId);
		checkArgument(user != null, String.format("No \"User\" entity belongs to \"userId\"=%d in database.", userId));			
		
		UserGroup userGroup = new UserGroup();
		userGroup.setName(name);
		userGroup.setEvent(event);
		userGroup.setPublicEditableAsBoolean(false);
		userGroup.setPublicVisibleAsBoolean(true);
		userGroup.setOwner(user);
		userGroup.addUser(user);
		
		commonDao.persistEntity(userGroup);
		
		return userGroup;
	}

	/**
	 * Persists a new user group into database by importing the given user group.
	 * 
	 * @param eventId
	 * @param userId - creator/owner
	 * @param importedUserGroupId - userGroupId to be imported
	 * @return persisted {@link UserGroup} entity instance
	 * @throws IllegalArgumentException if any of the given parameters is invalid
	 */
	public UserGroup importUserGroup(Long eventId, Long userId, Long importedUserGroupId) {
		checkNotNull(eventId);
		checkNotNull(userId);
		checkNotNull(importedUserGroupId);
		Event event = commonDao.findEntityById(Event.class, eventId);
		checkArgument(event != null, String.format("No \"Event\" entity belongs to \"eventId\"=%d in database.", eventId));			
		User user = commonDao.findEntityById(User.class, userId);
		checkArgument(user != null, String.format("No \"User\" entity belongs to \"userId\"=%d in database.", userId));			
		UserGroup importedUserGroup = commonDao.findEntityById(UserGroup.class, importedUserGroupId);
		checkArgument(importedUserGroup != null, String.format("No \"UserGroup\" entity belongs to \"importedUserGroupId\"=%d in database.", importedUserGroupId));			
		
		UserGroup userGroup = new UserGroup();
		userGroup.setName(importedUserGroup.getName());
		userGroup.setEvent(event);
		userGroup.setPublicEditableAsBoolean(false);
		userGroup.setPublicVisibleAsBoolean(true);
		userGroup.setOwner(user);
		userGroup.getUsers().addAll(importedUserGroup.getUsers());
		if (!userGroup.getUsers().contains(user)) {
			userGroup.getUsers().add(user);
		}
		userGroup.getUsers().stream().forEach(u -> { u.getUserGroups().add(userGroup); });
		
		commonDao.persistEntity(userGroup);
		
		return userGroup;
	}

	/**
	 * Removes the {@link UserGroup} instance belongs to the given {@code userGroupId} parameter 
	 * from database.
	 * 
	 * @param userGroupId
	 * @throws IllegalArgumentException if any of the given parameters is invalid
	 */
	public void deleteUserGroup(Long userGroupId) {
		checkNotNull(userGroupId);
		UserGroup userGroup = commonDao.findEntityById(UserGroup.class, userGroupId);
		checkState(userGroup != null, String.format("No \"UserGroup\" entity belongs to \"userGroupId\"=%d in database.", userGroupId));			
		
		commonDao.removeEntity(userGroup);
	}
	
	/**
	 * Delete all userGroups where the given user is an owner.
	 * 
	 * @param userId - belongs to an {@link User} entity
	 * @throws IllegalArgumentException if any of the given parameters is {@code null}
	 */
	public void deleteUserGroupsByUser(Long userId) {
		checkNotNull(userId);
		
		QUserGroup qUserGroup = QUserGroup.userGroup;
		new JPADeleteClause(getEntityManager(), qUserGroup)
				.where(qUserGroup.owner.userId.eq(userId)).execute();
	}
}
