package com.zematix.jworldcup.backend.validation;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import com.zematix.jworldcup.backend.util.CommonUtil;

/**
 * Performs email address validation.
 */
public class EmailValidator implements ConstraintValidator<Email, String> {
	
	/**
	 * Returns {@code true} if the email address in the given value parameter is valid, 
	 * {@code false} otherwise.
	 * 
	 * @param value - contains email address to be validated
	 * @param context - context in which the constraint is evaluated
	 * @return {@code true} if the given {@code value} is valid or {@code null}, {@code false} otherwise
	 */
	@Override
	public boolean isValid(String value, ConstraintValidatorContext context) {
		if (value == null)
			return true;
		return CommonUtil.isEmailValid(value);
	}
}