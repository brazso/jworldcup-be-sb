package com.zematix.jworldcup.backend.dao;

import static org.junit.Assert.assertEquals;
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

import com.zematix.jworldcup.backend.entity.Event;
import com.zematix.jworldcup.backend.entity.WebService;

/**
 * Contains test functions of {@link UserStatusDao} class.
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles({"development", "test"})
@Transactional
public class WebServiceDaoTest {

	@Inject
	private WebServiceDao webServiceDao;

	@Inject
	private CommonDao commonDao;

	/**
	 * Test {@link WebServiceDao#getAllWebServices()} method.
	 * Scenario: successfully retrieves a list of all {@link WebService} entities
	 */
	@Test
	public void /*List<WebService>*/ getAllWebServices() {
		List<WebService> allExpectedWebServices = commonDao.findAllEntities(WebService.class);
		List<WebService> allWebServices = webServiceDao.getAllWebServices();
		
		// order does not matter
		assertEquals(new HashSet<>(allExpectedWebServices), new HashSet<>(allWebServices));
	}
	
	/**
	 * Test {@link WebServiceDao#retrieveWebServicesByEvent(Long)} method.
	 * Scenario: successfully retrieves a list of {@link WebService} entities belong to the given {@link Event#eventId}
	 */
	@Test
	public void /*List<WebService>*/ retrieveWebServicesByEvent(/*Long eventId*/) {
		Long eventId = 1L; // WC2014
		List<Long> expectedWebServiceIds = Arrays.asList(1L, 2L, 3L);
		List<WebService> webServices = webServiceDao.retrieveWebServicesByEvent(eventId);
		List<Long> webServiceIds = webServices.stream().map(e -> e.getWebServiceId()).toList();
		assertEquals(new HashSet<>(expectedWebServiceIds), new HashSet<>(webServiceIds));
	}

	/**
	 * Test {@link WebServiceDao#retrieveWebServicesByEvent(Long)} method.
	 * Scenario: Because the given {@link Event#eventId} is {@code null} {@link NullPointerException} is thrown.
	 */
	@Test(expected = NullPointerException.class)
	public void /*List<WebService>*/ retrieveWebServicesByEventNull(/*Long eventId*/) {
		Long eventId = null;

		webServiceDao.retrieveWebServicesByEvent(eventId);
	}

	/**
	 * Test {@link WebServiceDao#retrieveWebServicesByEvent(Long)} method.
	 * Scenario: Because given {@link Event#eventId is unknown it returns {@code null}.
	 */
	@Test
	public void /*List<WebService>*/ retrieveWebServicesByEventUnknown(/*Long eventId*/) {
		Long eventId = -1L;
		List<WebService> webServices = webServiceDao.retrieveWebServicesByEvent(eventId);
		assertTrue(webServices != null && webServices.isEmpty());
	}
}
