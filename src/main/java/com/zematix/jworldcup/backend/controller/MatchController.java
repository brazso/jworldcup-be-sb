package com.zematix.jworldcup.backend.controller;

import static com.google.common.base.Preconditions.checkArgument;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.ws.rs.core.Response;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.google.common.base.Strings;
import com.zematix.jworldcup.backend.dao.EventDao;
import com.zematix.jworldcup.backend.dto.GenericListResponse;
import com.zematix.jworldcup.backend.dto.MatchDto;
import com.zematix.jworldcup.backend.dto.RoundDto;
import com.zematix.jworldcup.backend.entity.Event;
import com.zematix.jworldcup.backend.entity.Match;
import com.zematix.jworldcup.backend.entity.Round;
import com.zematix.jworldcup.backend.mapper.MatchMapper;
import com.zematix.jworldcup.backend.mapper.RoundMapper;
import com.zematix.jworldcup.backend.service.MatchService;
import com.zematix.jworldcup.backend.service.ParametrizedMessage;
import com.zematix.jworldcup.backend.service.ServiceBase;
import com.zematix.jworldcup.backend.service.ServiceException;

import io.swagger.v3.oas.annotations.Operation;

/**
 * WS Rest wrapper class of {@link MatchService}.
 * Only the necessary public methods of its associated class are in play. 
 */
@RestController
@RequestMapping("matches")
public class MatchController extends ServiceBase {

	@Inject
	private MatchService matchService;
	
	@Inject
	private EventDao eventDao;
	
	@Inject 
	private RoundMapper roundMapper;
	
	@Inject
	private MatchMapper matchMapper;
	
//	@Inject
//	//@Context // although it works without an own producer, but it does not support mocking
//	private UriInfo uriInfo;
	
//	@Inject
//	private Logger logger;

	@Value("${app.key}")
	private String serverAppKey;

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
	@Operation(summary = "Retrieve rounds of an event", description = "Retrieve all rounds of the given event")
	@GetMapping(value = "/rounds")
	public ResponseEntity<GenericListResponse<RoundDto>> retrieveRounds(//@RequestParam("appKey") String appKey,
			@RequestParam("eventByShortDescWithYear") String eventByShortDescWithYear) throws ServiceException {

		List<ParametrizedMessage> errMsgs = new ArrayList<>();

//		if (Strings.isNullOrEmpty(appKey) || !appKey.equals(serverAppKey)) {
//			errMsgs.add(ParametrizedMessage.create("DISALLOWED_TO_CALL_WS"));
//		}

		if (!errMsgs.isEmpty()) {
			throw new ServiceException(errMsgs);
		}

		checkArgument(!Strings.isNullOrEmpty(eventByShortDescWithYear));
		
		Event event = eventDao.findEventByShortDescWithYear(eventByShortDescWithYear);
		checkArgument(event != null);
		
		if (!errMsgs.isEmpty()) {
			throw new ServiceException(errMsgs);
		}
		
		List<Round> rounds = matchService.retrieveRoundsByEvent(event.getEventId());
//		GenericEntity<List<Round>> genericRounds = new GenericEntity<List<Round>>(rounds){};
		
//		return Response.status(Response.Status.OK).entity(genericRounds).build();
		return new ResponseEntity<>(new GenericListResponse<>(roundMapper.entityListToDtoList(rounds)), HttpStatus.OK);
	}

	/**
	 * Returns a list of {@link Match} instances wrapped in {@link Response} belongs to the given
	 * {@link Event} identifier. It is a simple wrapper to the 
	 * {@link MatchService#retrieveMatchesByEvent(Long)}.
	 *  
	 * @param appKey - key belongs to the application
	 * @param eventByShortDescWithYear - event identifier
	 * @return list of {@link Match} instances wrapped in {@link Response}
	 * @throws ServiceException if the result cannot be retrieved 
	 */
//	@PreAuthorize("hasAnyRole('USER', 'ADMIN')")
	@Operation(summary = "Retrieve matches of an event", description = "Retrieve all matches of the given event")
	@GetMapping(value = "/matches")
	public ResponseEntity<GenericListResponse<MatchDto>>  retrieveMatches(//@RequestParam("appKey") String appKey,
			@RequestParam("eventByShortDescWithYear") String eventByShortDescWithYear) throws ServiceException {

		List<ParametrizedMessage> errMsgs = new ArrayList<>();

//		if (Strings.isNullOrEmpty(appKey) || !appKey.equals(serverAppKey)) {
//			errMsgs.add(ParametrizedMessage.create("DISALLOWED_TO_CALL_WS"));
//		}

		if (!errMsgs.isEmpty()) {
			throw new ServiceException(errMsgs);
		}

		checkArgument(!Strings.isNullOrEmpty(eventByShortDescWithYear));
		
		Event event = eventDao.findEventByShortDescWithYear(eventByShortDescWithYear);
		checkArgument(event != null);
		
		if (!errMsgs.isEmpty()) {
			throw new ServiceException(errMsgs);
		}
		
		List<Match> matches = matchService.retrieveMatchesByEvent(event.getEventId());
//		GenericEntity<List<Match>> genericMatches = new GenericEntity<List<Match>>(matches){};
		
//		return Response.status(Response.Status.OK).entity(genericMatches).build();
		return new ResponseEntity<>(new GenericListResponse<>(matchMapper.entityListToDtoList(matches)), HttpStatus.OK);
	}

}
