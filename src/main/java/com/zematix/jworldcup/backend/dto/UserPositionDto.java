package com.zematix.jworldcup.backend.dto;

import lombok.Data;

@Data
public class UserPositionDto {
	
	private Long userId;

	private String fullName;

	private String loginName;
	
	private int score = 0;
	
}
