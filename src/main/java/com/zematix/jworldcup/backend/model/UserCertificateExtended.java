package com.zematix.jworldcup.backend.model;

import java.util.Locale;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data @EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = true)
public class UserCertificateExtended extends UserCertificate {

	private static final long serialVersionUID = 1L;
	
    private Locale locale; //private String languageTag;

}