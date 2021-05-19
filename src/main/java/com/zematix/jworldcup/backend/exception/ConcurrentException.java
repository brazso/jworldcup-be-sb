package com.zematix.jworldcup.backend.exception;

public class ConcurrentException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public ConcurrentException(final String message) {
        super(message);
    }

    public ConcurrentException(final String message, final Throwable cause) {
        super(message, cause);
    }

}
