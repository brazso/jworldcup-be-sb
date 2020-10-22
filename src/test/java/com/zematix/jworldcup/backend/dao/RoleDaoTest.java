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

import com.zematix.jworldcup.backend.entity.Role;

/**
 * Contains test functions of {@link RoleDao} class.
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles({"development", "test"})
@Transactional
public class RoleDaoTest {

	@Inject
	private RoleDao roleDao;

	@Inject
	private CommonDao commonDao;

	/**
	 * Test {@link RoleDao#getAllMatchs()} method.
	 * Scenario: successfully retrieves a list of all {@link Role} entities
	 */
	@Test
	public void /*List<Role>*/ getAllRoles() {
		List<Role> allExpectedRoles = commonDao.findAllEntities(Role.class);
		List<Role> allRoles = roleDao.getAllRoles();
		
		// order does not matter
		assertEquals(new HashSet<>(allExpectedRoles), new HashSet<>(allRoles));
	}

	/**
	 * Test {@link RoleDao#findRoleByRole(String)} method.
	 * Scenario: successfully retrieves a {@link Role} instance.
	 */
	@Test
	public void /*Role*/ findRoleByRole(/*String sRole*/) {
		String sRole = "ADMIN";
		Role role = roleDao.findRoleByRole(sRole);
		
		assertTrue(role != null && role.getName().equals("Administrator"));
	}

	/**
	 * Test {@link RoleDao#findRoleByRole(String)} method.
	 * Scenario: retrieves {@code null} value because of mismatched given parameter.
	 */
	@Test
	public void /*Role*/ findRoleByRoleNotFound(/*String sRole*/) {
		String sRole = "ADMIN!";
		Role role = roleDao.findRoleByRole(sRole);
		
		assertNull(role);
	}

	/**
	 * Test {@link RoleDao#findRoleByRole(String)} method.
	 * Scenario: unsuccessfully retrieves a {@link Role} instance because sRole 
	 *           parameter is {@code null}, throws an exception.
	 */
	@Test(expected = IllegalArgumentException.class)
	public void /*Role*/ findRoleByRoleNull(/*String sRole*/) {
		String sRole = null;

		/*Role role =*/ roleDao.findRoleByRole(sRole);
	}
}
