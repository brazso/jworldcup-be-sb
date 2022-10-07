package com.zematix.jworldcup.backend.model;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.LinkedList;
import java.util.List;

public class HeaderMessageList {
	private List<HeaderMessage> headerMessages = new LinkedList<>();
	
	/**
	 * list elements which were created earlier in minutes are being purged 
	 */
	private static final int HEADER_MESSAGE_PURGE_IN_MINUTE = 60;

	/** 
	 * Add elements to list if it is not contained by message and priority yet. 
	 * @param headerMessage
	 * @return {@code true} if the push was successful
	 */
	public boolean push(HeaderMessage headerMessage) {
		boolean isExists = this.headerMessages.stream().anyMatch(e -> e.getMessage().equals(headerMessage.getMessage()) && e.getPriority() == headerMessage.getPriority());
		if (!isExists && this.headerMessages.add(headerMessage)) {
			headerMessage.setActive(true);
			this.sort();
			return true;
		}
		return false;
	}
	
	private void sort() {
		this.headerMessages.sort(HeaderMessage.comparator);
	}

	/**
	 * Inactivates the first active element in list and returns that element otherwise null 
	 * @return first active element in list otherwise null
	 */
	public HeaderMessage pop() {
		HeaderMessage headerMessage = this.headerMessages.stream().filter(e -> e.isActive()).findFirst().orElse(null);
		if (headerMessage != null) {
			headerMessage.setActive(false);
			this.sort();
		}
		return headerMessage;
	}

	/**
	 * Remove all elements from headerMessages list which were created earlier than {@link HeaderMessageList#HEADER_MESSAGE_PURGE_IN_MINUTE} minutes ago 
	 * @param actualDateTime
	 */
	public void purge(LocalDateTime actualDateTime) {
		this.headerMessages = this.headerMessages.stream().filter(e -> e.getCreationTime().isBefore(actualDateTime.minus(HEADER_MESSAGE_PURGE_IN_MINUTE, ChronoUnit.MINUTES))).toList();
		this.sort();
	}
	
	/**
	 * Remove all elements from headerMessages list which were created earlier than the given purgeDateTime 
	 * @param purgeDateTime
	 */
	public void purgeBefore(LocalDateTime purgeDateTime) {
		this.headerMessages = this.headerMessages.stream().filter(e -> e.getCreationTime().isBefore(purgeDateTime)).toList();
		this.sort();
	}
	
	public void clear() {
		this.headerMessages.clear();
	}
}
