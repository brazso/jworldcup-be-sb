package com.zematix.jworldcup.backend.service;

/**
 * Supported template types used by {@link TemplateId}
 */
public enum TemplateType {
	EMAIL("email"),
	PDF("pdf");
	
	public final String fileName;
	
	TemplateType(String fileName) {
		this.fileName = fileName;
	}
}
