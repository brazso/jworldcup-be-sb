package com.zematix.jworldcup.backend.exception;

import java.security.InvalidParameterException;
import java.sql.SQLException;
import java.text.ParseException;

import javax.servlet.http.HttpServletRequest;
import javax.validation.ValidationException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
public class ControllerExceptionHandler implements ApiErrorHelper {

    private static final Logger logger = LoggerFactory.getLogger(ControllerExceptionHandler.class);
    
    private static final String MSG_ERROR = "Error: ";
    private static final String MSG_PATTERN = "%s: %s";
    
    /**
     * General exception not handled by other handler
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleException(Exception e, HttpServletRequest request) {
        logger.error("{} processed by general exception handled from: {}", e.getClass().getSimpleName(), request.getRequestURI());
        logger.trace(MSG_ERROR, e);
        return buildResponseEntity(new ApiError(HttpStatus.INTERNAL_SERVER_ERROR, e));
    }

    @ExceptionHandler(SQLException.class)
    public ResponseEntity<Object> handleSQLException(SQLException e, HttpServletRequest request) {
        logger.error("{} handled from: {}", e.getClass().getSimpleName(), request.getRequestURI());
        logger.trace(MSG_ERROR, e);
        return buildResponseEntity(new ApiError(HttpStatus.INTERNAL_SERVER_ERROR, e));
    }

    @ExceptionHandler(InvalidParameterException.class)
    public ResponseEntity<Object> handleInvalidParameterException(InvalidParameterException e, HttpServletRequest request) {
    	logger.error("{} handled from: {}", e.getClass().getSimpleName(), request.getRequestURI());
        logger.trace(MSG_ERROR, e);
        return buildResponseEntity(new ApiError(HttpStatus.INTERNAL_SERVER_ERROR, e));
    }

    @ExceptionHandler(ParseException.class)
    public ResponseEntity<Object> handleParseException(ParseException e, HttpServletRequest request) {
    	logger.error("{} handled from: {}", e.getClass().getSimpleName(), request.getRequestURI());
        logger.trace(MSG_ERROR, e);
        return buildResponseEntity(new ApiError(HttpStatus.INTERNAL_SERVER_ERROR, e));
    }

    @ExceptionHandler(ServiceException.class)
    public ResponseEntity<Object> handleServiceException(ServiceException e, HttpServletRequest request) {
    	logger.error("{} handled from: {}", e.getClass().getSimpleName(), request.getRequestURI());
        logger.trace(MSG_ERROR, e);
        return buildResponseEntity(new ApiError(HttpStatus.INTERNAL_SERVER_ERROR, e));
    }
    
    @ExceptionHandler(JaxbValidationException.class)
    public ResponseEntity<Object> handleJaxbValidationException(JaxbValidationException e, HttpServletRequest request) {
    	logger.error("{} handled from: {}", e.getClass().getSimpleName(), request.getRequestURI());
        logger.trace(MSG_ERROR, e);
        String msg = String.format(MSG_PATTERN, e.getClass().getName(), e.getMessage());
        return buildResponseEntity(new ApiError(HttpStatus.UNPROCESSABLE_ENTITY, msg, e));
    }

    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<Object> handleValidationException(ValidationException e, HttpServletRequest request) {
    	logger.error("{} handled from: {}", e.getClass().getSimpleName(), request.getRequestURI());
        logger.trace(MSG_ERROR, e);
        String msg = String.format(MSG_PATTERN, e.getClass().getName(), e.getMessage());
        return buildResponseEntity(new ApiError(HttpStatus.UNPROCESSABLE_ENTITY, msg, e));
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<Object> handleNotFoundException(NotFoundException e, HttpServletRequest request) {
    	logger.error("{} handled from: {}", e.getClass().getSimpleName(), request.getRequestURI());
        logger.trace(MSG_ERROR, e);
        String msg = String.format(MSG_PATTERN, e.getClass().getName(), e.getMessage());
        return buildResponseEntity(new ApiError(HttpStatus.NOT_FOUND, msg, e));
    }

    @ExceptionHandler(ConcurrentException.class)
    public ResponseEntity<Object> handleConcurrentException(ConcurrentException e, HttpServletRequest request) {
    	logger.error("{} handled from: {}", e.getClass().getSimpleName(), request.getRequestURI());
        logger.trace(MSG_ERROR, e);
        String msg = String.format(MSG_PATTERN, e.getClass().getName(), e.getMessage());
        return buildResponseEntity(new ApiError(HttpStatus.CONFLICT, msg, e));
    }
    
}
