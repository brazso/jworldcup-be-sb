package com.zematix.jworldcup.backend.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;


/**
 * The persistent class for the bet database table.
 * 
 */
@Data @EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class BetDto {
	
	@EqualsAndHashCode.Include
	private Long betId;

	private Byte goalNormalByTeam1;
	private Byte goalNormalByTeam2;
//	private Event event;
	private MatchDto match;
	private UserDto user;
	private Integer score;
}