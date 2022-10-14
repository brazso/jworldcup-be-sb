package com.zematix.jworldcup.backend.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;

@Getter @AllArgsConstructor
public class PublishedEvent<T> {
	@NonNull
    private T entity;
	
    protected boolean success;
}