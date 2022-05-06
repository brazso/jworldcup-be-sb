package com.zematix.jworldcup.backend.controller;

import java.time.LocalDateTime;
import java.util.List;

import javax.inject.Inject;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.zematix.jworldcup.backend.dto.BetDto;
import com.zematix.jworldcup.backend.dto.CommonResponse;
import com.zematix.jworldcup.backend.dto.GenericListResponse;
import com.zematix.jworldcup.backend.dto.GenericMapResponse;
import com.zematix.jworldcup.backend.dto.GenericResponse;
import com.zematix.jworldcup.backend.entity.Bet;
import com.zematix.jworldcup.backend.entity.Match;
import com.zematix.jworldcup.backend.entity.UserGroup;
import com.zematix.jworldcup.backend.exception.ServiceException;
import com.zematix.jworldcup.backend.mapper.BetMapper;
import com.zematix.jworldcup.backend.service.BetService;
import com.zematix.jworldcup.backend.service.MatchService;
import com.zematix.jworldcup.backend.service.ServiceBase;

import io.swagger.v3.oas.annotations.Operation;

/**
 * WS Rest wrapper class of {@link MatchService}.
 * Only the necessary public methods of its associated service class are in play. 
 */
@RestController
@RequestMapping("bets")
public class BetController extends ServiceBase implements ResponseEntityHelper {

//	@Inject
//	private Logger logger;

	@Inject
	private BetService betService;
	
	@Inject
	private BetMapper betMapper;
	
	/**
	 * Returns a list of found {@link Bet} instances with the provided {@code eventId}
	 * and {@code userId}.
	 * 
	 * @param eventId
	 * @param userId
	 * @return list of found {@Bet} instances
	 * @throws IllegalArgumentException if any of the given parameters is {@code null}
	 */
	@PreAuthorize("hasAnyRole('USER', 'ADMIN')")	
	@Operation(summary = "Retrieve bets of an event and user", description = "Retrieve all bets of the given event and user")
	@GetMapping(value = "/bets-by-event-and-user")
	public ResponseEntity<GenericListResponse<BetDto>> retrieveBetsByEventAndUser(@RequestParam Long eventId, 
			@RequestParam Long userId) throws ServiceException {
		List<Bet> bets = betService.retrieveBetsByEventAndUser(eventId, userId);
		return buildResponseEntityWithOK(new GenericListResponse<>(betMapper.entityListToDtoList(bets)));
	}

	/**
	 * Returns {@link Bet} instances belongs to the provided {@link Match#matchId} and {@link UserGroup#userGroupId}.
	 * @param matchId
	 * @param userGroupId
	 * @return found bets
	 */
	@PreAuthorize("hasAnyRole('USER')")	
	@Operation(summary = "Retrieve bets of a match and userGroup", description = "Retrieve all bets of the given match and userGroup")
	@GetMapping(value = "/bets-by-match-and-userGroup")
	public ResponseEntity<GenericListResponse<BetDto>> retrieveBetsByMatchAndUserGroup(@RequestParam Long matchId,
			@RequestParam Long userGroupId) throws ServiceException {
		List<Bet> bets = betService.retrieveBetsByMatchAndUserGroup(matchId, userGroupId);
		return buildResponseEntityWithOK(new GenericListResponse<>(betMapper.entityListToDtoList(bets)));
	}

	/**
	 * Returns found {@link Bet} instance with the provided {@link Bet#betId}.
	 * @param betId
	 * @return found bet
	 */
	@PreAuthorize("hasAnyRole('USER', 'ADMIN')")	
	@Operation(summary = "Retrieve bet by its id", description = "Retrieve bet by its id")
	@GetMapping(value = "/bet/{id}")
	public ResponseEntity<GenericResponse<BetDto>> retrieveBet(@PathVariable("id") Long betId) throws ServiceException {
		var bet = betService.retrieveBet(betId);
		return buildResponseEntityWithOK(new GenericResponse<>(betMapper.entityToDto(bet)));
	}
	
	/**
	 * Creates / updates a new bet or deletes an existing bet.
	 * Creates a new bet if given {@code betId} is {@code null} and both given 
	 * {@code goalNormal} parameters are non negative numbers. Updates a bet if given 
	 * {@code betId} is not {@code null} and both given {@code goalNormal} parameters 
	 * are non negative numbers. Deletes a bet if {@code betId} is not {@code null} and 
	 * both {@code goalNormal} parameters have {@code null} values.
	 * Bet cannot be created or modified if application time is over the given startTime. 
	 * 
	 * @param userId - user belongs to the bet, mandatory
	 * @param matchId - match belongs to the bet, mandatory
	 * @param betId - optional
	 * @param startTime - match start time, mandatory
	 * @param goalNormal1 - bet goal scored by team1 during normal match time, optional
	 * @param goalNormal2 - bet goal scored by team2 during normal match time, optional
	 * @throws ServiceException - operation failed
	 * @return saved/updated {@link Bet} instance or {@code null} if deleted
	 */
	@PreAuthorize("hasAnyRole('USER')")
	@Operation(summary = "Save a bet", description = "Insert a new bet or update / delete an existing bet")
	@PutMapping(value = "/save-bet")
	public ResponseEntity<GenericResponse<BetDto>> saveBet(@RequestBody BetDto betDto)
			throws ServiceException {
		var bet = betService.saveBet(betDto.getUser().getUserId(), betDto.getMatch().getMatchId(), 
				betDto.getBetId(), betDto.getMatch().getStartTime(), betDto.getGoalNormalByTeam1(), 
				betDto.getGoalNormalByTeam2());
		return buildResponseEntityWithOK(new GenericResponse<>(betMapper.entityToDto(bet)));
	}
	
	/**
	 * Deletes a bet given by its id.
	 * 
	 * @param loginName
	 */
	@PreAuthorize("hasAnyRole('USER')")
	@Operation(summary = "Delete a bet by its id", description = "Delete a bet by its id")
	@DeleteMapping(value = "/delete-bet/{id}")
	public ResponseEntity<CommonResponse> deleteBet(@PathVariable("id") Long betId) throws ServiceException {
		betService.deleteBet(betId);
		return buildResponseEntityWithOK(new CommonResponse());
	}

	/**
	 * Returns a map containing calculated score gained by given {@code userId} user on given 
	 * {@code eventID} event on days of the event. The latter one are the keys of the map and
	 * those are the dates of the bets wagered by the user.
	 *  @param eventId
	 *  @param userId
	 *  @return date-score map  
	 */
	@PreAuthorize("hasAnyRole('USER', 'ADMIN')")	
	@Operation(summary = "Retrieve score by date map of an event and user", description = "Retrieve score by date map of an event and user")
	@GetMapping(value = "/date-score-map-by-event-and-user")
	public ResponseEntity<GenericMapResponse<LocalDateTime, Integer>> retrieveScoresByEventAndUser(@RequestParam Long eventId, @RequestParam Long userId) throws ServiceException {
		var mapScoreByDate = betService.retrieveScoresByEventAndUser(eventId, userId);
		return buildResponseEntityWithOK(new GenericMapResponse<>(mapScoreByDate));
	}
	
	/**
	 * Returns calculated score gained by given {@code userId} user on given {@code eventID} event.
	 * 
	 *  @param eventId
	 *  @param userId
	 */
	@PreAuthorize("hasAnyRole('USER', 'ADMIN')")	
	@Operation(summary = "Return calculated score by event and user", description = "Return calculated score by event and user")
	@GetMapping(value = "/find-score-by-event-and-user")
	public ResponseEntity<GenericResponse<Integer>> retrieveScoreByEventAndUser(@RequestParam Long eventId, @RequestParam Long userId) throws ServiceException {
		var score = betService.retrieveScoreByEventAndUser(eventId, userId);
		return buildResponseEntityWithOK(new GenericResponse<>(score));
	}
	
}
