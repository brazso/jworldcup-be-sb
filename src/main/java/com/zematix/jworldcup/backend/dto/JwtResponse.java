package com.zematix.jworldcup.backend.dto;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * This is class is required for creating a response containing the JWT to be 
 * returned to the user. 
 */
@AllArgsConstructor @Getter
public class JwtResponse implements Serializable {

	private static final long serialVersionUID = 1L;

	private final String token;

}