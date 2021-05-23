package com.zematix.jworldcup.backend.controller;

import java.util.List;

import javax.inject.Inject;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.zematix.jworldcup.backend.dto.BetDto;
import com.zematix.jworldcup.backend.dto.GenericListResponse;
import com.zematix.jworldcup.backend.dto.GenericResponse;
import com.zematix.jworldcup.backend.entity.Bet;
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
		//return new ResponseEntity<>(new GenericListResponse<>(betMapper.entityListToDtoList(bets)), HttpStatus.OK);
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
		Bet bet = betService.retrieveBet(betId);
		//return new ResponseEntity<>(new GenericResponse<>(betMapper.entityToDto(bet)), HttpStatus.OK);
		return buildResponseEntityWithOK(new GenericResponse<>(betMapper.entityToDto(bet)));
	}
	
}
