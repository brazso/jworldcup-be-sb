package com.zematix.jworldcup.backend.entity;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.NamedNativeQuery;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import com.zematix.jworldcup.backend.validation.Email;
import com.zematix.jworldcup.backend.validation.ModifyUserValidationGroup;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * The persistent class for the user database table.
 * 
 */
@Entity
@Table(name="user")
@NamedQuery(name="User.findAll", query="SELECT u FROM User u")
@NamedNativeQuery(
        name    =   "deleteUserRolesByUserId",
        query   =   "DELETE FROM user__role WHERE user_id = ?",
        			resultClass=User.class
)
@NamedNativeQuery(
        name    =   "deleteUserUserGroupsByUserId",
        query   =   "DELETE FROM user__user_group WHERE user_id = ?",
                    resultClass=User.class
)
@Data @NoArgsConstructor @EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class User implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	@Column(name="user_id", unique=true, nullable=false)
	@EqualsAndHashCode.Include
	private Long userId;

	@NotNull
	@Size(min=1, max=50, groups={/*Default.class, */ModifyUserValidationGroup.class})
	@Email(groups={/*Default.class, */ModifyUserValidationGroup.class})
	@Column(name="email_addr", unique=true, nullable=false, length=50)
	private String emailAddr;

	@Size(min=1, max=50, groups={/*Default.class, */ModifyUserValidationGroup.class})
	@Email(groups={/*Default.class, */ModifyUserValidationGroup.class})
	@Column(name="email_new", nullable=true, length=50)
	private String emailNew;

	@NotNull
	@Size(min=1, max=50)
	@Column(name="full_name", nullable=false, length=50)
	private String fullName;

	@NotNull
	@Size(min=1, max=25)
	@Column(name="login_name", unique=true, nullable=false, length=25)
	private String loginName;

	@NotNull
	@Size(min=1, max=128, groups={/*Default.class, */ModifyUserValidationGroup.class})
	@Column(name="login_password", nullable=false, length=128)
	private String loginPassword;

	@Size(min=1, max=128)
	@Column(name="reset_password", nullable=true, length=128)
	private String resetPassword;

	@Column(nullable=false, length=20)
	private String token;

	@NotNull
	@Size(min=1, max=50)
	@Column(name="zone_id", nullable=false, length=50)
	private String zoneId;

	@Column(name="modification_time", nullable=false)
	private LocalDateTime modificationTime;

	//bi-directional many-to-one association to Bet
	@OneToMany(mappedBy="user")
	private List<Bet> bets;

	//bi-directional many-to-many association to Role
	@ManyToMany(mappedBy="users")
	private Set<Role> roles;

	//bi-directional many-to-one association to UserStatus
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="user_status_id", nullable=false)
	private UserStatus userStatus;

	//bi-directional many-to-many association to UserGroup
	@ManyToMany(mappedBy="users")
	private Set<UserGroup> userGroups;

	//bi-directional many-to-one association to UserGroup
	@OneToMany(mappedBy="owner")
	private List<UserGroup> ownerUserGroups;

	//bi-directional many-to-one association to UserOfEvent
	@OneToMany(mappedBy="user")
	private List<UserOfEvent> userOfEvents;

	//bi-directional many-to-one association to UserGroup
	@OneToMany(mappedBy="user")
	private List<Chat> chats;

	@Transient
	private Boolean isOnline;

	@Transient
	private LocalDateTime loginTime;

	public Bet addBet(Bet bet) {
		getBets().add(bet);
		bet.setUser(this);

		return bet;
	}

	public Bet removeBet(Bet bet) {
		getBets().remove(bet);
		bet.setUser(null);

		return bet;
	}

	public UserGroup addOwnerUserGroups(UserGroup ownerUserGroups) {
		getOwnerUserGroups().add(ownerUserGroups);
		ownerUserGroups.setOwner(this);

		return ownerUserGroups;
	}

	public UserGroup removeOwnerUserGroups(UserGroup ownerUserGroups) {
		getOwnerUserGroups().remove(ownerUserGroups);
		ownerUserGroups.setOwner(null);

		return ownerUserGroups;
	}

	public UserOfEvent addUserOfEvent(UserOfEvent userOfEvent) {
		getUserOfEvents().add(userOfEvent);
		userOfEvent.setUser(this);

		return userOfEvent;
	}

	public UserOfEvent removeUserOfEvent(UserOfEvent userOfEvent) {
		getUserOfEvents().remove(userOfEvent);
		userOfEvent.setUser(null);

		return userOfEvent;
	}

	public Chat addChat(Chat chat) {
		getChats().add(chat);
		chat.setUser(this);

		return chat;
	}

	public Chat removeChat(Chat chat) {
		getChats().remove(chat);
		chat.setUser(null);

		return chat;
	}
}