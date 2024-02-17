package com.zematix.jworldcup.backend.controller;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import javax.inject.Inject;
import javax.transaction.Transactional;

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
import com.zematix.jworldcup.backend.dao.CommonDao;
import com.zematix.jworldcup.backend.dto.UserCertificateExtendedDto;
import com.zematix.jworldcup.backend.emun.ParameterizedMessageType;
import com.zematix.jworldcup.backend.entity.UserGroup;
import com.zematix.jworldcup.backend.exception.ServiceException;

@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles({"develop", "test"})
@Sql(scripts = { "/database/controller/user-group-controller-before.sql" }, executionPhase = ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(scripts = { "/database/controller/user-group-controller-after.sql" }, executionPhase = ExecutionPhase.AFTER_TEST_METHOD)
public class UserGroupControllerIT extends TestBase {

	@Inject
	private UserGroupController userGroupController;
	
	@Inject
	private CommonDao commonDao;

	@Test
	@WithMockUser(username = "normal", roles = {"USER"})
	public void retrieveUserGroups() throws ServiceException {
		// given
		Long eventId = 1L; // WC2014
		Long userId = 2L; // normal
		boolean isEverybodyIncluded = false;
		// when
		var result = userGroupController.retrieveUserGroups(eventId, userId, isEverybodyIncluded);
		// then
		assertEquals(HttpStatus.OK, result.getStatusCode());
		assertTrue(result.getBody().getSuccessful());
		assertEquals(1, result.getBody().getData().size());
		assertEquals("Zematix", result.getBody().getData().get(0).getName());
		assertEquals(2, result.getBody().getData().get(0).getUsers().size());
	}
	
	@Test
	@WithMockUser(username = "normal", roles = {"USER"})
	public void retrieveUsersByUserGroup() throws ServiceException {
		// given
		Long userGroupId = 1L;
		// when
		var result = userGroupController.retrieveUsersByUserGroup(userGroupId);
		// then
		assertEquals(HttpStatus.OK, result.getStatusCode());
		assertTrue(result.getBody().getSuccessful());
		assertEquals(2, result.getBody().getData().size());
	}

	@Test
	@WithMockUser(username = "normal", roles = {"USER"})
	public void retrieveUserPositions() throws ServiceException {
		// given
		Long eventId = 1L; // WC2014
		Long userGroupId = 1L;
		// when
		var result = userGroupController.retrieveUserPositions(eventId, userGroupId);
		// then
		assertEquals(HttpStatus.OK, result.getStatusCode());
		assertTrue(result.getBody().getSuccessful());
		assertEquals(2, result.getBody().getData().size());
		assertEquals(1, result.getBody().getData().get(0).getPosition());
		assertEquals(2, result.getBody().getData().get(0).getScore());
		assertEquals(2, result.getBody().getData().get(0).getUserId().longValue());
		assertEquals(2, result.getBody().getData().get(1).getPosition());
		assertEquals(0, result.getBody().getData().get(1).getScore());
		assertEquals(6, result.getBody().getData().get(1).getUserId().longValue());
	}
	
	@Test
	@WithMockUser(username = "normal", roles = {"USER"})
	public void insertUserGroup() throws ServiceException {
		// given
		Long eventId = 1L; // WC2014
		Long userId = 2L; // normal
		String name = "Zematix2";
		boolean isInsertConfirmed = false;
		var result = userGroupController.insertUserGroup(eventId, userId, name, isInsertConfirmed);
		assertEquals(HttpStatus.OK, result.getStatusCode());
		assertTrue(result.getBody().getSuccessful());
		assertEquals("Zematix2", result.getBody().getData().getName());
		UserGroup userGroup = commonDao.findEntityById(UserGroup.class, result.getBody().getData().getUserGroupId());
		assertNotNull(userGroup);
	}
	
	@Test
	@WithMockUser(username = "normal", roles = {"USER"})
	public void insertUserGroupWhenExisting() throws ServiceException {
		// given
		Long eventId = 1L; // WC2014
		Long userId = 2L; // normal
		String name = "Zematix";
		boolean isInsertConfirmed = false;
		// when
		ServiceException exception = assertThrows(ServiceException.class, () -> userGroupController.insertUserGroup(eventId, userId, name, isInsertConfirmed));
		// then
		assertEquals(ParameterizedMessageType.ERROR, exception.getOverallType());
		assertTrue(exception.containsMessage("USER_GROUP_NAME_ALREADY_EXIST"));
	}

	@Test
	@WithMockUser(username = "normal", roles = {"USER"})
	public void importUserGroup() throws ServiceException {
		// given
		Long eventId = 2L; // EC2016
		Long userId = 2L; // normal
		String name = "Zematix";
		// when
		var result = userGroupController.importUserGroup(eventId, userId, name);
		// then
		assertEquals(HttpStatus.OK, result.getStatusCode());
		assertTrue(result.getBody().getSuccessful());
		assertEquals("Zematix", result.getBody().getData().getName());
		assertEquals(2, result.getBody().getData().getUsers().size());
		UserGroup userGroup = commonDao.findEntityById(UserGroup.class, result.getBody().getData().getUserGroupId());
		assertNotNull(userGroup);
	}
	
	@Test
	@WithMockUser(username = "normal", roles = {"USER"})
	public void deleteUserGroup() throws ServiceException {
		// given
		Long userGroupId = 1L;
		// when
		var result = userGroupController.deleteUserGroup(userGroupId);
		// then
		assertEquals(HttpStatus.OK, result.getStatusCode());
		assertTrue(result.getBody().getSuccessful());
		UserGroup userGroup = commonDao.findEntityById(UserGroup.class, userGroupId);
		assertNull(userGroup);		
	}
	
	@Transactional
	@Test
	@WithMockUser(username = "normal", roles = {"USER"})
	public void findAndAddUserToUserGroup() throws ServiceException {
		// given
		Long userGroupId = 1L;
		String loginName = "deprecated_sha256";
		String fullName = null;
		// when
		var result = userGroupController.findAndAddUserToUserGroup(userGroupId, loginName, fullName);
		// then
		assertEquals(HttpStatus.OK, result.getStatusCode());
		assertTrue(result.getBody().getSuccessful());
		assertEquals(loginName, result.getBody().getData().getLoginName());
		UserGroup userGroup = commonDao.findEntityById(UserGroup.class, userGroupId);
		assertEquals(3, userGroup.getUsers().size()); // without @Transational we would get LazyInitializationException here
	}
	
	@Transactional
	@Test
	@WithMockUser(username = "normal", roles = {"USER"})
	public void removeUserFromUserGroup() throws ServiceException {
		// given
		Long userGroupId = 1L;
		Long userId = 2L;
		// when
		var result = userGroupController.removeUserFromUserGroup(userGroupId, userId);
		// then
		assertEquals(HttpStatus.OK, result.getStatusCode());
		assertTrue(result.getBody().getSuccessful());
		UserGroup userGroup = commonDao.findEntityById(UserGroup.class, userGroupId);
		assertEquals(1, userGroup.getUsers().size()); // without @Transational we would get LazyInitializationException here
	}
	
	@Test
	@WithMockUser(username = "normal", roles = {"USER"})
	public void retrieveUserCertificates() throws ServiceException {
		// given
		Long eventId = 1L; // WC2014
		Long userId = 2L; // normal
		// when
		var result = userGroupController.retrieveUserCertificates(eventId, userId);
		// then
		assertEquals(HttpStatus.OK, result.getStatusCode());
		assertTrue(result.getBody().getSuccessful());
		assertEquals(2, result.getBody().getData().size());
		assertArrayEquals(new Object[]{ 1L,0L }, result.getBody().getData().stream().map(e -> e.getUserGroupId()).toArray());
	}
	
	@Test
	@WithMockUser(username = "normal", roles = {"USER"})
	public void printUserCertificate() throws ServiceException {
		// given
		UserCertificateExtendedDto userCertificateExtendedDto = new UserCertificateExtendedDto();
		userCertificateExtendedDto.setLanguageTag("hu");
		// when
		var result = userGroupController.printUserCertificate(userCertificateExtendedDto);
		// then
		assertEquals(HttpStatus.OK, result.getStatusCode());
	}
	
	@Test
	@WithMockUser(username = "normal", roles = {"USER"})
	public void retrieveScoresByEventAndUserGroup() throws ServiceException {
		// given
		Long eventId = 1L; // WC2014
		Long userGroupId = 1L;
		// when
		var result = userGroupController.retrieveScoresByEventAndUserGroup(eventId, userGroupId);
		// then
		assertEquals(HttpStatus.OK, result.getStatusCode());
		assertTrue(result.getBody().getSuccessful());
		assertEquals(2, result.getBody().getData().getDatasets().size());
		assertEquals(25, result.getBody().getData().getMatchDates().size());
	}
	
}
