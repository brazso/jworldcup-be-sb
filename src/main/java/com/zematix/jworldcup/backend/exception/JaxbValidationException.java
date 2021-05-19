package com.zematix.jworldcup.backend.exception;

import javax.validation.ValidationException;

public class JaxbValidationException extends ValidationException {

    private static final long serialVersionUID = 1L;
    
    public JaxbValidationException(final String message) {
        super(message);
    }

    public JaxbValidationException(final String message, final Throwable cause) {
        super(message, cause);
    }

}
