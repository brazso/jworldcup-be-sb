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

import com.zematix.jworldcup.backend.TestBase;

@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles({"develop", "test"})
public class TeamControllerIT extends TestBase {

	@Inject
	private TeamController teamController;
	
	@Test
	@WithMockUser(username = "normal", roles = {"USER"})
	public void retrieveFavouriteGroupTeams() {
		// given
		Long eventId = 1L; // WC2014
		// when
		var result = teamController.retrieveFavouriteGroupTeams(eventId);
		// then
		assertEquals(HttpStatus.OK, result.getStatusCode());
		assertTrue(result.getBody().getSuccessful());
		assertEquals(32, result.getBody().getData().size());
	}
	
	@Test
	@WithMockUser(username = "normal", roles = {"USER"})
	public void retrieveFavouriteKnockoutTeams() {
		// given
		Long eventId = 1L; // WC2014
		// when
		var result = teamController.retrieveFavouriteKnockoutTeams(eventId);
		// then
		assertEquals(HttpStatus.OK, result.getStatusCode());
		assertTrue(result.getBody().getSuccessful());
		assertEquals(16, result.getBody().getData().size());
	}
}
