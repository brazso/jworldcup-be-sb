package com.zematix.jworldcup.backend.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data @EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = true)
public class UserGroupExtendedDto extends UserGroupDto {
	
	private Long userId;
	private String message; // chat
	
}