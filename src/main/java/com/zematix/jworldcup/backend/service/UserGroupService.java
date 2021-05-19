package com.zematix.jworldcup.backend.service;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Properties;

import javax.inject.Inject;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.dataformat.javaprop.JavaPropsMapper;
import com.google.common.base.Strings;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import com.zematix.jworldcup.backend.dao.CommonDao;
import com.zematix.jworldcup.backend.dao.UserDao;
import com.zematix.jworldcup.backend.dao.UserGroupDao;
import com.zematix.jworldcup.backend.emun.ParameterizedMessageType;
import com.zematix.jworldcup.backend.emun.TemplateId;
import com.zematix.jworldcup.backend.entity.Event;
import com.zematix.jworldcup.backend.entity.Role;
import com.zematix.jworldcup.backend.entity.User;
import com.zematix.jworldcup.backend.entity.UserGroup;
import com.zematix.jworldcup.backend.entity.UserStatus;
import com.zematix.jworldcup.backend.exception.ServiceException;
import com.zematix.jworldcup.backend.model.ParameterizedMessage;
import com.zematix.jworldcup.backend.model.UserCertificate;
import com.zematix.jworldcup.backend.model.UserPosition;

/**
 * Operations around {@link UserGroup} elements. 
 * Usually it injects at least its DAO class belongs to the same entities. 
 * It may also inject other services and DAO classes.
 */
@Service
@Transactional
public class UserGroupService extends ServiceBase {
	
	/**
	 * Maximum displayed users (rows) in topUsers list
	 */
	private static final int MAX_TOP_USERS = 20;

	@Inject 
	private UserGroupDao userGroupDao;
	
	@Inject
	private ApplicationService applicationService;
	
	@Inject
	private UserDao userDao;
	
	@Inject
	private CommonDao commonDao;

	@Inject
	private BetService betService;

	@Inject
	private TemplateService templateService;
	
	@Inject
	private EventService eventService;

	/**
	 * Returns a new virtual Everybody userGroup object belongs to the given 
	 * {@code eventId} event and {@code userId} user. Returned object is not 
	 * linked to the database at all.
	 * 
	 * @param eventId
	 * @param userId
	 * @return new virtual Everybody userGroup object
	 */
	@Transactional(readOnly = true)
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
	 * Returns a list of {@link UserGroup} instances which belongs to the 
	 * given {@link Event#eventId} and {@link User#userId}.
	 * The latter means that userGroup contains the given user as a member of it.
	 * If the given {@code isEverybodyIncluded} parameter is {@code true}, a virtual 
	 * Everybody userGroup is also added to the end of the result list.
	 * 
	 * @param eventId - filter
	 * @param userId - filter
	 * @return list of userGroups which belongs to the given eventId and userId
	 */
	@Transactional(readOnly = true)
	public List<UserGroup> retrieveUserGroups(Long eventId, Long userId, boolean isEverybodyIncluded) throws ServiceException {
		checkNotNull(eventId);
		checkNotNull(userId);
		
		List<UserGroup> userGroups = userGroupDao.retrieveUserGroups(eventId, userId);
		
		if (isEverybodyIncluded) {
			// used by scores ui
			UserGroup userGroup = createVirtualEverybodyUserGroup(eventId, userId);
			userGroups.add(userGroup);
		}
		else {
			// used by userGroups ui
			userGroups.stream().forEach(e -> e.setUsersAsList(userGroupDao.retrieveUsersByUserGroup(e.getUserGroupId())));
		}
		
		return userGroups;
	}
	
	/**
	 * Returns a list of found {@link User} instance with "USER" {@link Role#getRole()} 
	 * and with "NORMAL" {@link UserStatus#getStatus()} which belongs to the given 
	 * {@code userGroupId}. If the given userGroup is virtual Everybody then all users 
	 * are retrieved.
	 * 
	 * @param userGroupId - filter
	 * @return list of users which belongs to the given userGroupId
	 * @throws IllegalArgumentException if any of the given parameters is invalid
	 */
	@Transactional(readOnly = true)
	public List<User> retrieveUsersByUserGroup(Long userGroupId) throws ServiceException {
		checkNotNull(userGroupId);

		return userGroupDao.retrieveUsersByUserGroup(userGroupId);
	}

	/**
	 * Returns a sorted list of {@link UserPosition} instances which belong to the given {@link Event#eventId}
	 * and {@link UserGroup#userGroupId}. The returned elements are sorted by their scores. 
	 * @param eventId - filter for {@link Event}
	 * @param userGroupId - filter for {@link UserGroup}
	 * @return list of sorted userPositions which belongs to the given eventId and userGroupId
	 */
	@Transactional(readOnly = true)
	public List<UserPosition> retrieveUserPositions(Long eventId, Long userGroupId) throws ServiceException {
		List<UserPosition> userPositions = new ArrayList<>();
		
		checkNotNull(eventId);
		checkNotNull(userGroupId);
		
		List<User> users = userGroupDao.retrieveUsersWithBetsByUserGroup(userGroupId, eventId);
		
		for (User user : users) {
			UserPosition userPosition = new UserPosition();
			userPosition.setUserId(user.getUserId());
			userPosition.setLoginName(user.getLoginName());
			userPosition.setFullName(user.getFullName());
			userPosition.setScore(betService.retrieveScoreByEventAndUser(eventId, user.getUserId()));
			userPositions.add(userPosition);
		}
		
//		Comparator<UserPosition> comparator = new Comparator<UserPosition>() {
//			@Override
//			public int compare(UserPosition userPosition1, UserPosition userPosition2) {
//				return -1*Integer.valueOf(userPosition1.getScore()).compareTo(userPosition2.getScore());
//			}
//		};
		Comparator<UserPosition> comparator = (a, b) -> -1*Integer.valueOf(a.getScore()).compareTo(b.getScore());
		
		userPositions.sort(comparator);

		// set positions on each element of the sorted list
		userPositions.stream()
				.forEach(pos -> pos.setPosition(userPositions.indexOf(pos) == 0 ? 1
						: (comparator.compare(pos, userPositions.get(userPositions.indexOf(pos) - 1)) == 0
								? userPositions.get(userPositions.indexOf(pos) - 1).getPosition()
								: userPositions.indexOf(pos)+1)));
		
		return userPositions;
	}
	
//	/**
//	 * Returns {@link UserGroup} instance which matches the given event and 
//	 * user group name. Otherwise {@code null} is returned.
//	 */
//	@Override
//	@Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
//	public UserGroup findUserGroupByName(Long eventId, String name) throws ServiceException {
//		UserGroup userGroup = userGroupDao.findUserGroupByName(eventId, name);
//		return userGroup;
//	}
//
//	/**
//	 * Returns {@link UserGroup} instance which matches the given event and 
//	 * user group name. Otherwise {@code null} is returned.
//	 */
//	@Override
//	@Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
//	public UserGroup findLastUserGroupByName(Long eventId, String name) throws ServiceException {
//		UserGroup userGroup = userGroupDao.findLastUserGroupByName(eventId, name);
//		return userGroup;
//	}

	/**
	 * Persists the given user group into database.
	 * 
	 * @param eventId
	 * @param userId - creator/owner
	 * @param name - user group name
	 * @param isInsertConfirmed
	 * @return persisted UserGroup entity instance
	 */
	public UserGroup insertUserGroup(Long eventId, Long userId, String name, boolean isInserConfirmed) throws ServiceException {
		checkNotNull(eventId);
		checkNotNull(userId);
		
		List<ParameterizedMessage> errMsgs = new ArrayList<>();
		
		if (Strings.isNullOrEmpty(name)) {
			errMsgs.add(ParameterizedMessage.create("USER_GROUP_NAME_EMPTY"));
			throw new ServiceException(errMsgs);
		}
		
//		if (name.equals(UserGroup.EVERYBODY_NAME)) {
//			errMsgs.add(ParametrizedMessage.create("USER_GROUP_EVERYBODY_CANNOT_BE_MODIFIED"));
//			throw new ServiceException(errMsgs);
//		}
		
		UserGroup foundUserGroup = userGroupDao.findLastUserGroupByName(eventId, name);
		if (foundUserGroup != null) {
			if (!foundUserGroup.getEvent().getEventId().equals(eventId)) {
				if (!isInserConfirmed) {
					// userGroup found on earlier event, confirm dialog with either importing or doing insertion?
					errMsgs.add(ParameterizedMessage.create("USER_GROUP_NAME_OCCUPIED_ON_EARLIER_EVENT", 
							ParameterizedMessageType.INFO, 
							foundUserGroup.getEvent().getShortDescWithYear(),
							foundUserGroup.getUserGroupId()));
				}
			}
			else if (!foundUserGroup.getOwner().getUserId().equals(userId)) {
				errMsgs.add(ParameterizedMessage.create("USER_GROUP_NAME_OCCUPIED"));
			}
			else {
				errMsgs.add(ParameterizedMessage.create("USER_GROUP_NAME_ALREADY_EXIST"));
			}
		}

		if (!errMsgs.isEmpty()) {
			throw new ServiceException(errMsgs);
		}

		UserGroup userGroup = userGroupDao.insertUserGroup(eventId, userId, name);
		
		// init transient field
		userGroup.setUsersAsList(userGroupDao.retrieveUsersByUserGroup(userGroup.getUserGroupId()));
		
		return userGroup;
	}

	/**
	 * Persists a new user group into database by importing the given user group.
	 * 
	 * @param eventId
	 * @param userId - creator/owner
	 * @param importedUserGroup - userGroupId to be imported
	 * @return persisted UserGroup entity instance
	 */
	public UserGroup importUserGroup(Long eventId, Long userId, String name) throws ServiceException {
		checkNotNull(eventId);
		checkNotNull(userId);

		List<ParameterizedMessage> errMsgs = new ArrayList<>();
		
		if (Strings.isNullOrEmpty(name)) {
			errMsgs.add(ParameterizedMessage.create("USER_GROUP_NAME_EMPTY"));
			throw new ServiceException(errMsgs);
		}
		
//		if (name.equals(UserGroup.EVERYBODY_NAME)) {
//			errMsgs.add(ParametrizedMessage.create("USER_GROUP_EVERYBODY_CANNOT_BE_MODIFIED"));
//			throw new ServiceException(errMsgs);
//		}
		
		UserGroup foundUserGroup = userGroupDao.findLastUserGroupByName(eventId, name);
		
		if (foundUserGroup == null) {
			errMsgs.add(ParameterizedMessage.create("USER_GROUP_NAME_NOT_EXIST"));
		}
		else {
			if (foundUserGroup.getEvent().getEventId().equals(eventId)) {
				if (!foundUserGroup.getOwner().getUserId().equals(userId)) {
					errMsgs.add(ParameterizedMessage.create("USER_GROUP_NAME_OCCUPIED"));
				}
				else {
					errMsgs.add(ParameterizedMessage.create("USER_GROUP_NAME_ALREADY_EXIST"));
				}
			}
		}

		if (!errMsgs.isEmpty()) {
			throw new ServiceException(errMsgs);
		}

		UserGroup userGroup = userGroupDao.importUserGroup(eventId, userId, foundUserGroup.getUserGroupId());
		
		// init transient field
		userGroup.setUsersAsList(userGroupDao.retrieveUsersByUserGroup(userGroup.getUserGroupId()));

		return userGroup;
	}

	/**
	 * Removes the given user group from database.
	 * 
	 * @param userGroupId
	 */
	public void deleteUserGroup(Long userGroupId) throws ServiceException {
		checkNotNull(userGroupId);

		userGroupDao.deleteUserGroup(userGroupId);
	}

	/**
	 * Adds a User instance given by her login or full name to the given UserGroup instance. 
	 * As a precondition, both entity must exist in the database.
	 * If given user is already associated to given user group, the function does 
	 * nothing.
	 * 
	 * @param userGroupId - this user group should be expanded
	 * @param loginName - filter belongs to the user to be added
	 * @param fullName - filter belongs to the user to be added
	 * @throws ServiceException
	 * @throws IllegalArgumentException
	 * @throws IllegalStateException
	 */
	public User findAndAddUserToUserGroup(Long userGroupId, String loginName, String fullName) throws ServiceException {
		List<ParameterizedMessage> errMsgs = new ArrayList<>();

		checkNotNull(userGroupId);

		UserGroup userGroup = commonDao.findEntityById(UserGroup.class, userGroupId);
		checkState(userGroup != null,
				String.format("No \"UserGroup\" entity belongs to \"userGroupId\"=%d in database.", userGroupId));

		if (loginName.isEmpty() && fullName.isEmpty()) {
			errMsgs.add(ParameterizedMessage.create("USER_GROUP_FIELDS_EMPTY"));
			throw new ServiceException(errMsgs);
		}

		User user = userDao.findUserByLoginNameOrFullName(loginName, fullName);
		if (user == null) {
			errMsgs.add(ParameterizedMessage.create("NO_USER_BELONGS_TO_USER_GROUP"));
			throw new ServiceException(errMsgs);
		}
		
		if (userGroup.getUsers().contains(user)) {
			errMsgs.add(ParameterizedMessage.create("USER_IS_ALREADY_IN_USER_GROUP", ParameterizedMessageType.WARNING));
			throw new ServiceException(errMsgs);
		}
		
		boolean isAdded = userGroup.getUsers().add(user);
		if (isAdded) {
			userGroup = commonDao.mergeEntity(userGroup);
			user.getUserGroups().add(userGroup);
			user = commonDao.mergeEntity(user);
			
			commonDao.flushEntityManager();
		}
		
		return user;
	}
	
	/**
	 * Removes given User from the given UserGroup. Both entity must exist in the database.
	 * If given user is not associated to given user group, the function does nothing.
	 * 
	 * @param userGroupId - this user group should be changed
	 * @param userId - this user should be removed from the given user group
	 * @throws ServiceException
	 * @throws IllegalArgumentException
	 * @throws IllegalStateException
	 */
	public void removeUserFromUserGroup(Long userGroupId, Long userId) throws ServiceException {
		checkNotNull(userGroupId);
		checkNotNull(userId);

		UserGroup userGroup = commonDao.findEntityById(UserGroup.class, userGroupId);
		checkState(userGroup != null,
				String.format("No \"UserGroup\" entity belongs to \"userGroupId\"=%d in database.", userId));

		User user = commonDao.findEntityById(User.class, userId);
		if (user == null) {
			throw new IllegalStateException(String.format("No \"User\" entity belongs to \"userId\"=%d in database.", userId));
		}

		boolean isRemoved = userGroup.getUsers().remove(user); // sometimes does not work. why? later we will use Set instead of List at entity joins.
		if (isRemoved) {
			userGroup = commonDao.mergeEntity(userGroup);
			commonDao.flushEntityManager();
		}
		//logger.info(String.format("userGroup.user size=%d", userGroup.getUsers().size()));
	}

	/**
	 * Returns a sorted list of {@link UserCertificate} instances which belong to the given {@link Event#eventId}
	 * and {@link User#userId}.
	 * 
	 * @param eventId - filter for {@link Event}
	 * @param userId - filter for {@link User}
	 * @return list of sorted userCertificate objects which belongs to the given eventId and userId
	 */
	@Transactional(readOnly = true)
	public List<UserCertificate> retrieveUserCertificates(Long eventId, Long userId) throws ServiceException {
		List<UserCertificate> userCertificates = new ArrayList<>();
		
		checkNotNull(eventId);
		checkNotNull(userId);
		Event event = commonDao.findEntityById(Event.class, eventId);
		checkState(event != null, String.format("No \"Event\" entity belongs to \"eventId\"=%d in database.", eventId));
		User user = commonDao.findEntityById(User.class, userId);
		checkState(user != null, String.format("No \"User\" entity belongs to \"userId\"=%d in database.", userId));
		
		int maximumScoreByEvent = betService.retrieveMaximumScoreByEvent(eventId);
		
		List<UserGroup> userGroups = retrieveUserGroups(eventId, userId, /*isEverybodyIncluded*/ true);

		for (UserGroup userGroup : userGroups) {
			UserCertificate userCertificate = new UserCertificate();
			userCertificate.setUserGroupId(userGroup.getUserGroupId());
			userCertificate.setUserGroupName(userGroup.getName());
			
			userCertificate.setEventShortDescWithYear(event.getShortDescWithYear());
			userCertificate.setUserLoginName(user.getLoginName());
			userCertificate.setUserFullName(user.getFullName());
			
			List<UserPosition> userPositions = retrieveUserPositions(eventId, userGroup.getUserGroupId());
			if (userPositions.isEmpty()) {
				// exclude empty userGroup
				continue;
			}
			
			userCertificate.setNumberOfMembers(userPositions.size());
			if (userGroup.isEverybody()) {
				userCertificate.setEverybody(true);
				userCertificate.setNumberOfEverybodyMembers(userPositions.size());
			}
			
			Comparator<UserPosition> comparator = (a, b) -> -1*Integer.valueOf(a.getScore()).compareTo(b.getScore());
			userPositions.sort(comparator);
			// set positions on each element of the sorted list
			userPositions.stream()
					.forEach(pos -> pos.setPosition(userPositions.indexOf(pos) == 0 ? 1
							: (comparator.compare(pos, userPositions.get(userPositions.indexOf(pos) - 1)) == 0
									? userPositions.get(userPositions.indexOf(pos) - 1).getPosition()
									: userPositions.indexOf(pos)+1)));
			
			userCertificate.setMaximumScoreByEvent(maximumScoreByEvent);
			
			UserPosition firstUserPosition = Iterables.getFirst(userPositions, null);
			if (firstUserPosition != null) {
				userCertificate.setFirstUserScore(firstUserPosition.getScore());
			}
			
			UserPosition userPosition = userPositions.stream().filter(e -> e.getUserId().equals(userId)).findFirst().orElse(null);
			if (userPosition != null) {
				userCertificate.setUserScore(userPosition.getScore());
				userCertificate.setUserGroupPosition(userPosition.getPosition());
			}
			
			UserPosition lastUserPosition = Iterables.getLast(userPositions, null);
			if (lastUserPosition != null) {
				userCertificate.setUserGroupLastPosition(lastUserPosition.getPosition());
			}
			
			userCertificates.add(userCertificate);
		}
		
		UserCertificate everybodyUserCertificate = userCertificates.stream().filter(e -> e.isEverybody()).findFirst().orElse(null);
		if (everybodyUserCertificate != null) {
			userCertificates.stream().filter(e -> !e.isEverybody()).forEach(e -> e.setNumberOfEverybodyMembers(everybodyUserCertificate.getNumberOfEverybodyMembers()));
		}
		
		Comparator<UserCertificate> comparator = (a, b) -> -1*Double.valueOf(a.getScore()).compareTo(b.getScore());
		
		userCertificates.sort(comparator);

		// set positions on each element of the sorted list
		userCertificates.stream()
				.forEach(pos -> pos.setPosition(userCertificates.indexOf(pos) == 0 ? 1
						: (comparator.compare(pos, userCertificates.get(userCertificates.indexOf(pos) - 1)) == 0
								? userCertificates.get(userCertificates.indexOf(pos) - 1).getPosition()
								: userCertificates.indexOf(pos)+1)));
		
		return userCertificates;
	}

	/**
	 * Generates user certificate document in PDF format. 
	 * 
	 * @param userCertificate - template data
	 * @param locale - document language
	 * @return OutputStream object containing generated PDF in a byte array 
	 * @throws ServiceException if something goes wrong 
	 */
	@Transactional(readOnly = true)
	public ByteArrayOutputStream printUserCertificate(UserCertificate userCertificate, Locale locale) throws ServiceException {
		checkNotNull(userCertificate);
		checkNotNull(locale);
		
		List<ParameterizedMessage> errMsgs = new ArrayList<>();
		
		ByteArrayOutputStream pdfOutputStream = null;
		JavaPropsMapper mapper = new JavaPropsMapper();
		Properties properties = new Properties();

		try {
			properties = mapper.writeValueAsProperties(userCertificate);
			//UserCertificate userCertificate = mapper.readValue(properties, UserCertificate.class); // other direction
			pdfOutputStream = templateService.generatePDFContent(TemplateId.USER_CERTIFICATE_PDF, properties, locale);
		} catch (IOException e) {
			errMsgs.add(ParameterizedMessage.create("TEMPLATE_GENERATION_FAILED", TemplateId.USER_CERTIFICATE_PDF));
			logger.error(e.getMessage(), e);
		} catch (ServiceException e) {
			errMsgs.addAll(e.getMessages());
		}

		if(!errMsgs.isEmpty()) {
			throw new ServiceException(errMsgs);
		}
		
		return pdfOutputStream;
	}

	/**
	 * Returns a sorted list of {@link UserCertificate} instances which belong to the given {@link Event#eventId}
	 * and {@link User#userId}.
	 * 
	 * @param eventId - filter for {@link Event}
	 * @param userId - filter for {@link User}
	 * @return list of sorted userCertificate objects which belongs to the given eventId and userId
	 */
	@Transactional(readOnly = true)
	public List<UserCertificate> retrieveTopUsers() throws ServiceException {
		List<UserCertificate> topUsers = new ArrayList<>();
		Comparator<UserCertificate> comparator = (a, b) -> -1*Double.valueOf(a.getScore()).compareTo(b.getScore());
		
		Integer maxNumberOfEverybodyMembers = null;
		for (Event event : eventService.findCompletedEvents()) {
			int numberOfUsersWithBetsByUserGroup = userGroupDao.retrieveNumberOfUsersWithBetsByUserGroup(UserGroup.EVERYBODY_USER_GROUP_ID, event.getEventId());
			if (maxNumberOfEverybodyMembers == null || numberOfUsersWithBetsByUserGroup > maxNumberOfEverybodyMembers) {
				maxNumberOfEverybodyMembers = numberOfUsersWithBetsByUserGroup;
			}
		}
		
		for (Event event : eventService.findCompletedEvents()) {
			List<UserCertificate> topUsersByEvent = retrieveTopUsersByEvent(event.getEventId(), maxNumberOfEverybodyMembers);
			topUsers.addAll(topUsersByEvent);
			topUsers.sort(comparator);
			// remove elements above MAX_TOP_USERS position
			while (topUsers.size() > MAX_TOP_USERS) {
				topUsers.remove(topUsers.size()-1);
			}
		}
		
		// set positions on each element of the sorted list
		topUsers.stream()
				.forEach(pos -> pos.setPosition(topUsers.indexOf(pos) == 0 ? 1
						: (comparator.compare(pos, topUsers.get(topUsers.indexOf(pos) - 1)) == 0
								? topUsers.get(topUsers.indexOf(pos) - 1).getPosition()
								: topUsers.indexOf(pos)+1)));
		
		return topUsers;
	}
	
	/**
	 * Returns a sorted list of {@link UserCertificate} instances which belong to the given 
	 * completed {@link Event#eventId}.
	 * 
	 * @param eventId - filter for {@link Event}, must be completed
	 * @param maxNumberOfEverybodyMembers - maximum of members of all completed events
	 * @return list of sorted userCertificate objects which belongs to the given eventId
	 */
	@Transactional(readOnly = true)
	public List<UserCertificate> retrieveTopUsersByEvent(Long eventId, Integer maxNumberOfEverybodyMembers) throws ServiceException {
		List<UserCertificate> userCertificates = new ArrayList<>();
		
		checkNotNull(eventId);
		Event event = commonDao.findEntityById(Event.class, eventId);
		checkState(event != null, String.format("No \"Event\" entity belongs to \"eventId\"=%d in database.", eventId));
		checkState(applicationService.getEventCompletionPercentCache(eventId) == 100, 
				String.format("Event with eventId=%d must be completed.", eventId));
		
		int maximumScoreByEvent = betService.retrieveMaximumScoreByEvent(eventId);

		User adminUser = userDao.findFirstAdminUser();
		UserGroup userGroup = createVirtualEverybodyUserGroup(eventId, adminUser.getUserId());
		List<UserPosition> userPositions = retrieveUserPositions(eventId, userGroup.getUserGroupId());

		for (UserPosition userPosition : userPositions) {
			if (userCertificates.size() > MAX_TOP_USERS) {
				break;
			}
			
			UserCertificate userCertificate = new UserCertificate();
			userCertificate.setUserGroupId(userGroup.getUserGroupId());
			userCertificate.setUserGroupName(userGroup.getName());
			
			userCertificate.setEventShortDescWithYear(event.getShortDescWithYear());
			userCertificate.setUserLoginName(userPosition.getLoginName());
			userCertificate.setUserFullName(userPosition.getFullName());
			
			userCertificate.setNumberOfMembers(userPositions.size());
			if (userGroup.isEverybody()) {
				userCertificate.setEverybody(true);
				userCertificate.setNumberOfEverybodyMembers(maxNumberOfEverybodyMembers != null ? maxNumberOfEverybodyMembers : userPositions.size());
			}
			
			userCertificate.setMaximumScoreByEvent(maximumScoreByEvent);
			
			UserPosition firstUserPosition = Iterables.getFirst(userPositions, null);
			if (firstUserPosition != null) {
				userCertificate.setFirstUserScore(firstUserPosition.getScore());
			}
			
			userCertificate.setUserScore(userPosition.getScore());
			userCertificate.setUserGroupPosition(userPosition.getPosition());
			
			UserPosition lastUserPosition = Iterables.getLast(userPositions, null);
			if (lastUserPosition != null) {
				userCertificate.setUserGroupLastPosition(lastUserPosition.getPosition());
			}
			
			userCertificates.add(userCertificate);
		}
		
		return userCertificates;
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
	@Transactional
	public UserGroup insertUserGroup(Long eventId, Long userId, String name) {
		checkNotNull(eventId);
		checkNotNull(userId);
		checkArgument(!Strings.isNullOrEmpty(name), "Argument \"name\" cannot be null nor empty.");
		Event event = commonDao.findEntityById(Event.class, eventId);
		checkState(event != null, String.format("No \"Event\" entity belongs to \"eventId\"=%d in database.", eventId));			
		User user = commonDao.findEntityById(User.class, userId);
		checkState(user != null, String.format("No \"User\" entity belongs to \"userId\"=%d in database.", userId));			
		
		UserGroup userGroup = new UserGroup();
		userGroup.setName(name);
		userGroup.setEvent(event);
		userGroup.setPublicEditableAsBoolean(false);
		userGroup.setPublicVisibleAsBoolean(true);
		userGroup.setOwner(user);
		userGroup.setUsers(Sets.newHashSet(user));
		
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
	@Transactional
	public UserGroup importUserGroup(Long eventId, Long userId, Long importedUserGroupId) {
		checkNotNull(eventId);
		checkNotNull(userId);
		checkNotNull(importedUserGroupId);
		Event event = commonDao.findEntityById(Event.class, eventId);
		checkState(event != null, String.format("No \"Event\" entity belongs to \"eventId\"=%d in database.", eventId));			
		User user = commonDao.findEntityById(User.class, userId);
		checkState(user != null, String.format("No \"User\" entity belongs to \"userId\"=%d in database.", userId));			
		UserGroup importedUserGroup = commonDao.findEntityById(UserGroup.class, importedUserGroupId);
		checkState(importedUserGroup != null, String.format("No \"UserGroup\" entity belongs to \"importedUserGroupId\"=%d in database.", importedUserGroupId));			
		
		UserGroup userGroup = new UserGroup();
		userGroup.setName(importedUserGroup.getName());
		userGroup.setEvent(event);
		userGroup.setPublicEditableAsBoolean(false);
		userGroup.setPublicVisibleAsBoolean(true);
		userGroup.setOwner(user);
		userGroup.setUsers(Sets.newHashSet(user));
		userGroup.getUsers().addAll(importedUserGroup.getUsers());
		if (!userGroup.getUsers().contains(user)) {
			userGroup.getUsers().add(user);
		}
		userGroup.getUsers().stream().forEach(u -> { u.getUserGroups().add(userGroup); });
		
		commonDao.persistEntity(userGroup);
		
		return userGroup;
	}

}
