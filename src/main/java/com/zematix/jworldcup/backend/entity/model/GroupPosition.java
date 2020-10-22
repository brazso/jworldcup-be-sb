package com.zematix.jworldcup.backend.entity.model;

/**
 * GroupPosition consists of groupName and position. It is a helper class to
 * locate a team inside a group given by groupName on the given position.
 * If groupName consists of more groups (n), then groupPosition specifies the first 
 * available team of the best {@code m-n+1} teams on position of all groups (m). 
 */
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
	
	public GroupPosition(String groupName, Integer position) {
		this.setGroupName(groupName);
		this.setPosition(position);
	}

	public String getGroupName() {
		return groupName;
	}

	public void setGroupName(String groupName) {
		this.groupName = groupName;
	}

	public Integer getPosition() {
		return position;
	}

	public void setPosition(Integer position) {
		this.position = position;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((groupName == null) ? 0 : groupName.hashCode());
		result = prime * result + ((position == null) ? 0 : position.hashCode());
		return result;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		GroupPosition other = (GroupPosition) obj;
		if (groupName == null) {
			if (other.groupName != null)
				return false;
		} else if (!groupName.equals(other.groupName))
			return false;
		if (position == null) {
			if (other.position != null)
				return false;
		} else if (!position.equals(other.position))
			return false;
		return true;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "GroupPosition [groupName=" + groupName + ", position=" + position + "]";
	}
}
