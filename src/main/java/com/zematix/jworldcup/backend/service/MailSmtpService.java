package com.zematix.jworldcup.backend.service;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.annotation.PostConstruct;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.context.annotation.ApplicationScope;

import com.zematix.jworldcup.backend.exception.ServiceException;
import com.zematix.jworldcup.backend.model.ParameterizedMessage;

/**
 * Operations around sending emails 
 */
@ApplicationScope
@Service
public class MailSmtpService extends ServiceBase {

	@Value("${mail.smtp.host:#{null}}")
	private String smtpHost;

	@Value("${mail.smtp.port:#{null}}")
	private String smtpPort;

	@Value("${mail.smtp.auth:#{null}}") 
	private String smtpAuth;

	@Value("${mail.smtp.starttls.enable:#{null}}") 
	private String smtpStartTlsEnable;

	@Value("${mail.smtp.connectiontimeout:#{null}}") 
	private String smtpConnectionTimeout;

	@Value("${mail.smtp.timeout:#{null}}")
	private String smtpTimeout;

	@Value("${mail.smtp.user:#{null}}")
	private String smtpUser;

	@Value("${mail.smtp.pass:#{null}}")
	private String smtpPassword;

	@Value("${mail.smtp.socketFactory.class:#{null}}")
	private String smtpSocketFactoryClass;
	
	@Value("${mail.smtp.socketFactory.fallback:#{null}}")
	private String smtpSocketFactoryFallback;
	
	@Value("${mail.smtp.socketFactory.port:#{null}}")
	private String smtpSocketFactoryPort;
	
	private final Properties mailServerProperties = System.getProperties();
	
	/**
	 * @return the mailServerProperties
	 */
	public Properties getMailServerProperties() {
		return mailServerProperties;
	}

	@PostConstruct
	void init() {
		// setup mail SMTP server properties
		if (smtpHost != null) {
			mailServerProperties.put("mail.smtp.host", smtpHost);
		}
		if (smtpPort != null) {
			mailServerProperties.put("mail.smtp.port", smtpPort);
		}
		if (smtpAuth != null) {
			mailServerProperties.put("mail.smtp.auth", smtpAuth);
		}
		if (smtpStartTlsEnable != null) {
			mailServerProperties.put("mail.smtp.starttls.enable", "true");
		}
		if (smtpConnectionTimeout != null) {
			mailServerProperties.put("mail.smtp.connectiontimeout", smtpConnectionTimeout);
		}
		if (smtpTimeout != null) {
			mailServerProperties.put("mail.smtp.timeout", smtpTimeout);
		}
		if (smtpSocketFactoryClass != null) {
			mailServerProperties.put("mail.smtp.socketFactory.class", smtpSocketFactoryClass);
		}
		if (smtpSocketFactoryFallback != null) {
			mailServerProperties.put("mail.smtp.socketFactory.fallback", smtpSocketFactoryFallback);
		}
		if (smtpSocketFactoryPort != null) {
			mailServerProperties.put("mail.smtp.socketFactory.port", smtpSocketFactoryPort);
		}
	}

	/**
	 * Sends an email with multiple content, both html and plain text.
	 * 
	 * @param fromAddr - sender email address
	 * @param toAdd - recipient email address
	 * @param subject - subject of the email
	 * @param htmlContent - html content of the email
	 * @param plaincontent - plain text content of the email
	 * @throws ServiceException if the email could not be sent 
	 */
	public void sendHtmlEmail(String fromAddr, String toAddr, String subject, String htmlContent, String plainContent) throws ServiceException {
		List<ParameterizedMessage> errMsgs = new ArrayList<>();
		
		try {
			generateAndSendHtmlEmail(fromAddr, toAddr, subject, htmlContent, plainContent);
		} catch (AddressException e) {
			logger.error(e.getMessage(), e);
			errMsgs.add(ParameterizedMessage.create("EMAIL_SEND_RECEIPENT_FAILED", toAddr));
		} catch (MessagingException e) {
			logger.error(e.getMessage(), e);
			errMsgs.add(ParameterizedMessage.create("EMAIL_SEND_FAILED", toAddr));
		}
		
		if (!errMsgs.isEmpty()) {
			throw new ServiceException(errMsgs);
		}
	}
	
	/**
	 * Sends an email with multiple content, both html and plain text.
	 * 
	 * @param fromAddr - sender email address
	 * @param toAdd - recipient email address
	 * @param subject - subject of the email
	 * @param htmlContent - html content of the email
	 * @param plaincontent - plain text content of the email
	 * @throws AddressException if any of the email address is invalid
	 * @throws MessagingException if the constructed email message is wrong 
	 */
	private void generateAndSendHtmlEmail(String fromAddr, String toAddr, String subject, String htmlContent, String plainContent) throws AddressException, MessagingException {

//		mail.smtp.host=smtp.mailtrap.io
//		mail.smtp.port=2525
//		mail.smtp.user=...
//		mail.smtp.pass=...

//		mail.smtp.host=smtp.gmail.com
//		mail.smtp.port=587
//		mail.smtp.auth=true
//		mail.smtp.starttls.enable=true
//		mail.smtp.user=...
//		mail.smtp.pass=...
		
		// get Mail Session
		Session getMailSession = Session.getDefaultInstance(mailServerProperties, null);
		MimeMessage generateMailMessage = new MimeMessage(getMailSession);
		generateMailMessage.setFrom(new InternetAddress(fromAddr));
		generateMailMessage.addRecipient(Message.RecipientType.TO, new InternetAddress(toAddr));
		//generateMailMessage.addRecipient(Message.RecipientType.CC, new InternetAddress(ccAddr));
		generateMailMessage.setSubject(subject); // Greetings from JWorldcup...
		
		// Unformatted text version
		final MimeBodyPart textPart = new MimeBodyPart();
		textPart.setContent(plainContent, "text/plain; charset=" + StandardCharsets.UTF_8.name());
		// HTML version
		final MimeBodyPart htmlPart = new MimeBodyPart();
		htmlPart.setContent(htmlContent, "text/html; charset=" + StandardCharsets.UTF_8.name());
		// Create the Multipart. Add BodyParts to it.
		final Multipart mp = new MimeMultipart("alternative");
		mp.addBodyPart(textPart);
		mp.addBodyPart(htmlPart);
		// Set Multipart as the message's content
		generateMailMessage.setContent(mp);

		// get session and send mail
		Transport transport = getMailSession.getTransport("smtp");

		// Enter your correct UserID and Password
		// if you have 2FA enabled (gmail) then provide App Specific Password
		transport.connect(smtpUser, smtpPassword);
		transport.sendMessage(generateMailMessage, generateMailMessage.getAllRecipients());
		transport.close();
	}
}
