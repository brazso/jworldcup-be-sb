package com.zematix.jworldcup.backend.controller;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import javax.inject.Inject;

import org.json.JSONException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.skyscreamer.jsonassert.JSONAssert;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import com.zematix.jworldcup.backend.TestBase;
import com.zematix.jworldcup.backend.exception.ServiceException;

@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles({"develop", "test"})
public class GroupControllerIT extends TestBase {

	@Inject
	private GroupController groupController;
	
	@Test
	@WithMockUser(username = "normal", roles = {"USER"})
	public void retrieveGroupsByEvent() throws ServiceException {
		// given
		Long eventId = 1L; // WC2014
		// when
		var result = groupController.retrieveGroupsByEvent(eventId);
		// then
		assertEquals(HttpStatus.OK, result.getStatusCode());
		assertTrue(result.getBody().getSuccessful());
		assertEquals(8, result.getBody().getData().size());
	}
	
	@Test
	@WithMockUser(username = "normal", roles = {"USER"})
	public void getRankedGroupTeamsByGroup() throws ServiceException, JSONException{
		// given
		Long groupId = 1L; // A
		String expectedJsonResult = readStringResource("controller/group/getRankedGroupTeamsByGroup.json");
		// when
		var result = groupController.getRankedGroupTeamsByGroup(groupId);
		// then
		assertEquals(HttpStatus.OK, result.getStatusCode());
		assertTrue(result.getBody().getSuccessful());
		String jsonResult = generateJson(result.getBody().getData());
		JSONAssert.assertEquals(expectedJsonResult, jsonResult, false);
	}
	
	@Test
	@WithMockUser(username = "normal", roles = {"USER"})
	public void getRankedGroupTeamsByEvent() throws ServiceException, JSONException {
		// given
		Long eventId = 1L; // WC2014
		String expectedJsonResult = readStringResource("controller/group/getRankedGroupTeamsByEvent.json");
		// when
		var result = groupController.getRankedGroupTeamsByEvent(eventId);
		// then
		assertEquals(HttpStatus.OK, result.getStatusCode());
		assertTrue(result.getBody().getSuccessful());
		String jsonResult = generateJson(result.getBody().getData());
		JSONAssert.assertEquals(expectedJsonResult, jsonResult, false);
	}
}
