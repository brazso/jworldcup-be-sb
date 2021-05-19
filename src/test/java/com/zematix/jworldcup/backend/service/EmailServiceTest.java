package com.zematix.jworldcup.backend.service;

import java.util.Locale;
import java.util.Properties;

import javax.inject.Inject;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.MessageSource;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import com.zematix.jworldcup.backend.emun.TemplateId;
import com.zematix.jworldcup.backend.entity.User;
import com.zematix.jworldcup.backend.exception.ServiceException;

/**
 * Contains test functions of {@link EmailService} class.
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles({"development", "test"})
public class EmailServiceTest {
	
	@Inject
	private EmailService emailService;
	
	@Value("${app.url}") 
	private String appUrl;

	@Inject
	private MessageSource msgs;

	@MockBean
	private TemplateService templateService;
	
	@MockBean
	private MailSmtpService mailSmtpService;

	/**
	 * Test {@link TemplateService#sendRegistrationMail(User, Locale)} method.
	 * Scenario: successfully sends an email
	 */
	@Test
	public void sendRegistrationMail(/*User user, Locale locale*/) throws ServiceException {
		User user = new User();
		Locale locale = new Locale("en");
		
		user.setEmailAddr("brazso@zematix.hu");
		user.setFullName("Zsolt Branyiczky");

		String fromAddr = "jworldcup@zematix.hu";
		String toAddr = user.getEmailAddr();
		String subject = msgs.getMessage("general.email.registration", null, locale);
		String htmlContent = "";
		String plainContent = "";
		
		Properties properties = new Properties();
		properties.put("toEmailAddr", user.getEmailAddr());
		properties.put("userFullName", user.getFullName());
		String confirmationUrl = String.format("%slogin.xhtml?confirmation_token=%s&function=registration", appUrl, user.getToken());
		properties.put("confirmationUrl", confirmationUrl);

		Mockito.when(templateService.generateContent(TemplateId.REGISTRATION_HTML, properties, locale)).thenReturn(htmlContent);
		Mockito.when(templateService.generateContent(TemplateId.REGISTRATION_PLAIN, properties, locale)).thenReturn(plainContent);
		Mockito.doNothing().when(mailSmtpService).sendHtmlEmail(fromAddr, toAddr, subject, htmlContent, plainContent);
		
		emailService.sendRegistrationMail(user, locale);
		
		Mockito.verify(mailSmtpService).sendHtmlEmail(fromAddr, toAddr, subject, htmlContent, plainContent);
	}

	/**
	 * Test {@link TemplateService#sendEmailChangedMail(User, Locale)} method.
	 * Scenario: successfully sends an email
	 */
	@Test
	public void sendEmailChangedMail(/*User user, Locale locale*/) throws ServiceException {
		User user = new User();
		Locale locale = new Locale("hu");
		
		user.setEmailNew("brazso@zematix.hu");
		user.setFullName("Zsolt Branyiczky");

		String fromAddr = "jworldcup@zematix.hu";
		String toAddr = user.getEmailNew();
		String subject = msgs.getMessage("general.email.changeEmail", null, locale);
		String htmlContent = "";
		String plainContent = "";
		
		Properties properties = new Properties();
		properties.put("toEmailAddr", user.getEmailNew());
		properties.put("userFullName", user.getFullName());
		String confirmationUrl = String.format("%slogin.xhtml?confirmation_token=%s&function=changeEmail", appUrl, user.getToken());
		properties.put("confirmationUrl", confirmationUrl);

		Mockito.when(templateService.generateContent(TemplateId.CHANGE_EMAIL_HTML, properties, locale)).thenReturn(htmlContent);
		Mockito.when(templateService.generateContent(TemplateId.CHANGE_EMAIL_PLAIN, properties, locale)).thenReturn(plainContent);
		Mockito.doNothing().when(mailSmtpService).sendHtmlEmail(fromAddr, toAddr, subject, htmlContent, plainContent);
		
		emailService.sendEmailChangedMail(user, locale);
		
		Mockito.verify(mailSmtpService).sendHtmlEmail(fromAddr, toAddr, subject, htmlContent, plainContent);
	}

	/**
	 * Test {@link TemplateService#sendResetPasswordMail(User, String, Locale)} method.
	 * Scenario: successfully sends an email
	 */
	@Test
	public void sendResetPasswordMail(/*User user, String resetPassword, Locale locale*/) throws ServiceException {
		User user = new User();
		String resetPassword = "hello";
		Locale locale = new Locale("en");
		
		user.setEmailAddr("brazso@zematix.hu");
		user.setFullName("Zsolt Branyiczky");
		user.setLoginName("brazso");

		String fromAddr = "jworldcup@zematix.hu";
		String toAddr = user.getEmailAddr();
		String subject = msgs.getMessage("general.email.resetPassword", null, locale);
		String htmlContent = "";
		String plainContent = "";
		
		Properties properties = new Properties();
		properties.put("toEmailAddr", user.getEmailAddr());
		properties.put("userFullName", user.getFullName());
		properties.put("loginName", user.getLoginName());
		properties.put("resetPassword", resetPassword);
		String confirmationUrl = String.format("%slogin.xhtml?confirmation_token=%s&function=resetPassword", appUrl, user.getToken());
		properties.put("confirmationUrl", confirmationUrl);

		Mockito.when(templateService.generateContent(TemplateId.RESET_PASSWORD_HTML, properties, locale)).thenReturn(htmlContent);
		Mockito.when(templateService.generateContent(TemplateId.RESET_PASSWORD_PLAIN, properties, locale)).thenReturn(plainContent);
		Mockito.doNothing().when(mailSmtpService).sendHtmlEmail(fromAddr, toAddr, subject, htmlContent, plainContent);
		
		emailService.sendResetPasswordMail(user, resetPassword, locale);
		
		Mockito.verify(mailSmtpService).sendHtmlEmail(fromAddr, toAddr, subject, htmlContent, plainContent);
	}
}
