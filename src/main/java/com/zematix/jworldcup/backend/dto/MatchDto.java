package com.zematix.jworldcup.backend.dto;

import java.time.LocalDateTime;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data @EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class MatchDto {

	@EqualsAndHashCode.Include
	private Long matchId;

	private Byte goalExtraByTeam1;
	private Byte goalExtraByTeam2;
	private Byte goalNormalByTeam1;
	private Byte goalNormalByTeam2;
	private Byte goalPenaltyByTeam1;
	private Byte goalPenaltyByTeam2;
	private Short matchN;
	private String participantsRule;
	private LocalDateTime startTime;
//	private List<Bet> bets;
//	private Event event;
	private RoundDto round;
	private TeamDto team1;
	private TeamDto team2;
	private Integer resultSignByTeam1;

}