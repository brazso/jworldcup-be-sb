package com.zematix.jworldcup.backend.controller;

import javax.inject.Inject;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.zematix.jworldcup.backend.dao.EventDao;
import com.zematix.jworldcup.backend.dto.EventDto;
import com.zematix.jworldcup.backend.dto.GenericListResponse;
import com.zematix.jworldcup.backend.dto.GenericResponse;
import com.zematix.jworldcup.backend.dto.MatchDto;
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
}
