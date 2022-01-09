package com.zematix.jworldcup.backend.controller;

import javax.inject.Inject;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.zematix.jworldcup.backend.dto.GenericListResponse;
import com.zematix.jworldcup.backend.dto.GroupDto;
import com.zematix.jworldcup.backend.dto.GroupTeamDto;
import com.zematix.jworldcup.backend.entity.Event;
import com.zematix.jworldcup.backend.entity.Group;
import com.zematix.jworldcup.backend.exception.ServiceException;
import com.zematix.jworldcup.backend.mapper.GroupMapper;
import com.zematix.jworldcup.backend.mapper.GroupTeamMapper;
import com.zematix.jworldcup.backend.model.GroupTeam;
import com.zematix.jworldcup.backend.service.GroupService;
import com.zematix.jworldcup.backend.service.ServiceBase;

import io.swagger.v3.oas.annotations.Operation;

/**
 * WS Rest wrapper class of {@link GroupService}.
 * Only the necessary public methods of its associated service class are in play. 
 */
@RestController
@RequestMapping("groups")
public class GroupController extends ServiceBase implements ResponseEntityHelper {

//	@Inject
//	private Logger logger;

	@Inject
	private GroupService groupService;
	
	@Inject
	private GroupMapper groupMapper;
	
	@Inject
	private GroupTeamMapper groupTeamMapper;
	
	/**
	 * Return a list of {@link Group} instances belongs to the given {@link Event#eventId} 
	 * parameter. The retrieved elements are ordered by {@link Group#name}.
	 *  
	 * @param eventId - event belongs to the rounds to be retrieved
	 * @throws ServiceException if the matches cannot be retrieved 
	 * @throws IllegalArgumentException if given {@link Event#eventId} is {@code null}
	 */
	@PreAuthorize("hasAnyRole('USER', 'ADMIN')")	
	@Operation(summary = "Retrieve groups by event", description = "Retrieve groups by event")
	@GetMapping(value = "/groups-by-event")
	public ResponseEntity<GenericListResponse<GroupDto>> retrieveGroupsByEvent(@RequestParam Long eventId) throws ServiceException {
		var groups = groupService.retrieveGroupsByEvent(eventId);
		return buildResponseEntityWithOK(new GenericListResponse<>(groupMapper.entityListToDtoList(groups)));
	}
	
	/**
	 * Returns a list of {@link GroupTeam} instances belongs to the provided
	 * {@link Group#getGroupId() group. The list is sorted, starts with first position.
	 *
	 * @param groupId
	 * @return a list of ranked teams belongs to the provided group
	 */
	@PreAuthorize("hasAnyRole('USER', 'ADMIN')")	
	@Operation(summary = "Retrieve group teams by group", description = "Retrieve group teams by group")
	@GetMapping(value = "/group-teams-by-group/{id}")
	public ResponseEntity<GenericListResponse<GroupTeamDto>> getRankedGroupTeamsByGroup(@PathVariable("id") Long groupId) throws ServiceException{
		var groupTeams = groupService.getRankedGroupTeamsByGroup(groupId);
		return buildResponseEntityWithOK(new GenericListResponse<>(groupTeamMapper.entityListToDtoList(groupTeams)));
	}
	
	/**
	 * Returns a list of {@link GroupTeam} instances belongs to the provided
	 * {@link Event#getEventId() event. The list is sorted by its group and position inside its group.
	 *
	 * @param eventId
	 * @return a list of ranked teams belongs to the provided event
	 */
	@PreAuthorize("hasAnyRole('USER', 'ADMIN')")	
	@Operation(summary = "Retrieve group teams by event", description = "Retrieve group teams by event")
	@GetMapping(value = "/group-teams-by-event")
	public ResponseEntity<GenericListResponse<GroupTeamDto>> getRankedGroupTeamsByEvent(@RequestParam Long eventId) throws ServiceException{
		var groupTeams = groupService.getRankedGroupTeamsByEvent(eventId);
		return buildResponseEntityWithOK(new GenericListResponse<>(groupTeamMapper.entityListToDtoList(groupTeams)));
	}
}
