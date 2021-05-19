package com.zematix.jworldcup.backend.model;

/**
 * UserPosition class belongs to a user and consists of several fields. 
 * Most important is the {@code score} field. This structure helps us to make
 * a score list of the participants.
  */
public class UserPosition {
	
	private Long userId;

	private String fullName;

	private String loginName;
	
	private int score = 0;
	
	/**
	 * position started from 1 (and not 0)
	 */
	private int position = 1;
		

	public Long getUserId() {
		return userId;
	}
	
	public void setUserId(Long userId) {
		this.userId = userId;
	}
	
	public String getFullName() {
		return fullName;
	}
	
	public void setFullName(String fullName) {
		this.fullName = fullName;
	}
	
	public String getLoginName() {
		return loginName;
	}
	
	public void setLoginName(String loginName) {
		this.loginName = loginName;
	}
	
	public int getScore() {
		return score;
	}
	
	public void setScore(int score) {
		this.score = score;
	}
	
	public int getPosition() {
		return position;
	}
	
	public void setPosition(int position) {
		this.position = position;
	}
	
}
