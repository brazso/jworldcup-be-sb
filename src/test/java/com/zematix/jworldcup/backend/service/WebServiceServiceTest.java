package com.zematix.jworldcup.backend.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import javax.inject.Inject;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import com.msiggi.openligadb.client.ArrayOfMatchResult;
import com.msiggi.openligadb.client.MatchResult;
import com.msiggi.openligadb.client.Matchdata;
import com.zematix.jworldcup.backend.dao.CommonDao;
import com.zematix.jworldcup.backend.dao.WebServiceDao;
import com.zematix.jworldcup.backend.entity.Match;
import com.zematix.jworldcup.backend.entity.WebService;
import com.zematix.jworldcup.backend.exception.OpenLigaDBException;
import com.zematix.jworldcup.backend.exception.ServiceException;
import com.zematix.jworldcup.backend.util.CommonUtil;

/**
 * Contains test functions of {@link WebServiceService} class.
 */

@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles({"development", "test"})
@Transactional
public class WebServiceServiceTest {
	private static final Comparator<Matchdata> byMatchId = (m1, m2) -> Integer.compare(m1.getMatchID(), m2.getMatchID());

	@Inject
	private WebServiceService webServiceService;
	
	@Inject 
	private CommonDao commonDao;

	@MockBean
	private WebServiceDao webServiceDao; // used by some methods inside UserService
	
	@MockBean
	private ApplicationService applicationService;

	@MockBean
	private MatchService matchService;
	
	@MockBean
	private OpenLigaDBService openLigaDBService;
	
	@SpyBean
	private WebServiceService webServiceServicePartial;

	/**
	 * Test {@link WebServiceService#findMatchInMatchdatas(List<Matchdata>, Match)} private(!) method.
	 * Scenario: throws {@link NullPointerException} because of the given {@code null} matchdatas 
	 *           parameter 
	 */
	@Test(expected=NullPointerException.class)
	public /*private*/ void /*Matchdata*/ findMatchInMatchdatas_NullMatchdatas(/*List<Matchdata> matchdatas, Match match*/) {
		List<Matchdata> matchdatas = null;
		Match match = new Match();
		webServiceService.findMatchInMatchdatas(matchdatas, match);
	}

	/**
	 * Test {@link WebServiceService#findMatchInMatchdatas(List<Matchdata>, Match)} private(!) method.
	 * Scenario: throws {@link NullPointerException} because of the given {@code null} match 
	 *           parameter 
	 */
	@Test(expected=NullPointerException.class)
	public /*private*/ void /*Matchdata*/ findMatchInMatchdatas_NullMatch(/*List<Matchdata> matchdatas, Match match*/) {
		List<Matchdata> matchdatas = new ArrayList<>();
		Match match = null;
		webServiceService.findMatchInMatchdatas(matchdatas, match);
	}

	/**
	 * Test {@link WebServiceService#findMatchInMatchdatas(List<Matchdata>, Match)} private(!) method.
	 * Scenario: retrieved matchdata is {@code null} because the sought {@code match} is not in the given empty 
	 *           {@code matchdatas} list.
	 */
	@Test
	public /*private*/ void /*Matchdata*/ findMatchInMatchdatas_EmptyMatchdatas(/*List<Matchdata> matchdatas, Match match*/) {
		List<Matchdata> matchdatas = new ArrayList<>();
		Match match = new Match();
		//Matchdata expectedMatchdata = new Matchdata();
		Matchdata matchdata = webServiceService.findMatchInMatchdatas(matchdatas, match);
		assertNull("Retrieved matchdata should be null because the sought match is not in the given list.", matchdata);
	}
	
	/**
	 * Test {@link WebServiceService#findMatchInMatchdatas(List<Matchdata>, Match)} private(!) method.
	 * Scenario: successfully retrieves result belongs to the given {@code match} where the latter has
	 *           no participant teams.
	 */
	@Test
	public /*private*/ void /*Matchdata*/ findMatchInMatchdatas_MissingTeams(/*List<Matchdata> matchdatas, Match match*/) {
		Match match = commonDao.findEntityById(Match.class, 49L); // WC2014 first (knockout) match without participant teams
		Matchdata matchdata = new Matchdata();
		matchdata.setMatchID(match.getMatchId().intValue());
		matchdata.setMatchDateTimeUTC(CommonUtil.toXMLGregorianCalendar(match.getStartTime()));
		List<Matchdata> matchdatas = Arrays.asList(matchdata);
		Matchdata expectedMatchdata = matchdata;
		
		/*Matchdata*/ matchdata = webServiceService.findMatchInMatchdatas(matchdatas, match);
		assertTrue("Retrieved matchdata should be equal to the expected one.", matchdata!=null && byMatchId.compare(expectedMatchdata, matchdata)==0);
	}

	/**
	 * Test {@link WebServiceService#findMatchInMatchdatas(List<Matchdata>, Match)} private(!) method.
	 * Scenario: successfully retrieves result belongs to the given {@code match} where the latter has
	 *           participant teams.
	 */
	@Test
	public /*private*/ void /*Matchdata*/ findMatchInMatchdatas_ExistingTeams(/*List<Matchdata> matchdatas, Match match*/) {
		Match match = commonDao.findEntityById(Match.class, 1L); // WC2014 first (group stage) match
		Matchdata matchdata = new Matchdata();
		matchdata.setMatchID(match.getMatchId().intValue());
		matchdata.setMatchDateTimeUTC(CommonUtil.toXMLGregorianCalendar(match.getStartTime()));
		matchdata.setIdTeam1(match.getTeam1().getWsId().intValue());
		matchdata.setIdTeam2(match.getTeam2().getWsId().intValue());
		List<Matchdata> matchdatas = Arrays.asList(matchdata);
		Matchdata expectedMatchdata = matchdata;
		
		/*Matchdata*/ matchdata = webServiceService.findMatchInMatchdatas(matchdatas, match);
		assertTrue("Retrieved matchdata should be equal to the expected one.", matchdata!=null && byMatchId.compare(expectedMatchdata, matchdata)==0);
	}
	
	/**
	 * Test {@link WebServiceService#updateMatchResults(Long} method.
	 * Scenario: throws {@link NullPointerException} because of the given {@code null} eventId 
	 *           parameter 
	 */
	@Test(expected=NullPointerException.class)
	public void /*long*/ updateMatchResults_NullEventId(/*Long eventId*/) throws ServiceException {
		Long eventId = null;
		
		webServiceService.updateMatchResults(eventId);
	}
	
	/**
	 * Test {@link WebServiceService#updateMatchResults(Long} method.
	 * Scenario: throws {@link ServiceException} because there is no active row in WebService table 
	 */
	@Test(expected=ServiceException.class)
	public void /*long*/ updateMatchResults_NoActiveWebServiceConfiguration(/*Long eventId*/) throws ServiceException {
		Long eventId = 1L; // WC2014
		
		LocalDateTime actualDateTime = LocalDateTime.now();
		Mockito.when(applicationService.getActualDateTime()).thenReturn(actualDateTime);
		
//		WebService webService = commonDao.findEntityById(WebService.class, 1L); // WM-2014
//		List<WebService> expectedWebServices = Arrays.asList(webService);
		List<WebService> emptyWebServices = new ArrayList<>();
		Mockito.when(webServiceDao.retrieveWebServicesByEvent(eventId)).thenReturn(emptyWebServices);

		try {
			webServiceService.updateMatchResults(eventId);
		}
		catch (ServiceException e) {
			assertTrue("There must be a single message in ServiceException named NO_ACTIVE_WEBSERVICE_FOR_EVENT", 
					e.getMessages().size()==1 && e.getMessages().get(0).getMsgCode().equals("NO_ACTIVE_WEBSERVICE_FOR_EVENT"));
			throw e;
		}
	}
	
	/**
	 * Test {@link WebServiceService#updateMatchResults(Long} method.
	 * Scenario: throws {@link ServiceException} because the web service server is down 
	 */
	@Test(expected=ServiceException.class)
	public void /*long*/ updateMatchResults_NoFunctionalWebServiceServer(/*Long eventId*/) throws ServiceException, OpenLigaDBException {
		Long eventId = 1L; // WC2014
		
		LocalDateTime actualDateTime = LocalDateTime.now();
		Mockito.when(applicationService.getActualDateTime()).thenReturn(actualDateTime);
		
		WebService webService = commonDao.findEntityById(WebService.class, 1L); // WM-2014
		List<WebService> expectedWebServices = Arrays.asList(webService);
		Mockito.when(webServiceDao.retrieveWebServicesByEvent(eventId)).thenReturn(expectedWebServices);

		Mockito.when(openLigaDBService.getMatchdataByLeagueSaison(webService.getLeagueShortcut(),
				webService.getLeagueSaison())).thenThrow(new OpenLigaDBException("Dummy mesage"));
		
		try {
			webServiceService.updateMatchResults(eventId);
		}
		catch (ServiceException e) {
			assertTrue("There must be a single message in ServiceException named WEBSERVICE_CALL_FAILED_FOR_METHOD", 
					e.getMessages().size()==1 && e.getMessages().get(0).getMsgCode().equals("WEBSERVICE_CALL_FAILED_FOR_METHOD"));
			throw e;
		}
	}
	
	/**
	 * Test {@link WebServiceService#updateMatchResults(Long} method.
	 * Scenario: no updates because there is no escalated incomplete matches 
	 *           belongs to the eventId
	 */
	@Test
	public void /*long*/ updateMatchResults_EmptyIncompleteEscalatedMatches(/*Long eventId*/) throws ServiceException, OpenLigaDBException {
		Long eventId = 1L; // WC2014
		
		LocalDateTime actualDateTime = LocalDateTime.now();
		Mockito.when(applicationService.getActualDateTime()).thenReturn(actualDateTime);
		
		WebService webService = commonDao.findEntityById(WebService.class, 1L); // WM-2014
		List<WebService> expectedWebServices = Arrays.asList(webService);
		Mockito.when(webServiceDao.retrieveWebServicesByEvent(eventId)).thenReturn(expectedWebServices);

		List<Matchdata> matchdatas = new ArrayList<>();
		Mockito.when(openLigaDBService.getMatchdataByLeagueSaison(webService.getLeagueShortcut(),
				webService.getLeagueSaison())).thenReturn(matchdatas);
		
		List<Match> matches = new ArrayList<>(); // empty on purpose 
		Mockito.when(matchService.retrieveIncompleteEscalatedMatchesByEvent(eventId, actualDateTime)).thenReturn(matches);
		
		long expectedUpdatedMatches = 0;
		long updatedMatches = webServiceService.updateMatchResults(eventId);
		assertEquals("Result updatedMatches counter should be equal to the expected one", expectedUpdatedMatches, updatedMatches);
	}
	
	/**
	 * Test {@link WebServiceService#updateMatchResults(Long} method.
	 * Scenario: successfully updates matches belongs to the eventId
	 */
	@Test
	public void /*long*/ updateMatchResults(/*Long eventId*/) throws ServiceException, OpenLigaDBException {
		Long eventId = 1L; // WC2014
		
		Match match = commonDao.findEntityById(Match.class, 1L); // WC2014 first (group stage) match
		
		LocalDateTime actualDateTime = LocalDateTime.now();
		Mockito.when(applicationService.getActualDateTime()).thenReturn(actualDateTime);
		
		WebService webService = commonDao.findEntityById(WebService.class, 1L); // WM-2014
		List<WebService> expectedWebServices = Arrays.asList(webService);
		Mockito.when(webServiceDao.retrieveWebServicesByEvent(eventId)).thenReturn(expectedWebServices);

		List<Matchdata> matchdatas = new ArrayList<>();
		Mockito.when(openLigaDBService.getMatchdataByLeagueSaison(webService.getLeagueShortcut(),
				webService.getLeagueSaison())).thenReturn(matchdatas);
		
		List<Match> matches = Arrays.asList(match); 
		Mockito.when(matchService.retrieveIncompleteEscalatedMatchesByEvent(eventId, actualDateTime)).thenReturn(matches);
		
		Matchdata matchdata = new Matchdata();
		//matchdata.setMatchID(match.getMatchId().intValue());
		matchdata.setMatchIsFinished(true);

		Mockito.doReturn(matchdata).when(webServiceServicePartial).findMatchInMatchdatas(matchdatas, match);

		boolean isUpdated = true;
		Mockito.doReturn(isUpdated).when(webServiceServicePartial).updateMatchByMatchdata(match, matchdata, webService);
		
		long expectedUpdatedMatches = 1;
		long updatedMatches = webServiceServicePartial.updateMatchResults(eventId);
		assertEquals("Result updatedMatches counter should be equal to the expected one", expectedUpdatedMatches, updatedMatches);
	}
	
	/**
	 * Test {@link WebServiceService#updateMatchByMatchdata(Match, Matchdata, WebService)} private(!) method.
	 * Scenario: throws {@link NullPointerException} because of the given {@code null} match 
	 *           parameter 
	 */
	@Test(expected=NullPointerException.class)
	public /*private*/ void /*boolean*/ updateMatchByMatchdata_NullMatch(/*Match match, Matchdata matchdata, WebService webService*/) throws ServiceException {
		Match match = null;
		Matchdata matchdata = new Matchdata();
		WebService webService = new WebService();
		webServiceService.updateMatchByMatchdata(match, matchdata, webService);
	}

	/**
	 * Test {@link WebServiceService#updateMatchByMatchdata(Match, Matchdata, WebService)} private(!) method.
	 * Scenario: throws {@link NullPointerException} because of the given {@code null} matchdata 
	 *           parameter 
	 */
	@Test(expected=NullPointerException.class)
	public /*private*/ void /*boolean*/ updateMatchByMatchdata_NullMatchdata(/*Match match, Matchdata matchdata, WebService webService*/) throws ServiceException {
		Match match = new Match();
		Matchdata matchdata = null;
		WebService webService = new WebService();
		webServiceService.updateMatchByMatchdata(match, matchdata, webService);
	}

	/**
	 * Test {@link WebServiceService#updateMatchByMatchdata(Match, Matchdata, WebService)} private(!) method.
	 * Scenario: throws {@link NullPointerException} because of the given {@code null} webService 
	 *           parameter 
	 */
	@Test(expected=NullPointerException.class)
	public /*private*/ void /*boolean*/ updateMatchByMatchdata_NullWebService(/*Match match, Matchdata matchdata, WebService webService*/) throws ServiceException {
		Match match = new Match();
		Matchdata matchdata = new Matchdata();
		WebService webService = null;
		webServiceService.updateMatchByMatchdata(match, matchdata, webService);
	}

	/**
	 * Test {@link WebServiceService#updateMatchByMatchdata(Match, Matchdata, WebService)} private(!) method.
	 * Scenario: returns false because the teams in matchdata are not the same as in match
	 */
	@Test
	public /*private*/ void /*boolean*/ updateMatchByMatchdata_WrongTeams(/*Match match, Matchdata matchdata, WebService webService*/) throws ServiceException {
		Match match = commonDao.findEntityById(Match.class, 1L); // WC2014 first (group stage) match
		Match otherMatch = commonDao.findEntityById(Match.class, 2L); // WC2014 second (group stage) match
		Matchdata matchdata = new Matchdata();
		matchdata.setMatchID(match.getMatchId().intValue());
		matchdata.setMatchDateTimeUTC(CommonUtil.toXMLGregorianCalendar(match.getStartTime()));
		matchdata.setIdTeam1(otherMatch.getTeam1().getWsId().intValue());
		matchdata.setIdTeam2(otherMatch.getTeam2().getWsId().intValue());
		WebService webService = new WebService();

		// isNotReversedTeams
		Mockito.when(matchService.isCandidateMatchTeam(match, matchdata.getIdTeam1(), 1)).thenReturn(false);
		Mockito.when(matchService.isCandidateMatchTeam(match, matchdata.getIdTeam2(), 2)).thenReturn(false);
		// isReversedTeams
		Mockito.when(matchService.isCandidateMatchTeam(match, matchdata.getIdTeam1(), 2)).thenReturn(false);
		Mockito.when(matchService.isCandidateMatchTeam(match, matchdata.getIdTeam2(), 1)).thenReturn(false);
		
		boolean expectedIsUpdated = false;
		boolean isUpdated = webServiceService.updateMatchByMatchdata(match, matchdata, webService);
		assertEquals("Result updated flag should be equal to the expected one", expectedIsUpdated, isUpdated);
	}

	/**
	 * Test {@link WebServiceService#updateMatchByMatchdata(Match, Matchdata, WebService)} private(!) method.
	 * Scenario: sucessfully updates group-stage match with matchdata despite the teams in matchdata 
	 *           are in reverse order compared to match
	 */
	@Test
	public /*private*/ void /*boolean*/ updateMatchByMatchdata_TeamsInReverseOrder_GroupStage(/*Match match, Matchdata matchdata, WebService webService*/) throws ServiceException {
		Match match = commonDao.findEntityById(Match.class, 1L); // WC2014 first (group stage) match
		assertNotNull(match.getTeam1());
		assertNotNull(match.getTeam2());
		Matchdata matchdata = new Matchdata();
		WebService webService = commonDao.findEntityById(WebService.class, 1L); // WM-2014
		matchdata.setMatchID(match.getMatchId().intValue());
		matchdata.setMatchDateTimeUTC(CommonUtil.toXMLGregorianCalendar(match.getStartTime()));
		final Long team1WsId = null;
		final Long team2WsId = null;
		matchdata.setIdTeam1(match.getTeam2().getWsId().intValue()); // in reverse order on purpose
		matchdata.setIdTeam2(match.getTeam1().getWsId().intValue()); // in reverse order on purpose
		ArrayOfMatchResult aomr = new ArrayOfMatchResult();
		MatchResult matchResult = new MatchResult();
		matchResult.setResultName(webService.getResultNormalLabel());
		
		final Byte goalNormal1 = (byte)1;
		final Byte goalNormal2 = (byte)0;
		matchResult.setPointsTeam1(goalNormal2); // in reverse order on purpose
		matchResult.setPointsTeam2(goalNormal1); // in reverse order on purpose
		aomr.getMatchResult().add(matchResult);
		
		final Byte goalExtra1 = null;
		final Byte goalExtra2 = null;
		final Byte goalPenalty1 = null;
		final Byte goalPenalty2 = null;

		matchdata.setMatchResults(aomr);

		// isNotReversedTeams
		Mockito.when(matchService.isCandidateMatchTeam(match, matchdata.getIdTeam1(), 1)).thenReturn(false);
		Mockito.when(matchService.isCandidateMatchTeam(match, matchdata.getIdTeam2(), 2)).thenReturn(false);
		// isReversedTeams
		Mockito.when(matchService.isCandidateMatchTeam(match, matchdata.getIdTeam1(), 2)).thenReturn(true);
		Mockito.when(matchService.isCandidateMatchTeam(match, matchdata.getIdTeam2(), 1)).thenReturn(true);
		
		Mockito.when(matchService.updateMatchByMatchdata(match.getMatchId(), 
				team1WsId, team2WsId, goalNormal1, goalExtra1, goalPenalty1, 
				goalNormal2, goalExtra2, goalPenalty2)).thenReturn(match);
		
		boolean expectedIsUpdated = true;
		boolean isUpdated = webServiceService.updateMatchByMatchdata(match, matchdata, webService);
		assertEquals("Result updated flag should be equal to the expected one", expectedIsUpdated, isUpdated);
		
		Mockito.verify(matchService).updateMatchByMatchdata(match.getMatchId(), 
				team1WsId, team2WsId, goalNormal1, goalExtra1, goalPenalty1, 
				goalNormal2, goalExtra2, goalPenalty2);
	}

	/**
	 * Test {@link WebServiceService#updateMatchByMatchdata(Match, Matchdata, WebService)} private(!) method.
	 * Scenario: sucessfully updates knock-out match with matchdata

	 */
	@Test
	public /*private*/ void /*boolean*/ updateMatchByMatchdata_TeamsInCorrectOrder_KnockoutStage(/*Match match, Matchdata matchdata, WebService webService*/) throws ServiceException {
		Match match = commonDao.findEntityById(Match.class, 49L); // WC2014 first (knockout) match without participant teams
		assertNull(match.getTeam1());
		assertNull(match.getTeam2());
		Matchdata matchdata = new Matchdata();
		WebService webService = commonDao.findEntityById(WebService.class, 1L); // WM-2014
		matchdata.setMatchID(match.getMatchId().intValue());
		matchdata.setMatchDateTimeUTC(CommonUtil.toXMLGregorianCalendar(match.getStartTime()));
		final Long team1WsId = 753L; // Brazil
		final Long team2WsId = 760L; // Chile
		matchdata.setIdTeam1(team1WsId.intValue());
		matchdata.setIdTeam2(team2WsId.intValue());
		ArrayOfMatchResult aomr = new ArrayOfMatchResult();
		
		MatchResult matchResult = new MatchResult();
		matchResult.setResultName(webService.getResultNormalLabel());
		final Byte goalNormal1 = (byte)1;
		final Byte goalNormal2 = (byte)1;
		matchResult.setPointsTeam1(goalNormal1);
		matchResult.setPointsTeam2(goalNormal2);
		aomr.getMatchResult().add(matchResult);
		
		/*MatchResult*/ matchResult = new MatchResult();
		matchResult.setResultName(webService.getResultExtraLabel());
		final Byte goalExtra1 = (byte)2;
		final Byte goalExtra2 = (byte)2;
		matchResult.setPointsTeam1(goalExtra1);
		matchResult.setPointsTeam2(goalExtra2);
		aomr.getMatchResult().add(matchResult);
		
		/*MatchResult*/ matchResult = new MatchResult();
		matchResult.setResultName(webService.getResultPenaltyLabel());
		final Byte goalPenalty1 = (byte)7;
		final Byte goalPenalty2 = (byte)8;
		matchResult.setPointsTeam1(goalPenalty1);
		matchResult.setPointsTeam2(goalPenalty2);
		aomr.getMatchResult().add(matchResult);
		
		matchdata.setMatchResults(aomr);

		// isNotReversedTeams
		Mockito.when(matchService.isCandidateMatchTeam(match, matchdata.getIdTeam1(), 1)).thenReturn(true);
		Mockito.when(matchService.isCandidateMatchTeam(match, matchdata.getIdTeam2(), 2)).thenReturn(true);
		// isReversedTeams
		Mockito.when(matchService.isCandidateMatchTeam(match, matchdata.getIdTeam1(), 2)).thenReturn(false);
		Mockito.when(matchService.isCandidateMatchTeam(match, matchdata.getIdTeam2(), 1)).thenReturn(false);
		
		Mockito.when(matchService.updateMatchByMatchdata(match.getMatchId(), 
				team1WsId, team2WsId, goalNormal1, goalExtra1, goalPenalty1, 
				goalNormal2, goalExtra2, goalPenalty2)).thenReturn(match);
		
		boolean expectedIsUpdated = true;
		boolean isUpdated = webServiceService.updateMatchByMatchdata(match, matchdata, webService);
		assertEquals("Result updated flag should be equal to the expected one", expectedIsUpdated, isUpdated);
		
		Mockito.verify(matchService).updateMatchByMatchdata(match.getMatchId(), 
				team1WsId, team2WsId, goalNormal1, goalExtra1, goalPenalty1, 
				goalNormal2, goalExtra2, goalPenalty2);
	}

	/**
	 * Test {@link WebServiceService#updateMatchByMatchdata(Match, Matchdata, WebService)} private(!) method.
	 * Scenario: sucessfully updates knock-out match with matchdata where matchdata contains 
	 *           both NormalExtraLabel and ExtraLabel as resultNames

	 */
	@Test
	public /*private*/ void /*boolean*/ updateMatchByMatchdata_TeamsInCorrectOrder_KnockoutStage_HasNormalExtraLabel_HasExtraLabel(/*Match match, Matchdata matchdata, WebService webService*/) throws ServiceException {
		Match match = commonDao.findEntityById(Match.class, 49L); // WC2014 first (knockout) match without participant teams
		assertNull(match.getTeam1());
		assertNull(match.getTeam2());
		Matchdata matchdata = new Matchdata();
		WebService webService = commonDao.findEntityById(WebService.class, 3L); // fifa2014
		matchdata.setMatchID(match.getMatchId().intValue());
		matchdata.setMatchDateTimeUTC(CommonUtil.toXMLGregorianCalendar(match.getStartTime()));
		final Long team1WsId = 753L; // Brazil
		final Long team2WsId = 760L; // Chile
		matchdata.setIdTeam1(team1WsId.intValue());
		matchdata.setIdTeam2(team2WsId.intValue());
		ArrayOfMatchResult aomr = new ArrayOfMatchResult();
		
		MatchResult matchResult = new MatchResult(); // OK
		matchResult.setResultName(webService.getResultNormalExtraLabel());
		final Byte goalNormal1 = (byte)1;
		final Byte goalNormal2 = (byte)1;
		matchResult.setPointsTeam1(goalNormal1);
		matchResult.setPointsTeam2(goalNormal2);
		aomr.getMatchResult().add(matchResult);
		
		/*MatchResult*/ matchResult = new MatchResult();
		matchResult.setResultName(webService.getResultExtraLabel());
		final Byte goalExtra1 = (byte)2;
		final Byte goalExtra2 = (byte)2;
		matchResult.setPointsTeam1(goalExtra1);
		matchResult.setPointsTeam2(goalExtra2);
		aomr.getMatchResult().add(matchResult);
		
		/*MatchResult*/ matchResult = new MatchResult();
		matchResult.setResultName(webService.getResultNormalLabel());
		final Byte goalPenalty1 = (byte)7;
		final Byte goalPenalty2 = (byte)8;
		matchResult.setPointsTeam1(goalPenalty1);
		matchResult.setPointsTeam2(goalPenalty2);
		aomr.getMatchResult().add(matchResult);
		
		matchdata.setMatchResults(aomr);

		// isNotReversedTeams
		Mockito.when(matchService.isCandidateMatchTeam(match, matchdata.getIdTeam1(), 1)).thenReturn(true);
		Mockito.when(matchService.isCandidateMatchTeam(match, matchdata.getIdTeam2(), 2)).thenReturn(true);
		// isReversedTeams
		Mockito.when(matchService.isCandidateMatchTeam(match, matchdata.getIdTeam1(), 2)).thenReturn(false);
		Mockito.when(matchService.isCandidateMatchTeam(match, matchdata.getIdTeam2(), 1)).thenReturn(false);
		
		Mockito.when(matchService.updateMatchByMatchdata(match.getMatchId(), 
				team1WsId, team2WsId, goalNormal1, goalExtra1, goalPenalty1, 
				goalNormal2, goalExtra2, goalPenalty2)).thenReturn(match);
		
		boolean expectedIsUpdated = true;
		boolean isUpdated = webServiceService.updateMatchByMatchdata(match, matchdata, webService);
		assertEquals("Result updated flag should be equal to the expected one", expectedIsUpdated, isUpdated);
		
		Mockito.verify(matchService).updateMatchByMatchdata(match.getMatchId(), 
				team1WsId, team2WsId, goalNormal1, goalExtra1, goalPenalty1, 
				goalNormal2, goalExtra2, goalPenalty2);
	}
}
