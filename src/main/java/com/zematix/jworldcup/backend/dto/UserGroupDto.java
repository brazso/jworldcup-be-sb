package com.zematix.jworldcup.backend.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data @EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class UserGroupDto {
	
	@EqualsAndHashCode.Include
	private Long userGroupId;

	private Boolean isPublicEditable;

	private Boolean isPublicVisible;

	private String name;

//	private Event event;

//	private Set<User> users;

//	private User owner;
	
//	private List<Chat> chats;
	
//	@Transient
//	private List<User> usersAsList;
	
}