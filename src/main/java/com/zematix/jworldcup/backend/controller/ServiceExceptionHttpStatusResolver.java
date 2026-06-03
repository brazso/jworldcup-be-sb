package com.zematix.jworldcup.backend.controller;

import java.util.List;

import jakarta.ws.rs.core.GenericEntity;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

import com.zematix.jworldcup.backend.exception.ServiceException;
import com.zematix.jworldcup.backend.model.ParameterizedMessage;

/**
 * HTTP responses with ServiceException from REST services has status
 * BAD_REQUEST and they contain {@link ServiceException#getMessages()}
 */
@Provider
public class ServiceExceptionHttpStatusResolver implements
		ExceptionMapper<ServiceException> {
 
	@Override
	public Response toResponse(ServiceException exception) {
		Response.Status httpStatus = Response.Status.BAD_REQUEST;
		
		// Lists must be enclosed by GenericEntity for jaxb/json
		GenericEntity<List<ParameterizedMessage>> list = new GenericEntity<List<ParameterizedMessage>>(exception.getMessages()){};
		
		return Response.status(httpStatus).entity(list).build();
	}
}

