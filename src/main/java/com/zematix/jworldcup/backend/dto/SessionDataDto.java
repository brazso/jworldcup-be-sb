package com.zematix.jworldcup.backend.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data @EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class SessionDataDto {
    
    @EqualsAndHashCode.Include
    private String id;
        
    private String appShortName;
    private String appVersionNumber;
    private LocalDate appVersionDate;
    private LocalDateTime actualDateTime;
    private String appEmailAddr;
    
    private String localeId;
    private UserDto user;
    private EventDto event;
    private UserOfEventDto userOfEvent;
    private String newsLine;
    
    private Integer eventCompletionPercent;
    private List<LocalDateTime> eventTriggerStartTimes;

}
