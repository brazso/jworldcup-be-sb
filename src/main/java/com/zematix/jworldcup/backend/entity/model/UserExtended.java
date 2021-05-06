package com.zematix.jworldcup.backend.entity.model;

import java.util.Locale;

import com.zematix.jworldcup.backend.entity.User;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data @EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = true)
public class UserExtended extends User {

	private static final long serialVersionUID = 1L;
	
	private String emailNewAgain;
    private String loginPasswordNew;
    private String loginPasswordAgain;
    private Locale locale; //private String languageTag;

}