package com.zematix.jworldcup.backend.dto;

import java.util.List;

import com.zematix.jworldcup.backend.entity.Event;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data @EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class UserGroupDto {
	
	@EqualsAndHashCode.Include
	private Long userGroupId;

	private Boolean isPublicEditable;

	private Boolean isPublicVisible;

	private String name;

	private EventDto event;

//	private Set<UserDto> users = new HashSet<>();

	private UserDto owner;
	
//	private List<Chat> chats;
	
	private List<UserDto> virtualUsers;
	
}