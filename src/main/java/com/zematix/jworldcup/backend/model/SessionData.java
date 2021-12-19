package com.zematix.jworldcup.backend.model;

import java.time.LocalDateTime;
import java.util.Locale;

import com.zematix.jworldcup.backend.entity.Event;
import com.zematix.jworldcup.backend.entity.User;
import com.zematix.jworldcup.backend.entity.UserOfEvent;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data @EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class SessionData {
    
    @EqualsAndHashCode.Include
    private String id;
        
    private String appShortName;
    private String appVersionNumber;
    private LocalDateTime appVersionDate;
    private LocalDateTime appCheatDateTime;
    private String appEmailAddr;
    
    private Locale locale;
    private User user;
    private Event event;
    private UserOfEvent userOfEvent;
    private String newsLine;
    
    private Integer eventCompletionPercent;

    public SessionData(String id) {
    	super();
    	this.id = id;
    }
}
