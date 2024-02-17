package com.zematix.jworldcup.backend;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;

import javax.inject.Inject;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class TestBase {

	@Inject
	private ObjectMapper mapper;


	protected String generateJson(Object object) {
		String result;
		try {
			result = mapper.writeValueAsString(object);
		} catch (JsonProcessingException e) {
			result = null;
		}
		return result;
	}
	
	protected String readStringResource(String path) {
		String result;
		try {
			result = Files.readString(Paths.get(ClassLoader.getSystemClassLoader().getResource(path).toURI()));
		} catch (IOException | URISyntaxException e) {
			result = null;
		}
		return result;
	}
}
