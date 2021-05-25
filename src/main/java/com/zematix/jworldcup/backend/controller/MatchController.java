package com.zematix.jworldcup.backend.controller;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import java.time.LocalDateTime;
import java.util.List;

import javax.inject.Inject;
import javax.ws.rs.core.Response;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.google.common.base.Strings;
import com.zematix.jworldcup.backend.dao.EventDao;
import com.zematix.jworldcup.backend.dto.CommonResponse;
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
import com.zematix.jworldcup.backend.service.MatchService;
import com.zematix.jworldcup.backend.service.ServiceBase;

import io.swagger.v3.oas.annotations.Operation;

/**
 * WS Rest wrapper class of {@link MatchService}.
 * Only the necessary public methods of its associated service class are in play. 
 */
@RestController
@RequestMapping("matches")
public class MatchController extends ServiceBase implements ResponseEntityHelper {

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
	
	/**
	 * Returns a wrapped list of {@link Round} instances belongs to the given {@code eventId} 
	 * parameter. The retrieved elements are ordered by {@code roundId}.
	 *  
	 * @param eventId - event belongs to the rounds to be retrieved
	 * @throws ServiceException if the rounds cannot be retrieved 
	 */
	@PreAuthorize("hasAnyRole('USER', 'ADMIN')")
	@Operation(summary = "Retrieve rounds of an event", description = "Retrieve all rounds of the given event by its event id")
	@GetMapping(value = "/rounds-by-event-id")
	public ResponseEntity<GenericListResponse<RoundDto>> retrieveRoundsByEvent(@RequestParam Long eventId) throws ServiceException {
		List<Round> rounds = matchService.retrieveRoundsByEvent(eventId);
		return buildResponseEntityWithOK(new GenericListResponse<>(roundMapper.entityListToDtoList(rounds)));
	}

	/**
	 * Retrieves a wrapped {@link Match} instance.
	 * 
	 * @param matchId
	 * @return retrieved {@link Match} instance or ServiceException unless it is found
	 * @throws ServiceException
	 */
	@PreAuthorize("hasAnyRole('USER', 'ADMIN')")	
	@Operation(summary = "Retrieve match by its id", description = "Retrieve match by its id")
	@GetMapping(value = "/match/{id}")
	public ResponseEntity<GenericResponse<MatchDto>> retrieveMatch(@PathVariable("id") Long matchId) throws ServiceException {
		Match match = matchService.retrieveMatch(matchId);
		return buildResponseEntityWithOK(new GenericResponse<>(matchMapper.entityToDto(match)));
	}

	/**
	 * Updates the end result of the given match. It is a simple wrapper to
	 * the same method in {@link MatchService}.
	 * 
	 * @param matchDto - match to be updated
	 * @return updated match wrapped in 
	 * @throws ServiceException if the given match cannot be saved
	 */
	@PreAuthorize("hasAnyRole('USER', 'ADMIN')")
	@Operation(summary = "Save a match", description = "Updates the end result of a match with the given data")
	@PutMapping(value = "/save-match")
	public ResponseEntity<GenericResponse<MatchDto>> saveMatch(
			@RequestBody MatchDto matchDto) throws ServiceException {
		Match match = matchService.saveMatch(matchDto.getMatchId(), matchDto.getRound().getIsGroupmatch(), 
				matchDto.getRound().getIsOvertime(), matchDto.getStartTime(), matchDto.getGoalNormalByTeam1(), 
				matchDto.getGoalNormalByTeam2(), matchDto.getGoalExtraByTeam1(), matchDto.getGoalExtraByTeam2(), 
				matchDto.getGoalPenaltyByTeam1(), matchDto.getGoalPenaltyByTeam2());
		return buildResponseEntityWithOK(new GenericResponse<>(matchMapper.entityToDto(match)));
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
	@PreAuthorize("hasAnyRole('USER', 'ADMIN')")
	@Operation(summary = "Retrieve matches of an event", description = "Retrieve all matches of the given event")
	@GetMapping(value = "/matches-by-event")
	public ResponseEntity<GenericListResponse<MatchDto>> retrieveMatchesByEvent(
			@RequestParam String eventByShortDescWithYear) throws ServiceException {

		checkArgument(!Strings.isNullOrEmpty(eventByShortDescWithYear));
		
		Event event = eventDao.findEventByShortDescWithYear(eventByShortDescWithYear);
		checkNotNull(event);
		
		List<Match> matches = matchService.retrieveMatchesByEvent(event.getEventId());
		
		return buildResponseEntityWithOK(new GenericListResponse<>(matchMapper.entityListToDtoList(matches)));
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
	@PreAuthorize("hasAnyRole('USER', 'ADMIN')")
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
		return buildResponseEntityWithOK(new GenericResponse<>(result));
	}

	/**
	 * Returns score value after the evaluation of the given parameter.
	 *
	 * @param matchBetDto
	 * @return score value after the evaluation
	 * @throws ServiceException
	 */
	@PreAuthorize("hasAnyRole('USER', 'ADMIN')")
	@Operation(summary = "Return score by match and bet", 
			description = "Return score value after the evaluation of the given match and bet")
	@GetMapping(value = "/score-by-match-and-bet")
	public ResponseEntity<GenericResponse<Integer>> getScore(@RequestBody MatchBetDto matchBetDto) throws ServiceException {
		checkNotNull(matchBetDto);
		Integer score = matchService.getScore(matchBetDto.getFavTeamId(), matchBetDto.getTeam1Id(), matchBetDto.getTeam2Id(), 
				matchBetDto.getGoalNormalByTeam1(), matchBetDto.getGoalNormalByTeam2(), 
				matchBetDto.getGoalBetByTeam1(), matchBetDto.getGoalBetByTeam2());
		return buildResponseEntityWithOK(new GenericResponse<>(score));
	}
	
	/**
	 * Returns the calculated match endTime from the given {@code startTime}. 
	 * A match may last 45 minutes + 15 minutes (break) + 45 minutes.
	 * 
	 * @param startTime - start datetime of a match
	 * @return calculated endTime which equals to given {@code startTime} + 105 minutes
	 * @throws ServiceException
	 */
	@PreAuthorize("hasAnyRole('USER', 'ADMIN')")
	@Operation(summary = "Returns the calculated match endTime", description = "Returns the calculated match endTime from the given match startTime")
	@GetMapping(value = "/end-date-time")
	public ResponseEntity<GenericResponse<LocalDateTime>> getEndDateTime(@RequestParam LocalDateTime startTime) throws ServiceException {
		var endTime = matchService.getEndDateTime(startTime);
		return buildResponseEntityWithOK(new GenericResponse<>(endTime));
	}

	/**
	 * Relaunches scheduled retrieval of missing match results job if the trigger exists.
	 *  
	 * @param eventId
	 * @throws ServiceException
	 */
	@PreAuthorize("hasAnyRole('USER', 'ADMIN')")
	@Operation(summary = "Relaunches scheduled retrieval of missing match results job", description = "Relaunches scheduled retrieval of missing match results job if the trigger exists")
	@PutMapping(value = "/refresh-matches-by-scheduler")
	public ResponseEntity<CommonResponse> refreshMatchesByScheduler(@RequestParam Long eventId) throws ServiceException {
		matchService.refreshMatchesByScheduler(eventId);
		return buildResponseEntityWithOK(new CommonResponse());
	}
	
	/**
	 * Returns a distinct list of start time of matches belongs to the given event. 
	 * The result list is ordered by start time.
	 * 
	 * @param eventId
	 * @return distinct list of start time of matches belongs to the given event
	 * @throws ServiceException
	 */
	@PreAuthorize("hasAnyRole('USER', 'ADMIN')")
	@Operation(summary = "Returns match start time list of an event", description = "Returns all match start datetimes of the given event")
	@GetMapping(value = "/match-start-dates-by-event")
	public ResponseEntity<GenericListResponse<LocalDateTime>> retrieveMatchStartDatesByEvent(Long eventId) throws ServiceException {
		var startDates = matchService.retrieveMatchStartDatesByEvent(eventId);
		return buildResponseEntityWithOK(new GenericListResponse<>(startDates));
	}
}
