package com.zematix.jworldcup.backend.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;


@Data @EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class GroupDto {

	@EqualsAndHashCode.Include
	private Long groupId;

	private String name;
//	private Event event;
//	private Team team1;
//	private Team team2;
//	private List<Team> teams;

}