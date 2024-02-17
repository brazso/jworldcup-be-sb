package com.zematix.jworldcup.backend.controller;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import javax.inject.Inject;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.Sql.ExecutionPhase;
import org.springframework.test.context.junit4.SpringRunner;

import com.zematix.jworldcup.backend.dao.CommonDao;
import com.zematix.jworldcup.backend.dto.BetDto;
import com.zematix.jworldcup.backend.dto.MatchDto;
import com.zematix.jworldcup.backend.dto.RoundDto;
import com.zematix.jworldcup.backend.dto.UserDto;
import com.zematix.jworldcup.backend.entity.Bet;
import com.zematix.jworldcup.backend.exception.ServiceException;
import com.zematix.jworldcup.backend.service.ApplicationService;

@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles({"develop", "test"})
@Sql(scripts = { "/database/controller/bet-controller-before.sql" }, executionPhase = ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(scripts = { "/database/controller/bet-controller-after.sql" }, executionPhase = ExecutionPhase.AFTER_TEST_METHOD)
public class BetControllerIT {

	@Inject
	private BetController betController;

	@Inject
	private CommonDao commonDao;

	@MockBean
	private ApplicationService applicationService;

	@Test
	@WithMockUser(username = "normal", roles = {"USER"})
	public void retrieveBetsByEventAndUser() throws ServiceException {
		// given
		Long eventId = 1L; // WC2014
		Long userId = 2L; // normal
		// when
		var result = betController.retrieveBetsByEventAndUser(eventId, userId);
		// then
		assertEquals(HttpStatus.OK, result.getStatusCode());
		assertTrue(result.getBody().getSuccessful());
		assertEquals(1, result.getBody().getData().size());
	}

	@Test
	@WithMockUser(username = "normal", roles = {"USER"})
	public void retrieveBetsByMatchAndUserGroup() throws ServiceException {
		// given
		Long matchId = 1L;
		Long userGroupId = 1L;
		// when
		var result = betController.retrieveBetsByMatchAndUserGroup(matchId, userGroupId);
		// then
		assertEquals(HttpStatus.OK, result.getStatusCode());
		assertTrue(result.getBody().getSuccessful());
		assertEquals(1, result.getBody().getData().size());
	}

	@Test
	@WithMockUser(username = "normal", roles = {"USER"})
	public void retrieveBet() throws ServiceException {
		// given
		Long betId = 1L;
		// when
		var result = betController.retrieveBet(betId);
		// then
		assertEquals(HttpStatus.OK, result.getStatusCode());
		assertTrue(result.getBody().getSuccessful());
		assertEquals(1L, result.getBody().getData().getBetId().longValue());
	}
	
	@Test
	@WithMockUser(username = "normal", roles = {"USER"})
	public void saveBet()
			throws ServiceException {
		// given
		Long betId = null;
		Long matchId = 2L;
		Long userId = 2L; // normal
		LocalDateTime matchStartTime = LocalDateTime.parse("2014-06-13 16:00:00", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
		RoundDto roundDto = new RoundDto();
		roundDto.setIsGroupmatch(true);
		roundDto.setIsOvertime(false);
		MatchDto matchDto = new MatchDto();
		matchDto.setMatchId(matchId);
		matchDto.setRound(roundDto);
		matchDto.setStartTime(matchStartTime);
		matchDto.setGoalNormalByTeam1(null);
		matchDto.setGoalNormalByTeam2(null);
		UserDto userDto = new UserDto();
		userDto.setUserId(userId);
		BetDto betDto = new BetDto();
		betDto.setBetId(betId);
		betDto.setGoalNormalByTeam1((byte)1);
		betDto.setGoalNormalByTeam2((byte)0);
		betDto.setMatch(matchDto);
		betDto.setUser(userDto);
		// when
		Mockito.when(applicationService.getActualDateTime()).thenReturn(matchStartTime);
		var result = betController.saveBet(betDto);
		// then
		assertEquals(HttpStatus.OK, result.getStatusCode());
		assertTrue(result.getBody().getSuccessful());
		assertEquals(1, result.getBody().getData().getGoalNormalByTeam1().byteValue());
		assertEquals(0, result.getBody().getData().getGoalNormalByTeam2().byteValue());
		Bet bet = commonDao.findEntityById(Bet.class, result.getBody().getData().getBetId());
		assertEquals(bet.getGoalNormalByTeam1(), result.getBody().getData().getGoalNormalByTeam1());
		assertEquals(bet.getGoalNormalByTeam2(), result.getBody().getData().getGoalNormalByTeam2());
	}
	
	@Test
	@WithMockUser(username = "normal", roles = {"USER"})
	public void deleteBet() throws ServiceException {
		// given
		Long betId = 1L;
		// when
		var result = betController.deleteBet(betId);
		// then
		assertEquals(HttpStatus.OK, result.getStatusCode());
		Bet bet = commonDao.findEntityById(Bet.class, betId);
		assertNull(bet);
	}

	@Test
	@WithMockUser(username = "normal", roles = {"USER"})
	public void retrieveScoresByEventAndUser() throws ServiceException {
		// given
		Long eventId = 1L; // WC2014
		Long userId = 2L; // normal
		LocalDateTime matchStartDay = LocalDateTime.parse("2014-06-12 00:00:00", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
		// when
		var result = betController.retrieveScoresByEventAndUser(eventId, userId);
		// then
		assertEquals(HttpStatus.OK, result.getStatusCode());
		assertTrue(result.getBody().getSuccessful());
		assertEquals(1, result.getBody().getData().size());
		assertEquals(2, result.getBody().getData().get(matchStartDay).intValue());
	}
	
	@Test
	@WithMockUser(username = "normal", roles = {"USER"})
	public void retrieveScoreByEventAndUser() throws ServiceException {
		// given
		Long eventId = 1L; // WC2014
		Long userId = 2L; // normal
		// when
		var result = betController.retrieveScoreByEventAndUser(eventId, userId);
		// then
		assertEquals(HttpStatus.OK, result.getStatusCode());
		assertTrue(result.getBody().getSuccessful());
		assertEquals(2, result.getBody().getData().intValue());
	}
}
