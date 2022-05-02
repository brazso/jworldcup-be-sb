package com.zematix.jworldcup.backend.configuration; 

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Role;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.transaction.annotation.AnnotationTransactionAttributeSource;
import org.springframework.transaction.annotation.SpringTransactionAnnotationParser;
import org.springframework.transaction.interceptor.RollbackRuleAttribute;
import org.springframework.transaction.interceptor.RuleBasedTransactionAttribute;
import org.springframework.transaction.interceptor.TransactionAttribute;
import org.springframework.transaction.interceptor.TransactionAttributeSource;

import com.zematix.jworldcup.backend.exception.ServiceException;

@Configuration
public class TransactionManagementConfig {
	/**
	 * ServiceException.class is added as "rollbackFor" attribute to all Transactional annotations,
	 * because checked exceptions do not throw database rollback automatically in spring.
	 */
	@Bean
	@Primary
	@Role(BeanDefinition.ROLE_INFRASTRUCTURE)
	public TransactionAttributeSource transactionAttributeSourceWithDefaultRollBackForAllExceptions() {

		return new AnnotationTransactionAttributeSource(new SpringTransactionAnnotationParser() {

			@Override
			protected TransactionAttribute parseTransactionAnnotation(AnnotationAttributes attributes) {
				RuleBasedTransactionAttribute rbta = (RuleBasedTransactionAttribute) super.parseTransactionAnnotation(
						attributes);
				List<RollbackRuleAttribute> rules = new ArrayList<>(rbta.getRollbackRules());
				rules.add(new RollbackRuleAttribute(ServiceException.class));
				rbta.setRollbackRules(rules);
				return rbta;
			}

		});
	}
}
