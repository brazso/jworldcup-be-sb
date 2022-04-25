package com.zematix.jworldcup.backend.dto;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class UserCertificateDto {
	
	private Long userGroupId;
	
	private String eventShortDescWithYear;
	private String userLoginName;
	private String userFullName;
	private String userGroupName;
	private int maximumScoreByEvent = 0;
	private int firstUserScore = 0;
	private int userScore = 0;
	private int numberOfMembers = 0;
	private int numberOfEverybodyMembers = 0;
	private int userGroupPosition = 1;
	private int userGroupLastPosition = 1;
	private boolean everybody = false;
	private int position = 1;
	private double score;

}
