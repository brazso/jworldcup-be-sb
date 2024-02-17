package com.zematix.jworldcup.backend.configuration; 

import java.lang.reflect.AnnotatedElement;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Role;
import org.springframework.transaction.annotation.AnnotationTransactionAttributeSource;
import org.springframework.transaction.interceptor.DelegatingTransactionAttribute;
import org.springframework.transaction.interceptor.TransactionAttribute;
import org.springframework.transaction.interceptor.TransactionAttributeSource;

import com.zematix.jworldcup.backend.emun.ParameterizedMessageType;
import com.zematix.jworldcup.backend.exception.ServiceException;

@Configuration
public class TransactionManagementConfig {
	/**
	 * ServiceException must invoke rollback only if it is a real error. 
	 */
	@Bean
	@Primary
    @Role(BeanDefinition.ROLE_INFRASTRUCTURE)
    TransactionAttributeSource transactionAttributeSourceWithDefaultRollBackForAllExceptions() {
        return new AnnotationTransactionAttributeSource() {
			private static final long serialVersionUID = 1L;

			@Override
            protected TransactionAttribute determineTransactionAttribute(AnnotatedElement element) {
                TransactionAttribute ta = super.determineTransactionAttribute(element);
                if (ta == null) {
                    return null;
                } else {
                    return new DelegatingTransactionAttribute(ta) {
                        private static final long serialVersionUID = 1L;

						@Override
                        public boolean rollbackOn(Throwable ex) {
                            return ex instanceof ServiceException serviceException ? serviceException.getOverallType() == ParameterizedMessageType.ERROR : super.rollbackOn(ex);
                        }
                    };
                }
            }
        };
    }
}
