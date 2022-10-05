package com.zematix.jworldcup.backend.dto;

import java.time.LocalDateTime;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data @EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class ChatDto  {

	@EqualsAndHashCode.Include
	private Long chatId;
	private EventDto event;
	private String message;
	private LocalDateTime modificationTime;
	private LocalDateTime accessTime;
	private UserGroupDto userGroup;
	private UserDto user;
	private UserDto targetUser;
	
}