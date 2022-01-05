package com.zematix.jworldcup.backend.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data @EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class UserOfEventDto {

	@EqualsAndHashCode.Include
	private Long userOfEventId;

//	private User user;
//	private Event event;
	private TeamDto favouriteGroupTeam;
	private TeamDto favouriteKnockoutTeam;

}
