package com.zematix.jworldcup.backend.entity;

import java.io.Serializable;
import java.util.List;
import java.util.Set;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import lombok.Getter;
import lombok.Setter;


/**
 * The persistent class for the dictionary database table.
 * 
 */
@Entity
@Table(name="dictionary")
@NamedQuery(name="Dictionary.findAll", query="SELECT d FROM Dictionary d")
@Getter @Setter
public class Dictionary implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	@Column(name="dictionary_id", unique=true, nullable=false)
	private Long dictionaryId;

	@Column(name="key_", nullable=false, length=50)
	private String key;

	@Column(nullable=false, length=16)
	private String value;

	@Column(length=50)
	private String name;

	//bi-directional many-to-many association between virtual Role and User
	@ManyToMany
	@JoinTable(
		name="user__role"
		, joinColumns={
			@JoinColumn(name="role_id")
			}
		, inverseJoinColumns={
			@JoinColumn(name="user_id")
			}
		)
	private Set<User> roleUsers;
	
	//bi-directional many-to-one association to User
	@OneToMany(mappedBy="userStatus")
	private List<User> userStatusUsers;

	//bi-directional many-to-one association to UserNotification
	@OneToMany(mappedBy="userNotificationType")
	private List<UserNotification> userNotifications;

	public User addRoleUser(User user) {
		getRoleUsers().add(user);
		user.setUserStatus(this);

		return user;
	}

	public User removeRoleUser(User user) {
		getRoleUsers().remove(user);
		user.setUserStatus(null);

		return user;
	}
	
	public User addUserStatusUser(User user) {
		getUserStatusUsers().add(user);
		user.setUserStatus(this);

		return user;
	}

	public User removeUserStatusUser(User user) {
		getUserStatusUsers().remove(user);
		user.setUserStatus(null);

		return user;
	}
	
	public UserNotification addUserNotification(UserNotification userNotification) {
		getUserNotifications().add(userNotification);
		userNotification.setUserNotificationType(this);

		return userNotification;
	}

	public UserNotification removeUser(UserNotification userNotification) {
		getUserNotifications().remove(userNotification);
		userNotification.setUserNotificationType(null);

		return userNotification;
	}
}