package com.zematix.jworldcup.backend.exception;

import org.springframework.http.ResponseEntity;

public interface ApiErrorHelper {

    default ResponseEntity<Object> buildResponseEntity(ApiError apiError) {
        return new ResponseEntity<>(apiError, apiError.getStatus());
    }
}