package com.zematix.jworldcup.backend.entity;

import java.io.Serializable;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import lombok.Getter;
import lombok.Setter;


/**
 * The persistent class for the user_status database table.
 * 
 */
@Entity
@Table(name="user_status")
@NamedQuery(name="UserStatus.findAll", query="SELECT u FROM UserStatus u")
@Getter @Setter
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