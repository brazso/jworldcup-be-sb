package com.zematix.jworldcup.backend.dto;

import java.time.LocalDateTime;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data @EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class UserNotificationDto {
	
	@EqualsAndHashCode.Include
	private Long userNotificationId;

	private UserDto user;

	private DictionaryDto userNotificationType;
	
	private LocalDateTime creationTime;

	private LocalDateTime modificationTime;

	private String value;
}