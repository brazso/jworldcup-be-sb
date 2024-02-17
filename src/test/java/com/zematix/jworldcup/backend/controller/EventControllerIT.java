package com.zematix.jworldcup.backend.controller;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import javax.inject.Inject;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles({"develop", "test"})
public class EventControllerIT {

	@Inject
	private EventController eventController;
	
	@Test
	@WithMockUser(username = "normal", roles = {"USER"})
	public void findAllEvents() {
		// given
		// when
		var result = eventController.findAllEvents();
		// then
		assertEquals(HttpStatus.OK, result.getStatusCode());
		assertTrue(result.getBody().getSuccessful());
		assertTrue(result.getBody().getData().size()>0);
	}

	@Test
	@WithMockUser(username = "normal", roles = {"USER"})
	public void findEventByUserId() {
		// given
		Long userId = 2L; // normal
		// when
		var result = eventController.findEventByUserId(userId);
		// then
		assertEquals(HttpStatus.OK, result.getStatusCode());
		assertTrue(result.getBody().getSuccessful());
		assertTrue(result.getBody().getData().getEventId()>0);
	}
}
