package com.zematix.jworldcup.backend.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data @EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class DictionaryDto {
	
	@EqualsAndHashCode.Include
	private Long dictionaryId;

	private String key;

	private String value;

	private String name;
}