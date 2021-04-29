package com.zematix.jworldcup.backend.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.text.ParseException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.LongStream;

import javax.inject.Inject;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.internal.util.collections.Iterables;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import com.zematix.jworldcup.backend.dao.CommonDao;
import com.zematix.jworldcup.backend.dao.MatchDao;
import com.zematix.jworldcup.backend.dao.RoundDao;
import com.zematix.jworldcup.backend.entity.Event;
import com.zematix.jworldcup.backend.entity.Match;
import com.zematix.jworldcup.backend.entity.Round;
import com.zematix.jworldcup.backend.entity.Team;
import com.zematix.jworldcup.backend.entity.model.GroupPosition;
import com.zematix.jworldcup.backend.scheduler.SchedulerService;
import com.zematix.jworldcup.backend.util.CommonUtil;

/**
 * Contains test functions of {@link MatchService} class.
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles({"development", "test"})
@Transactional
public class MatchServiceTest {

	// Without mocking the inner helper (mostly private) methods of the injected class
	// cannot be stubbed out. Constructing test cases for a public method with more 
	// helper (private) methods can be verily exhaustive.
	@Inject
	private MatchService matchService;
	
	// Full mock (dependencies are neither injected nor mocked). You may call any original 
	// method with either Mockito.doCallRealMethod or Mockito.when....thenCallRealMethod,
	// however all called submethods should be also mocked, which is verily cumbersome.
//	@Produces
//	@MockBean
//	private MatchService matchService;

	// Partial mock (however dependencies are neither injected nor mocked), the original 
	// methods are not mocked, except if there is Mockito.do...when stubbing. 
	// Do not use Mockito.when...then because it directly calls the original method inside!
//	@Produces
//	@Spy
//	private MatchService matchService;

	// Partial mock (however dependencies are not injected), the original methods 
	// are not mocked, except if there is Mockito.do...when stubbing. 
	// Do not use Mockito.when...then because it directly calls the original method inside!
//	@Produces
//	@Spy
//	@InjectMocks
//	private MatchService matchService;

	// Relevant dependencies used by the tested method
	
	// cannot be mocked at all, neither on this tester class nor on the tested class
	@Inject
	private CommonDao commonDao;

	// If there is "@Inject MatchService" then:
	// WELD-001409: Ambiguous dependencies for type CommonDao with qualifiers @Default
	// Possible dependencies: 
	//  - Managed Bean [class com.zematix.jworldcup.backend.dao.CommonDao] with qualifiers [@Any @Default],
	//  - Producer Field [CommonDao] with qualifiers [@Any @Default] declared as [[UnbackedAnnotatedField] @Produces @Spy private com.zematix.jworldcup.backend.service.MatchServiceTest.commonDao]
//	@Produces
//	@Spy
//	private CommonDao commonDao;

	// Full mock (dependencies are neither injected nor mocked). You may call any original 
	// method with either Mockito.doCallRealMethod or Mockito.when....thenCallRealMethod,
	// however all called submethods should be also mocked, which is verily cumbersome.
//	@Produces
//	@MockBean
//	private CommonDao commonDao;

	@MockBean
	private MatchDao matchDao; // used by methods of the tested class
	
	@MockBean
	private RoundDao roundDao; // used by methods of the tested class
	
	@MockBean
	private ApplicationService applicationService; // used by methods of the tested class

	@MockBean
	private SchedulerService schedulerService; // used by methods of the tested class
	
	@SpyBean
	private MatchService matchServicePartial; // partial mock

	/**
	 * Test {@link MatchService#retrieveRoundsByEvent(Long)} method.
	 * Scenario: successfully retrieves the result
	 */
	@Test
	public void /*List<Round>*/ retrieveRoundsByEvent(/*Long eventId*/) throws ServiceException {
		Long eventId = 1L;
		
		Round round1 = new Round();
		round1.setMatches(new ArrayList<Match>());
		Round round2 = new Round();
		round2.setMatches(new ArrayList<Match>());
		List<Round> expectedRounds = Arrays.asList(round1, round2);
		Mockito.when(roundDao.retrieveRoundsByEvent(eventId)).thenReturn(expectedRounds);
		
		List<Round> rounds = matchService.retrieveRoundsByEvent(eventId);
		assertEquals("Result list should be equal to the expected one.", expectedRounds, rounds);
	}	

	/**
	 * Test {@link MatchService#retrieveRoundsByEvent(Long)} method.
	 * Scenario: throws {@link IllegalArgumentException} because of {@code null} input argument
	 */
	@Test(expected=IllegalArgumentException.class)
	public void /*List<Round>*/ retrieveRoundsByEventNull(/*Long eventId*/) throws ServiceException {
		Long eventId = null;
		
		/*List<Round> rounds =*/ matchService.retrieveRoundsByEvent(eventId);
	}	

	/**
	 * Test {@link MatchService#retrieveMatchesByEvent(Long)} method.
	 * Scenario: successfully retrieves the result
	 */
	@Test
	public void /*List<Match>*/ retrieveMatchesByEvent(/*Long eventId*/) throws ServiceException {
		Long eventId = 1L;
		
		Round round = new Round();
		Match match1 = new Match();
		match1.setRound(round);
		Match match2 = new Match();
		match2.setRound(round);
		List<Match> expectedMatches = Arrays.asList(match1, match2);
		Mockito.when(matchDao.retrieveMatchesByEvent(eventId)).thenReturn(expectedMatches);

		List<Match> matches = matchService.retrieveMatchesByEvent(eventId);
		assertEquals("Result list should be equal to the expected one.", expectedMatches, matches);
	}	

	/**
	 * Test {@link MatchService#retrieveMatchesByEvent(Long)} method.
	 * Scenario: throws {@link IllegalArgumentException} because of {@code null} input argument
	 */
	@Test(expected=IllegalArgumentException.class)
	public void /*List<Match>*/ retrieveMatchesByEventNull(/*Long eventId*/) throws ServiceException {
		Long eventId = null;
		
		/*List<Match> matches =*/ matchService.retrieveMatchesByEvent(eventId);
	}	

	/**
	 * Test {@link MatchService#retrieveMatchesWithoutParticipantsByEvent(Long)} method.
	 * Scenario: successfully retrieves the result
	 */
	@Test
	public void /*List<Match>*/ retrieveMatchesWithoutParticipantsByEvent(/*Long eventId*/) throws ServiceException {
		Long eventId = 1L;
		
		Match match1 = new Match();
		Match match2 = new Match();
		List<Match> expectedMatches = Arrays.asList(match1, match2);
		Mockito.when(matchDao.retrieveMatchesWithoutParticipantsByEvent(eventId)).thenReturn(expectedMatches);

		List<Match> matches = matchService.retrieveMatchesWithoutParticipantsByEvent(eventId);
		assertEquals("Result list should be equal to the expected one.", expectedMatches, matches);
	}	

	/**
	 * Test {@link MatchService#retrieveMatchesWithoutParticipantsByEvent(Long)} method.
	 * Scenario: throws {@link IllegalArgumentException} because of {@code null} input argument
	 */
	@Test(expected=IllegalArgumentException.class)
	public void /*List<Match>*/ retrieveMatchesWithoutParticipantsByEventNull(/*Long eventId*/) throws ServiceException {
		Long eventId = null;
		
		/*List<Match> matches =*/ matchService.retrieveMatchesWithoutParticipantsByEvent(eventId);
	}	

	/**
	 * Test {@link MatchService#retrieveMatch(Long)} method.
	 * Scenario: successfully retrieves the result
	 */
	@Test
	public void /*Match*/ retrieveMatch(/*Long matchId*/) throws ServiceException {
		Long matchId = 1L; // WC2014 first match
		Match expectedMatch = commonDao.findEntityById(Match.class, matchId);

		Match match = matchService.retrieveMatch(matchId);
		assertEquals("Result element should be equal to the expected one.", expectedMatch, match);
		
		assertFalse("Instance should be detached.", commonDao.containsEntity(match));
	}	

	/**
	 * Test {@link MatchService#retrieveMatch(Long)} method.
	 * Scenario: throws {@link IllegalArgumentException} because of {@code null} input argument
	 */
	@Test(expected=IllegalArgumentException.class)
	public void /*Match*/ retrieveMatchNull(/*Long matchId*/) throws ServiceException {
		Long matchId = null;
		
		/*Match match =*/ matchService.retrieveMatch(matchId);
	}	

	/**
	 * Test {@link MatchService#retrieveMatch(Long)} method.
	 * Scenario: throws {@link ServiceException} because of invalid input argument
	 */
	@Test(expected=ServiceException.class)
	public void /*Match*/ retrieveMatchInvalid(/*Long matchId*/) throws ServiceException {
		Long matchId = -1L;
		
		try {
			/*Match match =*/ matchService.retrieveMatch(matchId);
		}
		catch (ServiceException e) {
			assertTrue("There must be a single message in ServiceException named MISSING_MATCH", 
					e.getMessages().size()==1 && e.getMessages().get(0).getMsgCode().equals("MISSING_MATCH"));
			throw e;
		}
	}	

	/**
	 * Test {@link MatchService#saveMatch(Long, boolean, Boolean, Date, 
			Byte, Byte, Byte, Byte, Byte, Byte)} method.
	 * Scenario: throws {@link ServiceException} because of the startTime of the match to be saved
	 * 		not finished yet.
	 */
	@Test(expected=ServiceException.class)
	public void /*Match*/ saveMatchNotFinished(/*Long matchId, boolean isGroupmatch, Boolean isOvertime, 
			LocalDateTime startTime, Byte goalNormal1, Byte goalNormal2, Byte goalExtra1, 
			Byte goalExtra2, Byte goalPenalty1, Byte goalPenalty2*/) throws ServiceException {
		Long matchId = -1L;
		boolean isGroupmatch = false;
		Boolean isOvertime = true;
		LocalDateTime startTime = LocalDateTime.now(); 
		Byte goalNormal1 = null;
		Byte goalNormal2 = null;
		Byte goalExtra1 = null;
		Byte goalExtra2 = null;
		Byte goalPenalty1 = null;
		Byte goalPenalty2 = null;
		
		Mockito.when(applicationService.getActualDateTime()).thenReturn(startTime);
		
		try {
			/*Match match =*/ matchService.saveMatch(matchId, isGroupmatch, isOvertime, startTime, 
					goalNormal1, goalNormal2, goalExtra1, goalExtra2, 
					goalPenalty1, goalPenalty2);
		}
		catch (ServiceException e) {
			assertTrue("There must be a single message in ServiceException named MATCH_NOT_FINISHED_YET", 
					e.getMessages().size()==1 && e.getMessages().get(0).getMsgCode().equals("MATCH_NOT_FINISHED_YET"));
			throw e;
		}
	}	

	/**
	 * Test {@link MatchService#saveMatch(Long, boolean, Boolean, Date, 
			Byte, Byte, Byte, Byte, Byte, Byte)} method.
	 * Scenario: throws {@link ServiceException} because of the knockout (not group) match 
	 * 		to be saved has no result, it is without goalNormal1 and goalNormal2 values.
	 */
	@Test(expected=ServiceException.class)
	public void /*Match*/ saveMatchGroupWithoutResult(/*Long matchId, boolean isGroupmatch, Boolean isOvertime, 
			LocalDateTime startTime, Byte goalNormal1, Byte goalNormal2, Byte goalExtra1, 
			Byte goalExtra2, Byte goalPenalty1, Byte goalPenalty2*/) throws ServiceException {
		Long matchId = -1L;
		boolean isGroupmatch = true;
		Boolean isOvertime = null;
		LocalDateTime startTime = LocalDateTime.now(); 
		Byte goalNormal1 = null;
		Byte goalNormal2 = null;
		Byte goalExtra1 = null;
		Byte goalExtra2 = null;
		Byte goalPenalty1 = null;
		Byte goalPenalty2 = null;

		LocalDateTime epochDate = CommonUtil.getEpochDateTime();
		Mockito.when(applicationService.getActualDateTime()).thenReturn(epochDate);
	
		try {
			/*Match match =*/ matchService.saveMatch(matchId, isGroupmatch, isOvertime, startTime, 
					goalNormal1, goalNormal2, goalExtra1, goalExtra2, 
					goalPenalty1, goalPenalty2);
		}
		catch (ServiceException e) {
			assertTrue("There must be a single message in ServiceException named MISSING_MATCH_RESULT", 
					e.getMessages().size()==1 && e.getMessages().get(0).getMsgCode().equals("MISSING_MATCH_RESULT"));
			throw e;
		}
	}	

	/**
	 * Test {@link MatchService#saveMatch(Long, boolean, Boolean, Date, 
			Byte, Byte, Byte, Byte, Byte, Byte)} method.
	 * Scenario: throws {@link ServiceException} because of the knockout (not group) match 
	 * 		to be saved has no result, it is without goalNormal1 and goalNormal2 values.
	 */
	@Test(expected=ServiceException.class)
	public void /*Match*/ saveMatchKnockoutWithoutResult(/*Long matchId, boolean isGroupmatch, Boolean isOvertime, 
			LocalDateTime startTime, Byte goalNormal1, Byte goalNormal2, Byte goalExtra1, 
			Byte goalExtra2, Byte goalPenalty1, Byte goalPenalty2*/) throws ServiceException {
		Long matchId = -1L;
		boolean isGroupmatch = false;
		Boolean isOvertime = true;
		LocalDateTime startTime = LocalDateTime.now();
		Byte goalNormal1 = null;
		Byte goalNormal2 = null;
		Byte goalExtra1 = null;
		Byte goalExtra2 = null;
		Byte goalPenalty1 = null;
		Byte goalPenalty2 = null;

		LocalDateTime epochDate = CommonUtil.getEpochDateTime();
		Mockito.when(applicationService.getActualDateTime()).thenReturn(epochDate);
	
		try {
			/*Match match =*/ matchService.saveMatch(matchId, isGroupmatch, isOvertime, startTime, 
					goalNormal1, goalNormal2, goalExtra1, goalExtra2, 
					goalPenalty1, goalPenalty2);
		}
		catch (ServiceException e) {
			assertTrue("There must be a single message in ServiceException named MISSING_MATCH_AFTER_90_RESULT", 
					e.getMessages().size()==1 && e.getMessages().get(0).getMsgCode().equals("MISSING_MATCH_AFTER_90_RESULT"));
			throw e;
		}
	}	

	/**
	 * Test {@link MatchService#saveMatch(Long, boolean, Boolean, Date, 
			Byte, Byte, Byte, Byte, Byte, Byte)} method.
	 * Scenario: throws {@link ServiceException} because of the match 
	 * 		to be saved has result with negative result.
	 */
	@Test(expected=ServiceException.class)
	public void /*Match*/ saveMatchWithNegativeResult(/*Long matchId, boolean isGroupmatch, Boolean isOvertime, 
			LocalDateTime startTime, Byte goalNormal1, Byte goalNormal2, Byte goalExtra1, 
			Byte goalExtra2, Byte goalPenalty1, Byte goalPenalty2*/) throws ServiceException {
		Long matchId = -1L;
		boolean isGroupmatch = false;
		Boolean isOvertime = true;
		LocalDateTime startTime = LocalDateTime.now(); 
		Byte goalNormal1 = (byte)-1;
		Byte goalNormal2 = (byte)-2;
		Byte goalExtra1 = null;
		Byte goalExtra2 = null;
		Byte goalPenalty1 = null;
		Byte goalPenalty2 = null;

		LocalDateTime epochDate = CommonUtil.getEpochDateTime();
		Mockito.when(applicationService.getActualDateTime()).thenReturn(epochDate);
	
		try {
			/*Match match =*/ matchService.saveMatch(matchId, isGroupmatch, isOvertime, startTime, 
					goalNormal1, goalNormal2, goalExtra1, goalExtra2, 
					goalPenalty1, goalPenalty2);
		}
		catch (ServiceException e) {
			assertTrue("There must be a single message in ServiceException named NOT_POSITIVE_VALUE_INVALID", 
					e.getMessages().size()==1 && e.getMessages().get(0).getMsgCode().equals("NOT_POSITIVE_VALUE_INVALID"));
			throw e;
		}
	}

	/**
	 * Test {@link MatchService#saveMatch(Long, boolean, Boolean, Date, 
			Byte, Byte, Byte, Byte, Byte, Byte)} method.
	 * Scenario: throws {@link ServiceException} because of the group match 
	 * 		to be saved has overtime result, however it should not have.
	 */
	@Test(expected=ServiceException.class)
	public void /*Match*/ saveMatchGroupWithOvertime(/*Long matchId, boolean isGroupmatch, Boolean isOvertime, 
			LocalDateTime startTime, Byte goalNormal1, Byte goalNormal2, Byte goalExtra1, 
			Byte goalExtra2, Byte goalPenalty1, Byte goalPenalty2*/) throws ServiceException {
		Long matchId = -1L;
		boolean isGroupmatch = true;
		Boolean isOvertime = null;
		LocalDateTime startTime = LocalDateTime.now(); 
		Byte goalNormal1 = (byte)1;
		Byte goalNormal2 = (byte)1;
		Byte goalExtra1 = (byte)1;
		Byte goalExtra2 = (byte)1;
		Byte goalPenalty1 = null;
		Byte goalPenalty2 = null;

		LocalDateTime epochDate = CommonUtil.getEpochDateTime();
		Mockito.when(applicationService.getActualDateTime()).thenReturn(epochDate);
	
		try {
			/*Match match =*/ matchService.saveMatch(matchId, isGroupmatch, isOvertime, startTime, 
					goalNormal1, goalNormal2, goalExtra1, goalExtra2, 
					goalPenalty1, goalPenalty2);
		}
		catch (ServiceException e) {
			assertTrue("There must be a single message in ServiceException named GROUP_MATCH_WITH_OVERTIME", 
					e.getMessages().size()==1 && e.getMessages().get(0).getMsgCode().equals("GROUP_MATCH_WITH_OVERTIME"));
			throw e;
		}
	}

	/**
	 * Test {@link MatchService#saveMatch(Long, boolean, Boolean, Date, 
			Byte, Byte, Byte, Byte, Byte, Byte)} method.
	 * Scenario: throws {@link ServiceException} because of the group match 
	 * 		to be saved has overtime result, however it should not have.
	 */
	@Test(expected=ServiceException.class)
	public void /*Match*/ saveMatchGroupWithPenalty(/*Long matchId, boolean isGroupmatch, Boolena isOvertime, 
			LocalDateTime startTime, Byte goalNormal1, Byte goalNormal2, Byte goalExtra1, 
			Byte goalExtra2, Byte goalPenalty1, Byte goalPenalty2*/) throws ServiceException {
		Long matchId = -1L;
		boolean isGroupmatch = true;
		Boolean isOvertime = null;
		LocalDateTime startTime = LocalDateTime.now(); 
		Byte goalNormal1 = (byte)1;
		Byte goalNormal2 = (byte)1;
		Byte goalExtra1 = null;
		Byte goalExtra2 = null;
		Byte goalPenalty1 = (byte)1;
		Byte goalPenalty2 = (byte)1;

		LocalDateTime epochDate = CommonUtil.getEpochDateTime();
		Mockito.when(applicationService.getActualDateTime()).thenReturn(epochDate);
	
		try {
			/*Match match =*/ matchService.saveMatch(matchId, isGroupmatch, isOvertime, startTime, 
					goalNormal1, goalNormal2, goalExtra1, goalExtra2, 
					goalPenalty1, goalPenalty2);
		}
		catch (ServiceException e) {
			assertTrue("There must be a single message in ServiceException named GROUP_MATCH_WITH_PENALTY", 
					e.getMessages().size()==1 && e.getMessages().get(0).getMsgCode().equals("GROUP_MATCH_WITH_PENALTY"));
			throw e;
		}
	}

	/**
	 * Test {@link MatchService#saveMatch(Long, boolean, Boolean, Date, 
			Byte, Byte, Byte, Byte, Byte, Byte)} method.
	 * Scenario: throws {@link ServiceException} because of the group match 
	 * 		to be saved has overtime and penalty result, however it should not have.
	 */
	@Test(expected=ServiceException.class)
	public void /*Match*/ saveMatchGroupWithOvertimeAndPenalty(/*Long matchId, boolean isGroupmatch, Boolean isOvertime, 
			LocalDateTime startTime, Byte goalNormal1, Byte goalNormal2, Byte goalExtra1, 
			Byte goalExtra2, Byte goalPenalty1, Byte goalPenalty2*/) throws ServiceException {
		Long matchId = -1L;
		boolean isGroupmatch = true;
		Boolean isOvertime = null;
		LocalDateTime startTime = LocalDateTime.now(); 
		Byte goalNormal1 = (byte)1;
		Byte goalNormal2 = (byte)1;
		Byte goalExtra1 = (byte)1;
		Byte goalExtra2 = (byte)1;
		Byte goalPenalty1 = (byte)1;
		Byte goalPenalty2 = (byte)1;

		LocalDateTime epochDate = CommonUtil.getEpochDateTime();
		Mockito.when(applicationService.getActualDateTime()).thenReturn(epochDate);
	
		try {
			/*Match match =*/ matchService.saveMatch(matchId, isGroupmatch, isOvertime, startTime, 
					goalNormal1, goalNormal2, goalExtra1, goalExtra2, 
					goalPenalty1, goalPenalty2);
		}
		catch (ServiceException e) {
			assertTrue("There must be a message in ServiceException named GROUP_MATCH_WITH_OVERTIME", 
					e.getMessages().stream().anyMatch(m->m.getMsgCode().equals("GROUP_MATCH_WITH_OVERTIME")));
			assertTrue("There must be a message in ServiceException named GROUP_MATCH_WITH_PENALTY", 
					e.getMessages().stream().anyMatch(m->m.getMsgCode().equals("GROUP_MATCH_WITH_PENALTY")));
			throw e;
		}
	}

	/**
	 * Test {@link MatchService#saveMatch(Long, boolean, Boolean, Date, 
			Byte, Byte, Byte, Byte, Byte, Byte)} method.
	 * Scenario: throws {@link ServiceException} because of the knockout match 
	 * 		to be saved has overtime result, however it should not have because of its round.
	 */
	@Test(expected=ServiceException.class)
	public void /*Match*/ saveMatchKnockoutWithOvertime(/*Long matchId, boolean isGroupmatch, Boolean isOvertime, 
			LocalDateTime startTime, Byte goalNormal1, Byte goalNormal2, Byte goalExtra1, 
			Byte goalExtra2, Byte goalPenalty1, Byte goalPenalty2*/) throws ServiceException {
		Long matchId = -1L;
		boolean isGroupmatch = false;
		Boolean isOvertime = false;
		LocalDateTime startTime = LocalDateTime.now(); 
		Byte goalNormal1 = (byte)1;
		Byte goalNormal2 = (byte)1;
		Byte goalExtra1 = (byte)1;
		Byte goalExtra2 = (byte)1;
		Byte goalPenalty1 = null;
		Byte goalPenalty2 = null;

		LocalDateTime epochDate = CommonUtil.getEpochDateTime();
		Mockito.when(applicationService.getActualDateTime()).thenReturn(epochDate);
	
		try {
			/*Match match =*/ matchService.saveMatch(matchId, isGroupmatch, isOvertime, startTime, 
					goalNormal1, goalNormal2, goalExtra1, goalExtra2, 
					goalPenalty1, goalPenalty2);
		}
		catch (ServiceException e) {
			assertTrue("There must be a single message in ServiceException named KNOCKOUT_MATCH_WITH_DISALLOWED_OVERTIME", 
					e.getMessages().size()==1 && e.getMessages().get(0).getMsgCode().equals("KNOCKOUT_MATCH_WITH_DISALLOWED_OVERTIME"));
			throw e;
		}
	}

	/**
	 * Test {@link MatchService#saveMatch(Long, boolean, Boolean, Date, 
			Byte, Byte, Byte, Byte, Byte, Byte)} method.
	 * Scenario: throws {@link ServiceException} because of the knockout match 
	 * 		to be saved has winner after 90 minutes but has also overtime result.
	 */
	@Test(expected=ServiceException.class)
	public void /*Match*/ saveMatchFinishedAfter90MinNoOvertime(/*Long matchId, boolean isGroupmatch, Boolean isOvertime, 
			LocalDateTime startTime, Byte goalNormal1, Byte goalNormal2, Byte goalExtra1, 
			Byte goalExtra2, Byte goalPenalty1, Byte goalPenalty2*/) throws ServiceException {
		Long matchId = -1L;
		boolean isGroupmatch = false;
		Boolean isOvertime = true;
		LocalDateTime startTime = LocalDateTime.now(); 
		Byte goalNormal1 = (byte)1;
		Byte goalNormal2 = (byte)2;
		Byte goalExtra1 = (byte)1;
		Byte goalExtra2 = (byte)1;
		Byte goalPenalty1 = null;
		Byte goalPenalty2 = null;

		LocalDateTime epochDate = CommonUtil.getEpochDateTime();
		Mockito.when(applicationService.getActualDateTime()).thenReturn(epochDate);
	
		try {
			/*Match match =*/ matchService.saveMatch(matchId, isGroupmatch, isOvertime, startTime, 
					goalNormal1, goalNormal2, goalExtra1, goalExtra2, 
					goalPenalty1, goalPenalty2);
		}
		catch (ServiceException e) {
			assertTrue("There must be a single message in ServiceException named MATCH_FINISHED_AFTER_90_MIN_NO_OVERTIME", 
					e.getMessages().size()==1 && e.getMessages().get(0).getMsgCode().equals("MATCH_FINISHED_AFTER_90_MIN_NO_OVERTIME"));
			throw e;
		}
	}	

	/**
	 * Test {@link MatchService#saveMatch(Long, boolean, Boolean, Date, 
			Byte, Byte, Byte, Byte, Byte, Byte)} method.
	 * Scenario: throws {@link ServiceException} because of the knockout match 
	 * 		to be saved has winner after 90 minutes but has also penalty result.
	 */
	@Test(expected=ServiceException.class)
	public void /*Match*/ saveMatchFinishedAfter90MinNoPenalty(/*Long matchId, boolean isGroupmatch, Boolean isOvertime, 
			LocalDateTime startTime, Byte goalNormal1, Byte goalNormal2, Byte goalExtra1, 
			Byte goalExtra2, Byte goalPenalty1, Byte goalPenalty2*/) throws ServiceException {
		Long matchId = -1L;
		boolean isGroupmatch = false;
		Boolean isOvertime = true;
		LocalDateTime startTime = LocalDateTime.now(); 
		Byte goalNormal1 = (byte)1;
		Byte goalNormal2 = (byte)2;
		Byte goalExtra1 = null;
		Byte goalExtra2 = null;
		Byte goalPenalty1 = (byte)1;
		Byte goalPenalty2 = (byte)1;

		LocalDateTime epochDate = CommonUtil.getEpochDateTime();
		Mockito.when(applicationService.getActualDateTime()).thenReturn(epochDate);
	
		try {
			/*Match match =*/ matchService.saveMatch(matchId, isGroupmatch, isOvertime, startTime, 
					goalNormal1, goalNormal2, goalExtra1, goalExtra2, 
					goalPenalty1, goalPenalty2);
		}
		catch (ServiceException e) {
			assertTrue("There must be a single message in ServiceException named MATCH_FINISHED_AFTER_90_MIN_NO_PENALTY", 
					e.getMessages().size()==1 && e.getMessages().get(0).getMsgCode().equals("MATCH_FINISHED_AFTER_90_MIN_NO_PENALTY"));
			throw e;
		}
	}	

	/**
	 * Test {@link MatchService#saveMatch(Long, boolean, Boolean, Date, 
			Byte, Byte, Byte, Byte, Byte, Byte)} method.
	 * Scenario: throws {@link ServiceException} because of the knockout match 
	 * 		to be saved has winner after 90 minutes but has also penalty result.
	 */
	@Test(expected=ServiceException.class)
	public void /*Match*/ saveMatchFinishedAfter90MinNoOvertimeAndPenalty(/*Long matchId, boolean isGroupmatch, 
			Boolean isOvertime, LocalDateTime startTime, Byte goalNormal1, Byte goalNormal2, Byte goalExtra1, 
			Byte goalExtra2, Byte goalPenalty1, Byte goalPenalty2*/) throws ServiceException {
		Long matchId = -1L;
		boolean isGroupmatch = false;
		Boolean isOvertime = true;
		LocalDateTime startTime = LocalDateTime.now(); 
		Byte goalNormal1 = (byte)1;
		Byte goalNormal2 = (byte)2;
		Byte goalExtra1 = (byte)1;
		Byte goalExtra2 = (byte)1;
		Byte goalPenalty1 = (byte)1;
		Byte goalPenalty2 = (byte)1;

		LocalDateTime epochDate = CommonUtil.getEpochDateTime();
		Mockito.when(applicationService.getActualDateTime()).thenReturn(epochDate);
	
		try {
			/*Match match =*/ matchService.saveMatch(matchId, isGroupmatch, isOvertime, startTime, 
					goalNormal1, goalNormal2, goalExtra1, goalExtra2, 
					goalPenalty1, goalPenalty2);
		}
		catch (ServiceException e) {
			assertTrue("There must be a message in ServiceException named MATCH_FINISHED_AFTER_90_MIN_NO_OVERTIME", 
					e.getMessages().stream().anyMatch(m->m.getMsgCode().equals("MATCH_FINISHED_AFTER_90_MIN_NO_OVERTIME")));
			assertTrue("There must be a message in ServiceException named MATCH_FINISHED_AFTER_90_MIN_NO_PENALTY", 
					e.getMessages().stream().anyMatch(m->m.getMsgCode().equals("MATCH_FINISHED_AFTER_90_MIN_NO_PENALTY")));
			throw e;
		}
	}	

	/**
	 * Test {@link MatchService#saveMatch(Long, boolean, Boolean, Date, 
			Byte, Byte, Byte, Byte, Byte, Byte)} method.
	 * Scenario: throws {@link ServiceException} because of the knockout match 
	 * 		to be saved has draw normal result but without overtime result.
	 */
	@Test(expected=ServiceException.class)
	public void /*Match*/ saveMatchMissingOvertimeAfterDrawNormal(/*Long matchId, boolean isGroupmatch, 
			Boolean isOvertime, LocalDateTime startTime, Byte goalNormal1, Byte goalNormal2, Byte goalExtra1, 
			Byte goalExtra2, Byte goalPenalty1, Byte goalPenalty2*/) throws ServiceException {
		Long matchId = -1L;
		boolean isGroupmatch = false;
		Boolean isOvertime = true;
		LocalDateTime startTime = LocalDateTime.now(); 
		Byte goalNormal1 = (byte)1;
		Byte goalNormal2 = (byte)1;
		Byte goalExtra1 = null;
		Byte goalExtra2 = null;
		Byte goalPenalty1 = null;
		Byte goalPenalty2 = null;

		LocalDateTime epochDate = CommonUtil.getEpochDateTime();
		Mockito.when(applicationService.getActualDateTime()).thenReturn(epochDate);
	
		try {
			/*Match match =*/ matchService.saveMatch(matchId, isGroupmatch, isOvertime, startTime, 
					goalNormal1, goalNormal2, goalExtra1, goalExtra2, 
					goalPenalty1, goalPenalty2);
		}
		catch (ServiceException e) {
			assertTrue("There must be a single message in ServiceException named ENTER_RESULT_AFTER_OVERTIME", 
					e.getMessages().size()==1 && e.getMessages().get(0).getMsgCode().equals("ENTER_RESULT_AFTER_OVERTIME"));
			throw e;
		}
	}	

	/**
	 * Test {@link MatchService#saveMatch(Long, boolean, Boolean, Date, 
			Byte, Byte, Byte, Byte, Byte, Byte)} method.
	 * Scenario: throws {@link ServiceException} because of the knockout match 
	 * 		to be saved has partial overtime result.
	 */
	@Test(expected=ServiceException.class)
	public void /*Match*/ saveMatchPartialResultAfterOvertime(/*Long matchId, boolean isGroupmatch, Boolean isOvertime, 
			LocalDateTime startTime, Byte goalNormal1, Byte goalNormal2, Byte goalExtra1, 
			Byte goalExtra2, Byte goalPenalty1, Byte goalPenalty2*/) throws ServiceException {
		Long matchId = -1L;
		boolean isGroupmatch = false;
		Boolean isOvertime = true;
		LocalDateTime startTime = LocalDateTime.now(); 
		Byte goalNormal1 = (byte)1;
		Byte goalNormal2 = (byte)1;
		Byte goalExtra1 = (byte)1;
		Byte goalExtra2 = null;
		Byte goalPenalty1 = null;
		Byte goalPenalty2 = null;

		LocalDateTime epochDate = CommonUtil.getEpochDateTime();
		Mockito.when(applicationService.getActualDateTime()).thenReturn(epochDate);
	
		try {
			/*Match match =*/ matchService.saveMatch(matchId, isGroupmatch, isOvertime, startTime, 
					goalNormal1, goalNormal2, goalExtra1, goalExtra2, 
					goalPenalty1, goalPenalty2);
		}
		catch (ServiceException e) {
			assertTrue("There must be a single message in ServiceException named PARTIAL_RESULT_AFTER_OVERTIME", 
					e.getMessages().size()==1 && e.getMessages().get(0).getMsgCode().equals("PARTIAL_RESULT_AFTER_OVERTIME"));
			throw e;
		}
	}	

	/**
	 * Test {@link MatchService#saveMatch(Long, boolean, Boolean, Date, 
			Byte, Byte, Byte, Byte, Byte, Byte)} method.
	 * Scenario: throws {@link ServiceException} because of the knockout (not group) match 
	 * 		to be saved has no acceptable result, it is with negative goalExtra1 and goalExtra2 values.
	 */
	@Test(expected=ServiceException.class)
	public void /*Match*/ saveMatchOvertimeWithNegativeResult(/*Long matchId, boolean isGroupmatch, 
			Boolean isOvertime, LocalDateTime startTime, Byte goalNormal1, Byte goalNormal2, Byte goalExtra1, 
			Byte goalExtra2, Byte goalPenalty1, Byte goalPenalty2*/) throws ServiceException {
		Long matchId = -1L;
		boolean isGroupmatch = false;
		Boolean isOvertime = true;
		LocalDateTime startTime = LocalDateTime.now(); 
		Byte goalNormal1 = (byte)1;
		Byte goalNormal2 = (byte)1;
		Byte goalExtra1 = (byte)-1;
		Byte goalExtra2 = (byte)-1;
		Byte goalPenalty1 = null;
		Byte goalPenalty2 = null;

		LocalDateTime epochDate = CommonUtil.getEpochDateTime();
		Mockito.when(applicationService.getActualDateTime()).thenReturn(epochDate);
	
		try {
			/*Match match =*/ matchService.saveMatch(matchId, isGroupmatch, isOvertime, startTime, 
					goalNormal1, goalNormal2, goalExtra1, goalExtra2, 
					goalPenalty1, goalPenalty2);
		}
		catch (ServiceException e) {
			assertTrue("There must be a single message in ServiceException named NOT_POSITIVE_VALUE_INVALID", 
					e.getMessages().size()==1 && e.getMessages().get(0).getMsgCode().equals("NOT_POSITIVE_VALUE_INVALID"));
			throw e;
		}
	}

	/**
	 * Test {@link MatchService#saveMatch(Long, boolean, Boolean, Date, 
			Byte, Byte, Byte, Byte, Byte, Byte)} method.
	 * Scenario: throws {@link ServiceException} because of the knockout match 
	 * 		to be saved has overtime winner result but still has also penalty result.
	 */
	@Test(expected=ServiceException.class)
	public void /*Match*/ saveMatchFinishedAfterOvertime(/*Long matchId, boolean isGroupmatch, 
			Boolean isOvertime, LocalDateTime startTime, Byte goalNormal1, Byte goalNormal2, Byte goalExtra1, 
			Byte goalExtra2, Byte goalPenalty1, Byte goalPenalty2*/) throws ServiceException {
		Long matchId = -1L;
		boolean isGroupmatch = false;
		Boolean isOvertime = true;
		LocalDateTime startTime = LocalDateTime.now();
		Byte goalNormal1 = (byte)1;
		Byte goalNormal2 = (byte)1;
		Byte goalExtra1 = (byte)2;
		Byte goalExtra2 = (byte)1;
		Byte goalPenalty1 = (byte)2;
		Byte goalPenalty2 = (byte)1;

		LocalDateTime epochDate = CommonUtil.getEpochDateTime();
		Mockito.when(applicationService.getActualDateTime()).thenReturn(epochDate);
	
		try {
			/*Match match =*/ matchService.saveMatch(matchId, isGroupmatch, isOvertime, startTime, 
					goalNormal1, goalNormal2, goalExtra1, goalExtra2, 
					goalPenalty1, goalPenalty2);
		}
		catch (ServiceException e) {
			assertTrue("There must be a single message in ServiceException named MATCH_FINISHED_AFTER_OVERTIME", 
					e.getMessages().size()==1 && e.getMessages().get(0).getMsgCode().equals("MATCH_FINISHED_AFTER_OVERTIME"));
			throw e;
		}
	}	

	/**
	 * Test {@link MatchService#saveMatch(Long, boolean, Boolean, Date, 
			Byte, Byte, Byte, Byte, Byte, Byte)} method.
	 * Scenario: throws {@link ServiceException} because of the knockout match 
	 * 		to be saved has draw overtime result but without penalty result.
	 */
	@Test(expected=ServiceException.class)
	public void /*Match*/ saveMatchMissingPenaltyAfterDrawOvertime(/*Long matchId, boolean isGroupmatch, 
			Boolean isOvertime, LocalDateTime startTime, Byte goalNormal1, Byte goalNormal2, Byte goalExtra1, 
			Byte goalExtra2, Byte goalPenalty1, Byte goalPenalty2*/) throws ServiceException {
		Long matchId = -1L;
		boolean isGroupmatch = false;
		Boolean isOvertime = true;
		LocalDateTime startTime = LocalDateTime.now();
		Byte goalNormal1 = (byte)1;
		Byte goalNormal2 = (byte)1;
		Byte goalExtra1 = (byte)1;
		Byte goalExtra2 = (byte)1;
		Byte goalPenalty1 = null;
		Byte goalPenalty2 = null;

		LocalDateTime epochDate = CommonUtil.getEpochDateTime();
		Mockito.when(applicationService.getActualDateTime()).thenReturn(epochDate);
	
		try {
			/*Match match =*/ matchService.saveMatch(matchId, isGroupmatch, isOvertime, startTime, 
					goalNormal1, goalNormal2, goalExtra1, goalExtra2, 
					goalPenalty1, goalPenalty2);
		}
		catch (ServiceException e) {
			assertTrue("There must be a single message in ServiceException named ENTER_RESULT_AFTER_PENALTIES", 
					e.getMessages().size()==1 && e.getMessages().get(0).getMsgCode().equals("ENTER_RESULT_AFTER_PENALTIES"));
			throw e;
		}
	}	

	/**
	 * Test {@link MatchService#saveMatch(Long, boolean, Boolean, Date, 
			Byte, Byte, Byte, Byte, Byte, Byte)} method.
	 * Scenario: throws {@link ServiceException} because of the knockout match 
	 * 		to be saved has partial penalty result.
	 */
	@Test(expected=ServiceException.class)
	public void /*Match*/ saveMatchPartialResultAfterPenalty(/*Long matchId, boolean isGroupmatch, 
			Boolean isOvertime, LocalDateTime startTime, Byte goalNormal1, Byte goalNormal2, Byte goalExtra1, 
			Byte goalExtra2, Byte goalPenalty1, Byte goalPenalty2*/) throws ServiceException {
		Long matchId = -1L;
		boolean isGroupmatch = false;
		Boolean isOvertime = true;
		LocalDateTime startTime = LocalDateTime.now();
		Byte goalNormal1 = (byte)1;
		Byte goalNormal2 = (byte)1;
		Byte goalExtra1 = (byte)1;
		Byte goalExtra2 = (byte)1;
		Byte goalPenalty1 = (byte)1;
		Byte goalPenalty2 = null;

		LocalDateTime epochDate = CommonUtil.getEpochDateTime();
		Mockito.when(applicationService.getActualDateTime()).thenReturn(epochDate);
	
		try {
			/*Match match =*/ matchService.saveMatch(matchId, isGroupmatch, isOvertime, startTime, 
					goalNormal1, goalNormal2, goalExtra1, goalExtra2, 
					goalPenalty1, goalPenalty2);
		}
		catch (ServiceException e) {
			assertTrue("There must be a single message in ServiceException named PARTIAL_RESULT_AFTER_PENALTIES", 
					e.getMessages().size()==1 && e.getMessages().get(0).getMsgCode().equals("PARTIAL_RESULT_AFTER_PENALTIES"));
			throw e;
		}
	}	

	/**
	 * Test {@link MatchService#saveMatch(Long, boolean, Boolean, Date, 
			Byte, Byte, Byte, Byte, Byte, Byte)} method.
	 * Scenario: throws {@link ServiceException} because of the knockout (not group) match 
	 * 		to be saved has penalty result with negative value.
	 */
	@Test(expected=ServiceException.class)
	public void /*Match*/ saveMatchPenaltyWithNegativeResult(/*Long matchId, boolean isGroupmatch, 
			Boolean isOvertime, LocalDateTime startTime, Byte goalNormal1, Byte goalNormal2, Byte goalExtra1, 
			Byte goalExtra2, Byte goalPenalty1, Byte goalPenalty2*/) throws ServiceException {
		Long matchId = -1L;
		boolean isGroupmatch = false;
		Boolean isOvertime = true;
		LocalDateTime startTime = LocalDateTime.now();
		Byte goalNormal1 = (byte)1;
		Byte goalNormal2 = (byte)1;
		Byte goalExtra1 = (byte)1;
		Byte goalExtra2 = (byte)1;
		Byte goalPenalty1 = (byte)-1;
		Byte goalPenalty2 = (byte)-1;

		LocalDateTime epochDate = CommonUtil.getEpochDateTime();
		Mockito.when(applicationService.getActualDateTime()).thenReturn(epochDate);
	
		try {
			/*Match match =*/ matchService.saveMatch(matchId, isGroupmatch, isOvertime, startTime, 
					goalNormal1, goalNormal2, goalExtra1, goalExtra2, 
					goalPenalty1, goalPenalty2);
		}
		catch (ServiceException e) {
			assertTrue("There must be a single message in ServiceException named NOT_POSITIVE_VALUE_INVALID", 
					e.getMessages().size()==1 && e.getMessages().get(0).getMsgCode().equals("NOT_POSITIVE_VALUE_INVALID"));
			throw e;
		}
	}

	/**
	 * Test {@link MatchService#saveMatch(Long, boolean, Boolean, Date, 
			Byte, Byte, Byte, Byte, Byte, Byte)} method.
	 * Scenario: throws {@link ServiceException} because of the knockout (not group) match 
	 * 		to be saved has drawn overtime result but has also drawn penalty result.
	 */
	@Test(expected=ServiceException.class)
	public void /*Match*/ saveMatchPenaltyCannotBeDrawn(/*Long matchId, boolean isGroupmatch, 
			Boolean isOvertime, LocalDateTime startTime, Byte goalNormal1, Byte goalNormal2, Byte goalExtra1, 
			Byte goalExtra2, Byte goalPenalty1, Byte goalPenalty2*/) throws ServiceException {
		Long matchId = -1L;
		boolean isGroupmatch = false;
		Boolean isOvertime = true;
		LocalDateTime startTime = LocalDateTime.now(); 
		Byte goalNormal1 = (byte)1;
		Byte goalNormal2 = (byte)1;
		Byte goalExtra1 = (byte)1;
		Byte goalExtra2 = (byte)1;
		Byte goalPenalty1 = (byte)1;
		Byte goalPenalty2 = (byte)1;

		LocalDateTime epochDate = CommonUtil.getEpochDateTime();
		Mockito.when(applicationService.getActualDateTime()).thenReturn(epochDate);
	
		try {
			/*Match match =*/ matchService.saveMatch(matchId, isGroupmatch, isOvertime, startTime, 
					goalNormal1, goalNormal2, goalExtra1, goalExtra2, 
					goalPenalty1, goalPenalty2);
		}
		catch (ServiceException e) {
			assertTrue("There must be a single message in ServiceException named PENALTIES_RESULT_CANNOT_BE_DRAWN", 
					e.getMessages().size()==1 && e.getMessages().get(0).getMsgCode().equals("PENALTIES_RESULT_CANNOT_BE_DRAWN"));
			throw e;
		}
	}

	/**
	 * Test {@link MatchService#saveMatch(Long, boolean, Boolean, Date, 
			Byte, Byte, Byte, Byte, Byte, Byte)} method.
	 * Scenario: throws {@link ServiceException} because of the given matchId is invalid.
	 */
	@Test(expected=ServiceException.class)
	public void /*Match*/ saveMatchMissingMatchId(/*Long matchId, boolean isGroupmatch, 
			Boolean isOvertime, LocalDateTime startTime, Byte goalNormal1, Byte goalNormal2, Byte goalExtra1, 
			Byte goalExtra2, Byte goalPenalty1, Byte goalPenalty2*/) throws ServiceException {
		Long matchId = -1L;
		boolean isGroupmatch = false;
		Boolean isOvertime = true;
		LocalDateTime startTime = LocalDateTime.now();
		Byte goalNormal1 = (byte)1;
		Byte goalNormal2 = (byte)1;
		Byte goalExtra1 = (byte)1;
		Byte goalExtra2 = (byte)1;
		Byte goalPenalty1 = (byte)1;
		Byte goalPenalty2 = (byte)2;

		LocalDateTime epochDate = CommonUtil.getEpochDateTime();
		Mockito.when(applicationService.getActualDateTime()).thenReturn(epochDate);
		Mockito.when(matchService.saveMatch(matchId, isGroupmatch, isOvertime, startTime, 
				goalNormal1, goalNormal2, goalExtra1, goalExtra2, 
				goalPenalty1, goalPenalty2)).thenCallRealMethod();
	
		try {
			/*Match match =*/ matchService.saveMatch(matchId, isGroupmatch, isOvertime, startTime, 
					goalNormal1, goalNormal2, goalExtra1, goalExtra2, 
					goalPenalty1, goalPenalty2);
		}
		catch (ServiceException e) {
			assertTrue("There must be a single message in ServiceException named MISSING_MATCH", 
					e.getMessages().size()==1 && e.getMessages().get(0).getMsgCode().equals("MISSING_MATCH"));
			throw e;
		}
	}

	/**
	 * Test {@link MatchService#saveMatch(Long, boolean, Boolean, Date, 
			Byte, Byte, Byte, Byte, Byte, Byte)} method.
	 * Scenario: successfully updates a group match, its result.
	 */
	@Test
	public void /*Match*/ saveGroupMatch(/*Long matchId, boolean isGroupmatch, 
			Boolean isOvertime, LocalDateTime startTime, Byte goalNormal1, Byte goalNormal2, Byte goalExtra1, 
			Byte goalExtra2, Byte goalPenalty1, Byte goalPenalty2*/) throws ServiceException {
		Long matchId = 1L;
		boolean isGroupmatch = true;
		Boolean isOvertime = null;
		LocalDateTime startTime = LocalDateTime.now();
		Byte goalNormal1 = (byte)1;
		Byte goalNormal2 = (byte)1;
		Byte goalExtra1 = null;
		Byte goalExtra2 = null;
		Byte goalPenalty1 = null;
		Byte goalPenalty2 = null;

		LocalDateTime epochDate = CommonUtil.getEpochDateTime();
		Mockito.when(applicationService.getActualDateTime()).thenReturn(epochDate);
	
		Match savedMatch = matchService.saveMatch(matchId, isGroupmatch, isOvertime, startTime, 
				goalNormal1, goalNormal2, goalExtra1, goalExtra2, 
				goalPenalty1, goalPenalty2);
		
		assertNotNull("Result Match instance should not be null.", savedMatch);
		assertEquals("Result Match instance should have expected matchId field value", matchId, savedMatch.getMatchId());		

		Match retrievedMatch = commonDao.findEntityById(Match.class, matchId);		
		assertEquals("Saved Match instance should have expected normal1 field value", goalNormal1, retrievedMatch.getGoalNormalByTeam1());
		assertEquals("Saved Match instance should have expected normal2 field value", goalNormal2, retrievedMatch.getGoalNormalByTeam2());
	}

	/**
	 * Test {@link MatchService#saveMatch(Long, boolean, Boolean, Date, 
			Byte, Byte, Byte, Byte, Byte, Byte)} method.
	 * Scenario: successfully updates a knockout match, its result.
	 */
	@Test
	public void /*Match*/ saveKnockoutMatch(/*Long matchId, boolean isGroupmatch, 
			Boolean isOvertime, LocalDateTime startTime, Byte goalNormal1, Byte goalNormal2, Byte goalExtra1, 
			Byte goalExtra2, Byte goalPenalty1, Byte goalPenalty2*/) throws ServiceException {
		Long groupId = 1L;

		Comparator<Match> byMatchNr = (m1, m2) -> Short.compare(
				m1.getMatchN(), m2.getMatchN());
		Match match = commonDao.findAllEntities(Match.class).stream()
				.filter(m -> !m.getRound().getIsGroupmatchAsBoolean())
				.sorted(byMatchNr)
				.collect(Collectors.toList())
				.get(0);
		Long matchId = match.getMatchId();
		List<Team> teams = commonDao.findAllEntities(Team.class).stream()
				.filter(t -> t.getGroup().getGroupId().equals(groupId))
				.collect(Collectors.toList());
		
		match.setTeam1(teams.get(0));
		match.setTeam2(teams.get(1));
		
		boolean isGroupmatch = false;
		Boolean isOvertime = true;
		LocalDateTime startTime = LocalDateTime.now(); 
		Byte goalNormal1 = (byte)1;
		Byte goalNormal2 = (byte)1;
		Byte goalExtra1 = (byte)2;
		Byte goalExtra2 = (byte)2;
		Byte goalPenalty1 = (byte)5;
		Byte goalPenalty2 = (byte)4;

		LocalDateTime epochDate = CommonUtil.getEpochDateTime();
		Mockito.when(applicationService.getActualDateTime()).thenReturn(epochDate);
	
		Match savedMatch = matchService.saveMatch(matchId, isGroupmatch, isOvertime, startTime, 
				goalNormal1, goalNormal2, goalExtra1, goalExtra2, 
				goalPenalty1, goalPenalty2);
		
		assertNotNull("Result Match instance should not be null.", savedMatch);
		assertEquals("Result Match instance should have expected matchId field value", matchId, savedMatch.getMatchId());		

		Match retrievedMatch = commonDao.findEntityById(Match.class, matchId);		
		assertEquals("Saved Match instance should have expected goalNormal1 field value", goalNormal1, retrievedMatch.getGoalNormalByTeam1());
		assertEquals("Saved Match instance should have expected goalNormal2 field value", goalNormal2, retrievedMatch.getGoalNormalByTeam2());
		assertEquals("Saved Match instance should have expected goalExtra1 field value", goalExtra1, retrievedMatch.getGoalExtraByTeam1());
		assertEquals("Saved Match instance should have expected goalExtra2 field value", goalExtra2, retrievedMatch.getGoalExtraByTeam2());
		assertEquals("Saved Match instance should have expected goalPenalty1 field value", goalPenalty1, retrievedMatch.getGoalPenaltyByTeam1());
		assertEquals("Saved Match instance should have expected goalPenalty2 field value", goalPenalty2, retrievedMatch.getGoalPenaltyByTeam2());
	}

	/**
	 * Test {@link MatchService#sign(byte)} private method.
	 * Scenario: successfully retrieves the result
	 * 
	 * @throws Exception - because of reflective method call the exception can be anything 
	 */
	@Test
	public void /*byte*/ signZero(/*byte number*/) throws /*Service*/Exception {
		byte number = 0;

		// Private method cannot be invoked so we need some java reflection magic
		byte result = matchService.sign(number);
		
		assertEquals("Result should be equal to the expected one.", (byte)0, result);
	}	

	/**
	 * Test {@link MatchService#sign(byte)} private method.
	 * Scenario: successfully retrieves the result
	 * 
	 * @throws Exception - because of reflective method call the exception can be anything 
	 */
	@Test
	public void /*byte*/ signPositive(/*byte number*/) throws /*Service*/Exception {
		byte number = 2;

		// Private method cannot be invoked so we need some java reflection magic
		byte result = matchService.sign(number);
		
		assertEquals("Result should be equal to the expected one.", (byte)1, result);
	}	

	/**
	 * Test {@link MatchService#sign(byte)} private method.
	 * Scenario: successfully retrieves the result
	 * 
	 * @throws Exception - because of reflective method call the exception can be anything 
	 */
	@Test
	public void /*byte*/ signNegative(/*byte number*/) throws /*Service*/Exception {
		byte number = -2;

		// Private method cannot be invoked so we need some java reflection magic
		byte result = matchService.sign(number);
		
		assertEquals("Result should be equal to the expected one.", (byte)-1, result);
	}	

	/**
	 * Test {@link MatchService#getMatchResult(int, Byte, Byte, Byte, 
			Byte, Byte, Byte)} method.
	 * Scenario: throws {@link IllegalArgumentException} because of the input side parameter 
	 * 		is invalid.
	 */
	@Test(expected=IllegalArgumentException.class)
	public void /*int*/ getMatchResultInvalidSide(/*int side, Byte goal_normal1, Byte goal_extra1, Byte goal_penalty1, 
			Byte goal_normal2, Byte goal_extra2, Byte goal_penalty2*/) throws ServiceException {
		int side = 0;
		Byte goal_normal1 = null;
		Byte goal_extra1 = null; 
		Byte goal_penalty1 = null; 
		Byte goal_normal2 = null;
		Byte goal_extra2 = null;
		Byte goal_penalty2 = null;

		/*int result =*/ matchService.getMatchResult(side, goal_normal1, goal_extra1, goal_penalty1, 
				goal_normal2, goal_extra2, goal_penalty2);
	}

	/**
	 * Test {@link MatchService#getMatchResult(int, Byte, Byte, Byte, 
			Byte, Byte, Byte)} method.
	 * Scenario: successfully returns value {@code -2} because the input goal fields are 
	 * 		{@code null} values.
	 */
	@Test
	public void /*int*/ getMatchResultInvalidResult(/*int side, Byte goal_normal1, Byte goal_extra1, Byte goal_penalty1, 
			Byte goal_normal2, Byte goal_extra2, Byte goal_penalty2*/) throws ServiceException {
		int side = 1;
		Byte goal_normal1 = null;
		Byte goal_extra1 = null; 
		Byte goal_penalty1 = null; 
		Byte goal_normal2 = null;
		Byte goal_extra2 = null;
		Byte goal_penalty2 = null;

		int result = matchService.getMatchResult(side, goal_normal1, goal_extra1, goal_penalty1, 
				goal_normal2, goal_extra2, goal_penalty2);
		
		assertEquals("Result should be equal to the expected one.", -2, result);
	}	

	/**
	 * Test {@link MatchService#getMatchResult(int, Byte, Byte, Byte, 
			Byte, Byte, Byte)} method.
	 * Scenario: successfully returns value {@code 0} because the input goal fields 
	 * 		determine draw.
	 */
	@Test
	public void /*int*/ getMatchResultDraw(/*int side, Byte goal_normal1, Byte goal_extra1, Byte goal_penalty1, 
			Byte goal_normal2, Byte goal_extra2, Byte goal_penalty2*/) throws ServiceException {
		int side = 1;
		Byte goal_normal1 = 1;
		Byte goal_normal2 = 1;
		Byte goal_extra1 = 2; 
		Byte goal_extra2 = 2;
		Byte goal_penalty1 = null; 
		Byte goal_penalty2 = null;

		int result = matchService.getMatchResult(side, goal_normal1, goal_extra1, goal_penalty1, 
				goal_normal2, goal_extra2, goal_penalty2);
		
		assertEquals("Result should be equal to the expected one.", 0, result);
	}	

	/**
	 * Test {@link MatchService#getMatchResult(int, Byte, Byte, Byte, 
			Byte, Byte, Byte)} method.
	 * Scenario: successfully returns value {@code 1} because the input goal fields 
	 * 		determine winner on #1.
	 */
	@Test
	public void /*int*/ getMatchResultWinner1(/*int side, Byte goal_normal1, Byte goal_extra1, Byte goal_penalty1, 
			Byte goal_normal2, Byte goal_extra2, Byte goal_penalty2*/) throws ServiceException {
		int side = 1;
		Byte goal_normal1 = 1;
		Byte goal_normal2 = 1;
		Byte goal_extra1 = 2; 
		Byte goal_extra2 = 2;
		Byte goal_penalty1 = 3; 
		Byte goal_penalty2 = 2;

		int result = matchService.getMatchResult(side, goal_normal1, goal_extra1, goal_penalty1, 
				goal_normal2, goal_extra2, goal_penalty2);
		
		assertEquals("Result should be equal to the expected one.", 1, result);
	}	

	/**
	 * Test {@link MatchService#getMatchResult(int, Byte, Byte, Byte, 
			Byte, Byte, Byte)} method.
	 * Scenario: successfully returns value {@code 1} because the input goal fields 
	 * 		determine winner on #2.
	 */
	@Test
	public void /*int*/ getMatchResultWinner2(/*int side, Byte goal_normal1, Byte goal_extra1, Byte goal_penalty1, 
			Byte goal_normal2, Byte goal_extra2, Byte goal_penalty2*/) throws ServiceException {
		int side = 2;
		Byte goal_normal1 = 1;
		Byte goal_normal2 = 3;
		Byte goal_extra1 = null; 
		Byte goal_extra2 = null;
		Byte goal_penalty1 = null; 
		Byte goal_penalty2 = null;

		int result = matchService.getMatchResult(side, goal_normal1, goal_extra1, goal_penalty1, 
				goal_normal2, goal_extra2, goal_penalty2);
		
		assertEquals("Result should be equal to the expected one.", 1, result);
	}	

	/**
	 * Test {@link MatchService#getMatchResult(int, Byte, Byte, Byte, 
			Byte, Byte, Byte)} method.
	 * Scenario: successfully returns value {@code -1} because the input goal fields 
	 * 		determine defeat on #1.
	 */
	@Test
	public void /*int*/ getMatchResultDefeat1(/*int side, Byte goal_normal1, Byte goal_extra1, Byte goal_penalty1, 
			Byte goal_normal2, Byte goal_extra2, Byte goal_penalty2*/) throws ServiceException {
		int side = 1;
		Byte goal_normal1 = 1;
		Byte goal_normal2 = 1;
		Byte goal_extra1 = 2; 
		Byte goal_extra2 = 4;
		Byte goal_penalty1 = null; 
		Byte goal_penalty2 = null;

		int result = matchService.getMatchResult(side, goal_normal1, goal_extra1, goal_penalty1, 
				goal_normal2, goal_extra2, goal_penalty2);
		
		assertEquals("Result should be equal to the expected one.", -1, result);
	}	

	/**
	 * Test {@link MatchService#getMatchResult(int, Byte, Byte, Byte, 
			Byte, Byte, Byte)} method.
	 * Scenario: successfully returns value {@code -1} because the input goal fields 
	 * 		determine defeat on #2.
	 */
	@Test
	public void /*int*/ getMatchResultDefeat2(/*int side, Byte goal_normal1, Byte goal_extra1, Byte goal_penalty1, 
			Byte goal_normal2, Byte goal_extra2, Byte goal_penalty2*/) throws ServiceException {
		int side = 2;
		Byte goal_normal1 = 0;
		Byte goal_normal2 = 0;
		Byte goal_extra1 = 0; 
		Byte goal_extra2 = 0;
		Byte goal_penalty1 = 5; 
		Byte goal_penalty2 = 3;

		int result = matchService.getMatchResult(side, goal_normal1, goal_extra1, goal_penalty1, 
				goal_normal2, goal_extra2, goal_penalty2);
		
		assertEquals("Result should be equal to the expected one.", -1, result);
	}	

	/**
	 * Test {@link MatchService#getMatchResult(Match, Long)} method.
	 * Scenario: throws {@link IllegalArgumentException} because of the input match parameter 
	 * 		is invalid.
	 */
	@Test(expected=IllegalArgumentException.class)
	public void /*int*/ getMatchResultShortInvalidMatch(/*Match match, Long teamId*/) throws ServiceException {
		Match match = null;
		Long teamId = 1L;

		/*int result =*/ matchService.getMatchResult(match, teamId);
	}

	/**
	 * Test {@link MatchService#getMatchResult(Match, Long)} method.
	 * Scenario: throws {@link IllegalArgumentException} because of the input teamId parameter 
	 * 		is invalid.
	 */
	@Test(expected=IllegalArgumentException.class)
	public void /*int*/ getMatchResultShortInvalidTeamId(/*Match match, Long teamId*/) throws ServiceException {
		Match match = commonDao.findEntityById(Match.class, 1L);
		Long teamId = null;

		/*int result =*/ matchService.getMatchResult(match, teamId);
	}

	/**
	 * Test {@link MatchService#getMatchResult(Match, Long)} method.
	 * Scenario: throws {@link IllegalArgumentException} because of the input teamId parameter 
	 * 		is invalid.
	 */
	@Test(expected=IllegalArgumentException.class)
	public void /*int*/ getMatchResultShortWithoutTeam(/*Match match, Long teamId*/) throws ServiceException {
		Match match = commonDao.findAllEntities(Match.class).stream()
				.filter(m -> !m.getRound().getIsGroupmatchAsBoolean())
				.collect(Collectors.toList())
				.get(0);
		Long teamId = 1L;

		/*int result =*/ matchService.getMatchResult(match, teamId);
	}
	
	/**
	 * Test {@link MatchService#getMatchResult(Match, Long)} method.
	 * Scenario: successfully returns -2 because the input {@code teamId} does not
	 * 		belong to the input {@code match}.
	 */
	@Test
	public void /*int*/ getMatchResultUnknownTeam(/*Match match, Long teamId*/) throws ServiceException {
		Match match = commonDao.findAllEntities(Match.class).stream()
				.filter(m -> m.getRound().getIsGroupmatchAsBoolean())
				.collect(Collectors.toList())
				.get(0);
		Long teamId = -1L;

		int result = matchService.getMatchResult(match, teamId);
		assertEquals("Result should be equal to the expected one.", -2, result);
	}
	
	/**
	 * Test {@link MatchService#getMatchResult(Match, Long)} method.
	 * Scenario: successfully returns value {@code 1} because the input parameters 
	 * 		determine winner on #1.
	 */
	@Test
	public void /*int*/ getMatchResultShortWinner1(/*Match match, Long teamId*/) throws ServiceException {
		Match match = commonDao.findAllEntities(Match.class).stream()
				.filter(m -> m.getRound().getIsGroupmatchAsBoolean())
				.collect(Collectors.toList())
				.get(0);
		Long teamId = match.getTeam1().getTeamId();
		
		Byte goal_normal1 = 1;
		Byte goal_normal2 = 1;
		Byte goal_extra1 = 2; 
		Byte goal_extra2 = 2;
		Byte goal_penalty1 = 3; 
		Byte goal_penalty2 = 2;
		match.setGoalNormalByTeam1(goal_normal1);
		match.setGoalNormalByTeam2(goal_normal2);
		match.setGoalExtraByTeam1(goal_extra1);
		match.setGoalExtraByTeam2(goal_extra2);
		match.setGoalPenaltyByTeam1(goal_penalty1);
		match.setGoalPenaltyByTeam2(goal_penalty2);
		
		int result = matchService.getMatchResult(match, teamId);

		assertEquals("Result should be equal to the expected one.", 1, result);
	}

	/**
	 * Test {@link MatchService#getMatchResult(Match, Long)} method.
	 * Scenario: successfully returns value {@code -1} because the input parameters 
	 * 		determine defeat on #2.
	 */
	@Test
	public void /*int*/ getMatchResultShortDefeat2(/*Match match, Long teamId*/) throws ServiceException {
		Match match = commonDao.findAllEntities(Match.class).stream()
				.filter(m -> m.getRound().getIsGroupmatchAsBoolean())
				.collect(Collectors.toList())
				.get(0);
		Long teamId = match.getTeam2().getTeamId();
		
		Byte goal_normal1 = 1;
		Byte goal_normal2 = 1;
		Byte goal_extra1 = 2; 
		Byte goal_extra2 = 2;
		Byte goal_penalty1 = 3; 
		Byte goal_penalty2 = 2;
		match.setGoalNormalByTeam1(goal_normal1);
		match.setGoalNormalByTeam2(goal_normal2);
		match.setGoalExtraByTeam1(goal_extra1);
		match.setGoalExtraByTeam2(goal_extra2);
		match.setGoalPenaltyByTeam1(goal_penalty1);
		match.setGoalPenaltyByTeam2(goal_penalty2);
		
		int result = matchService.getMatchResult(match, teamId);

		assertEquals("Result should be equal to the expected one.", -1, result);
	}

	/**
	 * Test {@link MatchService#isMatchCompleted(boolean, Byte, Byte, Byte, 
			Byte, Byte, Byte)} method.
	 * Scenario: successfully returns value false because the input goal fields on the
	 * 		input group match determine an incomplete result.
	 */
	@Test
	public void /*boolean*/ isMatchCompletedGroupIncomplete(/*int side, Byte goal_normal1, Byte goal_extra1, Byte goal_penalty1, 
			Byte goal_normal2, Byte goal_extra2, Byte goal_penalty2*/) throws ServiceException {
		boolean is_groupmatch = true;
		Byte goal_normal1 = null;
		Byte goal_normal2 = null;
		Byte goal_extra1 = null; 
		Byte goal_extra2 = null;
		Byte goal_penalty1 = null; 
		Byte goal_penalty2 = null;

		boolean result = matchService.isMatchCompleted(is_groupmatch, goal_normal1, goal_extra1, goal_penalty1, 
				goal_normal2, goal_extra2, goal_penalty2);
		
		assertFalse("Result should be false.", result);
	}	

	/**
	 * Test {@link MatchService#isMatchCompleted(boolean, Byte, Byte, Byte, 
			Byte, Byte, Byte)} method.
	 * Scenario: successfully returns {@code true} value because the input goal fields on the
	 * 		input group match determine a complete result.
	 */
	@Test
	public void /*boolean*/ isMatchCompletedGroup(/*int side, Byte goal_normal1, Byte goal_extra1, Byte goal_penalty1, 
			Byte goal_normal2, Byte goal_extra2, Byte goal_penalty2*/) throws ServiceException {
		boolean is_groupmatch = true;
		Byte goal_normal1 = 2;
		Byte goal_normal2 = 2;
		Byte goal_extra1 = null; 
		Byte goal_extra2 = null;
		Byte goal_penalty1 = null; 
		Byte goal_penalty2 = null;

		boolean result = matchService.isMatchCompleted(is_groupmatch, goal_normal1, goal_extra1, goal_penalty1, 
				goal_normal2, goal_extra2, goal_penalty2);
		
		assertTrue("Result should be true.", result);
	}	

	/**
	 * Test {@link MatchService#isMatchCompleted(boolean, Byte, Byte, Byte, 
			Byte, Byte, Byte)} method.
	 * Scenario: successfully returns value false because the input goal fields on the
	 * 		input knockout match determine an incomplete result.
	 */
	@Test
	public void /*boolean*/ isMatchCompletedKnockoutIncomplete(/*int side, Byte goal_normal1, Byte goal_extra1, Byte goal_penalty1, 
			Byte goal_normal2, Byte goal_extra2, Byte goal_penalty2*/) throws ServiceException {
		boolean is_groupmatch = false;
		Byte goal_normal1 = 1;
		Byte goal_normal2 = 1;
		Byte goal_extra1 = 2; 
		Byte goal_extra2 = 2;
		Byte goal_penalty1 = 3; 
		Byte goal_penalty2 = 3;

		boolean result = matchService.isMatchCompleted(is_groupmatch, goal_normal1, goal_extra1, goal_penalty1, 
				goal_normal2, goal_extra2, goal_penalty2);
		
		assertFalse("Result should be false.", result);
	}	

	/**
	 * Test {@link MatchService#isMatchCompleted(boolean, Byte, Byte, Byte, 
			Byte, Byte, Byte)} method.
	 * Scenario: successfully returns {@code true} value because the input goal fields on the
	 * 		input knockout match determine a complete result.
	 */
	@Test
	public void /*boolean*/ isMatchCompletedKnockout(/*int side, Byte goal_normal1, Byte goal_extra1, Byte goal_penalty1, 
			Byte goal_normal2, Byte goal_extra2, Byte goal_penalty2*/) throws ServiceException {
		boolean is_groupmatch = false;
		Byte goal_normal1 = 2;
		Byte goal_normal2 = 2;
		Byte goal_extra1 = 2; 
		Byte goal_extra2 = 2;
		Byte goal_penalty1 = 4; 
		Byte goal_penalty2 = 2;

		boolean result = matchService.isMatchCompleted(is_groupmatch, goal_normal1, goal_extra1, goal_penalty1, 
				goal_normal2, goal_extra2, goal_penalty2);
		
		assertTrue("Result should be true.", result);
	}	

	/**
	 * Test {@link MatchService#isMatchCompleted(Match} private method.
	 * Scenario: successfully returns {@code true} value because the input match is finished.
	 */
	@Test
	public void /*boolean*/ isMatchCompleted_Finished(/*Match match*/) throws ServiceException {
		Match match = commonDao.findEntityById(Match.class, 1L); // group match without result
		match.setGoalNormalByTeam1((byte)0);
		match.setGoalNormalByTeam2((byte)0);
		
		boolean expectedMatchCompleted = true;
		boolean matchCompleted = matchService.isMatchCompleted(match);
		
		assertEquals("Result should be equal to the expected one.", expectedMatchCompleted, matchCompleted);
	}

	/**
	 * Test {@link MatchService#isMatchCompleted(Match} private method.
	 * Scenario: successfully returns {@code true} value because the input match is finished.
	 */
	@Test
	public void /*boolean*/ isMatchCompleted_NotFinished(/*Match match*/) throws ServiceException {
		Match match = commonDao.findEntityById(Match.class, 1L); // group match without result
		
		boolean expectedMatchCompleted = false;
		boolean matchCompleted = matchService.isMatchCompleted(match);
		
		assertEquals("Result should be equal to the expected one.", expectedMatchCompleted, matchCompleted);
	}

	/**
	 * Test {@link MatchService#isMatchCompleted(Match} private method.
	 * Scenario: throws {@link IllegalArgumentException} because the given match is {@code null}
	 */
	@Test(expected=IllegalArgumentException.class)
	public void /*boolean*/ isMatchCompleted_NullMatch(/*Match match*/) throws ServiceException {
		Match match = null;
		matchService.isMatchCompleted(match);
	}

	/**
	 * Test {@link MatchService#getScore(Long, Long, Long, Byte, Byte, Byte, Byte)} method.
	 * Scenario: successfully returns 0 value because the input team1Id and team2Id 
                        fields are {@code null} values.
	 */
	@Test
	public void /*int*/ getScoreTeamsNull(/*Long favTeamId, Long team1Id, Long team2Id, 
			Byte goalResult1, Byte goalResult2, Byte goalBet1, Byte goalBet2*/) throws ServiceException {
		Long favTeamId = null;
		Long team1Id = null;
		Long team2Id = null; 
		Byte goalResult1 = null;
		Byte goalResult2 = null;
		Byte goalBet1 = null;
		Byte goalBet2 = null;

		int result = matchService.getScore(favTeamId, team1Id, team2Id, 
				goalResult1, goalResult2, goalBet1, goalBet2);
		
		assertEquals("Result should be equal to the expected one.", 0, result);
	}	

	/**
	 * Test {@link MatchService#getScore(Long, Long, Long, Byte, Byte, Byte, Byte)} method.
	 * Scenario: successfully returns 0 value because the input goal fields are 
	 * 		{@code null} values.
	 */
	@Test
	public void /*int*/ getScoreGoalsNull(/*Long favTeamId, Long team1Id, Long team2Id, 
			Byte goalResult1, Byte goalResult2, Byte goalBet1, Byte goalBet2*/) throws ServiceException {
		Long favTeamId = null;
		Long team1Id = 1L;
		Long team2Id = 2L; 
		Byte goalResult1 = null;
		Byte goalResult2 = null;
		Byte goalBet1 = null;
		Byte goalBet2 = null;

		int result = matchService.getScore(favTeamId, team1Id, team2Id, 
				goalResult1, goalResult2, goalBet1, goalBet2);
		
		assertEquals("Result should be equal to the expected one.", 0, result);
	}	

	/**
	 * Test {@link MatchService#getScore(Long, Long, Long, Byte, Byte, Byte, Byte)} method.
	 * Scenario: successfully returns 3 value because the input fields determine 
	 * 		exact bet.
	 */
	@Test
	public void /*int*/ getScoreBetExact(/*Long favTeamId, Long team1Id, Long team2Id, 
			Byte goalResult1, Byte goalResult2, Byte goalBet1, Byte goalBet2*/) throws ServiceException {
		Long favTeamId = null;
		Long team1Id = 1L;
		Long team2Id = 2L; 
		Byte goalResult1 = 1;
		Byte goalResult2 = 0;
		Byte goalBet1 = 1;
		Byte goalBet2 = 0;

		int result = matchService.getScore(favTeamId, team1Id, team2Id, 
				goalResult1, goalResult2, goalBet1, goalBet2);
		
		assertEquals("Result should be equal to the expected one.", 3, result);
	}	

	/**
	 * Test {@link MatchService#getScore(Long, Long, Long, Byte, Byte, Byte, Byte)} method.
	 * Scenario: successfully returns 6 value because the input fields determine 
	 * 		exact bet and favourite team is involved.
	 */
	@Test
	public void /*int*/ getScoreBetExactWithFavouriteTeam(/*Long favTeamId, Long team1Id, Long team2Id, 
			Byte goalResult1, Byte goalResult2, Byte goalBet1, Byte goalBet2*/) throws ServiceException {
		Long favTeamId = 1L;
		Long team1Id = 1L;
		Long team2Id = 2L; 
		Byte goalResult1 = 1;
		Byte goalResult2 = 0;
		Byte goalBet1 = 1;
		Byte goalBet2 = 0;

		int result = matchService.getScore(favTeamId, team1Id, team2Id, 
				goalResult1, goalResult2, goalBet1, goalBet2);
		
		assertEquals("Result should be equal to the expected one.", 6, result);
	}	

	/**
	 * Test {@link MatchService#getScore(Long, Long, Long, Byte, Byte, Byte, Byte)} method.
	 * Scenario: successfully returns 2 value because the input fields determine 
	 * 		almost exact bet, which means goal difference is exact.
	 */
	@Test
	public void /*int*/ getScoreBetAlmost(/*Long favTeamId, Long team1Id, Long team2Id, 
			Byte goalResult1, Byte goalResult2, Byte goalBet1, Byte goalBet2*/) throws ServiceException {
		Long favTeamId = null;
		Long team1Id = 1L;
		Long team2Id = 2L; 
		Byte goalResult1 = 1;
		Byte goalResult2 = 0;
		Byte goalBet1 = 2;
		Byte goalBet2 = 1;

		int result = matchService.getScore(favTeamId, team1Id, team2Id, 
				goalResult1, goalResult2, goalBet1, goalBet2);
		
		assertEquals("Result should be equal to the expected one.", 2, result);
	}	

	/**
	 * Test {@link MatchService#getScore(Long, Long, Long, Byte, Byte, Byte, Byte)} method.
	 * Scenario: successfully returns 4 value because the input fields determine 
	 * 		exact bet, which means goal difference is exact. Moreover favourite team is
	 * 		involved.
	 */
	@Test
	public void /*int*/ getScoreBetAlmostWithFavouriteTeam(/*Long favTeamId, Long team1Id, Long team2Id, 
			Byte goalResult1, Byte goalResult2, Byte goalBet1, Byte goalBet2*/) throws ServiceException {
		Long favTeamId = 2L;
		Long team1Id = 1L;
		Long team2Id = 2L; 
		Byte goalResult1 = 1;
		Byte goalResult2 = 0;
		Byte goalBet1 = 2;
		Byte goalBet2 = 1;

		int result = matchService.getScore(favTeamId, team1Id, team2Id, 
				goalResult1, goalResult2, goalBet1, goalBet2);
		
		assertEquals("Result should be equal to the expected one.", 4, result);
	}	

	/**
	 * Test {@link MatchService#getScore(Long, Long, Long, Byte, Byte, Byte, Byte)} method.
	 * Scenario: successfully returns 1 value because the input fields determine 
	 * 		at least the winner of the match.
	 */
	@Test
	public void /*int*/ getScoreBetOutcome(/*Long favTeamId, Long team1Id, Long team2Id, 
			Byte goalResult1, Byte goalResult2, Byte goalBet1, Byte goalBet2*/) throws ServiceException {
		Long favTeamId = null;
		Long team1Id = 1L;
		Long team2Id = 2L; 
		Byte goalResult1 = 1;
		Byte goalResult2 = 0;
		Byte goalBet1 = 2;
		Byte goalBet2 = 0;

		int result = matchService.getScore(favTeamId, team1Id, team2Id, 
				goalResult1, goalResult2, goalBet1, goalBet2);
		
		assertEquals("Result should be equal to the expected one.", 1, result);
	}	

	/**
	 * Test {@link MatchService#getScore(Long, Long, Long, Byte, Byte, Byte, Byte)} method.
	 * Scenario: successfully returns 2 value because the input fields determine 
	 * 		at least the winner of the match. Moreover favourite team is involved.
	 */
	@Test
	public void /*int*/ getScoreBetOutcomeWithFavouriteTeam(/*Long favTeamId, Long team1Id, Long team2Id, 
			Byte goalResult1, Byte goalResult2, Byte goalBet1, Byte goalBet2*/) throws ServiceException {
		Long favTeamId = 2L;
		Long team1Id = 1L;
		Long team2Id = 2L; 
		Byte goalResult1 = 1;
		Byte goalResult2 = 0;
		Byte goalBet1 = 2;
		Byte goalBet2 = 0;

		int result = matchService.getScore(favTeamId, team1Id, team2Id, 
				goalResult1, goalResult2, goalBet1, goalBet2);
		
		assertEquals("Result should be equal to the expected one.", 2, result);
	}	

	/**
	 * Test {@link MatchService#getEndDateTime(Date)} method.
	 * Scenario: throws {@link IllegalArgumentException} because of the input startTime
	 * 		parameter is {@code null} value. 
	 */
	@Test(expected=IllegalArgumentException.class)
	public void /*Date*/ getEndDateTimeNull(/*LocalDateTime startTime*/) throws ServiceException {
		LocalDateTime startTime = null;

		/*LocalDateTime result =*/ matchService.getEndDateTime(startTime);
	}	

	/**
	 * Test {@link MatchService#getEndDateTime(Date)} private method.
	 * Scenario: successfully returns the proper {@code Date} value.
	 * @throws ParseException 
	 */
	@Test
	public void /*Date*/ getEndDateTime(/*LocalDateTime startTime*/) throws ServiceException, ParseException {
		LocalDateTime startTime = CommonUtil.parseDateTime("2017-02-22 11:00");
		LocalDateTime expectedResult = CommonUtil.parseDateTime("2017-02-22 12:45");

		LocalDateTime result = matchService.getEndDateTime(startTime);

		assertEquals("Result should be equal to the expected one.", expectedResult, result);
	}	

	/**
	 * Test {@link MatchService#getExtraDateTime(Date)} method.
	 * Scenario: throws {@link IllegalArgumentException} because of the input startTime
	 * 		parameter is {@code null} value. 
	 */
	@Test(expected=IllegalArgumentException.class)
	public void /*Date*/ getExtraDateTimeNull(/*LocalDateTime startTime*/) throws ServiceException {
		LocalDateTime startTime = null;

		/*LocalDateTime result =*/ matchService.getExtraDateTime(startTime);
	}	

	/**
	 * Test {@link MatchService#getExtraDateTime(Date)} private method.
	 * Scenario: successfully returns the proper {@code Date} value.
	 * @throws ParseException 
	 */
	@Test
	public void /*Date*/ getExtraDateTime(/*LocalDateTime startTime*/) throws ServiceException, ParseException {
		LocalDateTime startTime = CommonUtil.parseDateTime("2017-02-22 11:00");
		LocalDateTime expectedResult = CommonUtil.parseDateTime("2017-02-22 13:20");

		LocalDateTime result = matchService.getExtraDateTime(startTime);

		assertEquals("Result should be equal to the expected one.", expectedResult, result);
	}	

	/**
	 * Test {@link MatchService#getPenaltyDateTime(Date)} method.
	 * Scenario: throws {@link IllegalArgumentException} because of the input startTime
	 * 		parameter is {@code null} value. 
	 */
	@Test(expected=IllegalArgumentException.class)
	public void /*Date*/ getPenaltyDateTimeNull(/*LocalDateTime startTime*/) throws ServiceException {
		LocalDateTime startTime = null;

		/*LocalDateTime result =*/ matchService.getPenaltyDateTime(startTime);
	}	

	/**
	 * Test {@link MatchService#getPenaltyDateTime(Date)} private method.
	 * Scenario: successfully returns the proper {@code Date} value.
	 * @throws ParseException 
	 */
	@Test
	public void /*Date*/ getPenaltyDateTime(/*LocalDateTime startTime*/) throws ServiceException, ParseException {
		LocalDateTime startTime = CommonUtil.parseDateTime("2017-02-22 11:00");
		LocalDateTime expectedResult = CommonUtil.parseDateTime("2017-02-22 13:30");

		LocalDateTime result = matchService.getPenaltyDateTime(startTime);

		assertEquals("Result should be equal to the expected one.", expectedResult, result);
	}	

	/**
	 * Test {@link MatchService#retrieveGroupPositionsOfParticipantRules(Long)} private method.
	 * Scenario: throws {@link IllegalArgumentException} because of the input eventId
	 * 		parameter is {@code null} value. 
	 */
	@Test(expected=IllegalArgumentException.class)
	public void /*List<GroupPosition>*/ retrieveGroupPositionsOfParticipantRulesNull(/*Long eventId*/) throws /*Service*/Exception {
		Long eventId = null;

		Mockito.when(matchDao.retrieveParticipantRulesOfMatchesByEvent(eventId)).thenThrow(new IllegalArgumentException());
		
		// Private method cannot be invoked so we need some java reflection magic
		/*List<GroupPosition> result =*/ matchService.retrieveGroupPositionsOfParticipantRules(eventId);
	}	

	/**
	 * Test {@link MatchService#retrieveGroupPositionsOfParticipantRules(Long)} private method.
	 * Scenario: successfully returns the empty result list because of the given eventId parameter 
	 * 		is unknown.
	 */
	@Test
	public void /*List<GroupPosition>*/ retrieveGroupPositionsOfParticipantRulesUnknown(/*Long eventId*/) throws /*Service*/Exception {
		Long eventId = -1L;
		List<GroupPosition> expectedResult = new ArrayList<>();

		Mockito.when(matchDao.retrieveParticipantRulesOfMatchesByEvent(eventId)).thenReturn(new ArrayList<>());
		
		// Private method cannot be invoked so we need some java reflection magic
		List<GroupPosition> result = matchService.retrieveGroupPositionsOfParticipantRules(eventId);
		
		assertEquals("Result should be equal to the expected one.", expectedResult, result);
	}	

	/**
	 * Test {@link MatchService#retrieveGroupPositionsOfParticipantRules(Long)} private method.
	 * Scenario: successfully returns the expected list.
	 */
	@Test
	public void /*Date*/ retrieveGroupPositionsOfParticipantRules(/*Long eventId*/) throws /*Service*/Exception {
		Long eventId = -1L;

		List<String> participantRulesOfMatchesByEvent = Arrays.asList(
				"A2-C2",
				"B1-ACD3",
				"D1-BEF3",
				"F1-E2",
				"W37-W39",
				"L49-L50"
		);
		Mockito.when(matchDao.retrieveParticipantRulesOfMatchesByEvent(eventId)).thenReturn(participantRulesOfMatchesByEvent);

		List<GroupPosition> expectedResult = Arrays.asList(
				new GroupPosition("W", 37), new GroupPosition("W", 39),
				new GroupPosition("L", 49), new GroupPosition("L", 50)
		);
		
		
		// Private method cannot be invoked so we need some java reflection magic
		List<GroupPosition> result = matchService.retrieveGroupPositionsOfParticipantRules(eventId);
		
		assertEquals("Result should be equal to the expected one.", expectedResult, result);
	}	

	/**
	 * Test {@link MatchService#getTeamByGroupPositionMap(Long)} private method.
	 * Scenario: throws {@link IllegalArgumentException} because of the input eventId
	 * 		parameter is {@code null} value. 
	 */
	@Test(expected=IllegalArgumentException.class)
	public void /*List<GroupPosition>*/ getTeamByGroupPositionMapNull(/*Long eventId*/) throws /*Service*/Exception {
		Long eventId = null;

		Mockito.when(matchDao.retrieveParticipantRulesOfMatchesByEvent(eventId)).thenThrow(new IllegalArgumentException());
		
		// Private method cannot be invoked so we need some java reflection magic
		/*List<GroupPosition> result =*/ matchService.retrieveGroupPositionsOfParticipantRules(eventId);
	}	

	/**
	 * Test {@link MatchService#getTeamByGroupPositionMap(Long)} private method.
	 * Scenario: throws {@link IllegalArgumentException} because of the input eventId
	 * 		parameter belongs to no {@link Event} instance. 
	 */
	@Test
	public void /*List<GroupPosition>*/ getTeamByGroupPositionMapUnknown(/*Long eventId*/) throws /*Service*/Exception {
		Long eventId = -1L;
		List<GroupPosition> expectedResult = new ArrayList<>();

		Mockito.when(matchDao.retrieveParticipantRulesOfMatchesByEvent(eventId)).thenReturn(new ArrayList<>());
		
		// Private method cannot be invoked so we need some java reflection magic
		List<GroupPosition> result = matchService.retrieveGroupPositionsOfParticipantRules(eventId);
		
		assertEquals("Result should be equal to the expected one.", expectedResult, result);
	}	

	/**
	 * Test {@link MatchService#getTeamByGroupPositionMap(Long)} private method.
	 * Scenario: successfully retrieves the expected result. 
	 */
	@Test
	public void /*List<GroupPosition>*/ getTeamByGroupPositionMap(/*Long eventId*/) throws /*Service*/Exception {
		Long eventId = -1L;
		List<GroupPosition> expectedResult = new ArrayList<>();
		expectedResult.add(new GroupPosition("W", 37));
		expectedResult.add(new GroupPosition("W", 39));
		expectedResult.add(new GroupPosition("L", 49));
		expectedResult.add(new GroupPosition("L", 50));

		List<String> participantRulesOfMatchesByEvent = Arrays.asList(
				"A2-C2",
				"B1-ACD3",
				"D1-BEF3",
				"F1-E2",
				"W37-W39",
				"L49-L50"
		);
		Mockito.when(matchDao.retrieveParticipantRulesOfMatchesByEvent(eventId)).thenReturn(participantRulesOfMatchesByEvent);
		
		List<GroupPosition> result = matchService.retrieveGroupPositionsOfParticipantRules(eventId);

		assertEquals("Result should be equal to the expected one.", expectedResult, result);
	}	

	/**
	 * Test {@link MatchService#updateMatchParticipants(Long, Long)} method.
	 * Scenario: throws {@link IllegalArgumentException} because of the input eventId
	 * 		parameter is {@code null} value and the given updatedmatchid belongs to group match. 
	 */
	@Test(expected=IllegalArgumentException.class)
	public void /*int*/ updateMatchParticipantsGroupMatchEventIdNull(/*Long eventId, Long updatedMatchId*/) throws ServiceException {
		Long eventId = null;
		Long updatedMatchId = 1L;	

		// Private method cannot be invoked so we need some java reflection magic
		/*int result =*/ matchService.updateMatchParticipants(eventId, updatedMatchId);
	}	

	/**
	 * Test {@link MatchService#updateMatchParticipants(Long, Long)} method.
	 * Scenario: throws {@link IllegalArgumentException} because of the input eventId
	 * 		parameter is {@code null} value and the given updatedmatchid belongs to knock-out match. 
	 */
	@Test(expected=IllegalArgumentException.class)
	public void /*int*/ updateMatchParticipantsKnockoutMatchEventIdNull(/*Long eventId, Long updatedMatchId*/) throws ServiceException {
		Long eventId = null;
		Long updatedMatchId = 50L;	

		Mockito.when(matchDao.retrieveParticipantRulesOfMatchesByEvent(eventId)).thenThrow(new IllegalArgumentException());
		
		// Private method cannot be invoked so we need some java reflection magic
		/*int result =*/ matchService.updateMatchParticipants(eventId, updatedMatchId);
	}	

	/**
	 * Test {@link MatchService#updateMatchParticipants(Long, Long)} method.
	 * Scenario: throws {@link IllegalArgumentException} because of the input updatedMatchId
	 * 		parameter is {@code null} value. 
	 */
	@Test(expected=IllegalArgumentException.class)
	public void /*int*/ updateMatchParticipantsUpdatedMatchIdNull(/*Long eventId, Long updatedMatchId*/) throws ServiceException {
		Long eventId = 1L;
		Long updatedMatchId = null;	

		/*int result =*/ matchService.updateMatchParticipants(eventId, updatedMatchId);
	}	

	/**
	 * Test {@link MatchService#updateMatchParticipants(Long, Long)} method.
	 * Scenario: throws {@link IllegalArgumentException} because of the input updatedMatchId
	 * 		parameter is orphan value. 
	 */
	@Test(expected=ServiceException.class)
	public void /*int*/ updateMatchParticipantsUpdatedMatchIdUnknown(/*Long eventId, Long updatedMatchId*/) throws ServiceException {
		Long eventId = 1L;
		Long updatedMatchId = -1L;	

		try {
			/*int result =*/ matchService.updateMatchParticipants(eventId, updatedMatchId);
		}
		catch (ServiceException e) {
			assertTrue("There must be a single message in ServiceException named MISSING_MATCH", 
					e.getMessages().size()==1 && e.getMessages().get(0).getMsgCode().equals("MISSING_MATCH"));
			throw e;
		}
	}	

	/**
	 * Test {@link MatchService#updateMatchParticipants(Long, Long)} method.
	 * Scenario: successfully retrieves the expected result. 
	 */
	@Test
	public void /*int*/ updateMatchParticipants(/*Long eventId, Long updatedMatchId*/) throws /*Service*/Exception {
		Long eventId = 1L;
		Long updatedMatchId = 62L;
		
		int expectedResult = 2;

		List<String> participantRulesOfMatchesByEvent = Arrays.asList(
				"L61-L62", // belongs to matchId = 63
				"W61-W62" // belongs to matchId = 64
		);
		Mockito.when(matchDao.retrieveParticipantRulesOfMatchesByEvent(eventId)).thenReturn(participantRulesOfMatchesByEvent);
		
		Long matchId = 61L;
		Match match = commonDao.findEntityById(Match.class, matchId); 
		Team team61_1 = commonDao.findEntityById(Team.class, 1L);
		match.setTeam1(team61_1);
		Team team61_2 = commonDao.findEntityById(Team.class, 2L);
		match.setTeam2(team61_2);
		match.setGoalNormalByTeam1((byte)1);
		match.setGoalNormalByTeam2((byte)2);
		Mockito.when(matchDao.retrieveMatchByMatchN(eventId, matchId.shortValue())).thenReturn(match);
		
		/*Long*/ matchId = 62L;
		/*Match*/ match = commonDao.findEntityById(Match.class, matchId);
		Team team62_1 = commonDao.findEntityById(Team.class, 3L);
		match.setTeam1(team62_1);
		Team team62_2 = commonDao.findEntityById(Team.class, 4L);
		match.setTeam2(team62_2);
		match.setGoalNormalByTeam1((byte)1);
		match.setGoalNormalByTeam2((byte)0);
		Mockito.when(matchDao.retrieveMatchByMatchN(eventId, matchId.shortValue())).thenReturn(match);

		Match match1 = commonDao.findEntityById(Match.class, 63L);
		Match match2 = commonDao.findEntityById(Match.class, 64L);
		List<Match> orphanMatches = Arrays.asList(match1, match2);
		Mockito.when(matchDao.retrieveMatchesWithoutParticipantsByEvent(eventId)).thenReturn(orphanMatches);
		
		int result = matchService.updateMatchParticipants(eventId, updatedMatchId);

		assertEquals("Result should be equal to the expected one.", expectedResult, result);
		
		/*Match*/ match1 = commonDao.findEntityById(Match.class, 63L);
		assertEquals("Match for 3rd position should have the expected participant team #1", team61_1, match1.getTeam1());
		assertEquals("Match for 3rd position should have the expected participant team #2", team62_2, match1.getTeam2());

		/*Match*/ match2 = commonDao.findEntityById(Match.class, 64L);
		assertEquals("Match for 1st position should have the expected participant team #1", team61_2, match2.getTeam1());
		assertEquals("Match for 1st position should have the expected participant team #2", team62_1, match2.getTeam2());
	}	

	/**
	 * Test {@link MatchService#getFinishedMatchEndTime(Match)} private method.
	 * Scenario: successfully retrieves {@code null} because of the given group
	 *           match is not finished yet, it has no final result. 
	 */
	@Test
	public void /*Date*/ getFinishedMatchEndTime_GroupIncomplete(/*Match match*/) throws ServiceException {
		Long matchId = 1L; // WC2014, group match
		Match match = commonDao.findEntityById(Match.class, matchId);
		LocalDateTime matchEndTime = matchService.getFinishedMatchEndTime(match);
		assertNull("Result should be null.", matchEndTime);
	}

	/**
	 * Test {@link MatchService#getFinishedMatchEndTime(Match)} private method.
	 * Scenario: successfully retrieves {@code null} because of the given knock-out
	 *           match is not finished yet, it has no final result. 
	 */
	@Test
	public void /*Date*/ getFinishedMatchEndTime_KnockOutIncomplete(/*Match match*/) throws ServiceException {
		Long matchId = 61L; // WC2014, semi-final match
		Match match = commonDao.findEntityById(Match.class, matchId);
		LocalDateTime matchEndTime = matchService.getFinishedMatchEndTime(match);
		assertNull("Result should be null.", matchEndTime);
	}

	/**
	 * Test {@link MatchService#getFinishedMatchEndTime(Match)} private method.
	 * Scenario: throws {@link IllegalArgumentException} because the given match
	 *           parameter is null. 
	 */
	@Test(expected=IllegalArgumentException.class)
	public void /*Date*/ getFinishedMatchEndTime_NullMatch(/*Match match*/) throws ServiceException {
		Match match = null;
		matchService.getFinishedMatchEndTime(match);
	}

	/**
	 * Test {@link MatchService#getFinishedMatchEndTime(Match)} private method.
	 * Scenario: successfully retrieves result of the given finished group match
	 */
	@Test
	public void /*Date*/ getFinishedMatchEndTime_GroupComplete(/*Match match*/) throws ServiceException {
		Long matchId = 1L; // WC2014, group match
		Match match = commonDao.findEntityById(Match.class, matchId);
		match.setGoalNormalByTeam1((byte)0);
		match.setGoalNormalByTeam2((byte)0);
		
		LocalDateTime expectedMatchEndTime = matchService.getEndDateTime(match.getStartTime());
		LocalDateTime matchEndTime = matchService.getFinishedMatchEndTime(match);
		assertEquals("Result should be equal to the expected one.", expectedMatchEndTime, matchEndTime);
	}

	/**
	 * Test {@link MatchService#getFinishedMatchEndTime(Match)} private method.
	 * Scenario: successfully retrieves result of the given in normal play finished 
	 *           knock-out match
	 */
	@Test
	public void /*Date*/ getFinishedMatchEndTime_KnockOutNormalComplete(/*Match match*/) throws ServiceException {
		Long matchId = 61L; // WC2014, semi-final match
		Match match = commonDao.findEntityById(Match.class, matchId);
		match.setGoalNormalByTeam1((byte)1);
		match.setGoalNormalByTeam2((byte)0);
		
		LocalDateTime expectedMatchEndTime = matchService.getEndDateTime(match.getStartTime());
		LocalDateTime matchEndTime = matchService.getFinishedMatchEndTime(match);
		assertEquals("Result should be equal to the expected one.", expectedMatchEndTime, matchEndTime);
	}

	/**
	 * Test {@link MatchService#getFinishedMatchEndTime(Match)} private method.
	 * Scenario: successfully retrieves result of the given in extra play finished 
	 *           knock-out match
	 */
	@Test
	public void /*Date*/ getFinishedMatchEndTime_KnockOutExtraComplete(/*Match match*/) throws ServiceException {
		Long matchId = 61L; // WC2014, semi-final match
		Match match = commonDao.findEntityById(Match.class, matchId);
		match.setGoalNormalByTeam1((byte)0);
		match.setGoalNormalByTeam2((byte)0);
		match.setGoalExtraByTeam1((byte)1);
		match.setGoalExtraByTeam2((byte)0);
		
		LocalDateTime expectedMatchEndTime = matchService.getExtraDateTime(match.getStartTime());
		LocalDateTime matchEndTime = matchService.getFinishedMatchEndTime(match);
		assertEquals("Result should be equal to the expected one.", expectedMatchEndTime, matchEndTime);
	}

	/**
	 * Test {@link MatchService#getFinishedMatchEndTime(Match)} private method.
	 * Scenario: successfully retrieves result of the given in penalty play finished 
	 *           knock-out match
	 */
	@Test
	public void /*Date*/ getFinishedMatchEndTime_KnockOutPenaltyComplete(/*Match match*/) throws ServiceException {
		Long matchId = 61L; // WC2014, semi-final match
		Match match = commonDao.findEntityById(Match.class, matchId);
		match.setGoalNormalByTeam1((byte)0);
		match.setGoalNormalByTeam2((byte)0);
		match.setGoalExtraByTeam1((byte)0);
		match.setGoalExtraByTeam2((byte)0);
		match.setGoalPenaltyByTeam1((byte)1);
		match.setGoalPenaltyByTeam2((byte)0);
		
		LocalDateTime expectedMatchEndTime = matchService.getPenaltyDateTime(match.getStartTime());
		LocalDateTime matchEndTime = matchService.getFinishedMatchEndTime(match);
		assertEquals("Result should be equal to the expected one.", expectedMatchEndTime, matchEndTime);
	}
	
	/**
	 * Test {@link MatchService#getMatchResultEscalationTime(Match)} private method.
	 * Scenario: throws {@link IllegalArgumentException} because of the given {@code null} match parameter
	 */
	@Test(expected=IllegalArgumentException.class)
	public void /*Date*/ getMatchResultEscalationTime_NullMatch(/*Match match*/) throws ServiceException {
		Match match = null;
		matchService.getMatchResultEscalationTime(match);
	}

	/**
	 * Test {@link MatchService#getMatchResultEscalationTime(Match)} private method.
	 * Scenario: retrieves escalation time of a match which has not result yet in normal play    
	 */
	@Test
	public void /*Date*/ getMatchResultEscalationTime_WithoutNormal(/*Match match*/) throws ServiceException {
		Long matchId = 1L; // WC2014, group match
		Match match = commonDao.findEntityById(Match.class, matchId);

		LocalDateTime expectedMatchEscalationTime = matchService.getEndDateTime(match.getStartTime());
		LocalDateTime matchEscalationTime = matchService.getMatchResultEscalationTime(match);
		assertEquals("Result should be equal to the expected one.", expectedMatchEscalationTime, matchEscalationTime);
	}

	/**
	 * Test {@link MatchService#getMatchResultEscalationTime(Match)} private method.
	 * Scenario: Scenario: retrieves escalation time of a knock-out match which has 
	 *           draw result in normal play but no result yet in extra play.
	 */
	@Test
	public void /*Date*/ getMatchResultEscalationTime_KnockOutWithoutExtra(/*Match match*/) throws ServiceException {
		Long matchId = 61L; // WC2014, semi-final
		Match match = commonDao.findEntityById(Match.class, matchId);
		match.setGoalNormalByTeam1((byte)0);
		match.setGoalNormalByTeam2((byte)0);
		
		LocalDateTime expectedMatchEscalationTime = matchService.getExtraDateTime(match.getStartTime());
		LocalDateTime matchEscalationTime = matchService.getMatchResultEscalationTime(match);
		assertEquals("Result should be equal to the expected one.", expectedMatchEscalationTime, matchEscalationTime);
	}

	/**
	 * Test {@link MatchService#getMatchResultEscalationTime(Match)} private method.
	 * Scenario: Scenario: retrieves escalation time of a knock-out match which has 
	 *           draw results in normal and extra play but no result yet in penalty play.
	 */
	@Test
	public void /*Date*/ getMatchResultEscalationTime_KnockOutWithoutPenalty(/*Match match*/) throws ServiceException {
		Long matchId = 61L; // WC2014, semi-final
		Match match = commonDao.findEntityById(Match.class, matchId);
		match.setGoalNormalByTeam1((byte)0);
		match.setGoalNormalByTeam2((byte)0);
		match.setGoalExtraByTeam1((byte)0);
		match.setGoalExtraByTeam2((byte)0);
		
		LocalDateTime expectedMatchEscalationTime = matchService.getPenaltyDateTime(match.getStartTime());
		LocalDateTime matchEscalationTime = matchService.getMatchResultEscalationTime(match);
		assertEquals("Result should be equal to the expected one.", expectedMatchEscalationTime, matchEscalationTime);
	}

	/**
	 * Test {@link MatchService#getMatchParticipantsEscalationTime(Match)} private method.
	 * Scenario: throws {@link IllegalArgumentException} because of the given {@code null} match parameter
	 */
	@Test(expected=IllegalArgumentException.class)
	public void /*Date*/ getMatchParticipantsEscalationTime_NullMatch(/*Match match*/) throws ServiceException {
		Match match = null;
		matchService.getMatchParticipantsEscalationTime(match);
	}

	/**
	 * Test {@link MatchService#getMatchParticipantsEscalationTime_BothTeams(Match)} private method.
	 * Scenario: retrieves {@code null} because there are both not {@code null} team
	 *           participants of the given match 
	 */
	@Test
	public void /*Date*/ getMatchParticipantsEscalationTime_BothTeams(/*Match match*/) throws ServiceException {
		Long matchId = 1L; // (WC2014) group match - has always team participants
		Match match = commonDao.findEntityById(Match.class, matchId);
		
		LocalDateTime matchEndTime = matchService.getMatchParticipantsEscalationTime(match);
		assertNull("Result should be null.", matchEndTime);
	}

	/**
	 * Test {@link MatchService#getFinishedMatchEndTime(Match)} private method.
	 * Scenario: throws IllegalStateException because one of the given group stage 
	 *           match has no team participant. Normally this cannot be happened. 
	 */
	@Test(expected=IllegalStateException.class)
	public void /*Date*/ getMatchParticipantsEscalationTime_GroupMissingTeam(/*Match match*/) throws ServiceException {
		Long matchId = 1L; // (WC2014) group match - has always team participants
		Match match = commonDao.findEntityById(Match.class, matchId);
		match.setTeam1(null);
		
		matchService.getMatchParticipantsEscalationTime(match);
	}

	/**
	 * Test {@link MatchService#getFinishedMatchEndTime(Match)} private method.
	 * Scenario: retrieves escalation time of a knock-out match with group participant rule
	 */
	@Test
	public void /*Date*/ getMatchParticipantsEscalationTime_KnockOutGroup(/*Match match*/) throws ServiceException {
		Long matchId = 49L; // (WC2014) knock-out match with group participant rule: A1-B2
		Match match = commonDao.findEntityById(Match.class, matchId);

		Long groupId = 1L; // group A
		List<Long> matchIds = Arrays.asList(1L, 2L, 16L, 20L, 35L, 36L); // group A
		List<Match> matches = matchIds.stream().map(e -> commonDao.findEntityById(Match.class, e)).collect(Collectors.toList()) ;
		Mockito.when(matchDao.retrieveMatchesByGroup(groupId)).thenReturn(matches);
		
		// match with greatest id from the lists contains the latest datetime
		LocalDateTime expectedMatchEscalationTime = matchService.getEndDateTime(matches.get(5).getStartTime());
		
		groupId = 2L; // group B
		matchIds = Arrays.asList(3L, 4L, 18L, 19L, 33L, 34L); // group B
		matches = matchIds.stream().map(e -> commonDao.findEntityById(Match.class, e)).collect(Collectors.toList()) ;
		Mockito.when(matchDao.retrieveMatchesByGroup(groupId)).thenReturn(matches);
		
		LocalDateTime matchEscalationTime = matchService.getMatchParticipantsEscalationTime(match);
		assertEquals("Result should be equal to the expected one.", expectedMatchEscalationTime, matchEscalationTime);
	}

	/**
	 * Test {@link MatchService#getFinishedMatchEndTime(Match)} private method.
	 * Scenario: retrieves escalation time of a knock-out match with non group participant rule
	 */
	@Test
	public void /*Date*/ getMatchParticipantsEscalationTime_KnockOut(/*Match match*/) throws ServiceException {
		Long matchId = 61L; // (WC2014) semi-final match with non group participant rule: W57-W58
		Match match = commonDao.findEntityById(Match.class, matchId);

		Match parentMatch1 = commonDao.findEntityById(Match.class, 57L);
		Mockito.when(matchDao.retrieveMatchByMatchN(match.getEvent().getEventId(), (short)57L)).thenReturn(parentMatch1);
		Match parentMatch2 = commonDao.findEntityById(Match.class, 58L);
		Mockito.when(matchDao.retrieveMatchByMatchN(match.getEvent().getEventId(), (short)58L)).thenReturn(parentMatch2);

		// 2nd parent match is the latest
		LocalDateTime expectedMatchEscalationTime = matchService.getEndDateTime(parentMatch2.getStartTime());
		LocalDateTime matchEscalationTime = matchService.getMatchParticipantsEscalationTime(match);
		assertEquals("Result should be equal to the expected one.", expectedMatchEscalationTime, matchEscalationTime);
	}

	/**
	 * Test {@link MatchService#getMatchTriggerStartTime(Match, Date)} method.
	 * Scenario: throws {@link IllegalArgumentException} because of the given {@code null} match parameter
	 */
	@Test(expected=IllegalArgumentException.class)
	public void /*Date*/ getMatchTriggerStartTime_NullMatch(/*Match match, LocalDateTime actualDateTime*/) throws ServiceException {
		Match match = null;
		LocalDateTime actualDateTime = LocalDateTime.now();

		matchService.getMatchTriggerStartTime(match, actualDateTime);
	}

	/**
	 * Test {@link MatchService#getMatchTriggerStartTime(Match, Date)} method.
	 * Scenario: throws {@link IllegalArgumentException} because of the given {@code null} match parameter
	 */
	@Test(expected=IllegalArgumentException.class)
	public void /*Date*/ getMatchTriggerStartTime_NullActualDateTime(/*Match match, LocalDateTime actualDateTime*/) throws ServiceException {
		Long matchId = 1L;
		Match match = commonDao.findEntityById(Match.class, matchId);
		LocalDateTime actualDateTime = null;

		matchService.getMatchTriggerStartTime(match, actualDateTime);
	}

	/**
	 * Test {@link MatchService#getMatchTriggerStartTime(Match, Date)} method.
	 * Scenario: throws {@link IllegalStateException} because of the given group match
	 *           is without participant team 
	 */
	@Test(expected=IllegalStateException.class)
	public void /*Date*/ getMatchTriggerStartTime_GroupMatchWithoutTeam(/*Match match, LocalDateTime actualDateTime*/) throws ServiceException {
		Long matchId = 1L; // WC2014 group match
		Match match = commonDao.findEntityById(Match.class, matchId);
		match.setTeam1(null);
		LocalDateTime actualDateTime = LocalDateTime.now();

		//Mockito.when(matchService.getMatchTriggerStartTime(match, actualDateTime)).thenCallRealMethod();
		matchService.getMatchTriggerStartTime(match, actualDateTime);
	}

	/**
	 * Test {@link MatchService#getMatchTriggerStartTime(Match, Date)} method.
	 * Scenario: retrieves escalation time of the given knock-out match on the given 
	 *           datetime. The given match lacks one of its participant teams. 
	 *           The given datetime is before by that time when the match gets to be 
	 *           complete.
	 */
	@Test
	public void /*Date*/ getMatchTriggerStartTime_KnockOutMatchWithoutParticipant(/*Match match, LocalDateTime actualDateTime*/) throws ServiceException, ParseException {
		Long matchId = 49L; // (WC2014) knock-out match without participant teams with group participant rule A1-B2
		Match match = commonDao.findEntityById(Match.class, matchId);
		LocalDateTime actualDateTime = CommonUtil.parseDateTime("2014-06-20 00:00");
		LocalDateTime expectedMatchTriggerStartTime = CommonUtil.parseDateTime("2014-06-23 21:45");

		// stub out dependencies, e.g. local helper methods. Mockito when...thenReturn cannot be used with Spy
		//Mockito.when(mockedMatchService.getMatchParticipantsEscalationTime(match)).thenReturn(expectedMatchTriggerStartTime);
		//Mockito.doReturn(expectedMatchTriggerStartTime).when(matchService).getMatchParticipantsEscalationTime(match);

		// stubbed methods used by helper methods of the tested method
		Long groupId = 1L;
		List<Long> matchIds = Arrays.asList(1L, 2L, 16L, 20L, 35L, 36L); // group A
		List<Match> matches = matchIds.stream().map(e -> commonDao.findEntityById(Match.class, e)).collect(Collectors.toList()) ;
		Mockito.when(matchDao.retrieveMatchesByGroup(groupId)).thenReturn(matches);
		
		groupId = 2L; // group B
		matchIds = Arrays.asList(3L, 4L, 18L, 19L, 33L, 34L); // group B
		matches = matchIds.stream().map(e -> commonDao.findEntityById(Match.class, e)).collect(Collectors.toList()) ;
		Mockito.when(matchDao.retrieveMatchesByGroup(groupId)).thenReturn(matches);
		
		LocalDateTime matchTriggerStartTime = matchService.getMatchTriggerStartTime(match, actualDateTime);
		
		assertEquals("Result should be equal to the expected one.", expectedMatchTriggerStartTime, matchTriggerStartTime);
	}
	
	/**
	 * Test {@link MatchService#getMatchTriggerStartTime(Match, Date)} method.
	 * Scenario: retrieves escalation time of the given knock-out match on the given 
	 *           datetime. The given match lacks one of its participant teams.
	 *           The given datetime is far after the tournament, so that will be
	 *           the returned result.
	 */
	@Test
	public void /*Date*/ getMatchTriggerStartTime_KnockOutMatchWithoutParticipantFuture(/*Match match, LocalDateTime actualDateTime*/) throws ServiceException, ParseException {
		Long matchId = 49L; // (WC2014) knock-out match without participant teams with group participant rule A1-B2
		Match match = commonDao.findEntityById(Match.class, matchId);
		LocalDateTime actualDateTime = CommonUtil.parseDateTime("2015-01-01 00:00");
		LocalDateTime expectedMatchTriggerStartTime = actualDateTime;

		// stubbed methods used by helper methods of the tested method
		Long groupId = 1L;
		List<Long> matchIds = Arrays.asList(1L, 2L, 16L, 20L, 35L, 36L); // group A
		List<Match> matches = matchIds.stream().map(e -> commonDao.findEntityById(Match.class, e)).collect(Collectors.toList()) ;
		Mockito.when(matchDao.retrieveMatchesByGroup(groupId)).thenReturn(matches);
		
		groupId = 2L; // group B
		matchIds = Arrays.asList(3L, 4L, 18L, 19L, 33L, 34L); // group B
		matches = matchIds.stream().map(e -> commonDao.findEntityById(Match.class, e)).collect(Collectors.toList()) ;
		Mockito.when(matchDao.retrieveMatchesByGroup(groupId)).thenReturn(matches);
		
		LocalDateTime matchTriggerStartTime = matchService.getMatchTriggerStartTime(match, actualDateTime);
		
		assertEquals("Result should be equal to the expected one.", expectedMatchTriggerStartTime, matchTriggerStartTime);
	}
	
	/**
	 * Test {@link MatchService#getMatchTriggerStartTime(Match, Date)} method.
	 * Scenario: retrieves escalation time of the given knock-out match on the given 
	 *           datetime. The given match lacks one of its participant teams. 
	 *           The given datetime is before by that time when the match gets to be 
	 *           complete.
	 */
	@Test
	public void /*Date*/ getMatchTriggerStartTime_GroupMatchWithoutResult(/*Match match, LocalDateTime actualDateTime*/) throws ServiceException, ParseException {
		Long matchId = 1L; // (WC2014) group match without result 
		Match match = commonDao.findEntityById(Match.class, matchId);
		LocalDateTime actualDateTime = CommonUtil.parseDateTime("2014-06-12 00:00");
		LocalDateTime expectedMatchTriggerStartTime = CommonUtil.parseDateTime("2014-06-12 21:45");

		LocalDateTime matchTriggerStartTime = matchService.getMatchTriggerStartTime(match, actualDateTime);
		
		assertEquals("Result should be equal to the expected one.", expectedMatchTriggerStartTime, matchTriggerStartTime);
	}
	
	/**
	 * Test {@link MatchService#getMatchTriggerStartTime(Match, Date)} method.
	 * Scenario: retrieves escalation time of the given knock-out match on the given 
	 *           datetime. The given match lacks one of its participant teams. 
	 *           The given datetime is far after the tournament.
	 */
	@Test
	public void /*Date*/ getMatchTriggerStartTime_GroupMatchWithoutResultFuture(/*Match match, LocalDateTime actualDateTime*/) throws ServiceException, ParseException {
		Long matchId = 1L; // (WC2014) group match without result 
		Match match = commonDao.findEntityById(Match.class, matchId);
		LocalDateTime actualDateTime = CommonUtil.parseDateTime("2015-01-01 00:00");
		LocalDateTime expectedMatchTriggerStartTime = actualDateTime;

		LocalDateTime matchTriggerStartTime = matchService.getMatchTriggerStartTime(match, actualDateTime);
		
		assertEquals("Result should be equal to the expected one.", expectedMatchTriggerStartTime, matchTriggerStartTime);
	}

	/**
	 * Test {@link MatchService#retrieveIncompleteMatchesByEvent(Long)} method.
	 * Scenario: throws {@link IllegalArgumentException} because of the given {@code null} eventId parameter
	 */
	@Test(expected=IllegalArgumentException.class)
	public void /*List<Match>*/ retrieveIncompleteMatchesByEventNull(/*Long eventId*/) throws ServiceException {
		Long eventId = null;
		matchService.retrieveIncompleteMatchesByEvent(eventId);
	}

	/**
	 * Test {@link MatchService#retrieveIncompleteMatchesByEvent(Long)} method.
	 * Scenario: retrieves result list of the given event. Because there are no results at all, 
	 *           the result list will contain all matches.
	 */
	@Test
	public void /*List<Match>*/ retrieveIncompleteMatchesByEvent(/*Long eventId*/) throws ServiceException {
		Long eventId = 1L; // WC2014
		
		List <Match> expectedMatches = commonDao.findAllEntities(Match.class);
		expectedMatches = expectedMatches.stream().filter(e -> e.getEvent().getEventId().equals(eventId)).collect(Collectors.toList());
		
		Mockito.when(matchDao.retrieveMatchesByEvent(eventId)).thenReturn(expectedMatches);
		
		List<Match> matches = matchService.retrieveIncompleteMatchesByEvent(eventId);
		
		assertEquals("Result list should be equal to the expected one.", expectedMatches, matches);
	}

	/**
	 * Test {@link MatchService#retrieveIncompleteMatchesByEvent(Long)} method.
	 * Scenario: retrieves result list of the given event. Because only one match has result, 
	 *           the result list will contain all matches but one.
	 */
	@Test
	public void /*List<Match>*/ retrieveIncompleteMatchesByEventButOne(/*Long eventId*/) throws ServiceException {
		Long eventId = 1L; // WC2014
		
		List <Match> expectedMatches = commonDao.findAllEntities(Match.class);
		expectedMatches = expectedMatches.stream().filter(e -> e.getEvent().getEventId().equals(eventId)).collect(Collectors.toList());
		Mockito.when(matchDao.retrieveMatchesByEvent(eventId)).thenReturn(expectedMatches);

		Match firstExpectedMatch = Iterables.firstOf(expectedMatches);
		firstExpectedMatch.setGoalNormalByTeam1((byte)1);
		firstExpectedMatch.setGoalNormalByTeam2((byte)0);
		expectedMatches.remove(firstExpectedMatch);
				
		List<Match> matches = matchService.retrieveIncompleteMatchesByEvent(eventId);
		
		assertEquals("Result list should be equal to the expected one.", expectedMatches, matches);
	}

	/**
	 * Test {@link MatchService#retrieveIncompleteEscalatedMatchesByEvent(Long, Date)} method.
	 * Scenario: retrieves result list of the given event and datetime.
	 * @throws ParseException 
	 */
	@Test
	public void /*List<Match>*/ retrieveIncompleteEscalatedMatchesByEvent(/*Long eventId, LocalDateTime actualDateTime*/) throws ServiceException, ParseException {
		Long eventId = 1L; // WC2014
		LocalDateTime actualDateTime = CommonUtil.parseDateTime("2014-06-14 14:00"); // before the 5th match of the event


		final List<Match> allMatches = commonDao.findAllEntities(Match.class).stream()
				.filter(e -> e.getEvent().getEventId().equals(eventId)).collect(Collectors.toList());
		Mockito.when(matchDao.retrieveMatchesByEvent(eventId)).thenReturn(allMatches);

		List<Match> expectedMatches = allMatches.stream().filter(e -> e.getStartTime().isBefore(actualDateTime)).collect(Collectors.toList());
		
//		Long groupId = 1L; // group A
//		List<Long> matchIds = Arrays.asList(1L, 2L, 16L, 20L, 35L, 36L); // group A
//		List<Match> matches = matchIds.stream().map(e -> commonDao.findEntityById(Match.class, e)).collect(Collectors.toList()) ;
//		Mockito.when(matchDao.retrieveMatchesByGroup(groupId)).thenReturn(matches);
//		
		LongStream.rangeClosed(49L, 62L).forEach(m -> {Match match = allMatches.stream().filter(e -> e.getMatchId().equals(m)).findFirst().get(); Mockito.when(matchDao.retrieveMatchByMatchN(eventId, (short)m)).thenReturn(match);});
		
		List<Match> matches = matchService.retrieveIncompleteEscalatedMatchesByEvent(eventId, actualDateTime);
		
		assertEquals("Result list should be equal to the expected one.", expectedMatches, matches);
	}

	/**
	 * Test {@link MatchService#retrieveIncompleteEscalatedMatchesByEvent(Long, Date)} method.
	 * Scenario: throws IllegalArgumentException because the given eventId is {@code null}.
	 */
	@Test(expected=IllegalArgumentException.class)
	public void /*List<Match>*/ retrieveIncompleteEscalatedMatchesByEvent_NullEventId(/*Long eventId, LocalDateTime actualDateTime*/) throws ServiceException {
		Long eventId = null;
		LocalDateTime actualDateTime = LocalDateTime.now();
		
		matchService.retrieveIncompleteEscalatedMatchesByEvent(eventId, actualDateTime);
	}

	/**
	 * Test {@link MatchService#retrieveIncompleteEscalatedMatchesByEvent(Long, Date)} method.
	 * Scenario: throws IllegalArgumentException because the given actualDateTime is {@code null}.
	 */
	@Test(expected=IllegalArgumentException.class)
	public void /*List<Match>*/ retrieveIncompleteEscalatedMatchesByEvent_NullActualDateTime(/*Long eventId, LocalDateTime actualDateTime*/) throws ServiceException {
		Long eventId = 1L; // WC2014
		LocalDateTime actualDateTime = null;
		
		matchService.retrieveIncompleteEscalatedMatchesByEvent(eventId, actualDateTime);
	}

	/**
	 * Test {@link MatchService#retrieveFirstIncompleteMatchByEvent(Long)} method.
	 * Scenario: retrieves result match of the event given by eventId parameter.
	 * @throws ParseException 
	 */
	@Test
	public void /*Match*/ retrieveFirstIncompleteMatchByEvent(/*Long eventId*/) throws ServiceException, ParseException {
		Long eventId = 1L; // WC2014
		Long expectedMatchId = 2L; // 2nd match of the event

		LocalDateTime actualDateTime = CommonUtil.parseDateTime("2014-06-14 14:00"); // before the 5th match of the event
		Mockito.when(applicationService.getActualDateTime()).thenReturn(actualDateTime);
		
		final List<Match> allMatches = commonDao.findAllEntities(Match.class).stream()
				.filter(e -> e.getEvent().getEventId().equals(eventId)).collect(Collectors.toList());

		// set results to the matches before the expected one
		allMatches.stream().filter(e -> e.getMatchId() < expectedMatchId).forEach(e -> {
			e.setGoalNormalByTeam1((byte) 1); e.setGoalNormalByTeam2((byte) 0);
		});

		Mockito.when(matchDao.retrieveMatchesByEvent(eventId)).thenReturn(allMatches);
		
		LongStream.rangeClosed(49L, 62L).forEach(m -> {Match match = allMatches.stream().filter(e -> e.getMatchId().equals(m)).findFirst().get(); Mockito.when(matchDao.retrieveMatchByMatchN(eventId, (short)m)).thenReturn(match);});
		
		Match expectedMatch = commonDao.findEntityById(Match.class, expectedMatchId);
		
		Match match = matchService.retrieveFirstIncompleteMatchByEvent(eventId);
		
		assertEquals("Result match should be equal to the expected one.", expectedMatch, match);
	}
	
	/**
	 * Test {@link MatchService#retrieveFirstIncompleteMatchByEvent(Long)} method.
	 * Scenario: throws IllegalArgumentException because the given eventId parameter is {@code null}.
	 */
	@Test(expected=IllegalArgumentException.class)
	public void /*Match*/ retrieveFirstIncompleteMatchByEvent_NullEventId(/*Long eventId*/) throws ServiceException {
		Long eventId = null;
		matchService.retrieveFirstIncompleteMatchByEvent(eventId);
	}
	
	/**
	 * Test {@link MatchService#retrieveFirstIncompleteMatchesOfEvents()} method.
	 * Scenario: retrieves result list.
	 * @throws ParseException 
	 */
	@Test
	public void /*List<Match>*/ retrieveFirstIncompleteMatchesOfEvents() throws ServiceException, ParseException {
		Long eventId = 1L; // only WC2014 is tested
		Long expectedMatchId = 2L; // 2nd match of the event

		LocalDateTime actualDateTime = CommonUtil.parseDateTime("2014-06-14 14:00"); // before the 5th match of the event
		Mockito.when(applicationService.getActualDateTime()).thenReturn(actualDateTime);
		
		final List<Match> allMatches = commonDao.findAllEntities(Match.class).stream()
				.filter(e -> e.getEvent().getEventId().equals(eventId)).collect(Collectors.toList());

		// set results to the matches before the expected one
		allMatches.stream().filter(e -> e.getMatchId() < expectedMatchId).forEach(e -> {
			e.setGoalNormalByTeam1((byte) 1); e.setGoalNormalByTeam2((byte) 0);
		});

		Mockito.when(matchDao.retrieveMatchesByEvent(eventId)).thenReturn(allMatches);
		
		LongStream.rangeClosed(49L, 62L).forEach(m -> {Match match = allMatches.stream().filter(e -> e.getMatchId().equals(m)).findFirst().get(); Mockito.when(matchDao.retrieveMatchByMatchN(eventId, (short)m)).thenReturn(match);});
		
		List<Match> expectedMatches = allMatches.stream().filter(e -> e.getMatchId().equals(expectedMatchId)).collect(Collectors.toList());

		List<Match> matches = matchService.retrieveFirstIncompleteMatchesOfEvents();
		
		assertEquals("Result match should be equal to the expected one.", expectedMatches, matches);
	}

	/**
	 * Test {@link MatchService#retrieveMatchesByGroup(Long)} method.
	 * Scenario: retrieves result list of the group given by groupId parameter.
	 */
	@Test
	public void /*List<Match>*/ retrieveMatchesByGroup(/*Long groupId*/) throws ServiceException {
		Long groupId = 1L; // group A
		List<Long> matchIds = Arrays.asList(1L, 2L, 16L, 20L, 35L, 36L); // group A
		List<Match> expectedMatches = matchIds.stream().map(e -> commonDao.findEntityById(Match.class, e)).collect(Collectors.toList()) ;
		Mockito.when(matchDao.retrieveMatchesByGroup(groupId)).thenReturn(expectedMatches);
		
		List<Match> matches = matchService.retrieveMatchesByGroup(groupId);
		assertEquals("Result list should be equal to the expected one.", expectedMatches, matches);
	}

	/**
	 * Test {@link MatchService#retrieveMatchesByGroup(Long)} method.
	 * Scenario: throws IllegalArgumentException because the given groupId parameter is {@code null}.
	 */
	@Test(expected=IllegalArgumentException.class)
	public void /*List<Match>*/ retrieveMatchesByGroup_NullGroupId(/*Long groupId*/) throws ServiceException {
		Long groupId = null;
		matchService.retrieveMatchesByGroup(groupId);
	}

	
	/**
	 * Test {@link MatchService#isCandidateMatchTeam(Match, long, int)} method.
	 * Scenario: retrieves {@code true} because team given by teamWsId is equal to
	 *           the team given by match on given index.
	 */
	@Test
	public void /*boolean*/ isCandidateMatchTeam(/*Match match, long teamWsId, int index*/) throws ServiceException {
		Match match = commonDao.findEntityById(Match.class, 1L); // WC2014 first match: Brazil vs Croatia
		long teamWsId = 753L; // Brazil
		int index = 1;
		boolean isExpectedCandidateMatchTeam = true;
		
		boolean isCandidateMatchTeam = matchService.isCandidateMatchTeam(match, teamWsId, index);
		
		assertEquals("Result should be equal to the expected one.", isExpectedCandidateMatchTeam, isCandidateMatchTeam);
	}
	
	/**
	 * Test {@link MatchService#isCandidateMatchTeam(Match, long, int)} method.
	 * Scenario: retrieves {@code false} because index is opposite.
	 */
	@Test
	public void /*boolean*/ isCandidateMatchTeam_OppositeIndex(/*Match match, long teamWsId, int index*/) throws ServiceException {
		Match match = commonDao.findEntityById(Match.class, 1L); // WC2014 first match: Brazil vs Croatia
		long teamWsId = 753L; // Brazil
		int index = 2; // wrong side
		boolean isExpectedCandidateMatchTeam = false;
		
		boolean isCandidateMatchTeam = matchService.isCandidateMatchTeam(match, teamWsId, index);
		
		assertEquals("Result should be equal to the expected one.", isExpectedCandidateMatchTeam, isCandidateMatchTeam);
	}
	
	/**
	 * Test {@link MatchService#isCandidateMatchTeam(Match, long, int)} method.
	 * Scenario: retrieves {@code false} because the given teamWsId is unknown.
	 */
	@Test
	public void /*boolean*/ isCandidateMatchTeam_GroupStageMismatchedTeamWsId(/*Match match, long teamWsId, int index*/) throws ServiceException {
		Match match = commonDao.findEntityById(Match.class, 1L); // WC2014 first match: Brazil vs Croatia
		long teamWsId = -1L; // unknown
		int index = 1;
		boolean isExpectedCandidateMatchTeam = false;
		
		boolean isCandidateMatchTeam = matchService.isCandidateMatchTeam(match, teamWsId, index);
		
		assertEquals("Result should be equal to the expected one.", isExpectedCandidateMatchTeam, isCandidateMatchTeam);
	}
	
	/**
	 * Test {@link MatchService#isCandidateMatchTeam(Match, long, int)} method.
	 * Scenario: throws IllegalStateException because the group match has no participant team
	 */
	@Test(expected=IllegalStateException.class)
	public void /*boolean*/ isCandidateMatchTeam_GroupStageMissingMatchParticipant(/*Match match, long teamWsId, int index*/) throws ServiceException {
		Match match = commonDao.findEntityById(Match.class, 1L); // WC2014 first match: Brazil vs Croatia
		match.setTeam1(null);
		long teamWsId = 753L; // Brazil
		int index = 1;
		
		matchService.isCandidateMatchTeam(match, teamWsId, index);
	}
	
	/**
	 * Test {@link MatchService#isCandidateMatchTeam(Match, long, int)} method.
	 * Scenario: throws IllegalStateException because the given teamWsId is missing.
	 */
	@Test(expected=IllegalStateException.class)
	public void /*boolean*/ isCandidateMatchTeam_KnockOutStageMissingTeamWsId(/*Match match, long teamWsId, int index*/) throws ServiceException {
		Long matchId = 49L; // (WC2014) knock-out match with group participant rule: A1-B2
		Match match = commonDao.findEntityById(Match.class, matchId);
		long teamWsId = -1L; // unknown
		int index = 1;
		
		matchService.isCandidateMatchTeam(match, teamWsId, index);
	}
	
	/**
	 * Test {@link MatchService#isCandidateMatchTeam(Match, long, int)} method.
	 * Scenario: retrieves {@code true} because there is no participant team on the given
	 *           match on given index but the team given by teamWsId can be a possible
	 *           candidate for that. 
	 */
	@Test
	public void /*boolean*/ isCandidateMatchTeam_KnockOutStageFromGroup(/*Match match, long teamWsId, int index*/) throws ServiceException {
		Long matchId = 49L; // (WC2014) knock-out match with group participant rule: A1-B2
		Match match = commonDao.findEntityById(Match.class, matchId);
		long teamWsId = 753L; // Brazil
		int index = 1;
		
		boolean isExpectedCandidateMatchTeam = true; // Brazil is in Group A so it is a candidate for A1
		
		boolean isCandidateMatchTeam = matchService.isCandidateMatchTeam(match, teamWsId, index);
		assertEquals("Result should be equal to the expected one.", isExpectedCandidateMatchTeam, isCandidateMatchTeam);
	}
	
	/**
	 * Test {@link MatchService#isCandidateMatchTeam(Match, long, int)} method.
	 * Scenario: retrieves {@code false} because there is no participant team on the given
	 *           match on given index and the team given by teamWsId can not be a possible
	 *           candidate for that. 
	 */
	@Test
	public void /*boolean*/ isCandidateMatchTeam_KnockOutStageFromOtherGroup(/*Match match, long teamWsId, int index*/) throws ServiceException {
		Long matchId = 49L; // (WC2014) knock-out match with group participant rule: A1-B2
		Match match = commonDao.findEntityById(Match.class, matchId);
		long teamWsId = 760L; // Chile from group B
		int index = 1; // participant rule: A1
		
		boolean isExpectedCandidateMatchTeam = false; // Chile is in Group B so it is not a candidate for A1
		
		boolean isCandidateMatchTeam = matchService.isCandidateMatchTeam(match, teamWsId, index);
		assertEquals("Result should be equal to the expected one.", isExpectedCandidateMatchTeam, isCandidateMatchTeam);
	}
	
	/**
	 * Test {@link MatchService#isCandidateMatchTeam(Match, long, int)} method.
	 * Scenario: retrieves {@code true} because although there is no participant team 
	 *           on the given match on given index but the team given by teamWsId can be 
	 *           a possible candidate for that. 
	 */
	@Test
	public void /*boolean*/ isCandidateMatchTeam_KnockOutStageFromKnockOut(/*Match match, long teamWsId, int index*/) throws ServiceException {
		Long matchId = 62L; // (WC2014) knock-out match with knock-out participant rule: W59-W60
		Match match = commonDao.findEntityById(Match.class, matchId);
		long teamWsId = 753L; // Brazil
		int index = 1; // participant rule: W59
		Long parentMatchId = 59L;
		Match parentMatch = commonDao.findEntityById(Match.class, parentMatchId);
		Long teamId = 6L; // Brazil
		Team team = commonDao.findEntityById(Team.class, teamId);
		parentMatch.setTeam1(team);
		
		Mockito.when(matchDao.retrieveMatchByMatchN(match.getEvent().getEventId(), parentMatchId.shortValue())).thenReturn(parentMatch);
		
		boolean isExpectedCandidateMatchTeam = true; // Brazil plays in parent match so it is a candidate for this match
		
		boolean isCandidateMatchTeam = matchService.isCandidateMatchTeam(match, teamWsId, index);
		assertEquals("Result should be equal to the expected one.", isExpectedCandidateMatchTeam, isCandidateMatchTeam);
	}
	
	/**
	 * Test {@link MatchService#isCandidateMatchTeam(Match, long, int)} method.
	 * Scenario: throws IllegalArgumentException because the given match parameter is {@code null}.
	 */
	@Test(expected=IllegalArgumentException.class)
	public void /*boolean*/ isCandidateMatchTeam_NullMatch(/*Match match, long teamWsId, int index*/) throws ServiceException {
		Match match = null;
		long teamWsId = 753L; // Brazil
		int index = 1;
		
		matchService.isCandidateMatchTeam(match, teamWsId, index);
	}
	
	/**
	 * Test {@link MatchService#isCandidateMatchTeam(Match, long, int)} method.
	 * Scenario: throws IllegalArgumentException because the given match parameter is {@code null}.
	 */
	@Test(expected=IllegalArgumentException.class)
	public void /*boolean*/ isCandidateMatchTeam_InvalidIndex(/*Match match, long teamWsId, int index*/) throws ServiceException {
		Match match = commonDao.findEntityById(Match.class, 1L); // WC2014 first match: Brazil vs Croatia
		long teamWsId = 753L; // Brazil
		int index = 0;
		
		matchService.isCandidateMatchTeam(match, teamWsId, index);
	}
	
	/**
	 * Test {@link MatchService#isCandidateMatchTeam(Match, long, int)} method.
	 * Scenario: throws IllegalArgumentException because the given matchId is null
	*/
	@Test(expected=IllegalArgumentException.class)
	public void /*Match*/ updateMatchByMatchdata_NullMatchId(/*Long matchId, Long team1WsId, Long team2WsId, 
			Byte goalNormal1, Byte goalExtra1, Byte goalPenalty1,
			Byte goalNormal2, Byte goalExtra2, Byte goalPenalty2*/) throws ServiceException {
		Long matchId = null;
		Long team1WsId = null;
		Long team2WsId = null;
		Byte goalNormal1 = null;
		Byte goalNormal2 = null;
		Byte goalExtra1 = null;
		Byte goalExtra2 = null;
		Byte goalPenalty1 = null;
		Byte goalPenalty2 = null;
		matchService.updateMatchByMatchdata(matchId, team1WsId, team2WsId, goalNormal1, goalExtra1, 
				goalPenalty1, goalNormal2, goalExtra2, goalPenalty2);
	}
	
	/**
	 * Test {@link MatchService#updateMatchByMatchdata(Long, Long, Long, Byte, Byte, 
	 *      Byte, Byte, Byte, Byte)} method.
	 * Scenario: updates the left participant team of the given match by the given team1WsId
	*/
	@Test
	public void /*Match*/ updateMatchByMatchdata_team1WsId(/*Long matchId, Long team1WsId, Long team2WsId, 
			Byte goalNormal1, Byte goalExtra1, Byte goalPenalty1,
			Byte goalNormal2, Byte goalExtra2, Byte goalPenalty2*/) throws ServiceException {
		Long matchId = 49L; // (WC2014) knock-out match with group participant rule: A1-B2
		Long team1WsId = 753L; // Brazil
		Long team1Id = 6L; // Brazil
		Long team2WsId = null;
		Byte goalNormal1 = null;
		Byte goalNormal2 = null;
		Byte goalExtra1 = null;
		Byte goalExtra2 = null;
		Byte goalPenalty1 = null;
		Byte goalPenalty2 = null;
		
		Match updatedMatch = matchService.updateMatchByMatchdata(matchId, team1WsId, team2WsId, goalNormal1, goalExtra1, 
				goalPenalty1, goalNormal2, goalExtra2, goalPenalty2);
		assertTrue("Match should be updated.", updatedMatch != null && updatedMatch.getTeam1().getTeamId().equals(team1Id));
	}
	
	/**
	 * Test {@link MatchService#updateMatchByMatchdata(Long, Long, Long, Byte, Byte, 
	 *      Byte, Byte, Byte, Byte)} method.
	 * Scenario: updates the right participant team of the given match by the given team1WsId
	*/
	@Test
	public void /*Match*/ updateMatchByMatchdata_team2WsId(/*Long matchId, Long team1WsId, Long team2WsId, 
			Byte goalNormal1, Byte goalExtra1, Byte goalPenalty1,
			Byte goalNormal2, Byte goalExtra2, Byte goalPenalty2*/) throws ServiceException {
		Long matchId = 49L; // (WC2014) knock-out match with group participant rule: A1-B2
		Long team1WsId = null;
		Long team2WsId = 753L; // Brazil
		Long team2Id = 6L; // Brazil
		Byte goalNormal1 = null;
		Byte goalNormal2 = null;
		Byte goalExtra1 = null;
		Byte goalExtra2 = null;
		Byte goalPenalty1 = null;
		Byte goalPenalty2 = null;
		
		Match updatedMatch = matchService.updateMatchByMatchdata(matchId, team1WsId, team2WsId, goalNormal1, goalExtra1, 
				goalPenalty1, goalNormal2, goalExtra2, goalPenalty2);
		assertTrue("Match should be updated.", updatedMatch != null && updatedMatch.getTeam2().getTeamId().equals(team2Id));
	}
	
	/**
	 * Test {@link MatchService#updateMatchByMatchdata(Long, Long, Long, Byte, Byte, 
	 *      Byte, Byte, Byte, Byte)} method.
	 * Scenario: there is no update because the given team1WsId is unknown
	*/
	@Test
	public void /*Match*/ updateMatchByMatchdata_missingTeam1WsId(/*Long matchId, Long team1WsId, Long team2WsId, 
			Byte goalNormal1, Byte goalExtra1, Byte goalPenalty1,
			Byte goalNormal2, Byte goalExtra2, Byte goalPenalty2*/) throws ServiceException {
		Long matchId = 49L; // (WC2014) knock-out match with group participant rule: A1-B2
		Long team1WsId = -1L;
		Long team2WsId = null;
		Byte goalNormal1 = null;
		Byte goalNormal2 = null;
		Byte goalExtra1 = null;
		Byte goalExtra2 = null;
		Byte goalPenalty1 = null;
		Byte goalPenalty2 = null;
		
		Match updatedMatch = matchService.updateMatchByMatchdata(matchId, team1WsId, team2WsId, goalNormal1, goalExtra1, 
				goalPenalty1, goalNormal2, goalExtra2, goalPenalty2);
		assertNull("There should not be update.", updatedMatch);
	}
	
	/**
	 * Test {@link MatchService#updateMatchByMatchdata(Long, Long, Long, Byte, Byte, 
	 *      Byte, Byte, Byte, Byte)} method.
	 * Scenario: there is no update because the knock-out match result is not complete
	*/
	@Test
	public void /*Match*/ updateMatchByMatchdata_incompleteResult(/*Long matchId, Long team1WsId, Long team2WsId, 
			Byte goalNormal1, Byte goalExtra1, Byte goalPenalty1,
			Byte goalNormal2, Byte goalExtra2, Byte goalPenalty2*/) throws ServiceException {
		Long matchId = 49L; // (WC2014) knock-out match with group participant rule: A1-B2
		Long team1WsId = null;
		Long team2WsId = null;
		Byte goalNormal1 = 1;
		Byte goalNormal2 = 1;
		Byte goalExtra1 = 2;
		Byte goalExtra2 = 2;
		Byte goalPenalty1 = 4; // after penalty the match cannot be draw
		Byte goalPenalty2 = 4;
		
		Match updatedMatch = matchService.updateMatchByMatchdata(matchId, team1WsId, team2WsId, goalNormal1, goalExtra1, 
				goalPenalty1, goalNormal2, goalExtra2, goalPenalty2);
		assertNull("There should not be update.", updatedMatch);
	}
	
	/**
	 * Test {@link MatchService#updateMatchByMatchdata(Long, Long, Long, Byte, Byte, 
	 *      Byte, Byte, Byte, Byte)} method.
	 * Scenario: the given match is updated with the given complete result
	 */
	@Test
	public void /*Match*/ updateMatchByMatchdata_completeResult(/*Long matchId, Long team1WsId, Long team2WsId, 
			Byte goalNormal1, Byte goalExtra1, Byte goalPenalty1,
			Byte goalNormal2, Byte goalExtra2, Byte goalPenalty2*/) throws ServiceException {
		Long matchId = 49L; // (WC2014) knock-out match with group participant rule: A1-B2
		Long team1WsId = null;
		Long team2WsId = null;
		Byte goalNormal1 = 1;
		Byte goalNormal2 = 1;
		Byte goalExtra1 = 2;
		Byte goalExtra2 = 2;
		Byte goalPenalty1 = 4; // after penalty the match cannot be draw
		Byte goalPenalty2 = 5;
		
		Match updatedMatch = matchService.updateMatchByMatchdata(matchId, team1WsId, team2WsId, goalNormal1, goalExtra1, 
				goalPenalty1, goalNormal2, goalExtra2, goalPenalty2);
		assertTrue("The result of the updated match should be equal to the expected one.", 
				updatedMatch.getGoalNormalByTeam1().equals(goalNormal1) 
				&& updatedMatch.getGoalNormalByTeam2().equals(goalNormal2)
				&& updatedMatch.getGoalExtraByTeam2().equals(goalExtra2)
				&& updatedMatch.getGoalExtraByTeam2().equals(goalExtra2)
				&& updatedMatch.getGoalPenaltyByTeam2().equals(goalPenalty2)
				&& updatedMatch.getGoalPenaltyByTeam2().equals(goalPenalty2));
	}
	
	/**
	 * Test {@link MatchService#retrieveMatchStartDatesByEvent(Long)} method.
	 * Scenario: retrieves result list of the event given by eventId parameter.
	 * @throws ParseException 
	 */
	@Test
	public void /*List<Date>*/ retrieveMatchStartDatesByEvent(/*Long eventId*/) throws ServiceException, ParseException {
		Long eventId = 1L; // WC2014
		
		List<Match> allMatches = new ArrayList<>();
		Round round = commonDao.findEntityById(Round.class, 1L);
		
		Match match = new Match();
		match.setRound(round);
		LocalDateTime startTime1 = CommonUtil.parseDateTime("2014-06-13 12:00");
		match.setStartTime(startTime1);
		allMatches.add(match);
		
		match = new Match();
		match.setRound(round);
		LocalDateTime startTime2 = CommonUtil.parseDateTime("2014-06-14 14:00");
		match.setStartTime(startTime2);
		allMatches.add(match);

		Mockito.when(matchDao.retrieveMatchesByEvent(eventId)).thenReturn(allMatches);
		
		List<LocalDateTime> expectedMatchStartDates = Arrays.asList(
				CommonUtil.parseDateTime("2014-06-13 00:00"),
				CommonUtil.parseDateTime("2014-06-14 00:00")); // ordered list
		
		List<LocalDateTime> matchStartDates = matchService.retrieveMatchStartDatesByEvent(eventId);
		assertEquals("Result list should be equal to the expected one.", expectedMatchStartDates, matchStartDates);
	}
	
	/**
	 * Test {@link MatchService#retrieveMatchStartDatesByEvent(Long)} method.
	 * Scenario: throws IllegalArgumentException because the given eventId parameter is {@code null}.
	 */
	@Test(expected=IllegalArgumentException.class)
	public void /*List<Date>*/ retrieveMatchStartDatesByEvent_NullEventId(/*Long eventId*/) throws ServiceException {
		Long eventId = null;
		matchService.retrieveMatchStartDatesByEvent(eventId);
	}
	
	/**
	 * Test {@link MatchService#retrieveIncompleteMatchesByEvent(Long)} method.
	 * Scenario: retrieves number of complete matches in percent of the given event.
	 */
	@Test
	public void /*int*/ retriveMatchesAccomplishedInPercent(/*Long eventId*/) throws ServiceException {
		Long eventId = 1L;
		int expectedMatchesAccomplishedInPercent = 0;
		
		// stubbed out methods called by tested method
		List <Match> expectedMatches = commonDao.findAllEntities(Match.class);
		expectedMatches = expectedMatches.stream().filter(e -> e.getEvent().getEventId().equals(eventId)).collect(Collectors.toList());
		Mockito.when(matchDao.retrieveMatchesByEvent(eventId)).thenReturn(expectedMatches);
		
		int matchesAccomplishedInPercent = matchService.retriveMatchesAccomplishedInPercent(eventId);
		assertEquals("Result should be equal to the expected one.", 
				expectedMatchesAccomplishedInPercent, matchesAccomplishedInPercent);
	}

	/**
	 * Test {@link MatchService#retrieveIncompleteMatchesByEvent(Long)} method.
	 * Scenario: retrieves number of complete matches in percent of the given event,
	 *           where only the first group match has result.
	 */
	@Test
	public void /*int*/ retriveMatchesAccomplishedInPercent_Result1(/*Long eventId*/) throws ServiceException {
		Long eventId = 1L;
		int expectedMatchesAccomplishedInPercent = 1;
		
		// stubbed out methods called by tested method
		List <Match> expectedMatches = commonDao.findAllEntities(Match.class);
		expectedMatches = expectedMatches.stream().filter(e -> e.getEvent().getEventId().equals(eventId)).collect(Collectors.toList());
		Match firstExpectedMatch = Iterables.firstOf(expectedMatches);
		firstExpectedMatch.setGoalNormalByTeam1((byte)1);
		firstExpectedMatch.setGoalNormalByTeam2((byte)0);
		Mockito.when(matchDao.retrieveMatchesByEvent(eventId)).thenReturn(expectedMatches);

		int matchesAccomplishedInPercent = matchService.retriveMatchesAccomplishedInPercent(eventId);
		assertEquals("Result should be equal to the expected one.", 
				expectedMatchesAccomplishedInPercent, matchesAccomplishedInPercent);
	}

	/**
	 * Test {@link MatchService#retrieveIncompleteMatchesByEvent(Long)} method.
	 * Scenario: retrieves number of complete matches in percent of the given event,
	 *           where all group matches have results.
	 * 
	 */
	@Test
	public void /*int*/ retriveMatchesAccomplishedInPercent_Result75(/*Long eventId*/) throws ServiceException {
		Long eventId = 1L;
		int expectedMatchesAccomplishedInPercent = 75;
		
		// stubbed out methods called by tested method
		List <Match> expectedMatches = commonDao.findAllEntities(Match.class);
		expectedMatches = expectedMatches.stream().filter(e -> e.getEvent().getEventId().equals(eventId)).collect(Collectors.toList());
		expectedMatches.stream().filter(e -> e.getRound().getIsGroupmatchAsBoolean()).forEach(e -> { e.setGoalNormalByTeam1((byte)1); e.setGoalNormalByTeam2((byte)1);});
		Match firstExpectedMatch = Iterables.firstOf(expectedMatches);
		firstExpectedMatch.setGoalNormalByTeam1((byte)1);
		firstExpectedMatch.setGoalNormalByTeam2((byte)0);
		Mockito.when(matchDao.retrieveMatchesByEvent(eventId)).thenReturn(expectedMatches);

		int matchesAccomplishedInPercent = matchService.retriveMatchesAccomplishedInPercent(eventId);
		assertEquals("Result should be equal to the expected one.", 
				expectedMatchesAccomplishedInPercent, matchesAccomplishedInPercent);
	}

	/**
	 * Test {@link MatchService#retrieveIncompleteMatchesByEvent(Long)} method.
	 * Scenario: retrieves number of complete matches in percent of the given event,
	 *           where all matches have results and participant teams.
	 * 
	 */
	@Test
	public void /*int*/ retriveMatchesAccomplishedInPercent_Result100(/*Long eventId*/) throws ServiceException {
		Long eventId = 1L;
		int expectedMatchesAccomplishedInPercent = 100;
		
		// stubbed out methods called by tested method
		Team team = commonDao.findEntityById(Team.class, 1L);
		List <Match> expectedMatches = commonDao.findAllEntities(Match.class);
		expectedMatches = expectedMatches.stream().filter(e -> e.getEvent().getEventId().equals(eventId)).collect(Collectors.toList());
		expectedMatches.stream().forEach(e -> {e.setGoalNormalByTeam1((byte)1); e.setGoalNormalByTeam2((byte)0); e.setTeam1(team); e.setTeam2(team);});
		Mockito.when(matchDao.retrieveMatchesByEvent(eventId)).thenReturn(expectedMatches);

		int matchesAccomplishedInPercent = matchService.retriveMatchesAccomplishedInPercent(eventId);
		assertEquals("Result should be equal to the expected one.", 
				expectedMatchesAccomplishedInPercent, matchesAccomplishedInPercent);
	}

	/**
	 * Test {@link MatchService#retrieveIncompleteMatchesByEvent(Long)} method.
	 * Scenario: throws {@link IllegalArgumentException} because of the given {@code null} eventId parameter
	 */
	@Test(expected=IllegalArgumentException.class)
	public void /*List<Match>*/ retriveMatchesAccomplishedInPercent_NullEventId(/*Long eventId*/) throws ServiceException {
		Long eventId = null;
		matchService.retriveMatchesAccomplishedInPercent(eventId);
	}

	/**
	 * Test {@link MatchService#retrieveIncompleteMatchesByEvent(Long)} method.
	 * Scenario: retrieves 0 value because there are no match entities of 
	 * the given eventId parameter in the database.
	 */
	@Test
	public void /*List<Match>*/ retriveMatchesAccomplishedInPercent_InvalidEventId(/*Long eventId*/) throws ServiceException {
		Long eventId = -1L;
		int expectedMatchesAccomplishedInPercent = 0;
		
		int matchesAccomplishedInPercent = matchService.retriveMatchesAccomplishedInPercent(eventId);
		assertEquals("Result should be equal to the expected one.", 
				expectedMatchesAccomplishedInPercent, matchesAccomplishedInPercent);

	}
	
	/**
	 * Test {@link MatchService#refreshMatchesByScheduler(Long)} method.
	 * Scenario: throws {@link IllegalArgumentException} because of the given {@code null} eventId parameter
	 */
	@Test(expected=IllegalArgumentException.class)
	public void refreshMatchesByScheduler_NullEventId(/*Long eventId*/) throws ServiceException {
		Long eventId = null;
		matchService.refreshMatchesByScheduler(eventId);
	}
	
	/**
	 * Test {@link MatchService#refreshMatchesByScheduler(Long)} method.
	 * Scenario: throws {@link ServiceException} because scheduler is disabled by configuration
	 */
	@Test(expected=ServiceException.class)
	public void refreshMatchesByScheduler_SchedulerDisabled(/*Long eventId*/) throws ServiceException {
		Long eventId = 1L; // WC2014
		
		Mockito.when(schedulerService.isAppSchedulerEnabled()).thenReturn(false);
		
		try {
			matchService.refreshMatchesByScheduler(eventId);
		}
		catch (ServiceException e) {
			String expectedMsgCode = "SCHEDULER_DISABLED";
			assertTrue("There must be a single message in ServiceException named " + expectedMsgCode, 
					e.getMessages().size()==1 && e.getMessages().get(0).getMsgCode().equals(expectedMsgCode));
			throw e;
		}
	}
	
	/**
	 * Test {@link MatchService#refreshMatchesByScheduler(Long)} method.
	 * Scenario: throws {@link ServiceException} because there is no scheduler job
	 */
	@Test(expected=ServiceException.class)
	public void refreshMatchesByScheduler_MissingSchedulerJob(/*Long eventId*/) throws ServiceException {
		Long eventId = 1L; // WC2014
		
		Mockito.when(schedulerService.isAppSchedulerEnabled()).thenReturn(true);
		Mockito.when(schedulerService.isExistsRetrieveMatchResultsJobTrigger(eventId)).thenReturn(false);
		
		try {
			matchService.refreshMatchesByScheduler(eventId);
		}
		catch (ServiceException e) {
			String expectedMsgCode = "MISSING_SCHEDULED_RETRIEVAL_MATCH_RESULTS_JOB";
			assertTrue("There must be a single message in ServiceException named " + expectedMsgCode, 
					e.getMessages().size()==1 && e.getMessages().get(0).getMsgCode().equals(expectedMsgCode));
			throw e;
		}
	}
	
	/**
	 * Test {@link MatchService#refreshMatchesByScheduler(Long)} method.
	 * Scenario: throws {@link ServiceException} because relaunching done
	 */
	@Test(expected=ServiceException.class)
	public void refreshMatchesByScheduler_RelaunchhDone(/*Long eventId*/) throws ServiceException {
		Long eventId = 1L; // WC2014
		
		Mockito.when(schedulerService.isAppSchedulerEnabled()).thenReturn(true);
		Mockito.when(schedulerService.isExistsRetrieveMatchResultsJobTrigger(eventId)).thenReturn(true);
		
		Match match = commonDao.findEntityById(Match.class, 1L); //WC2014 first match
		Mockito.doReturn(match).when(matchServicePartial).retrieveFirstIncompleteMatchByEvent(eventId);
		Mockito.when(schedulerService.relaunchRetrieveMatchResultsJobTrigger(eventId, match.getMatchId())).thenReturn(true);
		
		try {
			matchServicePartial.refreshMatchesByScheduler(eventId);
		}
		catch (ServiceException e) {
			String expectedMsgCode = "SCHEDULED_RMRJ_RELAUNCH_DONE";
			assertTrue("There must be a single message in ServiceException named " + expectedMsgCode, 
					e.getMessages().size()==1 && e.getMessages().get(0).getMsgCode().equals(expectedMsgCode));
			throw e;
		}
	}

	/**
	 * Test {@link MatchService#refreshMatchesByScheduler(Long)} method.
	 * Scenario: throws {@link ServiceException} because relaunching failed
	 */
	@Test(expected=ServiceException.class)
	public void refreshMatchesByScheduler_RelaunchhFailed(/*Long eventId*/) throws ServiceException {
		Long eventId = 1L; // WC2014
		
		Mockito.when(schedulerService.isAppSchedulerEnabled()).thenReturn(true);
		Mockito.when(schedulerService.isExistsRetrieveMatchResultsJobTrigger(eventId)).thenReturn(true);
		
		Match match = commonDao.findEntityById(Match.class, 1L); //WC2014 first match
		Mockito.doReturn(match).when(matchServicePartial).retrieveFirstIncompleteMatchByEvent(eventId);
		Mockito.when(schedulerService.relaunchRetrieveMatchResultsJobTrigger(eventId, match.getMatchId())).thenReturn(false);
		
		try {
			matchServicePartial.refreshMatchesByScheduler(eventId);
		}
		catch (ServiceException e) {
			String expectedMsgCode = "SCHEDULED_RMRJ_RELAUNCH_FAILED";
			assertTrue("There must be a single message in ServiceException named " + expectedMsgCode, 
					e.getMessages().size()==1 && e.getMessages().get(0).getMsgCode().equals(expectedMsgCode));
			throw e;
		}
	}

	/**
	 * Test {@link MatchService#refreshMatchesByScheduler(Long)} method.
	 * Scenario: throws {@link ServiceException} because there is no incomplete match belongs 
	 *           to the given event
	 */
	@Test(expected=ServiceException.class)
	public void refreshMatchesByScheduler_NoIncompleteMatch(/*Long eventId*/) throws ServiceException {
		Long eventId = 1L; // WC2014
		
		Mockito.when(schedulerService.isAppSchedulerEnabled()).thenReturn(true);
		Mockito.when(schedulerService.isExistsRetrieveMatchResultsJobTrigger(eventId)).thenReturn(true);
		
		Mockito.doReturn(null).when(matchServicePartial).retrieveFirstIncompleteMatchByEvent(eventId);
		
		try {
			matchServicePartial.refreshMatchesByScheduler(eventId);
		}
		catch (ServiceException e) {
			String expectedMsgCode = "NO_INCOMPLETE_MATCH";
			assertTrue("There must be a single message in ServiceException named " + expectedMsgCode, 
					e.getMessages().size()==1 && e.getMessages().get(0).getMsgCode().equals(expectedMsgCode));
			throw e;
		}
	}
}
