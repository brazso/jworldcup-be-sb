package com.zematix.jworldcup.backend.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.HashSet;
import java.util.List;

import javax.inject.Inject;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import com.zematix.jworldcup.backend.entity.UserStatus;

/**
 * Contains test functions of {@link UserStatusDao} class.
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles({"development", "test"})
@Transactional
public class UserStatusDaoTest {

	@Inject
	private UserStatusDao userStatusDao;

	@Inject
	private CommonDao commonDao;

	/**
	 * Test {@link UserStatusDao#getAllUserStatuses()} method.
	 * Scenario: successfully retrieves a list of all {@link UserStatus} entities
	 */
	@Test
	public void /*List<UserStatus>*/ getAllUserStatuses() {
		List<UserStatus> allExpectedUserStatuses = commonDao.findAllEntities(UserStatus.class);
		List<UserStatus> allUserStatuses = userStatusDao.getAllUserStatuses();
		
		// order does not matter
		assertEquals(new HashSet<>(allExpectedUserStatuses), new HashSet<>(allUserStatuses));
	}

	/**
	 * Test {@link UserStatusDao#findUserStatusByStatus(String)} method.
	 * Scenario: successfully retrieves a {@link UserStatus} instance.
	 */
	@Test
	public void /*UserStatus*/ findUserStatusByStatus(/*String status*/) {
		String status = "NORMAL";
		UserStatus userStatus = userStatusDao.findUserStatusByStatus(status);
		
		assertTrue(userStatus != null && userStatus.getName().equals("Normal"));
	}

	/**
	 * Test {@link UserStatusDao#findUserStatusByStatus(String)} method.
	 * Scenario: retrieves {@code null} value because of mismatched given parameter.
	 */
	@Test
	public void /*UserStatus*/ findUserStatusByStatusNotFound(/*String status*/) {
		String status = "NORMAL!";
		UserStatus userStatus = userStatusDao.findUserStatusByStatus(status);
		
		assertNull(userStatus);
	}

	/**
	 * Test {@link UserStatusDao#findUserStatusByStatus(String)} method.
	 * Scenario: unsuccessfully retrieves a {@link UserStatus} instance because status 
	 *           parameter is {@code null}, throws an exception.
	 */
	@Test(expected = IllegalArgumentException.class)
	public void /*UserStatus*/ findUserStatusByStatusNull(/*String status*/) {
		String status = null;

		/*UserStatus userStatus =*/ userStatusDao.findUserStatusByStatus(status);
	}
}
