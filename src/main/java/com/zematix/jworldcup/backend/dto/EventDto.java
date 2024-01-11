package com.zematix.jworldcup.backend.dto;

import java.time.LocalDateTime;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data @EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class EventDto {

	@EqualsAndHashCode.Include
	private Long eventId;

	private String description;
	private String location;
	private String shortDesc;
	private Short year;
	private String organizer;
	private String website;
//	private List<Bet> bets;
//	private List<Group> groups;
//	private List<Match> matches;
//	private List<Round> rounds;
//	private List<Team> teams;
//	private List<WebService> webServices;
//	private List<UserOfEvent> userOfEvents;
//	private List<UserGroup> userGroups;
//	private List<Chat> chats;
	private LocalDateTime startTime;
	private LocalDateTime knockoutStartTime;
	private LocalDateTime endTime;

}