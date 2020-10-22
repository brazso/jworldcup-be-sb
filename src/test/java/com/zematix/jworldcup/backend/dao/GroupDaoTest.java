package com.zematix.jworldcup.backend.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import com.zematix.jworldcup.backend.entity.Group;

/**
 * Contains test functions of {@link GroupDao} class.
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles({"development", "test"})
@Transactional
public class GroupDaoTest {

	@Inject
	private GroupDao groupDao;

	@Inject
	private CommonDao commonDao;

	/**
	 * Test {@link GroupDao#getAllGroups()} method.
	 * Scenario: successfully retrieves a list of all Group entities
	 */
	@Test
	public void /*List<Group>*/ getAllGroups() {
		List<Group> allExpectedGroups = commonDao.findAllEntities(Group.class);
		List<Group> allGroups = groupDao.getAllGroups();
		
		// order does not matter
		assertEquals(new HashSet<>(allExpectedGroups), new HashSet<>(allGroups));
	}
	
	/**
	 * Test {@link GroupDao#retrieveGroupsByEvent(Long)} method.
	 * Scenario: successfully retrieves the groups belongs to the given event.
	 *           The result list is sorted by group name.
	 */
	@Test
	public void /*List<Group>*/ retrieveGroupsByEvent(/*Long eventId*/) {
		Long eventId = 1L; // WC2014
		List<Long> expectedGroupIds = Arrays.asList(1L, 2L, 3L, 4L, 5L, 6L, 7L, 8L);
		List<Group> groups = groupDao.retrieveGroupsByEvent(eventId);
		List<Long> groupIds = groups.stream().map(e -> e.getGroupId()).collect(Collectors.toList());
		
		assertEquals(expectedGroupIds, groupIds);
	}
	
	/**
	 * Test {@link GroupDao#retrieveGroupsByEvent(Long)} method.
	 * Scenario: unsuccessfully retrieves list because of the given null parameter,
	 *           instead it throws {@link IllegalArgumentException}.
	 */
	@Test(expected = IllegalArgumentException.class)
	public void /*List<Group>*/ retrieveGroupsByEventNull(/*Long eventId*/) {
		Long eventId = null;
		
		/*List<Group> groups =*/ groupDao.retrieveGroupsByEvent(eventId);
	}

	/**
	 * Test {@link GroupDao#retrieveGroupsByEvent(Long)} method.
	 * Scenario: successfully retrieves empty list because the given eventId is unknown.
	 */
	@Test
	public void /*List<Group>*/ retrieveGroupsByEventUnknown(/*Long eventId*/) {
		Long eventId = -1L; // non existing
		List<Group> groups = groupDao.retrieveGroupsByEvent(eventId);
		
		assertTrue(groups.isEmpty());
	}

	/**
	 * Test {@link GroupDao#retrieveGroupByName(Long, String)} method.
	 * Scenario: successfully retrieves a group belongs to the given parameters.
	 */
	@Test
	public void /*Group*/ retrieveGroupByName(/*Long eventId, String name*/) {
		Long eventId = 1L; // WC2014
		String name = "A";
		Long expectedGroupId = 1L;
		
		Group group = groupDao.retrieveGroupByName(eventId, name);
		
		assertEquals(expectedGroupId, group.getGroupId());
	}
	
	/**
	 * Test {@link GroupDao#retrieveGroupByName(Long, String)} method.
	 * Scenario: unsuccessfully retrieves group because of the given {@code null} parameters,
	 *           instead it throws {@link IllegalArgumentException}.
	 */
	@Test(expected = IllegalArgumentException.class)
	public void /*Group*/ retrieveGroupByNameNull(/*Long eventId, String name*/) {
		Long eventId = null;
		String name = null;
		
		/*Group group =*/ groupDao.retrieveGroupByName(eventId, name);
	}

	/**
	 * Test {@link GroupDao#retrieveGroupByName(Long)} method.
	 * Scenario: successfully retrieves {@code null} because the given parameters are unknown.
	 */
	@Test
	public void /*Group*/ retrieveGroupByNameUnknown(/*Long eventId, String name*/) {
		Long eventId = -1L; // non existing
		String name = "@"; // non existing
		Group group = groupDao.retrieveGroupByName(eventId, name);
		
		assertNull(group);
	}
}
