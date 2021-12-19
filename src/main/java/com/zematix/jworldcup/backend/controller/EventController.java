package com.zematix.jworldcup.backend.controller;

import javax.inject.Inject;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.zematix.jworldcup.backend.dao.EventDao;
import com.zematix.jworldcup.backend.dto.EventDto;
import com.zematix.jworldcup.backend.dto.GenericListResponse;
import com.zematix.jworldcup.backend.dto.GenericResponse;
import com.zematix.jworldcup.backend.entity.Event;
import com.zematix.jworldcup.backend.mapper.EventMapper;
import com.zematix.jworldcup.backend.service.EventService;
import com.zematix.jworldcup.backend.service.ServiceBase;

import io.swagger.v3.oas.annotations.Operation;

/**
 * WS Rest wrapper class of {@link EventService}.
 * Only the necessary public methods of its associated service class are in play. 
 */
@RestController
@RequestMapping("events")
public class EventController extends ServiceBase implements ResponseEntityHelper {

//	@Inject
//	private Logger logger;

	@Inject
	private EventService eventService;
	
	@Inject
	private EventMapper eventMapper;

	/**
	 * Same as {@link EventDao#findAllEvents()} but it also loads all 
	 * transitive fields of {@link Event} instances.
	 * @return list of all Events entities
	 */
	@PreAuthorize("hasAnyRole('USER', 'ADMIN')")
	@Operation(summary = "Find all events", description = "Find all events")
	@GetMapping(value = "/find-all-events")
	public ResponseEntity<GenericListResponse<EventDto>> findAllEvents() {
		var events = eventService.findAllEvents();
		return buildResponseEntityWithOK(new GenericListResponse<>(eventMapper.entityListToDtoList(events)));
	}

	/**
	/**
	 * Retrieves that event which belongs to the last bet of the input user. If the user
	 * has no bet at all, the last event is retrieved.
	 * @return event proposed to the given user 
	 */
	@PreAuthorize("hasAnyRole('USER', 'ADMIN')")
	@Operation(summary = "Find event of the given user", description = "Find event which belongs to the last bet of the input user. If the user has no bet at all, the last event is retrieved.")
	@GetMapping(value = "/find-event-by-user")
	public ResponseEntity<GenericResponse<EventDto>> findEventByUserId(@RequestParam Long userId) {
		var event = eventService.findLastEventByUserId(userId);
		return buildResponseEntityWithOK(new GenericResponse<>(eventMapper.entityToDto(event)));
	}
}
