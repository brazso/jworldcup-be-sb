package com.zematix.jworldcup.backend.dto;

import java.time.LocalDateTime;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data @EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class ChatDto  {

	@EqualsAndHashCode.Include
	private Long chatId;
//	private Event event;
	private String message;
	private LocalDateTime modificationTime;
//	private UserGroup userGroup;
//	private User user;
	
}