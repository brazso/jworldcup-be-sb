package com.zematix.jworldcup.backend.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import javax.inject.Inject;

import org.json.JSONException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.skyscreamer.jsonassert.JSONAssert;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.Sql.ExecutionPhase;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import com.zematix.jworldcup.backend.TestBase;
import com.zematix.jworldcup.backend.emun.ParameterizedMessageType;
import com.zematix.jworldcup.backend.entity.Match;
import com.zematix.jworldcup.backend.entity.WebService;
import com.zematix.jworldcup.backend.exception.ServiceException;
import com.zematix.jworldcup.backend.mapper.MatchMapper;

/**
 * Contains test functions of {@link WebServiceService} class.
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles({"develop", "test"})
@Transactional
public class WebServiceServiceIT extends TestBase {
	
	@Inject
	private WebServiceService webServiceService;
	
//	@Inject
//	private CommonDao commonDao;
	
//	@MockBean
//	private WebServiceDao webServiceDao; // used by methods of EventService
	
//	@SpyBean // unfortunately partial mock does not work on bean with @ApplicationScope
	@MockBean
	private ApplicationService applicationService;
	
	@Inject
	private MatchMapper matchMapper;
	
	/**
	 * Test {@link WebServiceService#retrieveWebServicesByEvent(Long)} method.
	 * Scenario: successfully retrieves webServices belongs to the event
	 */
	@Test
	public void /*List<WebService>*/ retrieveWebServicesByEventFails(/*Long eventId*/) throws ServiceException {
		// given
		Long eventId = 3L; // CA2016
		// when
		ServiceException exception = assertThrows(ServiceException.class, () -> webServiceService.retrieveWebServicesByEvent(eventId));
		// then
		assertEquals(ParameterizedMessageType.WARNING, exception.getOverallType());
		assertTrue(exception.containsMessage("NO_ACTIVE_WEBSERVICE_FOR_EVENT"));

	}
	
	/**
	 * Test {@link WebServiceService#retrieveWebServicesByEvent(Long)} method.
	 * Scenario: successfully retrieves webServices belongs to the event
	 */
	@Test
	public void /*List<WebService>*/ retrieveWebServicesByEvent(/*Long eventId*/) throws ServiceException {
		// given
		Long eventId = 1L; // WC2014
		// when
		List<WebService> webServices = webServiceService.retrieveWebServicesByEvent(eventId);
		// then
		assertEquals(3, webServices.size());
	}
	
	/**
	 * Test {@link WebServiceService#updateMatchResults(Long)} method.
	 * Scenario: successfully updates incomplete but escalated matches from calling web service.
	 * @throws JSONException 
	 */
	@Test
	@Sql(scripts = { "/database/service/webservice-service-before.sql" }, executionPhase = ExecutionPhase.BEFORE_TEST_METHOD)
	@Sql(scripts = { "/database/service/webservice-service-after.sql" }, executionPhase = ExecutionPhase.AFTER_TEST_METHOD)
	public void /*List<Match>*/ updateMatchResultsForWC2014(/*Long eventId*/) throws ServiceException, JSONException {
		// given
		Long eventId = 1L; // WC2014
		LocalDateTime actualDateTime = LocalDateTime.parse("3000-01-01 00:00:00", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
		String expectedJsonResult = readStringResource("service/webserviceservice/WC2014-matches.json");
		// when
		Mockito.when(applicationService.getActualDateTime()).thenReturn(actualDateTime);
		List<Match> matches = webServiceService.updateMatchResults(eventId);
		// then
		String jsonResult = generateJson(matchMapper.entityListToDtoList(matches));
		JSONAssert.assertEquals(expectedJsonResult, jsonResult, false);
	}

	@Test
	@Sql(scripts = { "/database/service/webservice-service-before.sql" }, executionPhase = ExecutionPhase.BEFORE_TEST_METHOD)
	@Sql(scripts = { "/database/service/webservice-service-after.sql" }, executionPhase = ExecutionPhase.AFTER_TEST_METHOD)
	public void /*List<Match>*/ updateMatchResultsForEC2016(/*Long eventId*/) throws ServiceException, JSONException {
		// given
		Long eventId = 2L; // EC2016
		LocalDateTime actualDateTime = LocalDateTime.parse("3000-01-01 00:00:00", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
		String expectedJsonResult = readStringResource("service/webserviceservice/EC2016-matches.json");
		// when
		Mockito.when(applicationService.getActualDateTime()).thenReturn(actualDateTime);
		List<Match> matches = webServiceService.updateMatchResults(eventId);
		// then
		String jsonResult = generateJson(matchMapper.entityListToDtoList(matches));
		JSONAssert.assertEquals(expectedJsonResult, jsonResult, false);
	}

//	/**
//	 * Test {@link WebServiceService#updateMatchResults(Long)} method.
//	 * Scenario: successfully updates incomplete but escalated matches from calling web service.
//	 */
//	@Test
//	@Sql(scripts = { "/database/service/webservice-service-before.sql" }, executionPhase = ExecutionPhase.BEFORE_TEST_METHOD)
//	@Sql(scripts = { "/database/service/webservice-service-after.sql" }, executionPhase = ExecutionPhase.AFTER_TEST_METHOD)
//	public void /*List<Match>*/ updateMatchResultsAfterGroupMatches(/*Long eventId*/) throws ServiceException {
//		// given
//		Long eventId = 1L; // WC2014
//		LocalDateTime actualDateTime = LocalDateTime.parse("2014-06-28 00:00:00", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
//		// when
//		Mockito.when(applicationService.getActualDateTime()).thenReturn(actualDateTime);
//		List<Match> matches = webServiceService.updateMatchResults(eventId);
//		// then
//		assertEquals(56, matches.size());
//	}
}
