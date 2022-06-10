package com.zematix.jworldcup.backend.entity;

import java.io.Serializable;
import java.util.List;
import java.util.Set;

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
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;


/**
 * The persistent class for the user_group database table.
 * 
 */
@Entity
@Table(name="user_group", uniqueConstraints=@UniqueConstraint(columnNames={"name", "event_id"}))
@NamedQuery(name="UserGroup.findAll", query="SELECT u FROM UserGroup u")
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
	private Set<User> users;

	//bi-directional many-to-one association to User
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="owner", nullable=false)
	private User owner;
	
	//bi-directional many-to-one association to Chat
	@OneToMany(mappedBy="userGroup")
	private List<Chat> chats;
	
	@Transient
	private List<User> virtualUsers; // includes parent everybody user group 
	
	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((userGroupId == null) ? 0 : userGroupId.hashCode());
		return result;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof UserGroup)) // use instanceof instead of getClass, due to hibernate creating proxys of subclasses that are lazy-loaded
			return false;
		UserGroup other = (UserGroup) obj;
		if (userGroupId == null) {
			if (other.getUserGroupId() != null) // remember to change to getter at other, simple property may result always null
				return false;
		} else if (!userGroupId.equals(other.getUserGroupId())) // remember to change to getter at other, simple property may result always null
			return false;
		return true;
	}

	public Long getUserGroupId() {
		return this.userGroupId;
	}

	public void setUserGroupId(Long userGroupId) {
		this.userGroupId = userGroupId;
	}

	public Byte getIsPublicEditable() {
		return this.isPublicEditable;
	}

	public void setIsPublicEditable(Byte isPublicEditable) {
		this.isPublicEditable = isPublicEditable;
	}

	public Boolean isPublicEditableAsBoolean() {
		return this.isPublicEditable == null ? null : this.isPublicEditable == 1;
	}

	public void setPublicEditableAsBoolean(Boolean isPublicEditable) {
		this.isPublicEditable = isPublicEditable == null ? null : (isPublicEditable.booleanValue() ? (byte) 1 : (byte) 0);
	}

	public Byte getIsPublicVisible() {
		return this.isPublicVisible;
	}

	public void setIsPublicVisible(Byte isPublicVisible) {
		this.isPublicVisible = isPublicVisible;
	}

	public Boolean isPublicVisibleAsBoolean() {
		return this.isPublicVisible == null ? null : this.isPublicVisible == 1;
	}

	public void setPublicVisibleAsBoolean(Boolean isPublicVisible) {
		this.isPublicVisible = isPublicVisible == null ? null : (isPublicVisible.booleanValue() ? (byte) 1 : (byte) 0);
	}

	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Event getEvent() {
		return this.event;
	}

	public void setEvent(Event event) {
		this.event = event;
	}

	public Set<User> getUsers() {
		return this.users;
	}

	public void setUsers(Set<User> users) {
		this.users = users;
	}

	public User getOwner() {
		return this.owner;
	}

	public void setOwner(User owner) {
		this.owner = owner;
	}

	public List<Chat> getChats() {
		return chats;
	}

	public void setChats(List<Chat> chats) {
		this.chats = chats;
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

	public List<User> getVirtualUsers() {
		return virtualUsers;
	}

	public void setVirtualUsers(List<User> virtualUsers) {
		this.virtualUsers = virtualUsers;
	}

	// Getters
	
	/**
	 * Returns {@true} if actual userGroup belongs to Everybody.
	 *  
	 * @return {@code true} if it is Everybody group, {@code false} otherwise
	 */
	public boolean isEverybody() {
		return this.getUserGroupId() != null && this.getUserGroupId().equals(EVERYBODY_USER_GROUP_ID);
	}
	
}