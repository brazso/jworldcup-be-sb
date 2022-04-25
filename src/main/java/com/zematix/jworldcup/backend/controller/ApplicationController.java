package com.zematix.jworldcup.backend.controller;

import javax.inject.Inject;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.zematix.jworldcup.backend.dto.GenericListResponse;
import com.zematix.jworldcup.backend.dto.UserCertificateDto;
import com.zematix.jworldcup.backend.exception.ServiceException;
import com.zematix.jworldcup.backend.mapper.UserCertificateMapper;
import com.zematix.jworldcup.backend.model.UserCertificate;
import com.zematix.jworldcup.backend.service.ApplicationService;
import com.zematix.jworldcup.backend.service.ServiceBase;

import io.swagger.v3.oas.annotations.Operation;

/**
 * WS Rest wrapper class of {@link ApplicatgionService}.
 * Only the necessary public methods of its associated service class are in play.
 */
@RestController
@RequestMapping("application")
public class ApplicationController extends ServiceBase implements ResponseEntityHelper {

	@Inject
	private ApplicationService applicationService;

	@Inject
	private UserCertificateMapper userCertificateMapper;
	
	/**
	 * Returns a sorted list of {@link UserCertificate} instances from all events.
	 * 
	 * @return list of sorted userCertificate objects from all events
	 */
	@PreAuthorize("hasAnyRole('USER', 'ADMIN')")
	@Operation(summary = "Retrive sorted userCertificate list from all events", description = "Retrive sorted userCertificate list from all events")
	@GetMapping(value = "/retrieve-top-users")
	public ResponseEntity<GenericListResponse<UserCertificateDto>> retrieveTopUsers() throws ServiceException {
		var topUsers = applicationService.getTopUsersCache();
		return buildResponseEntityWithOK(new GenericListResponse<>(userCertificateMapper.entityListToDtoList(topUsers)));
	}
}
