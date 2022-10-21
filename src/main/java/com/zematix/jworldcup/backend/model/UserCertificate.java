package com.zematix.jworldcup.backend.model;

import java.io.Serializable;
import java.util.Properties;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * UserCertificate class belongs to a userGroup and consists of several fields. 
 * Most important is the {@code score} field. This structure helps us to make
 * a certificate list of all userGroups of a user and an event.
 * The object is later stored in {@link Properties}, so it got 
 * {@link Serializable} flag.
 */
@Data @NoArgsConstructor
public class UserCertificate implements Serializable {
	
	private static final long serialVersionUID = 1L;

	private Long userGroupId;
	
	private String eventShortDescWithYear;
	
	private String userLoginName;
	
	private String userFullName;
	
	private String userGroupName;
	
	/**
	 * maximum reachable score on an event
	 */
	private int maximumScoreByEvent = 0;
	
	private int firstUserScore = 0;
	
	private int userScore = 0;
	
	private int numberOfMembers = 0;
	
	/**
	 * number of members of the virtual Everybody group. However in case of topUsers, where only
	 * virtual Everybody group is used, it contains the members of the most populated virtual 
	 * Everybody group of the finished events. 
	 */
	private int numberOfEverybodyMembers = 0;
	
	private int userGroupPosition = 1;
	
	private int userGroupLastPosition = 1;
	
	private boolean everybody = false;
	
	/**
	 * position started from 1 (and not 0)
	 */
	private int position = 1;

	/**
	 * Calculated score of a user in a userGroup after a finished event/tournament.
	 * It consist of more parts in different weights:
	 * - user bet score relative to the theoretically maximum bet score of the event (60%)
	 * - user bet score relative to the maximum bet score of the userGroup (20%)
	 * - user position inverse relative to the last position in the userGroup (10%)
	 * - members of the userGroup relative to the members of Everybody userGroup (10%)
	 * 
	 * @return calculated score of a user in a userGroup
	 */
	public double getScore() {
		double maximumScoreByEvent = this.maximumScoreByEvent == 0 ? 0D : (double)this.userScore / this.maximumScoreByEvent;
		double firstUserScore = this.firstUserScore == 0 ? 0D : (double)this.userScore / this.firstUserScore; 
		double userGroupPosition = this.userGroupLastPosition == 0 ? 0D : this.userGroupLastPosition == 1 ? 1D : (double)(this.userGroupLastPosition - this.userGroupPosition) / (this.userGroupLastPosition-1);
		double numberOfMembers = this.numberOfEverybodyMembers == 0 ? 0D : this.numberOfEverybodyMembers == 1 ? 1D : (double)(this.numberOfMembers-1) / (this.numberOfEverybodyMembers-1);
		
		return maximumScoreByEvent * 0.6 + firstUserScore * 0.2 + userGroupPosition * 0.1 + numberOfMembers * 0.1;
	}
}
