package com.zematix.jworldcup.backend.service;

/**
 * Supported templates used by {@link TemplateService}
 * There are different EMAIL and PDF templates here.
 */
public enum TemplateId {
	REGISTRATION_PLAIN(TemplateType.EMAIL, "registration", "ftl"),
	REGISTRATION_HTML(TemplateType.EMAIL, "registration", "html"),
	CHANGE_EMAIL_PLAIN(TemplateType.EMAIL, "change_email", "ftl"),
	CHANGE_EMAIL_HTML(TemplateType.EMAIL, "change_email", "html"),
	RESET_PASSWORD_PLAIN(TemplateType.EMAIL, "reset_password", "ftl"),
	RESET_PASSWORD_HTML(TemplateType.EMAIL, "reset_password", "html"),
	USER_CERTIFICATE_PDF(TemplateType.PDF, "user_certificate", "odt");
	
	public final TemplateType templateType;
	public final String fileName;
	public final String fileExtension;
	
	TemplateId(TemplateType templateType, String fileName, String fileExtension) {
		this.templateType = templateType;
		this.fileName = fileName;
		this.fileExtension = fileExtension;
	}
}
