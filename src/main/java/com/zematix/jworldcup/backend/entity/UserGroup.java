package com.zematix.jworldcup.backend.entity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;


/**
 * The persistent class for the user_group database table.
 * 
 */
@Entity
@Table(name="user_group", uniqueConstraints=@UniqueConstraint(columnNames={"name", "event_id"}))
@NamedQuery(name="UserGroup.findAll", query="SELECT u FROM UserGroup u")
@Getter @Setter @EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class UserGroup implements Serializable {
	private static final long serialVersionUID = 1L;
	
	/**
	 * Virtual Everybody userGroupId value. It cannot be used by the database.
	 */
	public static final long EVERYBODY_USER_GROUP_ID =  0L;
	
	/**
	 * Virtual Everybody name (English ) value.
	 */
	public static final String EVERYBODY_NAME =  "Everybody";

	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	@Column(name="user_group_id", unique=true, nullable=false)
	@EqualsAndHashCode.Include
	private Long userGroupId;

	@Column(name="is_public_editable", nullable=false)
	private Byte isPublicEditable;

	@Column(name="is_public_visible", nullable=false)
	private Byte isPublicVisible;

	@Column(nullable=false, length=50)
	private String name;

	//bi-directional many-to-one association to Event
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="event_id", nullable=false)
	@EqualsAndHashCode.Include // outside database there might be EVERYBODY_USER_GROUP_ID elements where "event" is part of the PK
	private Event event;

	//bi-directional many-to-many association to User
	@ManyToMany //(fetch=FetchType.LAZY, cascade=CascadeType.ALL)
	@JoinTable(
		name="user__user_group"
		, joinColumns={
			@JoinColumn(name="user_group_id")
			}
		, inverseJoinColumns={
			@JoinColumn(name="user_id")
			}
		)
	@OrderBy("loginName ASC")
	private List<User> users = new ArrayList<>();

	//bi-directional many-to-one association to User
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="owner", nullable=false)
	private User owner;
	
	//bi-directional many-to-one association to Chat
	@OneToMany(mappedBy="userGroup")
	private List<Chat> chats = new ArrayList<>();
	
	public Boolean isPublicEditableAsBoolean() {
		return this.isPublicEditable == null ? null : this.isPublicEditable == 1;
	}

	public void setPublicEditableAsBoolean(Boolean isPublicEditable) {
		this.isPublicEditable = isPublicEditable == null ? null : (isPublicEditable.booleanValue() ? (byte) 1 : (byte) 0);
	}

	public Boolean isPublicVisibleAsBoolean() {
		return this.isPublicVisible == null ? null : this.isPublicVisible == 1;
	}

	public void setPublicVisibleAsBoolean(Boolean isPublicVisible) {
		this.isPublicVisible = isPublicVisible == null ? null : (isPublicVisible.booleanValue() ? (byte) 1 : (byte) 0);
	}

	public User addUser(User user) {
		getUsers().add(user);
		user.getUserGroups().add(this);

		return user;
	}

	public User removeUser(User user) {
		getUsers().remove(user);
		user.getUserGroups().remove(this);

		return user;
	}

	public Chat addChat(Chat chat) {
		getChats().add(chat);
		chat.setUserGroup(this);

		return chat;
	}

	public Chat removeChat(Chat chat) {
		getChats().remove(chat);
		chat.setUserGroup(null);

		return chat;
	}

	// Calculated getters
	
	/**
	 * Returns {@true} if actual userGroup belongs to Everybody.
	 *  
	 * @return {@code true} if it is Everybody group, {@code false} otherwise
	 */
	public boolean isEverybody() {
		return this.getUserGroupId() != null && this.getUserGroupId().equals(EVERYBODY_USER_GROUP_ID);
	}
	
}