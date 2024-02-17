package com.zematix.jworldcup.backend.controller;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import javax.inject.Inject;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import com.zematix.jworldcup.backend.TestBase;
import com.zematix.jworldcup.backend.dto.SessionDataDto;
import com.zematix.jworldcup.backend.exception.ServiceException;
import com.zematix.jworldcup.backend.model.SessionData;
import com.zematix.jworldcup.backend.service.MessageQueueService;

@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles({"develop", "test"})
public class SessionControllerIT extends TestBase {

	@Inject
	private SessionController sessionController;
	
	@MockBean
	private MessageQueueService messageQueueService;

	@Test
	@WithMockUser(username = "normal", roles = {"USER"})
	public void refreshSessionData() throws ServiceException {
		// given
		SessionDataDto sessionDataDto = new SessionDataDto();
		// when
		var result = sessionController.refreshSessionData(sessionDataDto);
		// then
		assertEquals(HttpStatus.OK, result.getStatusCode());
		assertTrue(result.getBody().getSuccessful());
		assertEquals("jworldcup@zematix.hu", result.getBody().getData().getAppEmailAddr());
		assertNotNull(result.getBody().getData().getEvent());
		assertNotNull(result.getBody().getData().getId());
		assertEquals("CLIENT", result.getBody().getData().getOperationFlag().name());
		assertEquals("normal", result.getBody().getData().getUser().getLoginName());
		assertEquals("Everybody", result.getBody().getData().getUserGroups().get(0).getName());
		assertEquals(2, result.getBody().getData().getUserGroups().get(0).getUsers().size());
	}

	@Test
	@WithMockUser(username = "normal", roles = {"USER"})
	public void notifySessionData() throws ServiceException {
		// given
		SessionDataDto sessionDataDto = new SessionDataDto();
		// when
		Mockito.doNothing().when(messageQueueService).sendSession(Mockito.any(SessionData.class));
		var result = sessionController.notifySessionData(sessionDataDto);
		// then
		assertEquals(HttpStatus.OK, result.getStatusCode());
		assertTrue(result.getBody().getSuccessful());

	}
}
