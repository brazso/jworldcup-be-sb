package com.zematix.jworldcup.backend.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.persistence.PersistenceException;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.collect.Ordering;
import com.zematix.jworldcup.backend.dao.CommonDao;
import com.zematix.jworldcup.backend.dao.UserDao;
import com.zematix.jworldcup.backend.entity.Event;
import com.zematix.jworldcup.backend.entity.User;
import com.zematix.jworldcup.backend.entity.UserOfEvent;
import com.zematix.jworldcup.backend.entity.UserStatus;
import com.zematix.jworldcup.backend.util.CommonUtil;

/**
 * Contains test functions of {@link UserService} class.
 */

@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles({"development", "test"})
@Transactional
public class UserServiceTest {

	@Inject
	private UserService userService;
	
	@MockBean
	private UserDao userDao; // used by some methods inside UserService

	@Inject 
	private CommonDao commonDao;

	@MockBean
	private EmailService emailService; // used by some methods inside UserService

//	@MockBean
//	private CommonUtil commonUtil;
	
	@MockBean
	private ApplicationService applicationService;

	@MockBean
	private BetService betService;

	/**
	 * Test {@link UserService#login()} method.
	 * Scenario: due to dummy login data the login method fails and throws {@link ServiceException}
	 * @throws  
	 */
	@Test(expected=ServiceException.class)
	public void loginWrongLoginData() throws ServiceException {
		String loginName = "admin";
		String loginPassword = "blablabla";

		try {
			userService.login(loginName, loginPassword);
		}
		catch (ServiceException e) {
			assertTrue("There must be a single message in ServiceException named USER_DISALLOWED_TO_LOGIN", 
					e.getMessages().size()==1 && e.getMessages().get(0).getMsgCode().equals("USER_DISALLOWED_TO_LOGIN"));
			throw e;
		}
	}

	/**
	 * Test {@link UserService#login()} method.
	 * Scenario: due to CANDIDATE status of the user the login fails and throws {@link ServiceException}
	 */
	@Test(expected=ServiceException.class)
	public void loginWrongStatusCandidate() throws ServiceException {
		String loginName = "admin";
		String loginPassword = "admin_!";
		String encryptedLoginPassword = "87326fa89d072eb3f575e62350184dc70131fae2645728b278e2c683799a951327fa3bcb06ec7d045c7543edf1c79ab1dc1ec3bacf054e2c1eae39148b9a5c29";

		User user = new User();
		user.setLoginName(loginName);
		user.setLoginPassword(encryptedLoginPassword);
		UserStatus userStatus = new UserStatus();
		userStatus.setStatus("CANDIDATE");
		user.setUserStatus(userStatus);
		Mockito.when(userDao.findUserByLoginName(loginName)).thenReturn(user);

		try {
			userService.login(loginName, loginPassword);
		}
		catch (ServiceException e) {
			assertTrue("There must be a single message in ServiceException named USER_CANDIDATE_DISALLOWED_TO_LOGIN", 
					e.getMessages().size()==1 && e.getMessages().get(0).getMsgCode().equals("USER_CANDIDATE_DISALLOWED_TO_LOGIN"));
			throw e;
		}
	}

	/**
	 * Test {@link UserService#login()} method.
	 * Scenario: due to LOCKED status of the user the login fails and throws {@link ServiceException}
	 */
	@Test(expected=ServiceException.class)
	public void loginWrongStatusLocked() throws ServiceException {
		String loginName = "admin";
		String loginPassword = "admin_!";
		String encryptedLoginPassword = "87326fa89d072eb3f575e62350184dc70131fae2645728b278e2c683799a951327fa3bcb06ec7d045c7543edf1c79ab1dc1ec3bacf054e2c1eae39148b9a5c29";

		User user = new User();
		user.setLoginName(loginName);
		user.setLoginPassword(encryptedLoginPassword);
		UserStatus userStatus = new UserStatus();
		userStatus.setStatus("LOCKED");
		user.setUserStatus(userStatus);
		Mockito.when(userDao.findUserByLoginName(loginName)).thenReturn(user);

		try {
			userService.login(loginName, loginPassword);
		}
		catch (ServiceException e) {
			assertTrue("There must be a single message in ServiceException named USER_LOCKED_DISALLOWED_TO_LOGIN", 
					e.getMessages().size()==1 && e.getMessages().get(0).getMsgCode().equals("USER_LOCKED_DISALLOWED_TO_LOGIN"));
			throw e;
		}
	}

	/**
	 * Test {@link UserService#login()} method.
	 * Scenario: successful login
	 */
	@Test
	public void loginSuccessful() throws ServiceException {
		String loginName = "admin";
		String loginPassword = "admin_!";
		String encryptedLoginPassword = "87326fa89d072eb3f575e62350184dc70131fae2645728b278e2c683799a951327fa3bcb06ec7d045c7543edf1c79ab1dc1ec3bacf054e2c1eae39148b9a5c29";
		
		User user = new User();
		user.setLoginName(loginName);
		user.setLoginPassword(encryptedLoginPassword);
		UserStatus userStatus = new UserStatus();
		userStatus.setStatus("NORMAL");
		user.setUserStatus(userStatus);
		Mockito.when(userDao.findUserByLoginName(loginName)).thenReturn(user);

		User user2 = userService.login(loginName, loginPassword);
		assertEquals("Returned user must be the same as the given one", user, user2);
	}
	
	/**
	 * Test {@link UserService#login()} method.
	 * Scenario: due to LOCKED status of the user the login fails and throws {@link ServiceException}
	 */
	@Test(expected=ServiceException.class)
	public void login_deprecatedPassword() throws ServiceException {
		String loginName = "admin";
		String loginPassword = "admin_!";
		String encryptedLoginPassword = "87326fa89d072eb3f575e62350184dc70131fae2645728b278e2c683799a9513";

		User user = new User();
		user.setLoginName(loginName);
		user.setLoginPassword(encryptedLoginPassword);
		UserStatus userStatus = new UserStatus();
		userStatus.setStatus("NORMAL");
		user.setUserStatus(userStatus);
		Mockito.when(userDao.findUserByLoginName(loginName)).thenReturn(user);

		try {
			userService.login(loginName, loginPassword);
		}
		catch (ServiceException e) {
			assertTrue("There must be a single message in ServiceException named USER_DEPRECATED_PASSWORD_TO_LOGIN", 
					e.getMessages().size()==1 && e.getMessages().get(0).getMsgCode().equals("USER_DEPRECATED_PASSWORD_TO_LOGIN"));
			throw e;
		}
	}

	/**
	 * Test {@link UserService#signUp()} method.
	 * Scenario: unsuccessful sign up due to wrong email address
	 */
	@Test(expected=ServiceException.class)
	public void signUpWrongEmailAddr() throws ServiceException {
		String loginName = "brazso";
		String loginPassword1 = "yell0wsubmarine";
		String loginPassword2 = "yell0wsubmarine";
		String fullName = "Zsolt Branyiczky";
		String emailAddr = "zbranyiczky@@dummy987.com";
		String zoneId = "CET";
		Locale locale = new Locale("en");
		
		try {
			userService.signUp(loginName, loginPassword1, loginPassword2, fullName, emailAddr, zoneId, locale);
		}
		catch (ServiceException e) {
			assertTrue("There must be a single message in ServiceException named GIVEN_EMAIL_ADDRESS_INVALID", 
					e.getMessages().size()==1 && e.getMessages().get(0).getMsgCode().equals("GIVEN_EMAIL_ADDRESS_INVALID"));
			throw e;
		}
	}

	/**
	 * Test {@link UserService#signUp()} method.
	 * Scenario: unsuccessful sign up of an user due to mismatched login passwords
	 */
	@Test(expected=ServiceException.class)
	public void signUpMismatchedLoginPasswords() throws ServiceException {
		String loginName = "brazso";
		String loginPassword1 = "yell0wsubmarine";
		String loginPassword2 = "yell0wsubmarinE";
		String fullName = "Zsolt Branyiczky";
		String emailAddr = "zbranyiczky@dummy987.com";
		String zoneId = "CET";
		Locale locale = new Locale("en");
		
		try {
			userService.signUp(loginName, loginPassword1, loginPassword2, fullName, emailAddr, zoneId, locale);
		}
		catch (ServiceException e) {
			assertTrue("There must be a single message in ServiceException named GIVEN_PASSWORDS_MISMATCH", 
					e.getMessages().size()==1 && e.getMessages().get(0).getMsgCode().equals("GIVEN_PASSWORDS_MISMATCH"));
			throw e;
		}
	}

	/**
	 * Test {@link UserService#signUp()} method.
	 * Scenario: unsuccessful sign up of an user given due to some database error
	 */
	@Test(expected=ServiceException.class)
	public void signUpFailsWithDatabaseError() throws ServiceException {
		String loginName = "brazso";
		String loginPassword1 = "yell0wsubmarine";
		String loginPassword2 = "yell0wsubmarine";
		String fullName = "Zsolt Branyiczky";
		String emailAddr = "zbranyiczky@dummy987.com";
		Locale locale = new Locale("en");
		String encryptedLoginPassword = CommonUtil.getEncryptedLoginPassword(loginName, loginPassword1);
		String sRole = "USER";
		String sUserStatus = "CANDIDATE";
		String token = "Y54Fd1fjjegzB0yymkoz";
		String zoneId = "CET";
		LocalDateTime modificationTime = LocalDateTime.now();
		
//		Mockito.when(commonUtil.getEncryptedLoginPassword(loginName, loginPassword1)).thenReturn(encryptedLoginPassword);
		Mockito.when(userDao.findUserByLoginNameOrEmailAddress(loginName, emailAddr)).thenReturn(null);
//		Mockito.when(commonUtil.generateRandomToken()).thenReturn(token);
		Mockito.when(applicationService.getActualDateTime()).thenReturn(modificationTime);
		
		Mockito.when(userDao.saveUser(Mockito.eq(loginName), /* encryptedLoginPassword */ Mockito.anyString(),
				Mockito.eq(fullName), Mockito.eq(emailAddr), Mockito.eq(sRole), Mockito.eq(sUserStatus),
				/* token */ Mockito.anyString(), Mockito.eq(zoneId), Mockito.eq(modificationTime)))
				.thenThrow(new PersistenceException());
		
		try {
			userService.signUp(loginName, loginPassword1, loginPassword2, fullName, emailAddr, zoneId, locale);
		}
		catch (ServiceException e) {
			assertTrue("There must be a single message in RestServiceException named DB_SAVE_FAILED", 
					e.getMessages().size()==1 && e.getMessages().get(0).getMsgCode().equals("DB_SAVE_FAILED"));
			throw e;
		}
	}

	/**
	 * Test {@link UserService#signUp()} method.
	 * Scenario: successful sign up of an user given by login data
	 */
	@Test
	public void signUpSuccessful() throws ServiceException {
		String loginName = "brazso";
		String loginPassword1 = "yell0wsubmarine";
		String loginPassword2 = "yell0wsubmarine";
		String fullName = "Zsolt Branyiczky";
		String emailAddr = "zbranyiczky@dummy987.com";
		Locale locale = new Locale("en");
		String encryptedLoginPassword = CommonUtil.getEncryptedLoginPassword(loginName, loginPassword1);
		String sRole = "USER";
		String sUserStatus = "CANDIDATE";
		String token = "Y54Fd1fjjegzB0yymkoz";
		String zoneId = "CET";
		LocalDateTime modificationTime = LocalDateTime.now();

//		Mockito.when(commonUtil.getEncryptedLoginPassword(loginName, loginPassword1)).thenReturn(encryptedLoginPassword);
		Mockito.when(userDao.findUserByLoginNameOrEmailAddress(loginName, emailAddr)).thenReturn(null);
//		Mockito.when(commonUtil.generateRandomToken()).thenReturn(token);
		Mockito.when(applicationService.getActualDateTime()).thenReturn(modificationTime);
		
		User user = new User();
		user.setLoginName(loginName);
		user.setLoginPassword(loginPassword1);
		Mockito.when(userDao.saveUser(Mockito.eq(loginName), /* encryptedLoginPassword */ Mockito.anyString(),
				Mockito.eq(fullName), Mockito.eq(emailAddr), Mockito.eq(sRole), Mockito.eq(sUserStatus),
				/* token */ Mockito.anyString(), Mockito.eq(zoneId), Mockito.eq(modificationTime))).thenReturn(user);
		
		Mockito.doNothing().when(emailService).sendRegistrationMail(user, locale);
		
		User user2 = userService.signUp(loginName, loginPassword1, loginPassword2, fullName, emailAddr, zoneId, locale);
		assertEquals("Returned user must be the same as the given one", user, user2);
	}

	/**
	 * Test {@link UserService#modifyUser()} method.
	 * Scenario: successful modification of a given user
	 */
	@Test
	public void modifyUserSuccessful() throws ServiceException {
		String loginName = "admin";
		String loginPasswordActual = "admin_!";
		String encryptedLoginPasswordActual = "87326fa89d072eb3f575e62350184dc70131fae2645728b278e2c683799a951327fa3bcb06ec7d045c7543edf1c79ab1dc1ec3bacf054e2c1eae39148b9a5c29";
		String loginPasswordNew = "admin_!!";
		String loginPasswordAgain = "admin_!!";
		String fullName = "Mr Zsolt Branyiczky";
		String emailNew = "zbranyiczky@dummy988.com";
		String emailNewAgain = "zbranyiczky@dummy988.com";
		String zoneId = "UTC";
		LocalDateTime modificationTime = LocalDateTime.now();
		Locale locale = new Locale("en");
		String encryptedNewLoginPassword = CommonUtil.getEncryptedLoginPassword(loginName, loginPasswordNew);

		Mockito.when(userDao.findUserByLoginNameOrEmailAddress(loginName, emailNew)).thenReturn(null);
		
		User user = new User();
		user.setLoginName(loginName);
		user.setLoginPassword(encryptedLoginPasswordActual /*loginPasswordNew*/);
		
//		Mockito.when(commonUtil.getEncryptedLoginPassword(loginName, loginPasswordNew)).thenReturn(encryptedNewLoginPassword);
		Mockito.when(userDao.findUserByLoginName(loginName)).thenReturn(user);
//		Mockito.when(commonUtil.validateLoginPassword(loginPasswordActual, encryptedLoginPasswordActual)).thenReturn(true);
		Mockito.when(userDao.existUserByEmailAddrExceptUser(loginName, emailNew)).thenReturn(false);
		Mockito.when(userDao.modifyUser(Mockito.eq(user), Mockito.eq(fullName), Mockito.eq(emailNew),
				/* encryptedNewLoginPassword */ Mockito.anyString(), Mockito.eq(zoneId), Mockito.eq(modificationTime)))
				.thenReturn(user);
		Mockito.when(applicationService.getActualDateTime()).thenReturn(modificationTime);
		Mockito.doNothing().when(emailService).sendEmailChangedMail(user, locale);
		
		User user2 = userService.modifyUser(loginName, loginPasswordActual, loginPasswordNew, loginPasswordAgain, fullName, emailNew, emailNewAgain, zoneId, locale);
		assertEquals("Returned user must be the same as the given one", user, user2);
	}
	
	/**
	 * Test {@link UserService#processRegistrationToken(String)} method.
	 * Scenario: successful process of an user with CANDIDATE status
	 */
	@Test
	public void processRegistrationTokenSuccessfull(/*String registrationToken*/) throws ServiceException {
		String registrationToken = "some dummy value";
		String fromStatus = "CANDIDATE";
		String toStatus = "NORMAL";
		LocalDateTime modificationTime = LocalDateTime.now();
		
		User user = new User();
		UserStatus userStatus = new UserStatus();
		userStatus.setStatus(fromStatus);
		user.setUserStatus(userStatus);
		
		User user2 = new User();
		userStatus.setStatus(toStatus);
		user2.setUserStatus(userStatus);
		
		Mockito.when(userDao.findUserByToken(registrationToken)).thenReturn(user);
		Mockito.when(userDao.modifyUserStatusToken(user, "NORMAL", modificationTime)).thenReturn(user2);
		Mockito.when(applicationService.getActualDateTime()).thenReturn(modificationTime);
		
		userService.processRegistrationToken(registrationToken);
	}

	/**
	 * Test {@link UserService#processRegistrationToken(String)} method.
	 * Scenario: unsuccessful process of an user because the user cannot be specified
	 * from the given registrationToken
	 */
	@Test(expected=ServiceException.class)
	public void processRegistrationTokenWithUnknownUser(/*String registrationToken*/) throws ServiceException {
		String registrationToken = "some dummy value";
		
		Mockito.when(userDao.findUserByToken(registrationToken)).thenReturn(null);

		try {
			userService.processRegistrationToken(registrationToken);
		}
		catch (ServiceException e) {
			assertTrue("There must be a single message in ServiceException named REGISTRATION_TOKEN_UNKNOWN", 
					e.getMessages().size()==1 && e.getMessages().get(0).getMsgCode().equals("REGISTRATION_TOKEN_UNKNOWN"));
			throw e;
		}
	}

	/**
	 * Test {@link UserService#processRegistrationToken(String)} method.
	 * Scenario: unsuccessful process of an user because the found user has LOCKED status
	 */
	@Test(expected=ServiceException.class)
	public void processRegistrationTokenWithLockedStatus(/*String registrationToken*/) throws ServiceException {
		String registrationToken = "some dummy value";
		String fromStatus = "LOCKED";
		
		User user = new User();
		UserStatus userStatus = new UserStatus();
		userStatus.setStatus(fromStatus);
		user.setUserStatus(userStatus);
		
		Mockito.when(userDao.findUserByToken(registrationToken)).thenReturn(user);

		try {
			userService.processRegistrationToken(registrationToken);
		}
		catch (ServiceException e) {
			assertTrue("There must be a single message in ServiceException named REGISTRATION_TOKEN_LOCKED",
					e.getMessages().size() == 1 && e.getMessages().get(0).getMsgCode().equals("REGISTRATION_TOKEN_LOCKED"));
			throw e;
		}
	}

	/**
	 * Test {@link UserService#processChangeEmailToken(String)} method.
	 * Scenario: successful process of an user with CANDIDATE status
	 */
	@Test(expected=ServiceException.class)
	public void processChangeEmailToken(/*String userToken*/) throws ServiceException {
		String userToken = "some dummy value";
		String status = "NORMAL";
		LocalDateTime modificationTime = LocalDateTime.now();
		
		User user = new User();
		UserStatus userStatus = new UserStatus();
		userStatus.setStatus(status);
		user.setUserStatus(userStatus);
		
		Mockito.when(userDao.findUserByToken(userToken)).thenReturn(user);
		Mockito.when(userDao.modifyUserEmailAddr(user, modificationTime)).thenReturn(true);
		Mockito.when(applicationService.getActualDateTime()).thenReturn(modificationTime);
		
		try {
			userService.processChangeEmailToken(userToken);
		}
		catch (ServiceException e) {
			assertTrue("There must be a single message in ServiceException named CHANGE_EMAIL_ACKNOWLEDGED", 
					e.getMessages().size()==1 && e.getMessages().get(0).getMsgCode().equals("CHANGE_EMAIL_ACKNOWLEDGED"));
			throw e;
		}
	}

	/**
	 * Test {@link UserService#processChangeEmailToken(String)} method.
	 * Scenario: successful process of an user with CANDIDATE status
	 */
	@Test
	public void processChangeEmailToken_NoEmailModification(/*String userToken*/) throws ServiceException {
		String userToken = "some dummy value";
		String status = "NORMAL";
		LocalDateTime modificationTime = LocalDateTime.now();
		
		User user = new User();
		UserStatus userStatus = new UserStatus();
		userStatus.setStatus(status);
		user.setUserStatus(userStatus);
		
		Mockito.when(userDao.findUserByToken(userToken)).thenReturn(user);
		Mockito.when(userDao.modifyUserEmailAddr(user, modificationTime)).thenReturn(false);
		Mockito.when(applicationService.getActualDateTime()).thenReturn(modificationTime);
		
		userService.processChangeEmailToken(userToken);
	}

	/**
	 * Test {@link UserService#processChangeEmailToken(String)} method.
	 * Scenario: throws IllegalArgumentException because of the given {@code null}
	 *           {@code userToken} parameter.
	 */
	@Test(expected=IllegalArgumentException.class)
	public void processChangeEmailToken_NullUserToken(/*String userToken*/) throws ServiceException {
		String userToken = null;
		
		userService.processChangeEmailToken(userToken);
	}

	/**
	 * Test {@link UserService#processChangeEmailToken(String)} method.
	 * Scenario: throws IllegalArgumentException because of the given empty
	 *           {@code userToken} parameter.
	 */
	@Test(expected=IllegalArgumentException.class)
	public void processChangeEmailToken_EmptyUserToken(/*String userToken*/) throws ServiceException {
		String userToken = "";
		
		userService.processChangeEmailToken(userToken);
	}

	/**
	 * Test {@link UserService#processChangeEmailToken(String)} method.
	 * Scenario: unsuccessful process of an user because the user cannot be specified
	 * from the given registrationToken
	 */
	@Test(expected=ServiceException.class)
	public void processChangeEmailToken_UnknownUserToken(/*String userToken*/) throws ServiceException {
		String userToken = "some dummy value";
		
		Mockito.when(userDao.findUserByToken(userToken)).thenReturn(null);

		try {
			userService.processChangeEmailToken(userToken);
		}
		catch (ServiceException e) {
			assertTrue("There must be a single message in ServiceException named USER_TOKEN_UNKNOWN", 
					e.getMessages().size()==1 && e.getMessages().get(0).getMsgCode().equals("USER_TOKEN_UNKNOWN"));
			throw e;
		}
	}

	/**
	 * Test {@link UserService#processChangeEmailToken(String)} method.
	 * Scenario: unsuccessful process of an user because the found user has not NORMAL status
	 */
	@Test(expected=ServiceException.class)
	public void processChangeEmailToken_UserNotNormalStatus(/*String userToken*/) throws ServiceException {
		String userToken = "some dummy value";
		String status = "LOCKED"; // not NORMAL
		
		User user = new User();
		UserStatus userStatus = new UserStatus();
		userStatus.setStatus(status);
		user.setUserStatus(userStatus);
		
		Mockito.when(userDao.findUserByToken(userToken)).thenReturn(user);

		try {
			userService.processChangeEmailToken(userToken);
		}
		catch (ServiceException e) {
			assertTrue("There must be a single message in ServiceException named USER_TOKEN_NOT_NORMAL",
					e.getMessages().size() == 1 && e.getMessages().get(0).getMsgCode().equals("USER_TOKEN_NOT_NORMAL"));
			throw e;
		}
	}
	
	/**
	 * Test {@link UserService#retrieveUserOfEvent(Long, Long)} method.
	 * Scenario: throws {@link IllegalArgumentException} because of the given {@code null}
	 *           {@code eventId} parameter
	 */
	@Test(expected=IllegalArgumentException.class)
	public void /*UserOfEvent*/ retrieveUserOfEvent_NullEventId(/*Long eventId, Long userId*/) throws ServiceException {
		Long eventId = null;
		Long userId = 2L; // normal
		userService.retrieveUserOfEvent(eventId, userId);
	}
	
	/**
	 * Test {@link UserService#retrieveUserOfEvent(Long, Long)} method.
	 * Scenario: throws {@link IllegalArgumentException} because of the given {@code null}
	 *           {@code userId} parameter
	 */
	@Test(expected=IllegalArgumentException.class)
	public void /*UserOfEvent*/ retrieveUserOfEvent_NullUserId(/*Long eventId, Long userId*/) throws ServiceException {
		Long eventId = 1L; // WC2014
		Long userId = null;
		userService.retrieveUserOfEvent(eventId, userId);
	}
	
	/**
	 * Test {@link UserService#retrieveUserOfEvent(Long, Long)} method.
	 * Scenario: returns {@code null} because of the given {@code userId} parameter
	 *           does not exist in the database
	 */
	@Test
	public void /*UserOfEvent*/ retrieveUserOfEvent_UnknownUserId(/*Long eventId, Long userId*/) throws ServiceException {
		Long eventId = 1L; // WC2014
		Long userId = -1L; // non existing id
		
		Mockito.when(userDao.retrieveUserOfEvent(eventId, userId)).thenReturn(null);
		
		UserOfEvent userOfEvent = userService.retrieveUserOfEvent(eventId, userId);
		assertNull("Retrieved object should be null.", userOfEvent);
	}
	
	/**
	 * Test {@link UserService#retrieveUserOfEvent(Long, Long)} method.
	 * Scenario: successfully returns result
	 */
	@Test
	public void /*UserOfEvent*/ retrieveUserOfEvent(/*Long eventId, Long userId*/) throws ServiceException {
		Long eventId = 1L; // WC2014
		Long userId = 2L; // normal
		
		UserOfEvent expectedUserOfEvent = new UserOfEvent();
		expectedUserOfEvent.setEvent(new Event());
		expectedUserOfEvent.setUser(new User());
		Mockito.when(userDao.retrieveUserOfEvent(eventId, userId)).thenReturn(expectedUserOfEvent);
		
		UserOfEvent userOfEvent = userService.retrieveUserOfEvent(eventId, userId);
		assertEquals("Retrieved object should be equal to the expected one.", 
				expectedUserOfEvent, userOfEvent);
	}
	
	/**
	 * Test {@link UserService#saveUserOfEvent(Long, Long, Long, Long)} method.
	 * Scenario: throws {@link IllegalArgumentException} because of the given {@code null}
	 *           {@code eventId} parameter
	 */
	@Test(expected=IllegalArgumentException.class)
	public void /*UserOfEvent*/ saveUserOfEvent_NullEventId(/*Long eventId, Long userId, Long favouriteGroupTeamId, Long favouriteKnockoutTeamId*/) throws ServiceException {
		Long eventId = null;
		Long userId = 2L; // normal
		Long favouriteGroupTeamId = 6L; // Brazil
		Long favouriteKnockoutTeamId = 2L; // Argentina
 
		userService.saveUserOfEvent(eventId, userId, favouriteGroupTeamId, favouriteKnockoutTeamId);
	}
	
	/**
	 * Test {@link UserService#saveUserOfEvent(Long, Long, Long, Long)} method.
	 * Scenario: throws {@link IllegalArgumentException} because of the given {@code null}
	 *           {@code userId} parameter
	 */
	@Test(expected=IllegalArgumentException.class)
	public void /*UserOfEvent*/ saveUserOfEvent_NullUserId(/*Long eventId, Long userId, Long favouriteGroupTeamId, Long favouriteKnockoutTeamId*/) throws ServiceException {
		Long eventId = 1L; // WC2014
		Long userId = null;
		Long favouriteGroupTeamId = 6L; // Brazil
		Long favouriteKnockoutTeamId = 2L; // Argentina
 
		userService.saveUserOfEvent(eventId, userId, favouriteGroupTeamId, favouriteKnockoutTeamId);
	}
	
	/**
	 * Test {@link UserService#saveUserOfEvent(Long, Long, Long, Long)} method.
	 * Scenario: throws {@link IllegalStateException} because of the given 
	 *           {@code eventId} parameter does not exist in database
	 */
	@Test(expected=IllegalStateException.class)
	public void /*UserOfEvent*/ saveUserOfEvent_NullUserOfEvent_UnknownEventId(/*Long eventId, Long userId, Long favouriteGroupTeamId, Long favouriteKnockoutTeamId*/) throws ServiceException {
		Long eventId = -1L; // unknown
		Long userId = 2L; // normal
		Long favouriteGroupTeamId = 6L; // Brazil
		Long favouriteKnockoutTeamId = 2L; // Argentina
 
		UserOfEvent retrievedUserOfEvent = null;
		Mockito.when(userDao.retrieveUserOfEvent(eventId, userId)).thenReturn(retrievedUserOfEvent);
		userService.saveUserOfEvent(eventId, userId, favouriteGroupTeamId, favouriteKnockoutTeamId);
	}
	
	/**
	 * Test {@link UserService#saveUserOfEvent(Long, Long, Long, Long)} method.
	 * Scenario: throws {@link IllegalStateException} because of the given 
	 *           {@code userId} parameter does not exist in database
	 */
	@Test(expected=IllegalStateException.class)
	public void /*UserOfEvent*/ saveUserOfEvent_NullUserOfEvent_UnknownUserId(/*Long eventId, Long userId, Long favouriteGroupTeamId, Long favouriteKnockoutTeamId*/) throws ServiceException {
		Long eventId = 1L; // WC2014
		Long userId = -1L; // unknown
		Long favouriteGroupTeamId = 6L; // Brazil
		Long favouriteKnockoutTeamId = 2L; // Argentina
 
		UserOfEvent retrievedUserOfEvent = null;
		Mockito.when(userDao.retrieveUserOfEvent(eventId, userId)).thenReturn(retrievedUserOfEvent);
		userService.saveUserOfEvent(eventId, userId, favouriteGroupTeamId, favouriteKnockoutTeamId);
	}
	
	/**
	 * Test {@link UserService#saveUserOfEvent(Long, Long, Long, Long)} method.
	 * Scenario: throws {@link IllegalStateException} because of the given 
	 *           {@code userId} parameter does not exist in database
	 */
	@Test(expected=IllegalStateException.class)
	public void /*UserOfEvent*/ saveUserOfEvent_NullUserOfEvent_UnknownFavouriteGroupTeamId(/*Long eventId, Long userId, Long favouriteGroupTeamId, Long favouriteKnockoutTeamId*/) throws ServiceException {
		Long eventId = 1L; // WC2014
		Long userId = 2L;
		Long favouriteGroupTeamId = -1L; // unknown
		Long favouriteKnockoutTeamId = 2L; // Argentina
 
		UserOfEvent retrievedUserOfEvent = null;
		Mockito.when(userDao.retrieveUserOfEvent(eventId, userId)).thenReturn(retrievedUserOfEvent);
		userService.saveUserOfEvent(eventId, userId, favouriteGroupTeamId, favouriteKnockoutTeamId);
	}
	
	/**
	 * Test {@link UserService#saveUserOfEvent(Long, Long, Long, Long)} method.
	 * Scenario: throws {@link IllegalStateException} because of the given 
	 *           {@code userId} parameter does not exist in database
	 */
	@Test(expected=IllegalStateException.class)
	public void /*UserOfEvent*/ saveUserOfEvent_NullUserOfEvent_UnknownFavouriteKnockoutTeamId(/*Long eventId, Long userId, Long favouriteGroupTeamId, Long favouriteKnockoutTeamId*/) throws ServiceException {
		Long eventId = 1L; // WC2014
		Long userId = 2L;
		Long favouriteGroupTeamId = 6L; // Brazil
		Long favouriteKnockoutTeamId = -1L; // unknown
 
		UserOfEvent retrievedUserOfEvent = null;
		Mockito.when(userDao.retrieveUserOfEvent(eventId, userId)).thenReturn(retrievedUserOfEvent);
		userService.saveUserOfEvent(eventId, userId, favouriteGroupTeamId, favouriteKnockoutTeamId);
	}
	
	/**
	 * Test {@link UserService#saveUserOfEvent(Long, Long, Long, Long)} method.
	 * Scenario: successfully saves a new {@link UserOfEvent} object into database
	 */
	@Test
	@Transactional
	public void /*UserOfEvent*/ saveUserOfEvent_NullUserOfEvent(/*Long eventId, Long userId, Long favouriteGroupTeamId, Long favouriteKnockoutTeamId*/) throws ServiceException {
		Long eventId = 1L; // WC2014
		Long userId = 2L;
		Long favouriteGroupTeamId = 6L; // Brazil
		Long favouriteKnockoutTeamId = 2L; // Argentina
 
		UserOfEvent retrievedUserOfEvent = null;
		Mockito.when(userDao.retrieveUserOfEvent(eventId, userId)).thenReturn(retrievedUserOfEvent);
		UserOfEvent userOfEvent = userService.saveUserOfEvent(eventId, userId, favouriteGroupTeamId, favouriteKnockoutTeamId);
		
		assertTrue("Entity should be in the persistence context.", commonDao.containsEntity(userOfEvent));
		assertNotNull("Entity should have not null identity value.", userOfEvent.getUserOfEventId());
		assertTrue("Entity should have the same relevant field values as the expected one.",
				userOfEvent.getEvent().getEventId().equals(eventId)
				&& userOfEvent.getUser().getUserId().equals(userId)
				&& userOfEvent.getFavouriteGroupTeam().getTeamId().equals(favouriteGroupTeamId)
				&& userOfEvent.getFavouriteKnockoutTeam().getTeamId().equals(favouriteKnockoutTeamId));
	}
	
	/**
	 * Test {@link UserService#saveUserOfEvent(Long, Long, Long, Long)} method.
	 * Scenario: successfully updates an existing {@link UserOfEvent} object in database
	 */
	@Test
	@Transactional
	public void /*UserOfEvent*/ saveUserOfEvent(/*Long eventId, Long userId, Long favouriteGroupTeamId, Long favouriteKnockoutTeamId*/) throws ServiceException {
		Long eventId = 1L; // WC2014
		Long userId = 2L;
		Long favouriteGroupTeamId = 6L; // Brazil
		Long favouriteKnockoutTeamId = 2L; // Argentina
		
		// creates a new UserOfEvent object in database
		UserOfEvent expectedUserOfEvent = new UserOfEvent();
		expectedUserOfEvent.setEvent(commonDao.findEntityById(Event.class, eventId));
		expectedUserOfEvent.setUser(commonDao.findEntityById(User.class, userId));
		commonDao.persistEntity(expectedUserOfEvent);
 
		Mockito.when(userDao.retrieveUserOfEvent(eventId, userId)).thenReturn(expectedUserOfEvent);
		UserOfEvent userOfEvent = userService.saveUserOfEvent(eventId, userId, favouriteGroupTeamId, favouriteKnockoutTeamId);
		
		assertTrue("Entity should be in the persistence context.", commonDao.containsEntity(userOfEvent));
		assertNotNull("Entity should have not null identity value.", userOfEvent.getUserOfEventId());
		assertTrue("Entity should have the same relevant field values as the expected one.",
				userOfEvent.getEvent().getEventId().equals(eventId)
				&& userOfEvent.getUser().getUserId().equals(userId)
				&& userOfEvent.getFavouriteGroupTeam().getTeamId().equals(favouriteGroupTeamId)
				&& userOfEvent.getFavouriteKnockoutTeam().getTeamId().equals(favouriteKnockoutTeamId));
		assertEquals("Saved UserOfEvent instance must be equal to the expected one.", expectedUserOfEvent, userOfEvent);
	}
	
	/**
	 * Test {@link UserService#findUserLoginNamesByLoginNamePrefix(String)} method.
	 * Scenario: successfully retrieves expected result
	 */
	@Test
	public void /*List<String>*/ findUserLoginNamesByLoginNamePrefix(/*String loginNamePrefix*/) {
		String loginNamePrefix = "nor";
		User user = commonDao.findEntityById(User.class, 2L); // normal
		List<User> expectedUsers = Arrays.asList(user);
		List<String> expectedUserLoginNames = expectedUsers.stream().map(e -> e.getLoginName())
				.collect(Collectors.toList());
		Mockito.when(userDao.findUsersByLoginNamePrefix(loginNamePrefix)).thenReturn(expectedUsers);
		
		List<String> userLoginNames = userService.findUserLoginNamesByLoginNamePrefix(loginNamePrefix);
		assertEquals("Retrieved list should be the same as the expected one apart from the order of their elements.", 
				new HashSet<>(expectedUserLoginNames), new HashSet<>(userLoginNames));
	}
	
	/**
	 * Test {@link UserService#findUserFullNamesByFullNameContain(String)} method.
	 * Scenario: successfully retrieves expected result
	 */
	@Test
	public void /*List<String>*/ findUserFullNamesByFullNameContain(/*String fullNameContain*/) {
		String fullNameContain = "l Du"; // fullName is "Normal Dummy"

		User user = commonDao.findEntityById(User.class, 2L); // normal
		List<User> expectedUsers = Arrays.asList(user);
		List<String> expectedUserLoginNames = expectedUsers.stream().map(e -> e.getFullName())
				.collect(Collectors.toList());
		Mockito.when(userDao.findUsersByFullNameContain(fullNameContain)).thenReturn(expectedUsers);
		
		List<String> userLoginNames = userService.findUserFullNamesByFullNameContain(fullNameContain);
		assertEquals("Retrieved list should be the same as the expected one apart from the order of their elements.", 
				new HashSet<>(expectedUserLoginNames), new HashSet<>(userLoginNames));
	}
	
	/**
	 * Test {@link UserService#retrieveUser(Long)} method.
	 * Scenario: successfully retrieves expected result
	 */
	@Test
	public void /*User*/ retrieveUser(/*Long userId*/) throws ServiceException {
		Long userId = 2L; // normal
		User expectedUser = commonDao.findEntityById(User.class, userId);
		
		User retrievedUser = userService.retrieveUser(userId);
		
		assertTrue("Entity should not be in the persistence context.", !commonDao.containsEntity(retrievedUser));
		assertEquals("Retrieved object should be equal to the expected one.", 
				expectedUser, retrievedUser);
	}
	
	/**
	 * Test {@link UserService#getAllSupportedTimeZoneIds()} method.
	 * Result map should be
	 * - contains at least 100 elements
	 * - sorted by key
	 * - values are in [+-][0-9][0-9]:[0-9][0-9] format
	 * Scenario: good case
	 */
	@Test
	public void /*Map<String, String>*/ getAllSupportedTimeZoneIds() {
		final int MIN_SIZE = 100;
		final String valueFormat = "^[+-][0-9][0-9]:[0-9][0-9]$";
		
		Map<String, String> allZoneIdsMap = userService.getAllSupportedTimeZoneIds();
		
//		allZoneIdsMap.forEach((k, v) ->
//		{
//			logger.info(String.format("key=%s, value=%s", k, v));
//		});		
		
		assertTrue(String.format("Time zone map should contain at least %d elements instead of %d.", MIN_SIZE, allZoneIdsMap.size()), allZoneIdsMap.size() >= MIN_SIZE);
		
		boolean isSorted = Ordering.natural().isOrdered(allZoneIdsMap.keySet());
		assertTrue("Key values of time zone map should be sorted.", isSorted);
		
		long unformatCount = allZoneIdsMap.values().stream().filter(k->!k.matches(valueFormat)).count();
		assertEquals(String.format("Some values of time zone map are in wrong format.", unformatCount), 0, unformatCount);
	}
	
	/**
	 * Test {@link UserService#resetPassword(String, Locale)} method.
	 * Scenario: throws {@link ServiceException} because of the given {@code null}
	 *           {@code emailAddr} parameter
	 */
	@Test(expected=ServiceException.class)
	public void resetPassword_NullEmailAddr(/*String emailAddr, Locale locale*/) throws ServiceException {
		String emailAddr = null;
		Locale locale = new Locale("en");
		
		try {
			userService.resetPassword(emailAddr, locale);
		}
		catch (ServiceException e) {
			assertTrue("There must be a single message in ServiceException named MISSING_RESET_PASSWORD_EMAIL", 
					e.getMessages().size()==1 && e.getMessages().get(0).getMsgCode().equals("MISSING_RESET_PASSWORD_EMAIL"));
			throw e;
		}
	}

	/**
	 * Test {@link UserService#resetPassword(String, Locale)} method.
	 * Scenario: throws {@link ServiceException} because of the given empty
	 *           {@code emailAddr} parameter
	 */
	@Test(expected=ServiceException.class)
	public void resetPassword_EmptyEmailAddr(/*String emailAddr, Locale locale*/) throws ServiceException {
		String emailAddr = "";
		Locale locale = new Locale("en");
		
		//Mockito.when(userDao.findUserByLoginNameOrFullName(loginName, fullName)).thenReturn(user);
		
		try {
			userService.resetPassword(emailAddr, locale);
		}
		catch (ServiceException e) {
			assertTrue("There must be a single message in ServiceException named MISSING_RESET_PASSWORD_EMAIL", 
					e.getMessages().size()==1 && e.getMessages().get(0).getMsgCode().equals("MISSING_RESET_PASSWORD_EMAIL"));
			throw e;
		}
	}
	
	/**
	 * Test {@link UserService#resetPassword(String, Locale)} method.
	 * Scenario: throws {@link ServiceException} because of the given {@code emailAddr} 
	 *           parameter does not specify any user in database
	 */
	@Test(expected=ServiceException.class)
	public void resetPassword_UnknownUser(/*String emailAddr, Locale locale*/) throws ServiceException {
		String emailAddr = "normal.dummy@zematix.hu";
		Locale locale = new Locale("en");
		
		Mockito.when(userDao.findUserByEmailAddress(emailAddr)).thenReturn(null);
		
		try {
			userService.resetPassword(emailAddr, locale);
		}
		catch (ServiceException e) {
			assertTrue("There must be a single message in ServiceException named GIVEN_EMAIL_ADDRESS_NOT_EXIST", 
					e.getMessages().size()==1 && e.getMessages().get(0).getMsgCode().equals("GIVEN_EMAIL_ADDRESS_NOT_EXIST"));
			throw e;
		}
	}
	
	/**
	 * Test {@link UserService#resetPassword(String, Locale)} method.
	 * Scenario: throws {@link ServiceException} because of the given {@code emailAddr} 
	 *           parameter specifies an user with improper user status
	 */
	@Test(expected=ServiceException.class)
	public void resetPassword_ImproperUserStatus(/*String emailAddr, Locale locale*/) throws ServiceException {
		String emailAddr = "candidate.dummy@zematix.hu";
		Locale locale = new Locale("en");
		User user = commonDao.findEntityById(User.class, 3L); // candidate, not normal
		
		Mockito.when(userDao.findUserByEmailAddress(emailAddr)).thenReturn(user);
		
		try {
			userService.resetPassword(emailAddr, locale);
		}
		catch (ServiceException e) {
			assertTrue("There must be a single message in ServiceException named USER_STATUS_INADEQUATE", 
					e.getMessages().size()==1 && e.getMessages().get(0).getMsgCode().equals("USER_STATUS_INADEQUATE"));
			throw e;
		}
	}
	
	/**
	 * Test {@link UserService#resetPassword(String, Locale)} method.
	 * Scenario: throws {@link ServiceException} because of database saving failure
	 */
	@Test(expected=ServiceException.class)
	public void resetPassword_SaveFailed(/*String emailAddr, Locale locale*/) throws ServiceException {
		String emailAddr = "normal.dummy@zematix.hu";
		Locale locale = new Locale("en");
		User user = commonDao.findEntityById(User.class, 2L); // candidate, not normal
		String newPassword = "12345678";
		String resetPassword = "abcdefgh";
		LocalDateTime modificationTime = LocalDateTime.now();
		
		Mockito.when(userDao.findUserByEmailAddress(emailAddr)).thenReturn(user);
//		Mockito.when(commonUtil.generateRandomPassword()).thenReturn(newPassword);
//		Mockito.when(commonUtil.getEncryptedLoginPassword(user.getLoginName(), newPassword)).thenReturn(resetPassword);
		Mockito.when(applicationService.getActualDateTime()).thenReturn(modificationTime);
		Mockito.when(userDao.modifyUserResetPassword(Mockito.eq(user), /* resetPassword */ Mockito.anyString(),
				Mockito.eq(modificationTime))).thenReturn(false);
		
		try {
			userService.resetPassword(emailAddr, locale);
		}
		catch (ServiceException e) {
			assertTrue("There must be a single message in ServiceException named DB_SAVE_FAILED", 
					e.getMessages().size()==1 && e.getMessages().get(0).getMsgCode().equals("DB_SAVE_FAILED"));
			throw e;
		}
	}
	
	/**
	 * Test {@link UserService#resetPassword(String, Locale)} method.
	 * Scenario: successfully resets password
	 */
	@Test
	public void resetPassword(/*String emailAddr, Locale locale*/) throws ServiceException {
		String emailAddr = "normal.dummy@zematix.hu";
		Locale locale = new Locale("en");
		User user = commonDao.findEntityById(User.class, 2L); // candidate, not normal
		String newPassword = "12345678";
		String resetPassword = "abcdefgh";
		LocalDateTime modificationTime = LocalDateTime.now();
		
		Mockito.when(userDao.findUserByEmailAddress(emailAddr)).thenReturn(user);
//		Mockito.when(commonUtil.generateRandomPassword()).thenReturn(newPassword);
//		Mockito.when(commonUtil.getEncryptedLoginPassword(user.getLoginName(), newPassword)).thenReturn(resetPassword);
		Mockito.when(applicationService.getActualDateTime()).thenReturn(modificationTime);
		Mockito.when(userDao.modifyUserResetPassword(Mockito.eq(user), /* resetPassword */ Mockito.anyString(),
				Mockito.eq(modificationTime))).thenReturn(true);
		Mockito.doNothing().when(emailService).sendResetPasswordMail(Mockito.eq(user),
				/* newPassword */ Mockito.anyString(), Mockito.eq(locale));
		
		userService.resetPassword(emailAddr, locale);
	}
	
	/**
	 * Test {@link UserService#processResetPasswordToken(String)} method.
	 * Scenario: throws {@link IllegalArgumentException} because of the given {@code null}
	 *           {@code userToken} parameter
	 */
	@Test(expected=IllegalArgumentException.class)
	public void processResetPasswordToken_NullUserToken(/*String userToken*/) throws ServiceException {
		String userToken = null;
		userService.processResetPasswordToken(userToken);
	}
	
	/**
	 * Test {@link UserService#processResetPasswordToken(String)} method.
	 * Scenario: throws {@link IllegalArgumentException} because of the given empty
	 *           {@code userToken} parameter
	 */
	@Test(expected=IllegalArgumentException.class)
	public void processResetPasswordToken_EmptyUserToken(/*String userToken*/) throws ServiceException {
		String userToken = "";
		userService.processResetPasswordToken(userToken);
	}
	
	/**
	 * Test {@link UserService#processResetPasswordToken(String)} method.
	 * Scenario: throws {@link ServiceException} because of the given
	 *           {@code userToken} parameter does not specify any user in database 
	 */
	@Test(expected=ServiceException.class)
	public void processResetPasswordToken_UnknownUser(/*String userToken*/) throws ServiceException {
		String userToken = "dummy userToken";
		
		Mockito.when(userDao.findUserByToken(userToken)).thenReturn(null);

		try {
			userService.processResetPasswordToken(userToken);
		}
		catch (ServiceException e) {
			assertTrue("There must be a single message in ServiceException named USER_TOKEN_UNKNOWN", 
					e.getMessages().size()==1 && e.getMessages().get(0).getMsgCode().equals("USER_TOKEN_UNKNOWN"));
			throw e;
		}
	}
	
	/**
	 * Test {@link UserService#processResetPasswordToken(String)} method.
	 * Scenario: throws {@link ServiceException} because of the given {@code userToken} 
	 *           parameter specifies an user with improper user status
	 */
	@Test(expected=ServiceException.class)
	public void processResetPasswordToken_ImproperUserStatus(/*String userToken*/) throws ServiceException {
		String userToken = "dummy userToken";
		User user = commonDao.findEntityById(User.class, 3L); // candidate, not normal
		
		Mockito.when(userDao.findUserByToken(userToken)).thenReturn(user);

		try {
			userService.processResetPasswordToken(userToken);
		}
		catch (ServiceException e) {
			assertTrue("There must be a single message in ServiceException named USER_STATUS_INADEQUATE", 
					e.getMessages().size()==1 && e.getMessages().get(0).getMsgCode().equals("USER_STATUS_INADEQUATE"));
			throw e;
		}
	}	
	
	/**
	 * Test {@link UserService#processResetPasswordToken(String)} method.
	 * Scenario: everything is gonna be allright, however somehow the password is not modified
	 */
	@Test
	public void processResetPasswordToken_NotModified(/*String userToken*/) throws ServiceException {
		String userToken = "dummy userToken";
		User user = commonDao.findEntityById(User.class, 2L); // normal
		LocalDateTime modificationTime = LocalDateTime.now();
		
		Mockito.when(userDao.findUserByToken(userToken)).thenReturn(user);
		Mockito.when(applicationService.getActualDateTime()).thenReturn(modificationTime);
		Mockito.when(userDao.finalizeUserResetPassword(user, modificationTime)).thenReturn(false);
		
		userService.processResetPasswordToken(userToken);
	}
	
	/**
	 * Test {@link UserService#processResetPasswordToken(String)} method.
	 * Scenario: successfully processes reset password token
	 */
	@Test(expected=ServiceException.class)
	public void processResetPasswordToken(/*String userToken*/) throws ServiceException {
		String userToken = "dummy userToken";
		User user = commonDao.findEntityById(User.class, 2L); // normal
		LocalDateTime modificationTime = LocalDateTime.now();
		
		Mockito.when(userDao.findUserByToken(userToken)).thenReturn(user);
		Mockito.when(applicationService.getActualDateTime()).thenReturn(modificationTime);
		Mockito.when(userDao.finalizeUserResetPassword(user, modificationTime)).thenReturn(true);
		
		try {
			userService.processResetPasswordToken(userToken);
		}
		catch (ServiceException e) {
			assertTrue("There must be a single message in ServiceException named RESET_PASSWORD_ACKNOWLEDGED", 
					e.getMessages().size()==1 && e.getMessages().get(0).getMsgCode().equals("RESET_PASSWORD_ACKNOWLEDGED"));
			throw e;
		}
	}
	
	/**
	 * Test {@link UserService#deleteUser(String)} method.
	 * Scenario: throws {@link IllegalArgumentException} because of the given {@code null}
	 *           {@code loginName} parameter
	 */
	@Test(expected=IllegalArgumentException.class)
	public void deleteUser_NullLoginName(/*String loginName*/) throws ServiceException {
		String loginName = null;
		userService.deleteUser(loginName);
	}
	
	/**
	 * Test {@link UserService#deleteUser(String)} method.
	 * Scenario: throws {@link IllegalArgumentException} because of the given empty
	 *           {@code loginName} parameter
	 */
	@Test(expected=IllegalArgumentException.class)
	public void deleteUser_EmptyLoginName(/*String loginName*/) throws ServiceException {
		String loginName = "";
		userService.deleteUser(loginName);
	}

	/**
	 * Test {@link UserService#deleteUser(String)} method.
	 * Scenario: throws {@link ServiceException} because of the given 
	 *           {@code loginName} parameter does not specify any user in database
	 */
	@Test(expected=ServiceException.class)
	public void deleteUser_UnknownUser(/*String loginName*/) throws ServiceException {
		String loginName = "normal";
		
		Mockito.when(userDao.findUserByLoginName(loginName)).thenReturn(null);
		
		try {
			userService.deleteUser(loginName);
		}
		catch (ServiceException e) {
			assertTrue("There must be a single message in ServiceException named GIVEN_USER_NOT_EXIST", 
					e.getMessages().size()==1 && e.getMessages().get(0).getMsgCode().equals("GIVEN_USER_NOT_EXIST"));
			throw e;
		}
	}

	/**
	 * Test {@link UserService#deleteUser(String)} method.
	 * Scenario: successfully deletes user found by the given {@code loginName} parameter
	 */
	@Test
	public void deleteUser(/*String loginName*/) throws ServiceException {
		String loginName = "normal";
		User user = commonDao.findEntityById(User.class, 2L); // normal
		
		Mockito.when(userDao.findUserByLoginName(loginName)).thenReturn(user);
		Mockito.doNothing().when(userDao).deleteUser(user);
		
		try {
			userService.deleteUser(loginName);
		}
		catch (ServiceException e) {
			assertTrue("There must be a single message in ServiceException named GIVEN_USER_NOT_EXIST", 
					e.getMessages().size()==1 && e.getMessages().get(0).getMsgCode().equals("GIVEN_USER_NOT_EXIST"));
			throw e;
		}
	}
	
	/**
	 * Test {@link UserService#deleteExpiredCandidateUsers()} method.
	 * Scenario: successfully deletes expired candidate users
	 */
	@Test
	public void /*int*/ deleteExpiredCandidateUsers() throws ServiceException {
		User user = commonDao.findEntityById(User.class, 3L); //candidate
		List<User> users = Arrays.asList(user);
		int expectedNumberOfDeletedUsers = users.size();
		LocalDateTime modificationTime = LocalDateTime.now();
		Mockito.when(applicationService.getActualDateTime()).thenReturn(modificationTime);
		Mockito.when(userDao.findExpiredCandidateUsers(Mockito.any(LocalDateTime.class))).thenReturn(users);
		int numberOfDeletedUsers = userService.deleteExpiredCandidateUsers();
		assertEquals("Number of deleted users must be the same as the expected one", expectedNumberOfDeletedUsers,
				numberOfDeletedUsers);
	}
	
	/**
	 * Test {@link UserService#deleteExpiredEmailModifications()} method.
	 * Scenario: successfully deletes expired email modificator users
	 */
	@Test
	public void deleteExpiredEmailModifications() throws ServiceException {
		LocalDateTime modificationTime = LocalDateTime.now();
		Mockito.when(applicationService.getActualDateTime()).thenReturn(modificationTime);
		User user = commonDao.findEntityById(User.class, 2L); //normal
		List<User> users = Arrays.asList(user);
		int expectedNumberOfEmailModifiedUsers = users.size();
		Mockito.when(userDao.findExpiredEmailModificationUsers(Mockito.any(LocalDateTime.class))).thenReturn(users);
		int numberOfEmailModifiedUsers = userService.deleteExpiredEmailModifications();
		assertEquals("Number of email modified users must be the same as the expected one", expectedNumberOfEmailModifiedUsers,
				numberOfEmailModifiedUsers);
		User modifiedUser = commonDao.findEntityById(User.class, 2L); //normal
		assertNull("Modified user should have null emailNew field value.", modifiedUser.getEmailNew());
		assertEquals("Modified user's modificationTime field value must be the same as the expected one.", modificationTime, 
				modifiedUser.getModificationTime());
	}
	
	/**
	 * Test {@link UserService#deleteExpiredPasswordResets()} method.
	 * Scenario: successfully deletes expired password reseter users
	 */
	@Test
	public void deleteExpiredPasswordResets() throws ServiceException {
		LocalDateTime modificationTime = LocalDateTime.now();
		Mockito.when(applicationService.getActualDateTime()).thenReturn(modificationTime);
		User user = commonDao.findEntityById(User.class, 2L); //normal
		List<User> users = Arrays.asList(user);
		int expectedNumberOfPasswordResetUsers = users.size();
		Mockito.when(userDao.findExpiredPasswordResetUsers(Mockito.any(LocalDateTime.class))).thenReturn(users);
		int numberOfPasswordResetUsers = userService.deleteExpiredPasswordResets();
		assertEquals("Number of password reset users must be the same as the expected one", expectedNumberOfPasswordResetUsers,
				numberOfPasswordResetUsers);
		User modifiedUser = commonDao.findEntityById(User.class, 2L); //normal
		assertNull("Modified user should have null resetPassword field value.", modifiedUser.getResetPassword());
		assertEquals("Modified user's modificationTime field value must be the same as the expected one.", modificationTime, 
				modifiedUser.getModificationTime());
	}
}
