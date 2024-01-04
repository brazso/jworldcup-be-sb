package com.zematix.jworldcup.backend.configuration;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.TimeZone;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ResourceBundleMessageSource;

@Configuration
public class ApplicationConfiguration {

	@Inject
	private Logger logger;

	@PostConstruct
	public void init() {
		TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
		logger.info("Application runs in UTC timezone: {}", LocalDateTime.now());
	}

	@Bean
	ResourceBundleMessageSource messageSource() {
		ResourceBundleMessageSource source = new ResourceBundleMessageSource();
		String basename = "i18n/messages";
		source.setBasenames(basename);
		source.setUseCodeAsDefaultMessage(true);
		source.setDefaultEncoding(StandardCharsets.UTF_8.name());
		logger.info("Application uses resourceBundleMessageSource: {}", basename);
		return source;
	}
}
