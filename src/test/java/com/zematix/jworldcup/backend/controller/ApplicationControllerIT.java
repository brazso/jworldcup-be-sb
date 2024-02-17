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
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.Sql.ExecutionPhase;
import org.springframework.test.context.junit4.SpringRunner;

import com.zematix.jworldcup.backend.exception.ServiceException;

@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles({"develop", "test"})
@Sql(scripts = { "/database/controller/application-controller-before.sql" }, executionPhase = ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(scripts = { "/database/controller/application-controller-after.sql" }, executionPhase = ExecutionPhase.AFTER_TEST_METHOD)
public class ApplicationControllerIT {

	@Inject
	private ApplicationController applicationController;

	@Test
	@WithMockUser(username = "normal", roles = {"USER"})
	public void retrieveTopUsers() throws ServiceException {
		// given
		// when
		var result = applicationController.retrieveTopUsers();
		// then
		assertEquals(HttpStatus.OK, result.getStatusCode());
		assertTrue(result.getBody().getSuccessful());
		assertEquals(1, result.getBody().getData().size());
	}
	
	@Test
	@WithMockUser(username = "normal", roles = {"USER"})
	public void retrieveChats() throws ServiceException {
		// given
		Long eventId = 1L; // WC2014
		Long userGroupId = 1L;
		// when
		var result = applicationController.retrieveChats(eventId, userGroupId);
		// then
		assertEquals(HttpStatus.OK, result.getStatusCode());
		assertTrue(result.getBody().getSuccessful());
		assertTrue(result.getBody().getData().size()>0);		
	}
}
