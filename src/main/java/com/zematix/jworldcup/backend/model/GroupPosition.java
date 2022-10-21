package com.zematix.jworldcup.backend.model;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * GroupPosition consists of groupName and position. It is a helper class to
 * locate a team inside a group given by groupName on the given position.
 * If groupName consists of more groups (n), then groupPosition specifies the first 
 * available team of the best {@code m-n+1} teams on position of all groups (m). 
 */
@Data @AllArgsConstructor
public class GroupPosition {
	/**
	 * Usually groupName is a {@code 1} character length group name, e.g. "A".
	 * However at some event (e.g. EC2016) it might contain more characters,
	 * e.g. "ABC", which denotes more groups.
	 */
	private String groupName;
	
	/**
	 * Position between 1 and number of teams inside a group
	 */
	private Integer position;
}
