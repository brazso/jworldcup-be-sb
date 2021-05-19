package com.zematix.jworldcup.backend.exception;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.mapstruct.factory.Mappers;
import org.springframework.http.HttpStatus;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.zematix.jworldcup.backend.mapper.ParameterizedMessageMapper;

import lombok.Getter;

/**
 * Rest API errors are encapsulated into it.
 * Because this class might be instantiated manually, injection of other 
 * classes cannot be used inside.
 */
@Getter
public class ApiError {

	@JsonIgnore
	private final ParameterizedMessageMapper parameterizedMessageMapper = Mappers.getMapper(ParameterizedMessageMapper.class);
	
	private HttpStatus status;
	private LocalDateTime timestamp;
	private String message;
	private String exceptionClassName;
	private List<ApiErrorItem> items;

	private ApiError() {
		timestamp = LocalDateTime.now();
	}

	public ApiError(HttpStatus status) {
		this();
		this.status = status;
	}

	public ApiError(HttpStatus status, Throwable ex) {
		this(status);
		this.message = ex.getMessage();
		this.exceptionClassName = ex.getClass().getSimpleName();
		if (ex instanceof ServiceException) {
			// Cannot cast from List<ParameterizedMessageDto> to List<ApiErrorItem>
			this.items = parameterizedMessageMapper.entityListToDtoList(((ServiceException) ex).getMessages()).stream().collect(Collectors.toList());
		}
	}

	public ApiError(HttpStatus status, String message, Throwable ex) {
		this(status, ex);
		this.message = message;
	}
}