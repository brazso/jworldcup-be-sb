package com.zematix.jworldcup.backend.dto;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

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
    private Set<String> authorities = new HashSet<>(); // similar to roles, but contains ("ROLE_" + role.getRole()) elements
//	private UserStatus userStatus;
    private String userStatus;
//	private Set<UserGroup> userGroups;
//	private List<UserGroup> ownerUserGroups;
//	private List<UserOfEvent> userOfEvents;
//	private List<Chat> chats;

}
