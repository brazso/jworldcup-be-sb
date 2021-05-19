package com.zematix.jworldcup.backend.dto;

import lombok.Data;

@Data
public class MatchBetDto {

	private Long favTeamId;
	private Long team1Id;
	private Long team2Id;
	
	private Byte goalNormalByTeam1;
	private Byte goalNormalByTeam2;
	private Byte goalBetByTeam1;
	private Byte goalBetByTeam2;

}