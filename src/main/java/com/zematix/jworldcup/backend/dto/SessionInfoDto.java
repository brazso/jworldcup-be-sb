package com.zematix.jworldcup.backend.dto;

import java.time.LocalDateTime;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data @EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class SessionInfoDto {
    
    @EqualsAndHashCode.Include
    private String id;
        
    private String appShortName;
    private String appVersionNumber;
    private LocalDateTime appVersionDate;
    private LocalDateTime appCheatDateTime;
    private String appEmailAddr;
    
    private String localeId;
    private UserDto user;
    private EventDto event;
    private UserOfEventDto userOfEvent;
    private String newsLine;

}
