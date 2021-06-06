package com.zematix.jworldcup.backend.dto;

import java.io.Serializable;

import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * This class is required for storing the username and password received from the client.
 * Default constructor is a must for JSON Parsing.
 */
@NoArgsConstructor @Getter
public class JwtRequest extends CommonResponse implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private String username;
	private String password;

}