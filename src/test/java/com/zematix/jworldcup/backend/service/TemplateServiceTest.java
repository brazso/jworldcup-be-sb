package com.zematix.jworldcup.backend.service;

import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Properties;

import javax.inject.Inject;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import com.zematix.jworldcup.backend.emun.TemplateId;
import com.zematix.jworldcup.backend.exception.ServiceException;

/**
 * Contains test functions of {@link TemplateService} class.
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles({"development", "test"})
public class TemplateServiceTest {
	
	@Inject
	private TemplateService templateService;
	
	@Value("${app.url}") 
	private String appUrl;

//	@MockBean
//	private ApplicationBean applicationBean; // MessageProducer may use its locale property
	
	/**
	 * Test {@link TemplateService#addDefaultProperties(Properties)} method.
	 * Scenario: successfully adds extra properties to the input {@code properties}
	 *           parameter
	 */
	@Test
	public /*private*/ void addDefaultProperties(/*Properties properties*/) {
		Properties properties = new Properties();
		templateService.addDefaultProperties(properties);
		
		List<String> expectedKeys = Arrays.asList("appShortName", "appUrl", "appEmailAddr");
		
		assertTrue("Property object should contain all of the expected values", 
				expectedKeys.stream().allMatch(e -> properties.containsKey(e)));
	}
	
	/**
	 * Test {@link TemplateService#generateContent(TemplateId, Properties, Locale)} method.
	 * Scenario: throws {@link NullPointerException} because input {@code templateId} parameter 
	 *           is {@code null}
	 */
	@Test(expected=NullPointerException.class)
	public void /*String*/ generateContent_NullTemplateId(/*TemplateId templateId, Properties properties, Locale locale*/) throws ServiceException {
		TemplateId templateId = null;
		Properties properties = new Properties();
		Locale locale = new Locale("en");
		
		/*String content =*/ templateService.generateContent(templateId, properties, locale);
	}

	/**
	 * Test {@link TemplateService#generateContent(TemplateId, Properties, Locale)} method.
	 * Scenario: throws {@link NullPointerException} because input {@code properties} parameter 
	 *           is {@code null}
	 */
	@Test(expected=NullPointerException.class)
	public void /*String*/ generateContent_NullProperties(/*TemplateId templateId, Properties properties, Locale locale*/) throws ServiceException {
		TemplateId templateId = TemplateId.REGISTRATION_HTML;
		Properties properties = null;
		Locale locale = new Locale("en");
		
		/*String content =*/ templateService.generateContent(templateId, properties, locale);
	}

	/**
	 * Test {@link TemplateService#generateContent(TemplateId, Properties, Locale)} method.
	 * Scenario: throws {@link NullPointerException} because input {@code locale} parameter 
	 *           is {@code null}
	 */
	@Test(expected=NullPointerException.class)
	public void /*String*/ generateContent_NullLocale(/*TemplateId templateId, Properties properties, Locale locale*/) throws ServiceException {
		TemplateId templateId = TemplateId.REGISTRATION_HTML;
		Properties properties = new Properties();
		Locale locale = null;
		
		/*String content =*/ templateService.generateContent(templateId, properties, locale);
	}

	/**
	 * Test {@link TemplateService#generateContent(TemplateId, Properties, Locale)} method.
	 * Scenario: throws {@link IllegalArgumentException} because input {@code templateId} parameter 
	 *           does not belong to EMAIL
	 */
	@Test(expected=IllegalArgumentException.class)
	public void /*String*/ generateContent_InvalidTemplateId(/*TemplateId templateId, Properties properties, Locale locale*/) throws ServiceException {
		TemplateId templateId = TemplateId.USER_CERTIFICATE_PDF;
		Properties properties = new Properties();
		Locale locale = new Locale("en");
		
		/*String content =*/ templateService.generateContent(templateId, properties, locale);
	}

	/**
	 * Test {@link TemplateService#generateContent(TemplateId, Properties, Locale)} method.
	 * Scenario: throws {@link ServiceException} because input {@code properties} parameter 
	 *           does not contain the necessary template parameters
	 */
	@Test(expected=ServiceException.class)
	public void /*String*/ generateContent_EmptyProperties(/*TemplateId templateId, Properties properties, Locale locale*/) throws ServiceException {
		TemplateId templateId = TemplateId.REGISTRATION_HTML;
		Properties properties = new Properties();
		Locale locale = new Locale("en");
		
		/*String content =*/ templateService.generateContent(templateId, properties, locale);
	}

	/**
	 * Test {@link TemplateService#generateContent(TemplateId, Properties, Locale)} method.
	 * Scenario: successfully generates content
	 */
	@Test
	public void /*String*/ generateContent(/*TemplateId templateId, Properties properties, Locale locale*/) throws ServiceException {
		TemplateId templateId = TemplateId.REGISTRATION_HTML;
		Properties properties = new Properties();
		Locale locale = new Locale("en");

		properties.put("toEmailAddr", "dummy@dummy.dy");
		String userFullName = "Dummy";
		properties.put("userFullName", userFullName);
		String confirmationUrl = String.format("%slogin.xhtml?confirmation_token=%s&function=registration", appUrl, "dummyToken");
		properties.put("confirmationUrl", confirmationUrl);
		
		String content = templateService.generateContent(templateId, properties, locale);
		assertTrue(String.format("Generated content must contain word \"%s\".", userFullName), content != null && content.contains(userFullName));
	}

	/**
	 * Test {@link TemplateService#generatePDFContent(TemplateId, Properties, Locale)} method.
	 * Scenario: throws {@link NullPointerException} because input {@code templateId}
	 *           parameter is {@code null}
	 */
	@Test(expected=NullPointerException.class)
	public void /*ByteArrayOutputStream*/ generatePDFContent_NullTemplateId(/*TemplateId templateId, Properties properties, Locale locale*/) throws ServiceException {
		TemplateId templateId = null;
		Properties properties = new Properties();
		Locale locale = new Locale("en");
		
		/*String content =*/ templateService.generatePDFContent(templateId, properties, locale);
	}

	/**
	 * Test {@link TemplateService#generatePDFContent(TemplateId, Properties, Locale)} method.
	 * Scenario: throws {@link NullPointerException} because input {@code properties} 
	 *           parameter is {@code null}
	 */
	@Test(expected=NullPointerException.class)
	public void /*ByteArrayOutputStream*/ generatePDFContent_NullProperties(/*TemplateId templateId, Properties properties, Locale locale*/) throws ServiceException {
		TemplateId templateId = TemplateId.USER_CERTIFICATE_PDF;
		Properties properties = null;
		Locale locale = new Locale("en");
		
		/*String content =*/ templateService.generatePDFContent(templateId, properties, locale);
	}

	/**
	 * Test {@link TemplateService#generatePDFContent(TemplateId, Properties, Locale)} method.
	 * Scenario: throws {@link NullPointerException} because input {@code locale} parameter 
	 *           is {@code null}
	 */
	@Test(expected=NullPointerException.class)
	public void /*ByteArrayOutputStream*/ generatePDFContent_NullLocale(/*TemplateId templateId, Properties properties, Locale locale*/) throws ServiceException {
		TemplateId templateId = TemplateId.USER_CERTIFICATE_PDF;
		Properties properties = new Properties();
		Locale locale = null;
		
		/*String content =*/ templateService.generatePDFContent(templateId, properties, locale);
	}

	/**
	 * Test {@link TemplateService#generatePDFContent(TemplateId, Properties, Locale)} method.
	 * Scenario: throws {@link IllegalArgumentException} because input {@code templateId}
	 *           parameter does not belong to PDF
	 */
	@Test(expected=IllegalArgumentException.class)
	public void /*ByteArrayOutputStream*/ generatePDFContent_InvalidTemplateId(/*TemplateId templateId, Properties properties, Locale locale*/) throws ServiceException {
		TemplateId templateId = TemplateId.REGISTRATION_HTML;
		Properties properties = new Properties();
		Locale locale = new Locale("en");
		
		/*String content =*/ templateService.generatePDFContent(templateId, properties, locale);
	}

	/**
	 * Test {@link TemplateService#generatePDFContent(TemplateId, Properties, Locale)} method.
	 * Scenario: throws {@link ServiceException} because input {@code properties} parameter 
	 *           does not contain the necessary template parameters
	 */
	@Test(expected=ServiceException.class)
	public void /*String*/ generatePDFContent_EmptyProperties(/*TemplateId templateId, Properties properties, Locale locale*/) throws ServiceException {
		TemplateId templateId = TemplateId.USER_CERTIFICATE_PDF;
		Properties properties = new Properties();
		Locale locale = new Locale("en");
		
		/*String content =*/ templateService.generatePDFContent(templateId, properties, locale);
	}

	/**
	 * Test {@link TemplateService#generatePDFContent(TemplateId, Properties, Locale)} method.
	 * Scenario: successfully generates PDF content.
	 */
	@Test
	public void /*ByteArrayOutputStream*/ generatePDFContent(/*TemplateId templateId, Properties properties, Locale locale*/) throws ServiceException {
		TemplateId templateId = TemplateId.USER_CERTIFICATE_PDF;
		Properties properties = new Properties();
		Locale locale = new Locale("en");
		
		// template parameters  
		properties.put("userLoginName", "dummy");
		properties.put("userFullName", "Dumster Dummy");
		properties.put("eventShortDescWithYear", "WC2018");
		properties.put("userGroupName", "Everybody");
		properties.put("userScore", 99);
		properties.put("userGroupPosition", 1);
		
		ByteArrayOutputStream content = templateService.generatePDFContent(templateId, properties, locale);
		assertTrue("Generated PDF content should not be empty.", content!=null && content.size() > 0);
	}
}
