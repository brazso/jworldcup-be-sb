package com.zematix.jworldcup.backend.controller;

import java.util.List;

import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import com.zematix.jworldcup.backend.service.ParametrizedMessage;
import com.zematix.jworldcup.backend.service.ServiceException;

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
		GenericEntity<List<ParametrizedMessage>> list = new GenericEntity<List<ParametrizedMessage>>(exception.getMessages()){};
		
		return Response.status(httpStatus).entity(list).build();
	}
}

