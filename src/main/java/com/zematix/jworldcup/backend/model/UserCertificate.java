package com.zematix.jworldcup.backend.model;

import java.io.Serializable;
import java.util.Properties;

/**
 * UserCertificate class belongs to a userGroup and consists of several fields. 
 * Most important is the {@code score} field. This structure helps us to make
 * a certificate list of all userGroups of a user and an event.
 * The object is later stored in {@link Properties}, so it got 
 * {@link Serializable} flag.
 */
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
		

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((eventShortDescWithYear == null) ? 0 : eventShortDescWithYear.hashCode());
		result = prime * result + (everybody ? 1231 : 1237);
		result = prime * result + firstUserScore;
		result = prime * result + maximumScoreByEvent;
		result = prime * result + numberOfEverybodyMembers;
		result = prime * result + numberOfMembers;
		result = prime * result + position;
		result = prime * result + ((userFullName == null) ? 0 : userFullName.hashCode());
		result = prime * result + ((userGroupId == null) ? 0 : userGroupId.hashCode());
		result = prime * result + userGroupLastPosition;
		result = prime * result + ((userGroupName == null) ? 0 : userGroupName.hashCode());
		result = prime * result + userGroupPosition;
		result = prime * result + ((userLoginName == null) ? 0 : userLoginName.hashCode());
		result = prime * result + userScore;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		UserCertificate other = (UserCertificate) obj;
		if (eventShortDescWithYear == null) {
			if (other.eventShortDescWithYear != null)
				return false;
		} else if (!eventShortDescWithYear.equals(other.eventShortDescWithYear))
			return false;
		if (everybody != other.everybody)
			return false;
		if (firstUserScore != other.firstUserScore)
			return false;
		if (maximumScoreByEvent != other.maximumScoreByEvent)
			return false;
		if (numberOfEverybodyMembers != other.numberOfEverybodyMembers)
			return false;
		if (numberOfMembers != other.numberOfMembers)
			return false;
		if (position != other.position)
			return false;
		if (userFullName == null) {
			if (other.userFullName != null)
				return false;
		} else if (!userFullName.equals(other.userFullName))
			return false;
		if (userGroupId == null) {
			if (other.userGroupId != null)
				return false;
		} else if (!userGroupId.equals(other.userGroupId))
			return false;
		if (userGroupLastPosition != other.userGroupLastPosition)
			return false;
		if (userGroupName == null) {
			if (other.userGroupName != null)
				return false;
		} else if (!userGroupName.equals(other.userGroupName))
			return false;
		if (userGroupPosition != other.userGroupPosition)
			return false;
		if (userLoginName == null) {
			if (other.userLoginName != null)
				return false;
		} else if (!userLoginName.equals(other.userLoginName))
			return false;
		return userScore == other.userScore;
	}

	public Long getUserGroupId() {
		return userGroupId;
	}
	
	public void setUserGroupId(Long userGroupId) {
		this.userGroupId = userGroupId;
	}
	
	public String getEventShortDescWithYear() {
		return eventShortDescWithYear;
	}

	public void setEventShortDescWithYear(String eventShortDescWithYear) {
		this.eventShortDescWithYear = eventShortDescWithYear;
	}

	public String getUserLoginName() {
		return userLoginName;
	}

	public void setUserLoginName(String userLoginName) {
		this.userLoginName = userLoginName;
	}

	public String getUserFullName() {
		return userFullName;
	}

	public void setUserFullName(String userFullName) {
		this.userFullName = userFullName;
	}

	public String getUserGroupName() {
		return userGroupName;
	}
	
	public void setUserGroupName(String userGroupName) {
		this.userGroupName = userGroupName;
	}
	
	public int getMaximumScoreByEvent() {
		return maximumScoreByEvent;
	}

	public void setMaximumScoreByEvent(int maximumScoreByEvent) {
		this.maximumScoreByEvent = maximumScoreByEvent;
	}

	public int getFirstUserScore() {
		return firstUserScore;
	}

	public void setFirstUserScore(int firstUserScore) {
		this.firstUserScore = firstUserScore;
	}

	public int getUserScore() {
		return userScore;
	}

	public void setUserScore(int userScore) {
		this.userScore = userScore;
	}

	public int getNumberOfMembers() {
		return numberOfMembers;
	}

	public void setNumberOfMembers(int numberOfMembers) {
		this.numberOfMembers = numberOfMembers;
	}

	public int getNumberOfEverybodyMembers() {
		return numberOfEverybodyMembers;
	}

	public void setNumberOfEverybodyMembers(int numberOfEverybodyMembers) {
		this.numberOfEverybodyMembers = numberOfEverybodyMembers;
	}

	public int getUserGroupPosition() {
		return userGroupPosition;
	}

	public void setUserGroupPosition(int userGroupPosition) {
		this.userGroupPosition = userGroupPosition;
	}

	public int getUserGroupLastPosition() {
		return userGroupLastPosition;
	}

	public void setUserGroupLastPosition(int userGroupLastPosition) {
		this.userGroupLastPosition = userGroupLastPosition;
	}

	public boolean isEverybody() {
		return everybody;
	}

	public void setEverybody(boolean everybody) {
		this.everybody = everybody;
	}

	public int getPosition() {
		return position;
	}
	
	public void setPosition(int position) {
		this.position = position;
	}

	/* Getters */
	
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
