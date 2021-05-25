package com.zematix.jworldcup.backend.dto;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class GroupTeamDto {

	private TeamDto team;
	private List<MatchDto> playedMatches;
	private List<Long> filterTeamIds;
	private int positionInGroup;
	private boolean isTeamInGroupFinished;
}
