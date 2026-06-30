package com.zematix.jworldcup.backend.service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;

import org.json.JSONException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.skyscreamer.jsonassert.JSONAssert;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.Sql.ExecutionPhase;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import com.zematix.jworldcup.backend.TestBase;
import com.zematix.jworldcup.backend.dto.TeamDto;
import com.zematix.jworldcup.backend.entity.Team;
import com.zematix.jworldcup.backend.exception.ServiceException;
import com.zematix.jworldcup.backend.mapper.TeamMapper;
import com.zematix.jworldcup.backend.model.GroupPosition;

import jakarta.inject.Inject;

/**
 * Contains test functions of {@link GroupService} class.
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles({"develop", "test"})
@Transactional
public class GroupServiceIT extends TestBase {
	
	@Inject
	private GroupService groupService;
	
//	@MockitoSpyBean // unfortunately partial mock does not work on bean with @ApplicationScope
	@MockitoBean
	private ApplicationService applicationService;
	
	@Inject
	private TeamMapper teamMapper;
	
	/**
	 * Test {@link GroupService#getTeamByGroupPositionMap(Long)} method.
	 * Scenario: successfully retrieves teams got into round-of-16 after group stage is finished
	 * @throws JSONException 
	 */
	@Test
	@Sql(scripts = { "/database/service/group-service-before.sql" }, executionPhase = ExecutionPhase.BEFORE_TEST_METHOD)
	//@Sql(scripts = { "/database/service/group-service-after.sql" }, executionPhase = ExecutionPhase.AFTER_TEST_METHOD)
	public void /*Map<GroupPosition, Team>*/ getTeamByGroupPositionMapForWC2026(/*Long eventId*/) throws ServiceException, JSONException {
		// given
		Long eventId = 17L; // WC2026
		LocalDateTime actualDateTime = LocalDateTime.parse("3000-01-01 00:00:00", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
		String expectedJsonResult = readStringResource("service/groupservice/WC2026-gpteam-map.json");
		// when
		Mockito.when(applicationService.getActualDateTime()).thenReturn(actualDateTime);
		Map<GroupPosition, Team> map = groupService.getTeamByGroupPositionMap(eventId);
		// jackson json mapping does not work on Team, it must be mapped at first
		Map<GroupPosition, TeamDto> mapDto = new LinkedHashMap<>(); // order of keys are relevant
		map.forEach((key, value) -> {
	        mapDto.put(key, teamMapper.entityToDto(value));
		});
		// then
		String jsonResult = generatePrettyJson(mapDto);
		JSONAssert.assertEquals(expectedJsonResult, jsonResult, false);
	}

}
