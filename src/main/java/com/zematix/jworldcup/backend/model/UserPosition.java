package com.zematix.jworldcup.backend.model;

import lombok.Getter;
import lombok.Setter;

/**
 * UserPosition class belongs to a user and consists of several fields. 
 * Most important is the {@code score} field. This structure helps us to make
 * a score list of the participants.
  */
@Getter @Setter
public class UserPosition {
	
	private Long userId;

	private String fullName;

	private String loginName;
	
	private int score = 0;
	
	/**
	 * position started from 1 (and not 0)
	 */
	private int position = 1;
		

}
