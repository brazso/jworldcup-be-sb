package com.zematix.jworldcup.backend.dto;

import java.time.LocalDateTime;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data @EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class UserDto {
    
    @EqualsAndHashCode.Include
    private Long userId;
    
    private String emailAddr;
    private String emailNew;
    private String fullName;
    private String loginName;
    private String loginPassword;
    private String resetPassword;
    private String token;
    private String zoneId;
    private LocalDateTime modificationTime;
//	private List<Bet> bets;
//	private Set<Role> roles;
//	private UserStatus userStatus;
//	private Set<UserGroup> userGroups;
//	private List<UserGroup> ownerUserGroups;
//	private List<UserOfEvent> userOfEvents;
//	private List<Chat> chats;

}
