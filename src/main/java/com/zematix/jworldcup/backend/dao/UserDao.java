package com.zematix.jworldcup.backend.dao;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;

import javax.inject.Inject;
import javax.persistence.Query;
import javax.persistence.TypedQuery;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.base.Strings;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQuery;
import com.zematix.jworldcup.backend.entity.QUser;
import com.zematix.jworldcup.backend.entity.QUserOfEvent;
import com.zematix.jworldcup.backend.entity.Role;
import com.zematix.jworldcup.backend.entity.User;
import com.zematix.jworldcup.backend.entity.UserGroup;
import com.zematix.jworldcup.backend.entity.UserOfEvent;
import com.zematix.jworldcup.backend.entity.UserStatus;

/**
 * Database operations around {@link User} entities.
 */
@Component
@Transactional
public class UserDao extends DaoBase {

	@Inject
	private CommonDao commonDao;

	@Inject
	private BetDao betDao;

	@Inject
	private RoleDao roleDao;

	@Inject
	private UserOfEventDao userOfEventDao;

	@Inject
	private UserStatusDao userStatusDao;

	/**
	 * Returns a list of all {@link User} entities from database.
	 * 
	 * @return list of all {@link User} entities
	 */
	@Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
	public List<User> getAllUsers() {
		TypedQuery<User> query = getEntityManager().createNamedQuery("User.findAll", User.class);
		List<User> users = query.getResultList();
		return users;
	}

	/**
	 * Detaches the {@link User} entity from persistence context if managed and
	 * nullify all of its dependencies.
	 * 
	 * @param user - {@link User} instance to be detached and purified
	 */
	@Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
	public void stripUser(User user) {
		checkNotNull(user);
		commonDao.detachEntity(user);
		user.setRoles(null);
		user.setBets(null);
		user.setUserGroups(null);
		user.setOwnerUserGroups(null);
	}

	/**
	 * Returns found {@link User} instance belongs to the given {@code loginName}.
	 * Otherwise {@code null} is returned.
	 * 
	 * @param loginName
	 * @return user with the given {@code loginName}
	 * @throws NullPointerException if given {@code loginName} is {@code null}
	 */
	@Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
	public User findUserByLoginName(String loginName) {
		User user = null;
		checkNotNull(loginName);

		QUser qUser = QUser.user;
		JPAQuery<User> query = new JPAQuery<>(getEntityManager());
		user = query.from(qUser)
//				.leftJoin(qUser.roles).fetchJoin()
				.where(qUser.loginName.equalsIgnoreCase(loginName)).fetchOne();

		return user;
	}

	/**
	 * Returns found {@link User} instance which belongs to the given
	 * {@code loginName} and {@code encryptedLoginPassword}. Otherwise {@code null}
	 * is returned.
	 * 
	 * @param loginName
	 * @param encryptedLoginPassword
	 * @return found user with the given {@code loginName} and
	 *         {@code encryptedLoginPassword}
	 * @throws IllegalArgumentException if any of the given parameters is invalid
	 */
	@Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
	public User findUserByLoginNameAndEncryptedLoginPassword(String loginName, String encryptedLoginPassword) {
		User user = null;
		checkNotNull(loginName);
		checkNotNull(encryptedLoginPassword);

		QUser qUser = QUser.user;
		JPAQuery<User> query = new JPAQuery<>(getEntityManager());
		user = query.from(qUser).where(qUser.loginName.equalsIgnoreCase(loginName),
				qUser.loginPassword.equalsIgnoreCase(encryptedLoginPassword)).fetchOne();

		return user;
	}

	/**
	 * Returns found {@link User} instance which matches the given {@code loginName}
	 * or {@code emailAddr} email address. Otherwise {@code null} is returned.
	 * 
	 * @param - loginName
	 * @param - emailAddr
	 * @return found user with the given {@code loginName} or {@code emailAddr}
	 * @throws IllegalArgumentException if any of the given parameters is invalid
	 */
	@Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
	public User findUserByLoginNameOrEmailAddress(String loginName, String emailAddr) {
		User user = null;
		checkNotNull(loginName);
		checkNotNull(emailAddr);

		QUser qUser = QUser.user;
		JPAQuery<User> query = new JPAQuery<>(getEntityManager());
		user = query.from(qUser)
				.where(qUser.loginName.equalsIgnoreCase(loginName).or(qUser.emailAddr.equalsIgnoreCase(emailAddr)))
				.fetchOne();

		return user;
	}

	/**
	 * Returns found {@link User} instance which matches the given {@code emailAddr}
	 * email address. Otherwise {@code null} is returned.
	 * 
	 * @param - emailAddr
	 * @return found user with the given {@code emailAddr}
	 * @throws IllegalArgumentException if any of the given parameters is invalid
	 */
	@Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
	public User findUserByEmailAddress(String emailAddr) {
		User user = null;
		checkNotNull(emailAddr);

		QUser qUser = QUser.user;
		JPAQuery<User> query = new JPAQuery<>(getEntityManager());
		user = query.from(qUser).where(qUser.emailAddr.equalsIgnoreCase(emailAddr)).fetchOne();

		return user;
	}

	/**
	 * Returns {@code true} if exists {@link User} instance which does not match the
	 * given {@code loginName} but matches the given {@code emailAddr} email
	 * address.
	 * 
	 * @param loginName
	 * @param emailAddr
	 * @return {@code true} if exists {@link User} instance which does not match the
	 *         given loginName but matches the given email address.
	 * @throws IllegalArgumentException if any of the given parameters is invalid
	 */
	@Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
	public boolean existUserByEmailAddrExceptUser(String loginName, String emailAddr) {
		User user = null;
		checkNotNull(loginName);
		checkNotNull(emailAddr);

		QUser qUser = QUser.user;
		JPAQuery<User> query = new JPAQuery<>(getEntityManager());
		user = query.from(qUser)
				.where(qUser.loginName.notEqualsIgnoreCase(loginName), qUser.emailAddr.equalsIgnoreCase(emailAddr))
				.fetchFirst();
		return user != null;
	}

	/**
	 * Persists the given user into database with her given role and userStatus.
	 * 
	 * @param loginName
	 * @param encryptedLoginPassword
	 * @param fullName
	 * @param emailAddr
	 * @param sRole                  - roles field value of a Role instance
	 * @param sStatus                - status field value of an UserStatus instance
	 * @param token
	 * @param zoneId                 - time zone
	 * @param modification_time      - usually the actual datetime
	 * @return persisted User entity instance
	 * @throws IllegalArgumentException if no {@link Role} or {@link UserStatus}
	 *                                  instances belong to the given parameters
	 */
	public User saveUser(String loginName, String encryptedLoginPassword, String fullName, String emailAddr,
			String sRole, String sStatus, String token, String zoneId, LocalDateTime modificationTime) {
		User user = new User();
		user.setLoginName(loginName);
		user.setLoginPassword(encryptedLoginPassword);
		user.setFullName(fullName);
		user.setEmailAddr(emailAddr);
		user.setRoles(new HashSet<Role>());
		user.setToken(token);
		user.setZoneId(zoneId);
		user.setModificationTime(modificationTime);
		user.setUserGroups(new HashSet<UserGroup>());

		Role role = roleDao.findRoleByRole(sRole);
		checkArgument(role != null, String.format("Role named \"%s\" cannot be found in database.", sRole));

		UserStatus userStatus = userStatusDao.findUserStatusByStatus(sStatus);
		checkArgument(userStatus != null,
				String.format("UserStatus named \"%s\" cannot be found in database.", sStatus));
		user.setUserStatus(userStatus);

		commonDao.persistEntity(user);

		user.getRoles().add(role);
		role.getUsers().add(user);

		return user;
	}

	/**
	 * Modifies the given managed {@link User} instance in database.
	 * 
	 * @param fullName
	 * @param emailNew
	 * @param encryptedLoginPassword
	 * @param zoneId                 - time zone
	 * @param modification_time      - usually the actual datetime
	 * @return modified {@link User} instance
	 * @throws IllegalArgumentException if given {@link User} instance is
	 *                                  {@code null} or detached
	 */
	public User modifyUser(User user, String fullName, String emailNew, String encryptedLoginPassword, String zoneId,
			LocalDateTime modificationTime) {
		checkNotNull(user);
		checkArgument(commonDao.containsEntity(user),
				"Argument \"user\" entity cannot be detached from persitence context.");

		user.setFullName(fullName);
		if (emailNew != null) {
			user.setEmailNew(emailNew);
		}
		if (encryptedLoginPassword != null) {
			user.setLoginPassword(encryptedLoginPassword);
		}
		user.setZoneId(zoneId);
		user.setModificationTime(modificationTime);

		commonDao.flushEntityManager();
		return user;
	}

	/**
	 * Returns found {@link User} instance which matches the given {@code token}.
	 * Otherwise {@code null} is returned.
	 * 
	 * @param token
	 * @return found {@link User} instance
	 * @throws IllegalArgumentException if any of the given parameters is invalid
	 */
	@Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
	public User findUserByToken(String token) {
		checkArgument(!Strings.isNullOrEmpty(token), "Argument \"token\" cannot be null nor empty.");

		User user = null;

		QUser qUser = QUser.user;
		JPAQuery<User> query = new JPAQuery<>(getEntityManager());
		user = query.from(qUser).where(qUser.token.eq(token)).fetchOne();

		return user;
	}

	/**
	 * Saves given status belongs to a {@link UserStatus} of the given {@link User}
	 * instance.
	 * 
	 * @param user              - {@link User} instance to be modified
	 * @param status            - status string belongs to a {@link UserStatus}
	 * @param modification_time - usually the actual datetime
	 * @return modified {@link User} instance
	 * @throws IllegalArgumentException if any of the given parameters is invalid
	 */
	public User modifyUserStatusToken(User user, String status, LocalDateTime modificationTime) {
		checkNotNull(user);
		checkArgument(commonDao.containsEntity(user),
				"Argument \"user\" entity cannot be detached from persitence context.");
		checkArgument(!Strings.isNullOrEmpty(status), "Argument \"status\" cannot be null nor empty.");

		UserStatus userStatus = userStatusDao.findUserStatusByStatus(status);
		checkState(userStatus != null, String.format("No value belongs to argument \"status\"=%s in database.", status));

		user.setUserStatus(userStatus);
		user.setModificationTime(modificationTime);

		commonDao.flushEntityManager();
		return user;
	}

	/**
	 * Returns found {@link UserOfEvent} instance which matches the given
	 * {@code userId} and {@code eventId}. Otherwise {@code null} is returned.
	 * 
	 * @param - eventId
	 * @param - userId
	 * @return found userOfEvent with the given {@code userId} and {@code eventId}
	 * @throws IllegalArgumentException if any of the given parameters is invalid
	 */
	@Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
	public UserOfEvent retrieveUserOfEvent(Long eventId, Long userId) {
		UserOfEvent userOfEvent = null;
		checkNotNull(eventId);
		checkNotNull(userId);

		QUserOfEvent qUserOfEvent = QUserOfEvent.userOfEvent;
		JPAQuery<UserOfEvent> query = new JPAQuery<>(getEntityManager());
		userOfEvent = query.from(qUserOfEvent)
				.where(qUserOfEvent.user.userId.eq(userId).and(qUserOfEvent.event.eventId.eq(eventId))).fetchOne();

		return userOfEvent;
	}

	/**
	 * Returns found {@link User} instance which matches the given {@code loginName}
	 * and {@code fullName} parameters. If one of the input parameters is empty,
	 * that one is not taken into consideration during search. However if both
	 * parameters are empty, {@link IllegalArgumentException} is thrown. If there is
	 * no result, {@code null} is returned.
	 * 
	 * @param loginName
	 * @param fullName
	 * @return user with the given loginName and fullName or {code null} if not
	 *         found
	 * @throws IllegalArgumentException if both input parameters are empty
	 */
	@Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
	public User findUserByLoginNameOrFullName(String loginName, String fullName) {
		User user = null;

		checkArgument(!Strings.isNullOrEmpty(loginName) || !Strings.isNullOrEmpty(fullName),
				"Argument \"loginName\" and \"fullName\" cannot be null nor empty at the same time.");

		QUser qUser = QUser.user;
		JPAQuery<User> query = new JPAQuery<>(getEntityManager());

		BooleanBuilder where = new BooleanBuilder();
		if (!Strings.isNullOrEmpty(loginName)) {
			where.and(qUser.loginName.equalsIgnoreCase(loginName));
		}
		if (!Strings.isNullOrEmpty(fullName)) {
			where.and(qUser.fullName.equalsIgnoreCase(fullName));
		}
		user = query.from(qUser).where(where).fetchOne();

		return user;
	}

	/**
	 * Returns a list of {@link User} instances, where the elements match the given
	 * {@code loginName} prefix. Empty list is returned if no elements are matched
	 * at all.
	 * 
	 * @param loginNamePrefix
	 * @return list of {@link User} instances with matched {@code loginNamePrefix}
	 * @throws IllegalArgumentException if the given parameter is {@code null} or
	 *                                  empty
	 */
	@Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
	public List<User> findUsersByLoginNamePrefix(String loginNamePrefix) {
		checkArgument(!Strings.isNullOrEmpty(loginNamePrefix),
				"Argument \"loginNamePrefix\" cannot be null nor empty.");

		QUser qUser = QUser.user;
		JPAQuery<User> query = new JPAQuery<>(getEntityManager());

		List<User> users = query.from(qUser).where(qUser.loginName.startsWithIgnoreCase(loginNamePrefix)).fetch();

		return users;
	}

	/**
	 * Returns a list of {@link User} instances, where the elements contain the
	 * given {@code fullNameContain} value. Empty list is returned if no elements
	 * are matched at all.
	 * 
	 * @param fullNameContain
	 * @return list of {@link User} instances with matched {@code fullNameContain}
	 * @throws IllegalArgumentException if the given parameter is {@code null} or
	 *                                  empty
	 * 
	 */
	@Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
	public List<User> findUsersByFullNameContain(String fullNameContain) {
		checkArgument(!Strings.isNullOrEmpty(fullNameContain),
				"Argument \"fullNameContain\" cannot be null nor empty.");

		QUser qUser = QUser.user;
		JPAQuery<User> query = new JPAQuery<>(getEntityManager());

		List<User> users = query.from(qUser).where(qUser.fullName.containsIgnoreCase(fullNameContain)).fetch();

		return users;
	}

	/**
	 * Accepts and executes email modification initiated earlier by user.
	 * 
	 * @param user - {@link User} instance to be modified
	 * @return {@code true} if the email modification is accepted and done
	 * @throws IllegalArgumentException if any of the given parameter is invalid
	 */
	public boolean modifyUserEmailAddr(User user, LocalDateTime modificationTime) {
		boolean isModified = false;

		checkNotNull(user);
		checkArgument(commonDao.containsEntity(user), "Argument \"user\" entity must exists in persistence context.");

		if (!Strings.isNullOrEmpty(user.getEmailNew())) {
			// do modify email address
			user.setEmailAddr(user.getEmailNew());
			user.setEmailNew(null);

			user.setModificationTime(modificationTime);

			commonDao.flushEntityManager();
			isModified = true;
		}

		return isModified;
	}

	/**
	 * Starts the password reset initiated earlier by user.
	 * 
	 * @param user          - {@link User} instance to be modified
	 * @param resetPassword - new password to be set later
	 * @return {@code true} if the password reset is started successfully
	 * @throws IllegalArgumentException if any of the given parameter is invalid
	 */
	public boolean modifyUserResetPassword(User user, String resetPassword, LocalDateTime modificationTime) {
		boolean isModified = false;

		checkNotNull(user);
		checkArgument(commonDao.containsEntity(user), "Argument \"user\" entity must exists in persistence context.");
		checkArgument(!Strings.isNullOrEmpty(resetPassword), "Argument \"resetPassword\" cannot be null nor empty.");

		// do modify reset password
		user.setResetPassword(resetPassword);

		user.setModificationTime(modificationTime);

		commonDao.flushEntityManager();
		isModified = true;

		return isModified;
	}

	/**
	 * Accepts and executes password reset initiated earlier by user.
	 * 
	 * @param user - {@link User} instance to be modified
	 * @return {@code true} if the password reset is accepted and done
	 * @throws IllegalArgumentException if any of the given parameter is invalid
	 */

	public boolean finalizeUserResetPassword(User user, LocalDateTime modificationTime) {
		boolean isModified = false;

		checkNotNull(user);
		checkArgument(commonDao.containsEntity(user), "Argument \"user\" entity must exists in persistence context.");

		// do modify reset password
		user.setLoginPassword(user.getResetPassword());
		user.setResetPassword(null);

		user.setModificationTime(modificationTime);

		commonDao.flushEntityManager();
		isModified = true;

		return isModified;
	}

	/**
	 * Delete all associations between users and roles of the given user.
	 * 
	 * @param userId - belongs to an {@link User} entity
	 * @throws IllegalArgumentException if any of the given parameters is
	 *                                  {@code null}
	 */
	public void deleteUserRolesByUserId(Long userId) {
		checkNotNull(userId);

		Query query = getEntityManager().createNamedQuery("deleteUserRolesByUserId", User.class);
		query.setParameter(1, userId);
		query.executeUpdate();
	}

	/**
	 * Delete all associations between users and userGroups of the given user.
	 * 
	 * @param userId - belongs to an {@link User} entity
	 * @throws IllegalArgumentException if any of the given parameters is
	 *                                  {@code null}
	 */
	public void deleteUserUserGroupsByUserId(Long userId) {
		checkNotNull(userId);

		Query query = getEntityManager().createNamedQuery("deleteUserUserGroupsByUserId", User.class);
		query.setParameter(1, userId);
		query.executeUpdate();
	}

	public void deleteUser(User user) {
		checkNotNull(user);
		checkArgument(commonDao.containsEntity(user), "Argument \"user\" entity must exists in persistence context.");

		// delete all dependencies of user
		betDao.deleteBetsByUser(user.getUserId());
		deleteUserRolesByUserId(user.getUserId());
		deleteUserUserGroupsByUserId(user.getUserId());
		userOfEventDao.deleteUserOfEventsByUser(user.getUserId());

		commonDao.removeEntity(user);

		commonDao.flushEntityManager();
	}

	@Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
	public List<User> findExpiredCandidateUsers(LocalDateTime expiredModificationTime) {
		QUser qUser = QUser.user;
		JPAQuery<User> query = new JPAQuery<>(getEntityManager());

		List<User> users = query.from(qUser).where(
				qUser.userStatus.status.eq("CANDIDATE").and(qUser.modificationTime.before(expiredModificationTime)))
				.fetch();
		return users;
	}

	@Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
	public List<User> findExpiredEmailModificationUsers(LocalDateTime expiredModificationTime) {
		QUser qUser = QUser.user;
		JPAQuery<User> query = new JPAQuery<>(getEntityManager());

		List<User> users = query.from(qUser)
				.where(qUser.emailNew.isNotNull().and(qUser.modificationTime.before(expiredModificationTime))).fetch();
		return users;
	}

	@Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
	public List<User> findExpiredPasswordResetUsers(LocalDateTime expiredModificationTime) {
		QUser qUser = QUser.user;
		JPAQuery<User> query = new JPAQuery<>(getEntityManager());

		List<User> users = query.from(qUser)
				.where(qUser.resetPassword.isNotNull().and(qUser.modificationTime.before(expiredModificationTime)))
				.fetch();
		return users;
	}

	/**
	 * Returns an{@link User} instance which has ADMIN role. First found one is returned.
	 * Otherwise {@code null} is returned.
	 * 
	 * @return an user with ADMIN role
	 * @throws IllegalStateException if there is no user with ADMIN role in the database
	 */
	@Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
	public User findFirstAdminUser() {
		User user = null;

		Role userRole = roleDao.findRoleByRole("ADMIN");
		checkState(userRole != null, "No \"Role\" instance belongs to \"role\"=%s in database.", "ADMIN");

		QUser qUser = QUser.user;
		JPAQuery<User> query = new JPAQuery<>(getEntityManager());
		user = query.from(qUser).where(qUser.roles.contains(userRole)).fetchFirst();

		return user;
	}
}
