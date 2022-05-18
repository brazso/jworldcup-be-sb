package com.zematix.jworldcup.backend.exception;

import com.zematix.jworldcup.backend.service.OpenLigaDBService;

/**
 * Simple exception class used by {@link OpenLigaDBService}. 
 */
public class OpenLigaDBException extends Exception {

	private static final long serialVersionUID = 1L;

	public OpenLigaDBException(String message) {
		super(message);
	}
}
