package com.zematix.jworldcup.backend.controller;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.time.LocalDateTime;

import javax.inject.Inject;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.Sql.ExecutionPhase;
import org.springframework.test.context.junit4.SpringRunner;

import com.zematix.jworldcup.backend.TestBase;
import com.zematix.jworldcup.backend.exception.ServiceException;
import com.zematix.jworldcup.backend.service.ApplicationService;

@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles({"develop", "test"})
@Sql(scripts = { "/database/controller/user-notification-controller-before.sql" }, executionPhase = ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(scripts = { "/database/controller/user-notification-controller-after.sql" }, executionPhase = ExecutionPhase.AFTER_TEST_METHOD)
public class UserNotificationControllerIT extends TestBase {

	@Inject
	private UserNotificationController userNotificationController;
	
	@MockBean
	private ApplicationService applicationService;

	@Test
	@WithMockUser(username = "normal", roles = {"USER"})
	public void findByUserAndKey() throws ServiceException {
		// given
		Long userId = 2L; // normal
		String key = "GPDR";
		LocalDateTime actualDateTime = LocalDateTime.now();
		// when
		Mockito.when(applicationService.getActualDateTime()).thenReturn(actualDateTime);
		var result = userNotificationController.findByUserAndKey(userId, key);
		// then
		assertEquals(HttpStatus.OK, result.getStatusCode());
		assertTrue(result.getBody().getSuccessful());
		assertEquals(1, result.getBody().getData().getUserNotificationId().longValue());
	}

	@Test
	@WithMockUser(username = "normal2", roles = {"USER"})
	public void insert() throws ServiceException {
		// given
		Long userId = 6L; // normal2
		String key = "GPDR";
		String value = null;
		boolean hasModificationTime = false;
		LocalDateTime actualDateTime = LocalDateTime.now();
		// when
		Mockito.when(applicationService.getActualDateTime()).thenReturn(actualDateTime);
		var result = userNotificationController.insert(userId, key, value, hasModificationTime);
		// then
		assertEquals(HttpStatus.OK, result.getStatusCode());
		assertTrue(result.getBody().getSuccessful());
		assertEquals(2, result.getBody().getData().getUserNotificationId().longValue());
	}
	
	@Test
	@WithMockUser(username = "normal", roles = {"USER"})
	public void update() throws ServiceException {
		// given
		Long userNotificationId = 1L;
		String value = null;
		LocalDateTime actualDateTime = LocalDateTime.now();
		// when
		Mockito.when(applicationService.getActualDateTime()).thenReturn(actualDateTime);
		var result = userNotificationController.update(userNotificationId, value); 
		// then
		assertEquals(HttpStatus.OK, result.getStatusCode());
		assertTrue(result.getBody().getSuccessful());
		assertEquals(1, result.getBody().getData().getUserNotificationId().longValue());
		assertEquals(actualDateTime, result.getBody().getData().getModificationTime());
	}
}
