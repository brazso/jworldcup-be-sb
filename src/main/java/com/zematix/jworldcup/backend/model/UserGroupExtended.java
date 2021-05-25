package com.zematix.jworldcup.backend.model;

import com.zematix.jworldcup.backend.entity.UserGroup;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data @EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = true)
public class UserGroupExtended extends UserGroup {

	private static final long serialVersionUID = 1L;
	
	private Long userId;
    private String message; // chat

}