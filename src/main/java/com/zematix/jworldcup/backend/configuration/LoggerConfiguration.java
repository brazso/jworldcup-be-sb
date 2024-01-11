package com.zematix.jworldcup.backend.configuration;

import static java.util.Optional.ofNullable;

import java.lang.reflect.Field;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InjectionPoint;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.core.MethodParameter;

/**
 * Inject logger implementation
 * @see <a href="https://medium.com/simars/inject-loggers-in-spring-java-or-kotlin-87162d02d068">Inject loggers in Spring</a>
 */
@Configuration
public class LoggerConfiguration {

	@Bean
	@Scope("prototype")
	Logger logger(final InjectionPoint ip) {
		return LoggerFactory.getLogger(ofNullable(ip.getMethodParameter())
				.<Class>map(MethodParameter::getContainingClass).orElseGet(() -> ofNullable(ip.getField())
						.map(Field::getDeclaringClass).orElseThrow(IllegalArgumentException::new)));
	}
}