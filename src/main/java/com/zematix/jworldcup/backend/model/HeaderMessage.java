package com.zematix.jworldcup.backend.model;

import java.time.LocalDateTime;
import java.util.Comparator;

import lombok.Builder;
import lombok.Data;

@Data @Builder
public class HeaderMessage {
	private String message;
	private int priority; // 1..5 (high to low)
	private LocalDateTime creationTime;
	private boolean isActive; // sent one becomes inactive until it is purged eventually

	/**
	 * sort by isActive asc, priority asc, creationTime asc
	 */
	public static final Comparator<HeaderMessage> comparator = (e1, e2) -> {
		return Boolean.compare(e1.isActive, e2.isActive) != 0 ? Boolean.compare(e1.isActive, e2.isActive)*-1
				: (Integer.compare(e1.getPriority(), e2.getPriority()) != 0
						? Integer.compare(e1.getPriority(), e2.getPriority())
						: e1.getCreationTime().compareTo(e1.getCreationTime()));
	};
}
