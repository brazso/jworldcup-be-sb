package com.zematix.jworldcup.backend.service;

/**
 * Simple exception class used by {@link OpenLigaDBService}. 
 */
public class OpenLigaDBException extends Exception {

	private static final long serialVersionUID = 1L;

	public OpenLigaDBException(String message) {
		super(message);
	}
}
