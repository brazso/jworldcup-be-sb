package com.zematix.jworldcup.backend.controller;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Locale;

import javax.inject.Inject;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.reflect.Whitebox;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.Sql.ExecutionPhase;
import org.springframework.test.context.junit4.SpringRunner;

import com.zematix.jworldcup.backend.TestBase;
import com.zematix.jworldcup.backend.configuration.JwtTokenUtil;
import com.zematix.jworldcup.backend.dao.CommonDao;
import com.zematix.jworldcup.backend.dto.JwtRequest;
import com.zematix.jworldcup.backend.dto.ReCaptchaDto;
import com.zematix.jworldcup.backend.dto.UserExtendedDto;
import com.zematix.jworldcup.backend.entity.User;
import com.zematix.jworldcup.backend.exception.GoogleException;
import com.zematix.jworldcup.backend.exception.ServiceException;
import com.zematix.jworldcup.backend.service.EmailService;
import com.zematix.jworldcup.backend.service.GoogleService;

@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles({"develop", "test"})
@Sql(scripts = { "/database/controller/jwt-authentication-controller-after.sql" }, executionPhase = ExecutionPhase.AFTER_TEST_METHOD)
public class JwtAuthenticationControllerIT extends TestBase {

	@Inject
	private JwtAuthenticationController jwtAuthenticationController;
	
	@Inject
	private CommonDao commonDao;
	
	@MockBean
	private EmailService emailService;
	
	@MockBean
	private GoogleService googleService;
	
	@SpyBean
	private JwtTokenUtil jwtTokenUtil;
	
	@Test
	public void createAuthenticationToken() throws ServiceException {
		// given
		JwtRequest authenticationRequest = new JwtRequest();
		Whitebox.setInternalState(authenticationRequest, "username", "normal");
		Whitebox.setInternalState(authenticationRequest, "password", "normal_!");
		// when
		var result = jwtAuthenticationController.createAuthenticationToken(authenticationRequest);
		// then
		assertEquals(HttpStatus.OK, result.getStatusCode());
		assertEquals(255, result.getBody().getToken().length());
	}

	@Test
	public void refreshAuthenticationToken() throws ServiceException {
		// given
		String refreshToken = "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJub3JtYWwiLCJpc3MiOiJKV29ybGRjdXAiLCJzY29wZXMiOiJST0xFX1VTRVIiLCJ0b2tlblR5cGUiOiJBQ0NFU1MiLCJleHAiOjE3MDcwNzYwODQsImlhdCI6MTcwNzA3NTE4NH0.uTsjsPtYvpduaVCZXZ3q3imtECyinG-M67zUfbbJvLSs0Z2uiY2j_PeSkFS-y5BLoWH7CYHAE1xCYveCWfLSJA";
		String username = "normal";
		// when
		Mockito.doReturn(username).when(jwtTokenUtil).getUsernameFromToken(refreshToken);
		var result = jwtAuthenticationController.refreshAuthenticationToken(refreshToken);
		// then
		assertEquals(HttpStatus.OK, result.getStatusCode());
		assertEquals(255, result.getBody().getToken().length());
	}
	
	@Test
	public void saveUser() throws ServiceException {
		// given
		UserExtendedDto userExtendedDto = new UserExtendedDto();
		userExtendedDto.setLoginName("normal2");
		userExtendedDto.setLoginPasswordNew("normal2_!");
		userExtendedDto.setLoginPasswordAgain("normal2_!");
		userExtendedDto.setFullName("Normal2 Dummy");
		userExtendedDto.setEmailAddr("normal2.dummy@zematix.hu");
		userExtendedDto.setZoneId("Europe/Budapest");
		userExtendedDto.setLanguageTag("hu");
		// when
		Mockito.doNothing().when(emailService).sendRegistrationMail(Mockito.any(User.class), Mockito.any(Locale.class));
		var result = jwtAuthenticationController.saveUser(userExtendedDto);
		// then
		assertEquals(HttpStatus.OK, result.getStatusCode());
		assertTrue(result.getBody().getSuccessful());
		User user = commonDao.findEntityById(User.class, result.getBody().getData().getUserId());
		assertNotNull(user);
	}

	@Test
	public void whoami() {
		// given
		// when
		var result = jwtAuthenticationController.whoami();
		// then
		assertEquals(HttpStatus.OK, result.getStatusCode());
		assertTrue(result.getBody().getSuccessful());
		assertNotNull(result.getBody().getData());
	}

	@Test
	public void verifyCaptcha() throws ServiceException, GoogleException {
		// given
		String response = "dummy";
		ReCaptchaDto reCaptchaDto = new ReCaptchaDto();
		reCaptchaDto.setSuccess(true);
		// when
		Mockito.when(googleService.siteVerify(Mockito.any(String.class), Mockito.eq(response), Mockito.any(String.class))).thenReturn(reCaptchaDto);
		var result = jwtAuthenticationController.verifyCaptcha(response);
		// then
		assertEquals(HttpStatus.OK, result.getStatusCode());
		assertTrue(result.getBody().getSuccessful());
	}
}