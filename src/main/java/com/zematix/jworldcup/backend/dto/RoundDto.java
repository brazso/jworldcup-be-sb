package com.zematix.jworldcup.backend.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data @EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class RoundDto {

	@EqualsAndHashCode.Include
	private Long roundId;
	
	private Boolean isGroupmatch;
	private Boolean isOvertime;
	private String name;
//	private List<Match> matches;
//	private Event event;

}