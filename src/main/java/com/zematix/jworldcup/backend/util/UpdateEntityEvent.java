package com.zematix.jworldcup.backend.util;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;

@Getter @AllArgsConstructor
public class UpdateEntityEvent<T> {
	@NonNull
    private T entity;
	
    protected boolean success;
}