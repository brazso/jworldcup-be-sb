package com.zematix.jworldcup.backend.model;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * This class is required for storing the username and password received from the client.
 * Default constructor is a must for JSON Parsing.
 */
@NoArgsConstructor @AllArgsConstructor @Getter @Setter
public class JwtRequest implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private String username;
	private String password;

}