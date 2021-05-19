package com.zematix.jworldcup.backend.controller;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.ws.rs.core.Response;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.google.common.base.Strings;
import com.zematix.jworldcup.backend.dao.EventDao;
import com.zematix.jworldcup.backend.dto.GenericListResponse;
import com.zematix.jworldcup.backend.dto.GenericResponse;
import com.zematix.jworldcup.backend.dto.MatchBetDto;
import com.zematix.jworldcup.backend.dto.MatchDto;
import com.zematix.jworldcup.backend.dto.RoundDto;
import com.zematix.jworldcup.backend.entity.Event;
import com.zematix.jworldcup.backend.entity.Match;
import com.zematix.jworldcup.backend.entity.Round;
import com.zematix.jworldcup.backend.exception.ServiceException;
import com.zematix.jworldcup.backend.mapper.MatchMapper;
import com.zematix.jworldcup.backend.mapper.RoundMapper;
import com.zematix.jworldcup.backend.model.ParameterizedMessage;
import com.zematix.jworldcup.backend.service.MatchService;
import com.zematix.jworldcup.backend.service.ServiceBase;

import io.swagger.v3.oas.annotations.Operation;

/**
 * WS Rest wrapper class of {@link MatchService}.
 * Only the necessary public methods of its associated service class are in play. 
 */
@RestController
@RequestMapping("matches")
public class MatchController extends ServiceBase {

//	@Inject
//	private Logger logger;

	@Inject
	private MatchService matchService;
	
	@Inject
	private EventDao eventDao;
	
	@Inject 
	private RoundMapper roundMapper;
	
	@Inject
	private MatchMapper matchMapper;
	
	@Value("${app.key}")
	private String serverAppKey;

	/**
	 * Returns a list of {@link Round} instances belongs to the given {@code eventId} 
	 * parameter. The retrieved elements are ordered by {@code roundId}.
	 * Otherwise it may throw ServiceException.
	 *  
	 * @param eventId - event belongs to the rounds to be retrieved
	 * @throws ServiceException if the rounds cannot be retrieved 
	 */
//	@PreAuthorize("hasAnyRole('USER', 'ADMIN')")
	@Operation(summary = "Retrieve rounds of an event", description = "Retrieve all rounds of the given event by its event id")
	@GetMapping(value = "/rounds-by-event-id")
	public ResponseEntity<GenericListResponse<RoundDto>> retrieveRoundsByEvent(@RequestParam Long eventId) throws ServiceException {
		List<Round> rounds = matchService.retrieveRoundsByEvent(eventId);
		return new ResponseEntity<>(new GenericListResponse<>(roundMapper.entityListToDtoList(rounds)), HttpStatus.OK); 
	}

	/**
	 * Returns a list of {@link Round} instances wrapped in {@link Response} belongs to the given
	 * {@link Event} identifier. It is a simple wrapper to the {@link MatchService#retrieveRoundsByEvent(Long)}.
	 *  
	 * @param appKey - key belongs to the application
	 * @param eventByShortDescWithYear - event identifier
	 * @return list of {@link Round} instances wrapped in {@link Response}
	 * @throws ServiceException if the result cannot be retrieved 
	 */
//	@PreAuthorize("hasAnyRole('USER', 'ADMIN')")
	@Operation(summary = "Retrieve rounds of an event", 
			description = "Retrieve all rounds of the given event by its event short description with year")
	@GetMapping(value = "/rounds-by-event-short-desc-with-year")
	public ResponseEntity<GenericListResponse<RoundDto>> retrieveRounds(
			@RequestParam String eventByShortDescWithYear) throws ServiceException {

		List<ParameterizedMessage> errMsgs = new ArrayList<>();

		checkArgument(!Strings.isNullOrEmpty(eventByShortDescWithYear));
		
		Event event = eventDao.findEventByShortDescWithYear(eventByShortDescWithYear);
		checkNotNull(event);
		
		if (!errMsgs.isEmpty()) {
			throw new ServiceException(errMsgs);
		}
		
		List<Round> rounds = matchService.retrieveRoundsByEvent(event.getEventId());
		
		return new ResponseEntity<>(new GenericListResponse<>(roundMapper.entityListToDtoList(rounds)), HttpStatus.OK);
	}

	/**
	 * Returns a list of {@link Match} instances wrapped in {@link Response} belongs to the given
	 * {@link Event} identifier. It is a simple wrapper to the 
	 * {@link MatchService#retrieveMatchesByEvent(Long)}.
	 *  
	 * @param eventByShortDescWithYear - event identifier
	 * @return list of {@link Match} instances wrapped in {@link Response}
	 * @throws ServiceException if the result cannot be retrieved 
	 */
//	@PreAuthorize("hasAnyRole('USER', 'ADMIN')")
	@Operation(summary = "Retrieve matches of an event", description = "Retrieve all matches of the given event")
	@GetMapping(value = "/matches")
	public ResponseEntity<GenericListResponse<MatchDto>> retrieveMatches(
			@RequestParam String eventByShortDescWithYear) throws ServiceException {

		List<ParameterizedMessage> errMsgs = new ArrayList<>();

		checkArgument(!Strings.isNullOrEmpty(eventByShortDescWithYear));
		
		Event event = eventDao.findEventByShortDescWithYear(eventByShortDescWithYear);
		checkNotNull(event);
		
		if (!errMsgs.isEmpty()) {
			throw new ServiceException(errMsgs);
		}
		
		List<Match> matches = matchService.retrieveMatchesByEvent(event.getEventId());
		
		return new ResponseEntity<>(new GenericListResponse<>(matchMapper.entityListToDtoList(matches)), HttpStatus.OK);
	}

	/**
	 * Returns given match result on behalf of the provided side team.
	 *  
	 * @param side - this team is taken into account, its possible values are 1 or 2
	 * @param goalNormal1 - goals scored in normal time by team #1
	 * @param goalExtra1 - goals scored in overtime by team #1
	 * @param goalPenalty1 - goals scored in penalties after overtime by team #1
	 * @param goalNormal2 - goals scored in normal time by team #2
	 * @param goalExtra2 - goals scored in overtime by team #2
	 * @param goalPenalty2 - goals scored in penalties after overtime by team #2
	 * @return team on given side 1 is winner, 0 is draw, -1 is defeat, -2 is unknown
	 * @throws ServiceException
	 */
//	@PreAuthorize("hasAnyRole('USER', 'ADMIN')")
	@Operation(summary = "Return match result by side", 
			description = "Return given match result on behalf of the given side team")
	@PostMapping(value = "/match-result-by-side")
	public ResponseEntity<GenericResponse<Integer>> getMatchResult(@RequestParam Integer side, 
			@RequestBody MatchDto matchDto) throws ServiceException {
		checkNotNull(side);
		checkArgument(side==1 || side==2, "Argument \"side\" value must be 1 or 2.");
		checkNotNull(matchDto);
		
		Integer result = matchService.getMatchResult(side, matchDto.getGoalNormalByTeam1(), matchDto.getGoalExtraByTeam1(), 
				matchDto.getGoalPenaltyByTeam1(), matchDto.getGoalNormalByTeam2(), 
				matchDto.getGoalExtraByTeam2(), matchDto.getGoalPenaltyByTeam2());
		return new ResponseEntity<>(new GenericResponse<>(result), HttpStatus.OK);
	}

	/**
	 * Returns score value after the evaluation of the given parameter.
	 *
	 * @param matchBetDto
	 * @return score value after the evaluation
	 * @throws ServiceException
	 */
//	@PreAuthorize("hasAnyRole('USER', 'ADMIN')")
	@Operation(summary = "Return score by match and bet", 
			description = "Return score value after the evaluation of the given match and bet")
	@PostMapping(value = "/score-by-match-and-bet")
	public ResponseEntity<GenericResponse<Integer>> getScore(@RequestBody MatchBetDto matchBetDto) throws ServiceException {
		checkNotNull(matchBetDto);
		Integer score = matchService.getScore(matchBetDto.getFavTeamId(), matchBetDto.getTeam1Id(), matchBetDto.getTeam2Id(), 
				matchBetDto.getGoalNormalByTeam1(), matchBetDto.getGoalNormalByTeam2(), 
				matchBetDto.getGoalBetByTeam1(), matchBetDto.getGoalBetByTeam2());
		return new ResponseEntity<>(new GenericResponse<>(score), HttpStatus.OK);
	}

}
