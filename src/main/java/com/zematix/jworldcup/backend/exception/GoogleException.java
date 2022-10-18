package com.zematix.jworldcup.backend.exception;

import com.zematix.jworldcup.backend.service.OpenLigaDBService;

/**
 * Simple exception class used by {@link OpenLigaDBService}. 
 */
public class GoogleException extends Exception {

	private static final long serialVersionUID = 1L;

	public GoogleException(String message) {
		super(message);
	}
}
