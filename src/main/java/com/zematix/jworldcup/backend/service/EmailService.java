package com.zematix.jworldcup.backend.service;

import java.util.Locale;
import java.util.Properties;

import javax.inject.Inject;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import org.springframework.web.context.annotation.ApplicationScope;

import com.zematix.jworldcup.backend.entity.User;

/**
 * Helper class to send emails based on templates. 
 */
@ApplicationScope
@Service
public class EmailService extends ServiceBase {

	@Inject
	private TemplateService templateService;

	@Inject
	private MailSmtpService mailSmtpService;

//	@Inject
//	@MessageBundle
//	private UTF8ResourceBundle msgs;
	@Inject
	private MessageSource msgs;

	@Value("${app.shortName}") 
	private String appShortName;

	@Value("${app.url}") 
	private String appUrl;

	@Value("${app.emailAddr}") 
	private String appEmailAddr;

	/**
	 * Sends registration email where the user must confirm her account.
	 * 
	 * @param user - target user
	 * @param locale - email written in this language 
	 * @throws ServiceException if the email could not be sent 
	 */
	public void sendRegistrationMail(User user, Locale locale) throws ServiceException {
		Properties properties = new Properties();
		properties.put("toEmailAddr", user.getEmailAddr());
		properties.put("userFullName", user.getFullName());
		String confirmationUrl = String.format("%slogin.xhtml?confirmation_token=%s&function=registration", appUrl, user.getToken());
		properties.put("confirmationUrl", confirmationUrl);
		String htmlContent = templateService.generateContent(TemplateId.REGISTRATION_HTML, properties, locale);
		String plainContent = templateService.generateContent(TemplateId.REGISTRATION_PLAIN, properties, locale);
		String subject = msgs.getMessage("general.email.registration", null, locale);
		mailSmtpService.sendHtmlEmail(appEmailAddr, user.getEmailAddr(), subject, htmlContent, plainContent);
	}
	
	/**
	 * Sends email about email modification initiated by the user. It must be confirmed using 
	 * the link inside the email.
	 * 
	 * @param user - target user
	 * @param locale - email written in this language 
	 * @throws ServiceException if the email could not be sent 
	 */
	public void sendEmailChangedMail(User user, Locale locale) throws ServiceException {
		Properties properties = new Properties();
		properties.put("toEmailAddr", user.getEmailNew());
		properties.put("userFullName", user.getFullName());
		String confirmationUrl = String.format("%slogin.xhtml?confirmation_token=%s&function=changeEmail", appUrl, user.getToken());
		properties.put("confirmationUrl", confirmationUrl);
		String htmlContent = templateService.generateContent(TemplateId.CHANGE_EMAIL_HTML, properties, locale);
		String plainContent = templateService.generateContent(TemplateId.CHANGE_EMAIL_PLAIN, properties, locale);
		String subject = msgs.getMessage("general.email.changeEmail", null, locale);
		mailSmtpService.sendHtmlEmail(appEmailAddr, user.getEmailNew(), subject, htmlContent, plainContent);
	}

	/**
	 * Sends email about reset password initiated by the user. It must be confirmed using 
	 * the link inside the email.
	 * 
	 * @param user - target user
	 * @param resetPassword - not encoded new temporary password for user
	 * @param locale - email written in this language 
	 * @throws ServiceException if the email could not be sent 
	 */
	public void sendResetPasswordMail(User user, String resetPassword, Locale locale) throws ServiceException {
		Properties properties = new Properties();
		properties.put("toEmailAddr", user.getEmailAddr());
		properties.put("userFullName", user.getFullName());
		properties.put("loginName", user.getLoginName());
		properties.put("resetPassword", resetPassword);
		String confirmationUrl = String.format("%slogin.xhtml?confirmation_token=%s&function=resetPassword", appUrl, user.getToken());
		properties.put("confirmationUrl", confirmationUrl);
		String htmlContent = templateService.generateContent(TemplateId.RESET_PASSWORD_HTML, properties, locale);
		String plainContent = templateService.generateContent(TemplateId.RESET_PASSWORD_PLAIN, properties, locale);
		String subject = msgs.getMessage("general.email.resetPassword", null, locale);
		mailSmtpService.sendHtmlEmail(appEmailAddr, user.getEmailAddr(), subject, htmlContent, plainContent);
	}
}
