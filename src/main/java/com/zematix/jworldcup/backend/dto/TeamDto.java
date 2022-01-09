package com.zematix.jworldcup.backend.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data @EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class TeamDto {

	@EqualsAndHashCode.Include
	private Long teamId;

	private Short fifaPoints;
	private String flag;
	private String name;
	private Long wsId;
//	private List<Group> groups1;
//	private List<Group> groups2;
//	private List<Match> matches1;
//	private List<Match> matches2;
//	private Event event;
	private GroupDto group;
}