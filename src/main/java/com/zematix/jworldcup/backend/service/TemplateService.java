package com.zematix.jworldcup.backend.service;

import static com.google.common.base.Preconditions.checkArgument;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Properties;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.context.annotation.ApplicationScope;

import com.google.common.annotations.VisibleForTesting;

import fr.opensagres.xdocreport.converter.ConverterTypeTo;
import fr.opensagres.xdocreport.converter.Options;
import fr.opensagres.xdocreport.core.XDocReportException;
import fr.opensagres.xdocreport.core.document.DocumentKind;
import fr.opensagres.xdocreport.document.IXDocReport;
import fr.opensagres.xdocreport.document.registry.XDocReportRegistry;
import fr.opensagres.xdocreport.template.IContext;
import fr.opensagres.xdocreport.template.TemplateEngineKind;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.TemplateExceptionHandler;

/**
 * Helper class to generate contents used by templates. Moreover it contains methods
 * to initiate the sending of the generated contents as email messages.
 * <P>
 * <b>Note:</b> Templates are based on Apache FreeMarker template engine. 
 */
@ApplicationScope
@Service
public class TemplateService extends ServiceBase {

	@Value("${app.shortName}")
	private String appShortName;

	@Value("${app.url}")
	private String appUrl;


	@Value("${app.emailAddr}") 
	private String appEmailAddr;

	private final Configuration cfg = new Configuration(Configuration.VERSION_2_3_23);
	private static final String TEMPLATE_PACKAGE_PATH = "/templates/";
	
	@PostConstruct
	void init() {
		// configure FreeMarker template engine
		cfg.setClassForTemplateLoading(this.getClass(), TEMPLATE_PACKAGE_PATH);
		cfg.setDefaultEncoding(StandardCharsets.UTF_8.name());
		cfg.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
	}

	/**
	 * Add missing default application properties to the input property.
	 * It does modify the input properties parameter.
	 * 
	 * @param properties
	 */
	@VisibleForTesting
	/*private*/ void addDefaultProperties(Properties properties) {
		if (!properties.containsKey("app.shortName")) {
			properties.put("appShortName", appShortName);
		}
		if (!properties.containsKey("app.url")) {
			properties.put("appUrl", appUrl);
		}
		if (!properties.containsKey("app.emailAddr")) {
			properties.put("appEmailAddr", appEmailAddr);
		}
	}
	
	/**
	 * Generates template content using a template, its parameters and a locale.  
	 * 
	 * @param templateId - template to be used
	 * @param properties - template parameters
	 * @param locale - output locale
	 * @return String instance containing the generated template
	 * @throws ServiceException if the template could not be generated
	 */
	public String generateContent(TemplateId templateId, Properties properties, Locale locale) throws ServiceException {
		List<ParametrizedMessage> errMsgs = new ArrayList<>();
		
		checkArgument(templateId != null, "Argument \"templateId\" cannot be null.");
		checkArgument(properties != null, "Argument \"properties\" cannot be null.");
		checkArgument(locale != null, "Argument \"locale\" cannot be null.");
		checkArgument(templateId.templateType == TemplateType.EMAIL, "TemplateType from argument \"templateId\" must be EMAIL.");
		
		addDefaultProperties(properties);
		
		// example templateName: email/registration_hu.ftl 
		String templateName = String.format("%s/%s_%s.%s", templateId.templateType.fileName, 
				templateId.fileName, locale.getLanguage(), templateId.fileExtension);
		String content = "";
		try {
			Template template = cfg.getTemplate(templateName);
			StringWriter stringWriter = new StringWriter();
			template.process(properties, stringWriter);
			content = stringWriter.toString();
		} catch (IOException | TemplateException e) {
			errMsgs.add(ParametrizedMessage.create("TEMPLATE_GENERATION_FAILED", templateId.toString()));
			throw new ServiceException(errMsgs);
		}

		return content;
	}

	/**
	 * Generates PDF content from ODT using a template, its parameters and a locale.  
	 * 
	 * @param templateId - template to be used
	 * @param properties - template parameters
	 * @param locale - output locale
	 * @return OutputStream object containing generated PDF in a byte array
	 * @throws ServiceException if the template could not be generated
	 */
	public ByteArrayOutputStream generatePDFContent(TemplateId templateId, Properties properties, Locale locale) throws ServiceException {
		List<ParametrizedMessage> errMsgs = new ArrayList<>();
		
		checkArgument(templateId != null, "Argument \"templateId\" cannot be null.");
		checkArgument(properties != null, "Argument \"properties\" cannot be null.");
		checkArgument(locale != null, "Argument \"locale\" cannot be null.");
		checkArgument(templateId.templateType == TemplateType.PDF, "TemplateType from argument \"templateId\" must be PDF.");
		
		addDefaultProperties(properties);
		
		// example templateName: pdf/user_certificate_hu.odt 
		String templateName = String.format("%s/%s_%s.%s", templateId.templateType.fileName, 
				templateId.fileName, locale.getLanguage(), templateId.fileExtension);

		ByteArrayOutputStream pdfOutputStream = new ByteArrayOutputStream();

		try {
			// use getClassLoader() only if test should read from /src/test/resources 
			InputStream templateOdtInputStream = getClass()/*.getClassLoader()*/
					.getResourceAsStream(TEMPLATE_PACKAGE_PATH + templateName);
			
			// Load ODT file and set the template engine to Freemarker
			IXDocReport xdocGenerator = XDocReportRegistry.getRegistry().loadReport(templateOdtInputStream,
					TemplateEngineKind.Freemarker);
			IContext context = xdocGenerator.createContext();
	
			// Configuring the XDOCReport Context by registering the java model 
			// stored in a Properties object.
			// Name "uc" is used as key in the ODT freemarker template to reference the java
			// object stored in properties.
			context.put("uc", properties);
			// Set format converter from ODT to PDF
			Options options = Options.getFrom(DocumentKind.ODT).to(ConverterTypeTo.PDF);
	
			// Merge Java model with the ODT and convert it to PDF
			xdocGenerator.convert(context, options, pdfOutputStream);
			templateOdtInputStream.close();
			pdfOutputStream.close();
		}
		catch (IOException | XDocReportException e) {
			errMsgs.add(ParametrizedMessage.create("TEMPLATE_GENERATION_FAILED", templateId.toString()));
			throw new ServiceException(errMsgs);
		}
		
		return pdfOutputStream;
	}
}
