package com.zematix.jworldcup.backend.controller;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import javax.inject.Inject;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.Sql.ExecutionPhase;
import org.springframework.test.context.junit4.SpringRunner;

import com.zematix.jworldcup.backend.TestBase;
import com.zematix.jworldcup.backend.exception.ServiceException;

@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles({"develop", "test"})
@Sql(scripts = { "/database/controller/user-of-event-controller-before.sql" }, executionPhase = ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(scripts = { "/database/controller/user-of-event-controller-after.sql" }, executionPhase = ExecutionPhase.AFTER_TEST_METHOD)
public class UserOfEventControllerIT extends TestBase {

	@Inject
	private UserOfEventController userOfEventController;
	
	@Test
	@WithMockUser(username = "normal", roles = {"USER"})
	public void retrieveUserOfEvent() throws ServiceException {
		// given
		Long eventId = 1L; // WC2014
		Long userId = 2L; // normal
		// when
		var result = userOfEventController.retrieveUserOfEvent(eventId, userId);
		// then
		assertEquals(HttpStatus.OK, result.getStatusCode());
		assertTrue(result.getBody().getSuccessful());
		assertNotNull(result.getBody().getData());
	}
	
	@Test
	@WithMockUser(username = "normal", roles = {"USER"})
	public void saveUserOfEventWhenUpdate() throws ServiceException {
		// given
		Long eventId = 1L; // WC2014
		Long userId = 2L; // normal
		Long favouriteGroupTeamId = 1L; // Algeria
		Long favouriteKnockoutTeamId = 2L; // Argentina
		// when
		var result = userOfEventController.saveUserOfEvent(eventId, userId, favouriteGroupTeamId, favouriteKnockoutTeamId);
		// then
		assertEquals(HttpStatus.OK, result.getStatusCode());
		assertTrue(result.getBody().getSuccessful());
		assertEquals(1, result.getBody().getData().getUserOfEventId().longValue());
	}
	
	@Test
	@WithMockUser(username = "normal2", roles = {"USER"})
	public void saveUserOfEventWhenInsert() throws ServiceException {
		// given
		Long eventId = 1L; // WC2014
		Long userId = 6L; // normal2
		Long favouriteGroupTeamId = 1L; // Algeria
		Long favouriteKnockoutTeamId = 2L; // Argentina
		// when
		var result = userOfEventController.saveUserOfEvent(eventId, userId, favouriteGroupTeamId, favouriteKnockoutTeamId);
		// then
		assertEquals(HttpStatus.OK, result.getStatusCode());
		assertTrue(result.getBody().getSuccessful());
		assertEquals(2, result.getBody().getData().getUserOfEventId().longValue());
	}
}
