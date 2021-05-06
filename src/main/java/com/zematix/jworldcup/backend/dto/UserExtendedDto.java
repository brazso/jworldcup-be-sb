package com.zematix.jworldcup.backend.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data @EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = true)
public class UserExtendedDto extends UserDto {
    
    private String emailNewAgain;
    private String loginPasswordNew;
    private String loginPasswordAgain;
    private String languageTag;

}
