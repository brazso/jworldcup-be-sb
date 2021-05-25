package com.zematix.jworldcup.backend.controller;

import javax.inject.Inject;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.zematix.jworldcup.backend.dto.GenericListResponse;
import com.zematix.jworldcup.backend.dto.TeamDto;
import com.zematix.jworldcup.backend.mapper.TeamMapper;
import com.zematix.jworldcup.backend.service.ServiceBase;
import com.zematix.jworldcup.backend.service.TeamService;

import io.swagger.v3.oas.annotations.Operation;

/**
 * WS Rest wrapper class of {@link TeamService}.
 * Only the necessary public methods of its associated service class are in play. 
 */
@RestController
@RequestMapping("teams")
public class TeamController extends ServiceBase implements ResponseEntityHelper {

//	@Inject
//	private Logger logger;

	@Inject
	private TeamService teamService;
	
	@Inject
	private TeamMapper teamMapper;
	
	/**
	 * Retrieves all favourite group teams of an event belongs to the given {@code eventId}.
	 * 
	 * @param eventId
	 * @return all favourite group teams of an event
	 */
	@PreAuthorize("hasAnyRole('USER', 'ADMIN')")
	@Operation(summary = "Find favourite group teams belongs to event", description = "Find favourite group teams belongs to the given event")
	@GetMapping(value = "/find-favourite-group-teams-by-event")
	public ResponseEntity<GenericListResponse<TeamDto>> retrieveFavouriteGroupTeams(@RequestParam Long eventId) {
		var teams = teamService.retrieveFavouriteGroupTeams(eventId);
		return buildResponseEntityWithOK(new GenericListResponse<>(teamMapper.entityListToDtoList(teams)));
	}
	
	/**
	 * Retrieves all favourite knock-out teams of an event belongs to the given {@code eventId}.
	 * 
	 * @param eventId
	 * @return all favourite knock-out teams of an event
	 */
	@PreAuthorize("hasAnyRole('USER', 'ADMIN')")
	@Operation(summary = "Find favourite knock-out teams belongs to event", description = "Find favourite knock-out teams belongs to the given event")
	@GetMapping(value = "/find-favourite-knock-out-teams-by-event")
	public ResponseEntity<GenericListResponse<TeamDto>> retrieveFavouriteKnockoutTeams(@RequestParam Long eventId) {
		var teams = teamService.retrieveFavouriteKnockoutTeams(eventId);
		return buildResponseEntityWithOK(new GenericListResponse<>(teamMapper.entityListToDtoList(teams)));
	}
}
