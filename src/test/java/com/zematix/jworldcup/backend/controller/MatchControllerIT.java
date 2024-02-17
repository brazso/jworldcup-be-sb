package com.zematix.jworldcup.backend.controller;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import javax.inject.Inject;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.Sql.ExecutionPhase;
import org.springframework.test.context.junit4.SpringRunner;

import com.zematix.jworldcup.backend.dao.CommonDao;
import com.zematix.jworldcup.backend.dto.GenericListResponse;
import com.zematix.jworldcup.backend.dto.GenericResponse;
import com.zematix.jworldcup.backend.dto.MatchBetDto;
import com.zematix.jworldcup.backend.dto.MatchDto;
import com.zematix.jworldcup.backend.dto.RoundDto;
import com.zematix.jworldcup.backend.emun.ParameterizedMessageType;
import com.zematix.jworldcup.backend.entity.Match;
import com.zematix.jworldcup.backend.exception.ServiceException;
import com.zematix.jworldcup.backend.model.PublishedEvent;

@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles({"develop", "test"})
@Sql(scripts = { "/database/controller/match-controller-before.sql" }, executionPhase = ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(scripts = { "/database/controller/match-controller-after.sql" }, executionPhase = ExecutionPhase.AFTER_TEST_METHOD)
public class MatchControllerIT {

	@Inject
	private MatchController matchController;
	
	@Inject
	private CommonDao commonDao;
	
	@MockBean
	private ApplicationEventPublisher applicationEventPublisher;

	@Test
	@WithMockUser(username = "normal", roles = {"USER"})
	public void retrieveRoundsByEvent() throws ServiceException {
		// given
		Long eventId = 1L; // WC2014
		// when
		ResponseEntity<GenericListResponse<RoundDto>> result = matchController.retrieveRoundsByEvent(eventId);
		// then
		assertEquals(HttpStatus.OK, result.getStatusCode());
		assertTrue(result.getBody().getSuccessful());
		assertEquals(8, result.getBody().getData().size());
	}

	@Test
	@WithMockUser(username = "normal", roles = {"USER"})
	public void retrieveMatch() throws ServiceException {
		// given
		Long matchId = 1L;
		// when
		ResponseEntity<GenericResponse<MatchDto>> result = matchController.retrieveMatch(matchId);
		// then
		assertEquals(HttpStatus.OK, result.getStatusCode());
		assertTrue(result.getBody().getSuccessful());
		assertEquals(1, result.getBody().getData().getMatchN().shortValue());
	}

	@Test
	@WithMockUser(username = "admin", roles = {"ADMIN"})
	public void saveMatch() throws ServiceException {
		// given
		Long matchId = 1L;
		RoundDto roundDto = new RoundDto();
		roundDto.setIsGroupmatch(true);
		roundDto.setIsOvertime(false);
		MatchDto matchDto = new MatchDto();
		matchDto.setMatchId(matchId);
		matchDto.setRound(roundDto);
		matchDto.setStartTime(LocalDateTime.parse("2014-06-12 20:00:00", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
		matchDto.setGoalNormalByTeam1((byte)3);
		matchDto.setGoalNormalByTeam2((byte)1);
		// when
		ResponseEntity<GenericResponse<MatchDto>> result = matchController.saveMatch(matchDto);
		// then
		assertEquals(HttpStatus.OK, result.getStatusCode());
		assertTrue(result.getBody().getSuccessful());
		assertEquals(3, result.getBody().getData().getGoalNormalByTeam1().byteValue());
		assertEquals(1, result.getBody().getData().getGoalNormalByTeam2().byteValue());
		Match match = commonDao.findEntityById(Match.class, matchId);
		assertEquals(match.getGoalNormalByTeam1(), result.getBody().getData().getGoalNormalByTeam1());
		assertEquals(match.getGoalNormalByTeam2(), result.getBody().getData().getGoalNormalByTeam2());
	}

	@Test
	@WithMockUser(username = "admin", roles = {"ADMIN"})
	public void resetMatch() throws ServiceException {
		// given
		Long matchId = 1L;
		// when
		Mockito.doNothing().when(applicationEventPublisher).publishEvent(Mockito.any(PublishedEvent.class));
		ResponseEntity<GenericResponse<MatchDto>> result = matchController.resetMatch(matchId);
		// then
		assertEquals(HttpStatus.OK, result.getStatusCode());
		assertTrue(result.getBody().getSuccessful());
		assertNull(result.getBody().getData().getGoalNormalByTeam1());
		assertNull(result.getBody().getData().getGoalNormalByTeam2());
		Match match = commonDao.findEntityById(Match.class, matchId);
		assertEquals(match.getGoalNormalByTeam1(), result.getBody().getData().getGoalNormalByTeam1());
		assertEquals(match.getGoalNormalByTeam2(), result.getBody().getData().getGoalNormalByTeam2());
	}	
	
	@Test
	@WithMockUser(username = "normal", roles = {"USER"})
	public void retrieveMatchesByEvent() throws ServiceException {
		// given
		String eventByShortDescWithYear = "WC2014";
		// when
		ResponseEntity<GenericListResponse<MatchDto>> result = matchController.retrieveMatchesByEvent(eventByShortDescWithYear);
		// then
		assertEquals(HttpStatus.OK, result.getStatusCode());
		assertTrue(result.getBody().getSuccessful());
		assertEquals(64, result.getBody().getData().size());
	}

	@Test
	@WithMockUser(username = "normal", roles = {"USER"})
	public void getMatchResult() throws ServiceException {
		// given
		Integer side = 1;
		MatchDto match = new MatchDto();
		match.setGoalNormalByTeam1((byte)1);
		match.setGoalNormalByTeam2((byte)0);
		// when
		ResponseEntity<GenericResponse<Integer>> result = matchController.getMatchResult(side, match);
		// then
		assertEquals(HttpStatus.OK, result.getStatusCode());
		assertTrue(result.getBody().getSuccessful());
		assertEquals(1, result.getBody().getData().intValue());
	}

	@Test
	@WithMockUser
	public void getScore() throws ServiceException {
		// given
		MatchBetDto matchBet = new MatchBetDto();
		matchBet.setFavTeamId(1L);
		matchBet.setTeam1Id(1L);
		matchBet.setTeam2Id(2L);
		matchBet.setGoalNormalByTeam1((byte)1);
		matchBet.setGoalNormalByTeam2((byte)0);
		matchBet.setGoalBetByTeam1((byte)2);
		matchBet.setGoalBetByTeam2((byte)1);
		// when
		ResponseEntity<GenericResponse<Integer>> result = matchController.getScore(matchBet);
		// then
		assertEquals(HttpStatus.OK, result.getStatusCode());
		assertTrue(result.getBody().getSuccessful());
		assertEquals(4, result.getBody().getData().intValue());
	}
	
	@Test
	@WithMockUser
	public void getEndDateTime() throws ServiceException {
		// given
		LocalDateTime startTime = LocalDateTime.parse("2014-06-12 20:00:00",
				DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
		LocalDateTime expectedEndTime = LocalDateTime.parse("2014-06-12 21:45:00",
				DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")); // + 105 minutes
		// when
		ResponseEntity<GenericResponse<LocalDateTime>> result = matchController.getEndDateTime(startTime);
		// then
		assertEquals(HttpStatus.OK, result.getStatusCode());
		assertTrue(result.getBody().getSuccessful());
		assertEquals(expectedEndTime, result.getBody().getData());
	}

	@Test
	@WithMockUser
	public void refreshMatchesByScheduler() throws ServiceException {
		// given
		Long eventId = 1L; // WC2014
		// when
		ServiceException thrown = assertThrows(ServiceException.class, () -> matchController.refreshMatchesByScheduler(eventId));
		// then
		assertEquals(ParameterizedMessageType.ERROR, thrown.getOverallType());
		assertTrue(thrown.containsMessage("SCHEDULER_DISABLED"));
	}
	
	@Test
	@WithMockUser
	public void retrieveMatchStartDatesByEvent() throws ServiceException {
		// given
		Long eventId = 1L; // WC2014
		// when
		ResponseEntity<GenericListResponse<LocalDateTime>> result = matchController.retrieveMatchStartDatesByEvent(eventId);
		// then
		assertEquals(HttpStatus.OK, result.getStatusCode());
		assertTrue(result.getBody().getSuccessful());
		assertEquals(25, result.getBody().getData().size()); // there is 25 match-days of WC2014
	}
}
