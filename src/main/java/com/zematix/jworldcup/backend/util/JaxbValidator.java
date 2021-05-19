package com.zematix.jworldcup.backend.util;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.groups.Default;
import javax.xml.bind.annotation.XmlElement;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.zematix.jworldcup.backend.exception.JaxbValidationException; 

/**
 * Validation of JAXB annotations referred to the required fields. Schema (xsd) file is not required.
 * Only @JsonProperty(required = true) és @XmlElement(required = true) are about to check on the 
 * field of the objects to be validated.
 */
@Service
public class JaxbValidator {

    private static final Logger logger = LoggerFactory.getLogger(JaxbValidator.class);
    private static final String DTO_PACKAGE_PREFIX = "com.zematix.jworldcup.backend.dto";
    
    private static final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    /**
     * Validates the required fields on the given{@code target} objects. If {@code target}
     * is an instance of {@link java.util.List}, then it checks its elements as well. With the given
     * {@code isDeep} parameter additional validation can be also forced, when it gets {@code true} value. 
     * In this case when the analysed field is not {@code null}, then it validates that object as well. 
     * If the latter is an instance of {@link java.util.List}, then those elements are validated as well.
     * Avoiding the circular process, in the given {@code processedClassNames} list there are the so far
     * process field types on the different level depth. With the given {@code constraintGroups}
     * parameter the vlaidation groups can be filtered. In case of {@code null} value the Default group is 
     * used. 
     * 
     * @param <T> type of the object to be validated
     * @param target object to be validated
     * @param isDeep deeper, cascade check runs
     * @param contsraintGroups validation runs only on these groups
     * @param processedClassNames so far processed filed types on different depth levels 
     */
    private <T> void validateRequired(T target, boolean isDeep, Class<?>[] contsraintGroups, List<String> processedClassNames) {
        StringBuilder errors = new StringBuilder();
        
        if (contsraintGroups == null) {
            contsraintGroups = new Class<?>[] {Default.class};
        }
        Set<ConstraintViolation<T>> violations = validator.validate(target, contsraintGroups);
        violations.stream().forEach(e -> {
            if (errors.length() != 0) {
                errors.append(", ");
            }
            errors.append("{").append(String.format("%s.%s %s", target.getClass().getSimpleName(), e.getPropertyPath(), e.getMessage())).append("}");
        });
        
        if (target instanceof List) {
            List<String> processedExClassNames = new ArrayList<>(processedClassNames);
            processedExClassNames.add(target.getClass().getName());
            processList(target, isDeep, contsraintGroups, errors, processedExClassNames);
        } else {
            Field[] fields = target.getClass().getDeclaredFields();
            for (Field field : fields) {
                List<String> processedExClassNames = new ArrayList<>(processedClassNames);
                processedExClassNames.add(target.getClass().getName());
                if (!processedExClassNames.contains(field.getType().getName())) {
                    processField(target, isDeep, contsraintGroups, errors, processedExClassNames, field);
                }
            }
        }
        if (errors.length() != 0) {
            throw new JaxbValidationException(errors.toString());
        }
    }

    /**
     * Process of the field belongs to the given object using reflection.
     * Precodition that security manager is disabled or default security manager must be used.
     * 
     * @param <T> type of the object to be validated
     * @param isDeep deeper, cascade check runs
     * @param contsraintGroups validation runs only on these groups
     * @param target object to be validated
     * @param errors storage of errors
     * @param processedClassNames so far processed filed types on different depth levels
     * @param field to be validated
     */
    private <T> void processField(T target, boolean isDeep, Class<?>[] contsraintGroups, StringBuilder errors, List<String> processedClassNames, Field field) {
        try {
            field.setAccessible(true);
            //logger.fine(String.format("processField: %s %s", target.getClass().getName(), field.getName()));

            checkRequiredNotNullField(target, errors, field);

            if (isDeep && field.get(target) != null) {
                if (field.get(target) instanceof List) {
                    processList(field.get(target), isDeep, contsraintGroups, errors, processedClassNames);
                } else if (!field.getType().isPrimitive() && field.get(target).getClass().getPackageName().startsWith(DTO_PACKAGE_PREFIX)) {
                    process(field.get(target), isDeep, contsraintGroups, errors, processedClassNames);
                }
            }
        } catch (
                 IllegalArgumentException
                 | IllegalAccessException e) {
            logger.error(field.getName(), e);
        }
    }

    /**
     * Helper method of {@link JaxbValidatorImpl#processField(Object, boolean, StringBuilder, List, Field)} 
     * 
     * @param <T> type of the object to be validated
     * @param target object to be validated
     * @param errors storage of errors
     * @param field to be validated
     * @throws IllegalArgumentException
     * @throws IllegalAccessException
     */
    private <T> void checkRequiredNotNullField(T target, StringBuilder errors, Field field) throws IllegalAccessException {
        XmlElement xmlAnnotation = field.getAnnotation(XmlElement.class);
        JsonProperty jsonAnnotation = field.getAnnotation(JsonProperty.class);
        if (((xmlAnnotation != null && xmlAnnotation.required()) ||
                (jsonAnnotation != null && jsonAnnotation.required())) && field.get(target) == null) {
            if (errors.length() != 0) {
                errors.append(", ");
            }
            String message = String.format("%s: required field '%s' is null", target.getClass().getSimpleName(), field.getName());
            logger.warn(message);
            errors.append(message);
        }
    }

    /**
     * Process of normal (not from {@link java.util.List} instance derived) object
     * 
     * @param <T> type of the object to be validated
     * @param target field to be validated
     * @param isDeep deeper, cascade check runs
     * @param contsraintGroups validation runs only on these groups
     * @param errors storage of errors
     * @param processedClassNames so far processed filed types on different depth levels
     */
    private <T> void process(T target, boolean isDeep, Class<?>[] contsraintGroups, StringBuilder errors, List<String> processedClassNames) {
        try {
            validateRequired(target, isDeep, contsraintGroups, processedClassNames);
        } catch (JaxbValidationException ex) {
            if (errors.length() != 0) {
                errors.append(", ");
            }
            errors.append("{").append(ex.getMessage()).append("}");
        }

    }
    /**
     * Process of list (from {@link java.util.List} derived) object
     * 
     * @param <T> type of the object to be validated
     * @param target field to be validated
     * @param isDeep deeper, cascade check runs
     * @param contsraintGroups validation runs only on these groups
     * @param errors storage of errors
     * @param processedClassNames so far processed filed types on different depth levels
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    private <T> void processList(T target, boolean isDeep, Class<?>[] contsraintGroups, StringBuilder errors, List<String> processedClassNames) {
        ((List) target).stream().forEach(e -> {
            try {
                validateRequired(e, isDeep, contsraintGroups, processedClassNames);
            } catch (JaxbValidationException ex) {
                if (errors.length() != 0) {
                    errors.append(", ");
                }
                errors.append("[").append(ex.getMessage()).append("]");
            }
        });
    }
}