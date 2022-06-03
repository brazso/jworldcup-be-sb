package com.zematix.jworldcup.backend.service;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.inject.Inject;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.base.Strings;
import com.google.common.collect.Range;
import com.zematix.jworldcup.backend.dao.CommonDao;
import com.zematix.jworldcup.backend.dao.UserDao;
import com.zematix.jworldcup.backend.emun.ParameterizedMessageType;
import com.zematix.jworldcup.backend.entity.Event;
import com.zematix.jworldcup.backend.entity.Team;
import com.zematix.jworldcup.backend.entity.User;
import com.zematix.jworldcup.backend.entity.UserOfEvent;
import com.zematix.jworldcup.backend.exception.ServiceException;
import com.zematix.jworldcup.backend.model.ParameterizedMessage;
import com.zematix.jworldcup.backend.util.CommonUtil;

/**
 * Operations around {@link User} elements. 
 * Usually it injects at least its DAO class belongs to the same entities. 
 * It may also inject other service and DAO classes.
 */
@Service
@Transactional
public class UserService extends ServiceBase {

	@Inject
	private ApplicationService applicationService;

	@Inject 
	private UserDao userDao;
	
	@Inject
	private UserGroupService userGroupService;
	
	@Inject
	private CommonDao commonDao;

	@Inject
	private EmailService emailService;
	
	@Inject
	private PasswordEncoder passwordEncoder;
	
	@Value("${app.expiredDays.user.candidate:0}")
	private String appExpiredDaysUserCandidate;
		
	@Value("${app.expiredDays.user.emailModification:0}")
	private String appExpiredDaysUserEmailModification;

	@Value("${app.expiredDays.user.passwordReset:0}")
	private String appExpiredDaysUserPasswordReset;

	/**
	 * Returns an {@link User} instance if the user given by the login parameters can be 
	 * authenticated. Otherwise it throws ServiceException.
	 *  
	 * @param loginName - login name of the user to be authenticated
	 * @param loginPassword - login password of the user to be authenticated
	 * @return authenticated {@link User} instance
	 * @throws ServiceException if the user cannot be authenticated based on the given login parameters 
	 */
	@Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
	public User login(String loginName, String loginPassword) throws ServiceException {

		List<ParameterizedMessage> errMsgs = new ArrayList<>();

		if (Strings.isNullOrEmpty(loginName) || Strings.isNullOrEmpty(loginPassword)) {
			errMsgs.add(ParameterizedMessage.create("MISSING_USER_OR_PASSWORD"));
			throw new ServiceException(errMsgs);
		}

		User user = userDao.findUserByLoginName(loginName);
		if (user == null) {
			errMsgs.add(ParameterizedMessage.create("USER_DISALLOWED_TO_LOGIN", loginName));
			throw new ServiceException(errMsgs);
		}
		
		if (passwordEncoder.upgradeEncoding(user.getLoginPassword())) {
			errMsgs.add(ParameterizedMessage.create("USER_DEPRECATED_PASSWORD_TO_LOGIN")); // due to shift SHA256 to SALT128+SHA384 
		}
		else if (!passwordEncoder.matches(loginPassword, user.getLoginPassword())) {
			errMsgs.add(ParameterizedMessage.create("USER_DISALLOWED_TO_LOGIN", loginName));
		}
		else if (user.getUserStatus().getStatus().equals("CANDIDATE")) {
			errMsgs.add(ParameterizedMessage.create("USER_CANDIDATE_DISALLOWED_TO_LOGIN"));
		}
		else if (user.getUserStatus().getStatus().equals("LOCKED")) {
			errMsgs.add(ParameterizedMessage.create("USER_LOCKED_DISALLOWED_TO_LOGIN"));
		}
		else if (!user.getUserStatus().getStatus().equals("NORMAL")) {
			logger.warn(String.format("User with unknown status '%s' gained login. Is it really allowed?"), user.getUserStatus().getName());
		}

		if (!errMsgs.isEmpty()) {
			throw new ServiceException(errMsgs);
		}
		
		return user;
	}
	
	/**
	 * Signs up a new user with "USER" role, store her personal data into database. Returns 
	 * the created {@link User} instance if the sign up is successful. Otherwise it throws 
	 * ServiceException.
	 * 
	 * @param loginName
	 * @param loginPassword1
	 * @param loginPassword2 - it must be the same as {@code loginPassword1}, just for confirmation
	 * @param fullName
	 * @param emailAddr - email address
	 * @param locale - email is written in the language of this
	 * @return stored new {@link User} entity instance
	 * @throws ServiceException if the user cannot be signed up, its login data is not valid 
	 *         or the user already exists in the database
	 */
	public User signUp(String loginName, String loginPassword1, String loginPassword2, 
			String fullName, String emailAddr, String zoneId, Locale locale) throws ServiceException {

		List<ParameterizedMessage> errMsgs = new ArrayList<>();

		if (Strings.isNullOrEmpty(loginName) || Strings.isNullOrEmpty(loginPassword1)
				|| Strings.isNullOrEmpty(loginPassword2) || Strings.isNullOrEmpty(fullName)
				|| Strings.isNullOrEmpty(emailAddr)) {
			errMsgs.add(ParameterizedMessage.create("MISSING_USER_PASSWORD_FULL_NAME_OR_EMAIL_ADDRESS"));
		}

		if (locale == null) {
			locale = Locale.getDefault();
			logger.error("Missing locale input, default %s locale is used.", locale.toLanguageTag());
		}
		
		if (!errMsgs.isEmpty()) {
			throw new ServiceException(errMsgs);
		}
		
		if (!loginPassword1.equals(loginPassword2)) {
			errMsgs.add(ParameterizedMessage.create("GIVEN_PASSWORDS_MISMATCH"));
		}

		if (!CommonUtil.isEmailValid(emailAddr)) {
			errMsgs.add(ParameterizedMessage.create("GIVEN_EMAIL_ADDRESS_INVALID", emailAddr));
		}
		
		// validate zoneId
		if (Strings.isNullOrEmpty(zoneId)) {
			errMsgs.add(ParameterizedMessage.create("MISSING_USER_ZONE_ID"));
		}
		ZoneId zone = ZoneId.of(zoneId);
		checkArgument(zone != null, String.format("Argument \"zoneId\" contains illegal \"%s\" value.", zoneId));

		if (!errMsgs.isEmpty()) {
			throw new ServiceException(errMsgs);
		}
		
		User user = userDao.findUserByLoginNameOrEmailAddress(loginName, emailAddr);
		if (user != null && !user.getUserStatus().getStatus().equals("CANDIDATE")) {
			errMsgs.add(ParameterizedMessage.create("USER_EXIST_OR_EMAIL_ADDRESS_OCCUPIED"));
		}
		else {
			String token = CommonUtil.generateRandomToken();
			try {
				String encryptedLoginPassword = passwordEncoder.encode(loginPassword1);
				if (user != null) { // CANDIDATE
					this.deleteUser(user.getLoginName());
				}
				user = userDao.saveUser(loginName, encryptedLoginPassword,
						fullName, emailAddr, /*sRole*/ "USER", /*sStatus*/ "CANDIDATE", token, 
						zoneId, applicationService.getActualDateTime());
			}
			catch (Exception e) {
				errMsgs.add(ParameterizedMessage.create("DB_SAVE_FAILED"));
				logger.error("Database operation named {} failed.", "userDao.saveUser", e);
			}
		}
		
		if (!errMsgs.isEmpty()) {
			throw new ServiceException(errMsgs);
		}

		try {
			emailService.sendRegistrationMail(user, locale);
		}
		catch (ServiceException e) {
			errMsgs.addAll(e.getMessages());
		}

		if (!errMsgs.isEmpty()) {
			throw new ServiceException(errMsgs);
		}

		return user;
	}

	/**
	 * Modifies registration data of an existing user.
	 * 
	 * @param loginName - user login name (mandatory)
	 * @param loginPasswordActual - old login password
	 * @param loginPasswordNew - new login password
	 * @param loginPasswordAgain - new login password for confirmation
	 * @param fullName - user fill name (mandatory)
	 * @param emailNew - email address
	 * @param emailNewAgain - email address for confirmation
	 * @param zoneId - time zone (mandatory)
	 * @param locale - email is written in the language of this
	 * @return {@link User} entity instance belongs to the modified user 
	 * @throws ServiceException if the user cannot be identified or modified
	 * @throws IllegalArgumentException
	 */
	public User modifyUser(String loginName, String loginPasswordActual, 
			String loginPasswordNew, String loginPasswordAgain, 
			String fullName, String emailNew, String emailNewAgain,
			String zoneId, Locale locale) throws ServiceException {

		List<ParameterizedMessage> errMsgs = new ArrayList<>();

		// validate loginName
		checkArgument(!Strings.isNullOrEmpty(loginName), "Argument \"loginName\" cannot be null nor empty.");

		User user = userDao.findUserByLoginName(loginName);
		if (user == null) {
			errMsgs.add(ParameterizedMessage.create("GIVEN_USER_NOT_EXIST", loginName));
			throw new ServiceException(errMsgs);
		}
		
		// validate mandatory field(s)
		if (Strings.isNullOrEmpty(fullName)) {
			errMsgs.add(ParameterizedMessage.create("MISSING_USER_FULL_NAME"));
		}

		// validate passwords: 0 or 3 passwords must be given
		if (Range.closed(1, 2).contains((Strings.isNullOrEmpty(loginPasswordActual) ? 0 : 1) +
				(Strings.isNullOrEmpty(loginPasswordNew) ? 0 : 1) + 
				(Strings.isNullOrEmpty(loginPasswordAgain) ? 0 : 1))) {
			errMsgs.add(ParameterizedMessage.create("MISSING_PASSWORDS"));
		}
		if ((!Strings.isNullOrEmpty(loginPasswordNew) && !Strings.isNullOrEmpty(loginPasswordAgain)) 
				&& !loginPasswordNew.equals(loginPasswordAgain)) {
			errMsgs.add(ParameterizedMessage.create("GIVEN_NEW_PASSWORDS_MISMATCH"));
		}

		if (!Strings.isNullOrEmpty(loginPasswordActual)) {
			user = userDao.findUserByLoginName(loginName);
			if (user == null || !passwordEncoder.matches(loginPasswordActual, user.getLoginPassword())) {
				errMsgs.add(ParameterizedMessage.create("OLD_PASSWORD_INVALID"));
				throw new ServiceException(errMsgs);
			}
		}

		String encryptedNewLoginPassword = null; // not modified
		if (!Strings.isNullOrEmpty(loginPasswordNew) && !Strings.isNullOrEmpty(loginPasswordAgain)
				&& loginPasswordNew.equals(loginPasswordAgain)) {
			encryptedNewLoginPassword = passwordEncoder.encode(loginPasswordNew);	
		}

		// validate email
		String newEmailAddr = null; // not modified
		if (Strings.isNullOrEmpty(emailNew) && !Strings.isNullOrEmpty(emailNewAgain)) {
			errMsgs.add(ParameterizedMessage.create("MISSING_EMAILS"));
		}
		if (!Strings.isNullOrEmpty(emailNew) && Strings.isNullOrEmpty(emailNewAgain)) {
			errMsgs.add(ParameterizedMessage.create("MISSING_EMAIL_AGAIN"));
		}
		if (!Strings.isNullOrEmpty(emailNew) && !CommonUtil.isEmailValid(emailNew)) {
			errMsgs.add(ParameterizedMessage.create("GIVEN_EMAIL_ADDRESS_INVALID", emailNew));
		}
		if (!Strings.isNullOrEmpty(emailNew) && !Strings.isNullOrEmpty(emailNewAgain)) {
			if (!emailNew.equalsIgnoreCase(emailNewAgain)) {
				errMsgs.add(ParameterizedMessage.create("GIVEN_NEW_EMAILS_MISMATCH"));
			}
			else if (!emailNew.equalsIgnoreCase(user.getEmailAddr())) {
				if (userDao.existUserByEmailAddrExceptUser(loginName, emailNew)) {
					errMsgs.add(ParameterizedMessage.create("GIVEN_EMAIL_ADDRESS_OCCUPIED", emailNew));
				}
				else {
					newEmailAddr = emailNew; // can be modified				
				}
			}
		}
		
		// validate zoneId
		if (Strings.isNullOrEmpty(zoneId)) {
			errMsgs.add(ParameterizedMessage.create("MISSING_USER_ZONE_ID"));
		}
		ZoneId zone = ZoneId.of(zoneId);
		checkArgument(zone != null, String.format("Argument \"zoneId\" contains illegal \"%s\" value.", zoneId));
		
		if (!errMsgs.isEmpty()) {
			throw new ServiceException(errMsgs);
		}

		try {
			user = userDao.modifyUser(user, fullName, newEmailAddr, encryptedNewLoginPassword, zoneId, applicationService.getActualDateTime());
		}
		catch (Exception e) {
			errMsgs.add(ParameterizedMessage.create("DB_SAVE_FAILED"));
			logger.error("Database operation named {} failed.", "userDao.modifyUser", e);
		}

		if (!errMsgs.isEmpty()) {
			throw new ServiceException(errMsgs);
		}
		
		if (newEmailAddr != null) {
			try {
				emailService.sendEmailChangedMail(user, locale);
			}
			catch (ServiceException e) {
				errMsgs.addAll(e.getMessages());
			}

			if (!errMsgs.isEmpty()) {
				throw new ServiceException(errMsgs);
			}
		}

		return user;
	}
	
	/**
	 * Processes user registration token. If the input registrationToken specifies 
	 * an user and this user has CANDIDATE userStatus, then alter her userStatus
	 * to NORMAL.
	 *  
	 * @param registrationToken - token for first login after registration
	 * @throws ServiceException contains only info if process is done
	 */
	public void processRegistrationToken(String registrationToken) throws ServiceException {
		List<ParameterizedMessage> errMsgs = new ArrayList<>();
		
		checkArgument(!Strings.isNullOrEmpty(registrationToken), "Argument \"registrationToken\" cannot be null nor empty.");
		
		User user = userDao.findUserByToken(registrationToken);
		if (user == null) {
			errMsgs.add(ParameterizedMessage.create("REGISTRATION_TOKEN_UNKNOWN"));
			throw new ServiceException(errMsgs);
		}
		
		if (user.getUserStatus().getStatus().equals("LOCKED")) {
			errMsgs.add(ParameterizedMessage.create("REGISTRATION_TOKEN_LOCKED"));
			throw new ServiceException(errMsgs);
		}
		else if (user.getUserStatus().getStatus().equals("CANDIDATE")) {
			// first login after registration
			userDao.modifyUserStatusToken(user, "NORMAL", applicationService.getActualDateTime());

			errMsgs.add(ParameterizedMessage.create("REGISTRATION_TOKEN_ACKNOWLEDGED", ParameterizedMessageType.INFO));
			throw new ServiceException(errMsgs);
		}
	}
	
	/**
	 * Processes token from a user initiated email change request. If the input 
	 * userToken specifies an user and this user has NORMAL userStatus, 
	 * then this function changes her email address.
	 *  
	 * @param userToken - token linked to the user
	 * @throws ServiceException if userToken specifies no user or userStatus is not NORMAL
	 * 		
	 */
	public void processChangeEmailToken(String userToken) throws ServiceException {
		List<ParameterizedMessage> errMsgs = new ArrayList<>();
		
		checkArgument(!Strings.isNullOrEmpty(userToken), "Argument \"userToken\" cannot be null nor empty.");
		
		User user = userDao.findUserByToken(userToken);
		if (user == null) {
			errMsgs.add(ParameterizedMessage.create("USER_TOKEN_UNKNOWN"));
			throw new ServiceException(errMsgs);
		}
		
		if (!user.getUserStatus().getStatus().equals("NORMAL")) {
			errMsgs.add(ParameterizedMessage.create("USER_TOKEN_NOT_NORMAL"));
			throw new ServiceException(errMsgs);
		}
		
		boolean isEmailModified = userDao.modifyUserEmailAddr(user, applicationService.getActualDateTime());
		if (isEmailModified) {
			errMsgs.add(ParameterizedMessage.create("CHANGE_EMAIL_ACKNOWLEDGED", ParameterizedMessageType.INFO));
			throw new ServiceException(errMsgs);
		}
	}
	
	/**
	 * Retrieves {@link UserOfEvent} instance by its given eventId and userId or {@code null}
	 * unless found. Returned entity is detached from PU.
	 * 
	 * @param eventId
	 * @param userId
	 * @return found {@link UserOfEvent} detached object or {@code null}
	 */
	@Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
	public UserOfEvent retrieveUserOfEvent(Long eventId, Long userId) throws ServiceException {
		checkNotNull(eventId);
		checkNotNull(userId);

		UserOfEvent userOfEvent = userDao.retrieveUserOfEvent(eventId, userId);
		if (userOfEvent == null) {
			return null;
		}
		
		// load lazy associations
		userOfEvent.getEvent().getEventId();
		userOfEvent.getUser().getUserId();
		if (userOfEvent.getFavouriteGroupTeam() != null) {
			userOfEvent.getFavouriteGroupTeam().getTeamId();
			//logger.info("userOfEvent.getFavouriteGroupTeam().getTeamId()=" + userOfEvent.getFavouriteGroupTeam().getTeamId());
		}
		if (userOfEvent.getFavouriteKnockoutTeam() != null) {
			userOfEvent.getFavouriteKnockoutTeam().getTeamId();
			//logger.info("userOfEvent.getFavouriteKnockoutTeam().getTeamId()=" + userOfEvent.getFavouriteKnockoutTeam().getTeamId());
		}
		
		commonDao.detachEntity(userOfEvent);
		return userOfEvent;
	}

	/**
	 * Saves given favourite teams of {@link UserOfEvent} instance by its given 
	 * userId and eventId. It creates a new database row or it just modifies that.
	 * 
	 * @param eventId
	 * @param userId
	 * @param favouriteGroupTeamId - favourite group team id
	 * @param favouriteKnockoutTeamId - favourite knockout team id 
	 * @return saved userOfEvent
	 */
	public UserOfEvent saveUserOfEvent(Long eventId, Long userId, Long favouriteGroupTeamId, Long favouriteKnockoutTeamId) throws ServiceException {
		checkNotNull(userId);
		checkNotNull(eventId);
		
		UserOfEvent userOfEvent = userDao.retrieveUserOfEvent(eventId, userId);
		if (userOfEvent == null) {
			userOfEvent = new UserOfEvent();
			Event event = commonDao.findEntityById(Event.class, eventId);
			if (event == null) {
				throw new IllegalStateException(String.format("No \"Event\" entity belongs to \"eventId\"=%d in database.", eventId));
			}
			userOfEvent.setEvent(event);
			User user = commonDao.findEntityById(User.class, userId);
			if (user == null) {
				throw new IllegalStateException(String.format("No \"User\" entity belongs to \"userId\"=%d in database.", userId));
			}
			userOfEvent.setUser(user);
		}
		
		Team favouriteGroupTeam = null;
		if (favouriteGroupTeamId != null) {
			favouriteGroupTeam = commonDao.findEntityById(Team.class, favouriteGroupTeamId);
			if (favouriteGroupTeam == null) {
				throw new IllegalStateException(String.format("No \"Team\" entity belongs to \"favouriteGroupTeamId\"=%d, cannot be found in database.", favouriteGroupTeamId));
			}
		}
		Team favouriteKnockoutTeam = null;
		if (favouriteKnockoutTeamId != null) {
			favouriteKnockoutTeam = commonDao.findEntityById(Team.class, favouriteKnockoutTeamId);
			if (favouriteKnockoutTeam == null) {
				throw new IllegalStateException(String.format("No \"Team\" entity belongs to \"favouriteKnockoutTeamId\"=%d, cannot be found in database.", favouriteKnockoutTeamId));
			}
		}
		
		userOfEvent.setFavouriteGroupTeam(favouriteGroupTeam);
		userOfEvent.setFavouriteKnockoutTeam(favouriteKnockoutTeam);
		
		if (userOfEvent.getUserOfEventId() == null) {
			commonDao.persistEntity(userOfEvent);
		}
		
		commonDao.flushEntityManager();

		return userOfEvent;
	}

	/**
	 * Returns found {@link User} instance belongs to the given {@code loginName}.
	 * It fetches some necessary lazy data as well.
	 * Otherwise {@code null} is returned.
	 * 
	 * @param loginName
	 * @return user with the given {@code loginName}
	 * @throws NullPointerException if given {@code loginName} is {@code null}
	 */
	@Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
	public User findUserByLoginName(String loginName) {
		checkNotNull(loginName);
		
		User user = userDao.findUserByLoginName(loginName);
		if (user != null) {
			user.getRoles().size(); // forced lazy fetch
		}
		
		return user;
	}

	/**
	 * Returns a list of strings containing user loginName values matched by the given 
	 * loginNamePrefix.
	 * 
	 * @param loginNamePrefix
	 * @return list of strings containing user loginName values matched by the given 
	 *         loginNamePrefix
	 * @throws IllegalArgumentException if any of the given parameters is invalid
	 */
	@Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
	public List<String> findUserLoginNamesByLoginNamePrefix(String loginNamePrefix) {
		checkArgument(!Strings.isNullOrEmpty(loginNamePrefix), "Argument \"loginNamePrefix\" cannot be null nor empty.");
		
		List<User> users = userDao.findUsersByLoginNamePrefix(loginNamePrefix);
		List<String> userLoginNames = users.stream().map(u->u.getLoginName()).toList();
		return userLoginNames;
	}
	
	/**
	 * Returns a list of strings containing user fullName values matched by the given 
	 * fullNameContain.
	 * 
	 * @param fullNameContain
	 * @return Returns a list of strings containing user fullName values matched by the given 
	 *         fullNameContain.
	 */
	@Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
	public List<String> findUserFullNamesByFullNameContain(String fullNameContain) {
		checkArgument(!Strings.isNullOrEmpty(fullNameContain), "Argument \"fullNameContain\" cannot be null nor empty.");
		
		List<User> users = userDao.findUsersByFullNameContain(fullNameContain);
		List<String> userFullNames = users.stream().map(u->u.getFullName()).toList();
		return userFullNames;
	}

	/**
	 * Returns detached {@link User} instance with the provided {@link User#userId}.
	 * 
	 * @param userId
	 * @return found user
	 */
	@Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
	public User retrieveUser(Long userId) throws ServiceException {
		checkNotNull(userId);
		
		List<ParameterizedMessage> errMsgs = new ArrayList<>();
		
		User user = commonDao.findEntityById(User.class, userId);
		if (user == null) {
			errMsgs.add(ParameterizedMessage.create("MISSING_USER"));
			throw new ServiceException(errMsgs);
		}

		commonDao.detachEntity(user);
		
		return user;
	}
	
	/**
	 * Returns all possible time zone key/value pairs in a map, for example
	 * [<"Europe/Berlin", "+02:00">, <"Africa/Algiers", "+01:00">, ...].
	 * The result map is sorted by its key.
	 * 
	 * @return a map containing all supported time zone key/value pairs
	 */
	@Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
	public Map<String, String> getAllSupportedTimeZoneIds() {
		List<String> zoneList = new ArrayList<>(ZoneId.getAvailableZoneIds());

		Map<String, String> map = new HashMap<>();
		LocalDateTime dt = LocalDateTime.now();

		for (String zoneId : zoneList) {
			ZoneId zone = ZoneId.of(zoneId);
			ZonedDateTime zdt = dt.atZone(zone);
			ZoneOffset zos = zdt.getOffset();

			//replace Z to +00:00
			String offset = zos.getId().replaceAll("Z", "+00:00");

			map.put(zone.toString(), offset);
		}
		
		LinkedHashMap<String, String> sortedMap = new LinkedHashMap<>();
		
		//sort map by key
		map.entrySet().stream()
				.sorted(Map.Entry.comparingByKey())
				.forEachOrdered(e -> sortedMap.put(e.getKey(), e.getValue()));

		return sortedMap;
	}

	/**
	 * Resets user password by the given email address. 
	 * After verification of the email address, if it belongs to
	 * an existing user, an email is being sent there containing 
	 * an url with a new temporary password and a token linked 
	 * to the user. 
	 * 
	 * @param emailAddr - belongs to the user with forgotten password or username
	 * @throws ServiceException if the given email address belongs to no user 
	 */
	public void resetPassword(String emailAddr, Locale locale) throws ServiceException {
		List<ParameterizedMessage> errMsgs = new ArrayList<>();

		if (Strings.isNullOrEmpty(emailAddr)) {
			errMsgs.add(ParameterizedMessage.create("MISSING_RESET_PASSWORD_EMAIL"));
			throw new ServiceException(errMsgs);
		}

		User user = userDao.findUserByEmailAddress(emailAddr);
		if (user == null) {
			errMsgs.add(ParameterizedMessage.create("GIVEN_EMAIL_ADDRESS_NOT_EXIST"));
			throw new ServiceException(errMsgs);
		}
		if (!user.getUserStatus().getStatus().equals("NORMAL")) {
			errMsgs.add(ParameterizedMessage.create("USER_STATUS_INADEQUATE"));
			throw new ServiceException(errMsgs);
		}
		
		String newPassword = CommonUtil.generateRandomPassword();
		String resetPassword = passwordEncoder.encode(newPassword);

		boolean isResetPasswordModified = userDao.modifyUserResetPassword(user, resetPassword, applicationService.getActualDateTime());
		if (!isResetPasswordModified) {
			errMsgs.add(ParameterizedMessage.create("DB_SAVE_FAILED"));
			throw new ServiceException(errMsgs);
		}
		
		user = commonDao.mergeEntity(user); // refresh resetPassword in entity
		
		try {
			emailService.sendResetPasswordMail(user, newPassword, locale);
		}
		catch (ServiceException e) {
			errMsgs.addAll(e.getMessages());
		}

		if (!errMsgs.isEmpty()) {
			throw new ServiceException(errMsgs);
		}
	}
	
	/**
	 * Processes token from a user initiated password reset request. If the input 
	 * userToken specifies an user, this user has NORMAL userStatus and there is
	 * a reset password of the user then this function updates the reset password 
	 * for the user.
	 *  
	 * @param userToken - token linked to the user
	 * @throws ServiceException if any of the preconditions is not met
	 */
	public void processResetPasswordToken(String userToken) throws ServiceException {
		List<ParameterizedMessage> errMsgs = new ArrayList<>();
		
		checkArgument(!Strings.isNullOrEmpty(userToken), "Argument \"userToken\" cannot be null nor empty.");
		
		User user = userDao.findUserByToken(userToken);
		if (user == null) {
			errMsgs.add(ParameterizedMessage.create("USER_TOKEN_UNKNOWN"));
			throw new ServiceException(errMsgs);
		}
		
		if (!user.getUserStatus().getStatus().equals("NORMAL")) {
			errMsgs.add(ParameterizedMessage.create("USER_STATUS_INADEQUATE"));
			throw new ServiceException(errMsgs);
		}

		boolean isPasswordModified = userDao.finalizeUserResetPassword(user, applicationService.getActualDateTime());
		if (isPasswordModified) {
			errMsgs.add(ParameterizedMessage.create("RESET_PASSWORD_ACKNOWLEDGED", ParameterizedMessageType.INFO));
			throw new ServiceException(errMsgs);
		}
	}

	/**
	 * Deletes a user given by loginName parameter.
	 * 
	 * @param loginName
	 */
	public void deleteUser(String loginName) throws ServiceException {
		List<ParameterizedMessage> errMsgs = new ArrayList<>();
		
		// validate loginName
		checkArgument(!Strings.isNullOrEmpty(loginName), "Argument \"loginName\" cannot be null nor empty.");

		User user = userDao.findUserByLoginName(loginName);
		if (user == null) {
			errMsgs.add(ParameterizedMessage.create("GIVEN_USER_NOT_EXIST", loginName));
			throw new ServiceException(errMsgs);
		}
		
		userDao.deleteUser(user);
	}
	
	/**
	 * Deletes users with candidate status if their registrations are expired.
	 */
	public int deleteExpiredCandidateUsers() throws ServiceException {
		LocalDateTime actualDateTime = applicationService.getActualDateTime();
		int expiredDays = Integer.parseInt(appExpiredDaysUserCandidate);
		if (expiredDays == 0) {
			return 0;
		}
		LocalDateTime expiredModificationTime = CommonUtil.plusDays(actualDateTime, -expiredDays);
		List<User> users = userDao.findExpiredCandidateUsers(expiredModificationTime);
		users.stream().forEach(user -> userDao.deleteUser(user));
		commonDao.flushEntityManager();
		return users.size();
	}
	
	/**
	 * Deletes email modification attempts if they are expired.
	 */
	public int deleteExpiredEmailModifications() throws ServiceException {
		LocalDateTime actualDateTime = applicationService.getActualDateTime();
		int expiredDays = Integer.parseInt(appExpiredDaysUserEmailModification);
		if (expiredDays == 0) {
			return 0;
		}
		LocalDateTime expiredModificationTime = CommonUtil.plusDays(actualDateTime, -expiredDays);
		List<User> users = userDao.findExpiredEmailModificationUsers(expiredModificationTime);
		users.stream().forEach(user -> {
			user.setEmailNew(null);
			user.setModificationTime(actualDateTime);
		});
		commonDao.flushEntityManager();
		return users.size();
	}

	/**
	 * Deletes password reset attempts if they are expired.
	 */
	public int deleteExpiredPasswordResets() throws ServiceException {
		LocalDateTime actualDateTime = applicationService.getActualDateTime();
		int expiredDays = Integer.parseInt(appExpiredDaysUserPasswordReset);
		if (expiredDays == 0) {
			return 0;
		}
		LocalDateTime expiredModificationTime = CommonUtil.plusDays(actualDateTime, -expiredDays);
		List<User> users = userDao.findExpiredPasswordResetUsers(expiredModificationTime);
		for (User user : users) {
			user.setResetPassword(null);
			user.setModificationTime(actualDateTime);
		}
		commonDao.flushEntityManager();
		return users.size();
	}

	/**
	 * Retrieves a list of all authenticated users
	 * @return all authenticated users
	 */
	public List<String> getAllAuthenticatedUserLoginNames() {
		List<org.springframework.security.core.userdetails.User> principals = applicationService
				.getAllAuthenticatedPrincipals();
		return principals.stream().map(e -> e.getUsername()).sorted().toList();
	}

	/**
	 * Retrieves a list of all authenticated users
	 * @return all authenticated users
	 */
	public List<User> getAllAuthenticatedUsers() {
		List<String> userLoginNames = getAllAuthenticatedUserLoginNames();
		return userLoginNames.stream().map(e -> findUserByLoginName(e)).toList();
	}

	/**
	 * Retrieves a list of all authenticated users of the given user group.
	 * @return all authenticated users of the given user group
	 * @throws ServiceException 
	 */
	public List<User> getAllAuthenticatedUsersByUserGroup(Long userGroupId) throws ServiceException {
		checkNotNull(userGroupId);
		
		List<String> authenticatedUserLoginNames = getAllAuthenticatedUserLoginNames();
		return userGroupService.retrieveUsersByUserGroup(userGroupId).stream()
				.filter(e -> authenticatedUserLoginNames.contains(e.getLoginName())).toList();
	}

}

