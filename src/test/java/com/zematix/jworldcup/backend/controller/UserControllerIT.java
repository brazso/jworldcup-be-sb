package com.zematix.jworldcup.backend.controller;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Date;
import java.util.Locale;

import javax.inject.Inject;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.session.SessionInformation;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.Sql.ExecutionPhase;
import org.springframework.test.context.junit4.SpringRunner;

import com.zematix.jworldcup.backend.TestBase;
import com.zematix.jworldcup.backend.dao.CommonDao;
import com.zematix.jworldcup.backend.dto.UserExtendedDto;
import com.zematix.jworldcup.backend.emun.ParameterizedMessageType;
import com.zematix.jworldcup.backend.entity.User;
import com.zematix.jworldcup.backend.entity.UserGroup;
import com.zematix.jworldcup.backend.exception.ServiceException;
import com.zematix.jworldcup.backend.service.EmailService;

@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles({"develop", "test"})
@Sql(scripts = { "/database/controller/user-controller-before.sql" }, executionPhase = ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(scripts = { "/database/controller/user-controller-after.sql" }, executionPhase = ExecutionPhase.AFTER_TEST_METHOD)
public class UserControllerIT extends TestBase {

	@Inject
	private UserController userController;
	
	@Inject
	private CommonDao commonDao;
	
	@MockBean
	private EmailService emailService;
	
	@MockBean 
	private SessionRegistry sessionRegistry;

	@Test
	@WithMockUser(username = "normal", roles = {"USER"})
	public void modifyUser() throws ServiceException {
		// given
		UserExtendedDto userExtendedDto = new UserExtendedDto();
		String loginName = "normal2";
		userExtendedDto.setLoginName(loginName);
		userExtendedDto.setFullName("Normal2 Dummy2");
		userExtendedDto.setLoginPassword("normal2_!");
		userExtendedDto.setLoginPasswordNew("normal2_!2");
		userExtendedDto.setLoginPasswordAgain("normal2_!2");
		userExtendedDto.setEmailNew("normal2.dummy2@zematix.hu");
		userExtendedDto.setEmailNewAgain("normal2.dummy2@zematix.hu");
		userExtendedDto.setZoneId("CET");
		userExtendedDto.setLanguageTag("hu");
		// when
		Mockito.doNothing().when(emailService).sendEmailChangedMail(Mockito.any(User.class), Mockito.any(Locale.class));
		var result = userController.modifyUser(userExtendedDto);
		// then
		assertEquals(HttpStatus.OK, result.getStatusCode());
		assertTrue(result.getBody().getSuccessful());
		User user = commonDao.findEntityById(User.class, result.getBody().getData().getUserId());
		assertNotNull(user);
		assertEquals("Normal2 Dummy2", user.getFullName());
	}
	
	@Test
	@WithMockUser(username = "normal", roles = {"USER"})
	public void findUserLoginNamesByLoginNamePrefix() {
		// given
		String loginNamePrefix = "norm";
		// when
		var result = userController.findUserLoginNamesByLoginNamePrefix(loginNamePrefix);
		// then
		assertEquals(HttpStatus.OK, result.getStatusCode());
		assertTrue(result.getBody().getSuccessful());
		assertEquals(2, result.getBody().getData().size());
	}
	
	@Test
	@WithMockUser(username = "normal", roles = {"USER"})
	public void findUserFullNamesByFullNameContain() {
		// given
		String fullNameContain = "Dummy";
		// when
		var result = userController.findUserFullNamesByFullNameContain(fullNameContain);
		// then
		assertEquals(HttpStatus.OK, result.getStatusCode());
		assertTrue(result.getBody().getSuccessful());
		assertEquals(8, result.getBody().getData().size());
	}

	@Test
	@WithMockUser(username = "normal", roles = {"USER"})
	public void resetPassword() throws ServiceException {
		// given
		String emailAddr = "normal2.dummy@zematix.hu";
		String languageTag = "hu";
		// when
		Mockito.doNothing().when(emailService).sendResetPasswordMail(Mockito.any(User.class), Mockito.anyString(), Mockito.any(Locale.class));
		var result = userController.resetPassword(emailAddr, languageTag);
		// then
		assertEquals(HttpStatus.OK, result.getStatusCode());
		assertTrue(result.getBody().getSuccessful());
		User user = commonDao.findEntityById(User.class, 6L);
		assertNotNull(user.getResetPassword());
	}
	
	@Test
	@WithMockUser(username = "normal", roles = {"USER"})
	public void getAllSupportedTimeZoneIds() {
		// given
		// when
		var result = userController.getAllSupportedTimeZoneIds();
		// then
		assertEquals(HttpStatus.OK, result.getStatusCode());
		assertTrue(result.getBody().getSuccessful());
		assertTrue(result.getBody().getData().size() > 0);
	}
	
	@Test
	@WithMockUser(username = "normal", roles = {"USER"})
	public void findUser() throws ServiceException {
		// given
		Long userId = 2L; // normal
		// when
		var result = userController.findUser(userId);
		// then
		assertEquals(HttpStatus.OK, result.getStatusCode());
		assertTrue(result.getBody().getSuccessful());
		assertEquals("normal", result.getBody().getData().getLoginName());
		User user = commonDao.findEntityById(User.class, userId);
		assertEquals("normal", user.getLoginName());
	}
	
	@Test
	@WithMockUser(username = "normal", roles = {"USER"})
	public void deleteUser() throws ServiceException {
		// given
		String loginName = "normal2";
		// when
		var result = userController.deleteUser(loginName);
		// then
		assertEquals(HttpStatus.OK, result.getStatusCode());
		assertTrue(result.getBody().getSuccessful());
		User user = commonDao.findEntityById(User.class, 6L);
		assertNull(user);
	}

	@Test
	@WithMockUser(username = "normal", roles = {"USER"})
	public void whoami() throws ServiceException {
		// given
		// when
		var result = userController.whoami();
		// then
		assertEquals(HttpStatus.OK, result.getStatusCode());
		assertTrue(result.getBody().getSuccessful());
		assertEquals("normal", result.getBody().getData().getLoginName());
	}

	@Test
	@WithMockUser(username = "normal", roles = {"USER"})
	public void processRegistrationToken() throws ServiceException {
		// given
		String userToken = "IF2YCcPnregistration";
		// when
		ServiceException exception = assertThrows(ServiceException.class, () -> userController.processRegistrationToken(userToken));
		// then
		assertEquals(ParameterizedMessageType.INFO, exception.getOverallType());
		assertTrue(exception.containsMessage("REGISTRATION_TOKEN_ACKNOWLEDGED"));
	}

	@Test
	@WithMockUser(username = "normal", roles = {"USER"})
	public void processChangeEmailToken() throws ServiceException {
		// given
		String userToken = "IF2YCcPnNulH8UEemail";
		// when
		ServiceException exception = assertThrows(ServiceException.class, () -> userController.processChangeEmailToken(userToken));
		// then
		assertEquals(ParameterizedMessageType.INFO, exception.getOverallType());
		assertTrue(exception.containsMessage("CHANGE_EMAIL_ACKNOWLEDGED"));
	}

	@Test
	@WithMockUser(username = "normal", roles = {"USER"})
	public void processResetPasswordToken() throws ServiceException {
		// given
		String userToken = "IF2YCcPnNulH8UEreset";
		// when
		ServiceException exception = assertThrows(ServiceException.class, () -> userController.processResetPasswordToken(userToken));
		// then
		assertEquals(ParameterizedMessageType.INFO, exception.getOverallType());
		assertTrue(exception.containsMessage("RESET_PASSWORD_ACKNOWLEDGED"));
	}

	@Test
	@WithMockUser(username = "normal", roles = {"USER"})
	public void getAllAuthenticatedUsersByUserGroup() throws ServiceException {
		// given
		Long userGroupId = UserGroup.EVERYBODY_USER_GROUP_ID;
		org.springframework.security.core.userdetails.User sessionUser = new org.springframework.security.core.userdetails.User("normal", "password", Arrays.asList(new SimpleGrantedAuthority("ROLE_USER")));
		SessionInformation sessionInformation = new SessionInformation(sessionUser, "sessionId", new Date());
		// when
		Mockito.when(sessionRegistry.getAllSessions(sessionUser, false)).thenReturn(Arrays.asList(sessionInformation));
		Mockito.when(sessionRegistry.getAllPrincipals()).thenReturn(Arrays.asList(sessionUser));
		var result = userController.getAllAuthenticatedUsersByUserGroup(userGroupId);
		// then
		assertEquals(HttpStatus.OK, result.getStatusCode());
		assertTrue(result.getBody().getSuccessful());
		assertEquals(1, result.getBody().getData().size());
		assertEquals("normal", result.getBody().getData().get(0).getLoginName());
	}
}
