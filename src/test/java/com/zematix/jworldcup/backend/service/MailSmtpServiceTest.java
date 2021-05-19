package com.zematix.jworldcup.backend.service;

import static org.junit.Assert.assertTrue;

import javax.inject.Inject;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.reflect.internal.WhiteboxImpl;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import com.zematix.jworldcup.backend.exception.ServiceException;

/**
 * Contains test functions of {@link MailSmtpService} class.
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles({"development", "test"})
public class MailSmtpServiceTest {

	@Inject
	private MailSmtpService mailSmtpService;

	/**
	 * Test {@link MailSmtpService#sendHtmlEmail(String, String, String, String, String)} method.
	 * Scenario: successfully sends an email
	 * @throws ServiceException 
	 */
	//@Ignore("temporarily: local firewall may block connecting to smtp host")
	@Test
	public void sendHtmlEmail(/*String fromAddr, String toAddr, String subject, String htmlContent, String plainContent*/) throws ServiceException {
		String fromAddr = "jworldcup@zematix.hu";
		String toAddr = "steve.macdonald@trouper.org";
		String subject = "subject";
		String htmlContent = "<p>htmlContent</p>";
		String plainContent = "plainContent"; 

		mailSmtpService.sendHtmlEmail(fromAddr, toAddr, subject, htmlContent, plainContent);
	}

	/**
	 * Test {@link MailSmtpService#sendHtmlEmail(String, String, String, String, String)} method.
	 * Scenario: successfully sends an email
	 * Scenario: when invalid email address is used then ServiceException is thrown.
	 * @throws ServiceException 
	 */
	@Test(expected=ServiceException.class)
	public void sendHtmlEmailInvalidAddress(/*String fromAddr, String toAddr, String subject, String htmlContent, String plainContent*/) throws ServiceException {
		String fromAddr = "jworldcup@zematix.hu";
		String toAddr = "steve.macdonald@@@trouper.org";
		String subject = "subject";
		String htmlContent = "<p>htmlContent</p>";
		String plainContent = "plainContent"; 

		try {
			mailSmtpService.sendHtmlEmail(fromAddr, toAddr, subject, htmlContent, plainContent);
		}
		catch (ServiceException e) {
			assertTrue("There must be a single message in ServiceException named EMAIL_SEND_RECEIPENT_FAILED", 
					e.getMessages().size()==1 && e.getMessages().get(0).getMsgCode().equals("EMAIL_SEND_RECEIPENT_FAILED"));
			throw e;
		}
	}

	/**
	 * Test {@link MailSmtpService#sendHtmlEmail(String, String, String, String, String)} method.
	 * Scenario: when invalid smtp host is used then ServiceException is thrown.
	 * @throws ServiceException 
	 */
	@Test(expected=ServiceException.class)
	public void sendHtmlEmailInvalidHost(/*String fromAddr, String toAddr, String subject, String htmlContent, String plainContent*/) throws ServiceException {
		String fromAddr = "jworldcup@zematix.hu";
		String toAddr = "steve.macdonald@trouper.org";
		String subject = "subject";
		String htmlContent = null;
		String plainContent = null; 

		mailSmtpService.getMailServerProperties().put("mail.smtp.host", "smtp.mailWRONGtrap.io");
		
		try {
			mailSmtpService.sendHtmlEmail(fromAddr, toAddr, subject, htmlContent, plainContent);
		}
		catch (ServiceException e) {
			assertTrue("There must be a single message in ServiceException named EMAIL_SEND_FAILED", 
					e.getMessages().size()==1 && e.getMessages().get(0).getMsgCode().equals("EMAIL_SEND_FAILED"));
			throw e;
		}
	}

	/**
	 * Test {@link MailSmtpService#generateAndSendHtmlEmail(String, String, String, String, String)} private method.
	 * Scenario: successfully sends an email
	 * Disabled because it is tested already in 
	 * {@link MailSmtpService#sendHtmlEmail(String, String, String, String, String)} method. 
	 * @throws Exception 
	 */
	@Ignore("already tested by caller method named sendHtmlEmail")
	@Test
	public void generateAndSendHtmlEmail(/*String fromAddr, String toAddr, String subject, String htmlContent, String plainContent*/) throws Exception {
		String fromAddr = "jworldcup@zematix.hu";
		String toAddr = "steve.macdonald@trouper.org";
		String subject = "subject";
		String htmlContent = "<p>htmlContent</p>";
		String plainContent = "plainContent"; 

		WhiteboxImpl.invokeMethod(mailSmtpService, "generateAndSendHtmlEmail", fromAddr, toAddr, subject, htmlContent, plainContent);
	}
}
