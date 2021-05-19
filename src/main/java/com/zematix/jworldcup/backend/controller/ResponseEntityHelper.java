package com.zematix.jworldcup.backend.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public interface ResponseEntityHelper {

	default <E> ResponseEntity<E> buildResponseEntityWithOK(E e) {
		return new ResponseEntity<>(e, HttpStatus.OK);
	}

}