package com.zematix.jworldcup.backend.entity;

import java.io.Serializable;
import javax.persistence.*;
import java.util.List;


/**
 * The persistent class for the user_status database table.
 * 
 */
@Entity
@Table(name="user_status")
@NamedQuery(name="UserStatus.findAll", query="SELECT u FROM UserStatus u")
public class UserStatus implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	@Column(name="user_status_id", unique=true, nullable=false)
	private Long userStatusId;

	@Column(nullable=false, length=50)
	private String name;

	@Column(nullable=false, length=16)
	private String status;

	//bi-directional many-to-one association to User
	@OneToMany(mappedBy="userStatus")
	private List<User> users;

	public Long getUserStatusId() {
		return this.userStatusId;
	}

	public void setUserStatusId(Long userStatusId) {
		this.userStatusId = userStatusId;
	}

	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getStatus() {
		return this.status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public List<User> getUsers() {
		return this.users;
	}

	public void setUsers(List<User> users) {
		this.users = users;
	}

	public User addUser(User user) {
		getUsers().add(user);
		user.setUserStatus(this);

		return user;
	}

	public User removeUser(User user) {
		getUsers().remove(user);
		user.setUserStatus(null);

		return user;
	}

}