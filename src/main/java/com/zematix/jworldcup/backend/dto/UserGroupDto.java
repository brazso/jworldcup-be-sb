package com.zematix.jworldcup.backend.dto;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data @EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class UserGroupDto {
	
	@EqualsAndHashCode.Include
	private Long userGroupId;

	private Boolean isPublicEditable;

	private Boolean isPublicVisible;

	private String name;

	@EqualsAndHashCode.Include
	private EventDto event;

	private List<UserDto> users = new ArrayList<>();

	private UserDto owner;
	
//	private List<Chat> chats;
	
}