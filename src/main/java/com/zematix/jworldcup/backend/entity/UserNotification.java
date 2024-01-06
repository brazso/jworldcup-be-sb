package com.zematix.jworldcup.backend.entity;

import java.io.Serializable;
import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

import lombok.Getter;
import lombok.Setter;


/**
 * The persistent class for the user_status database table.
 * 
 */
@Entity
@Table(name="user_notification")
@NamedQuery(name="UserNotification.findAll", query="SELECT u FROM UserNotification u")
@Getter @Setter
public class UserNotification implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	@Column(name="user_notification_id", unique=true, nullable=false)
	private Long userNotificationId;

	//bi-directional many-to-one association to User
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="user_id", nullable=false)
	private User user;

	//bi-directional many-to-one association to UserNotificationStatus
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="user_notification_key_id", nullable=false)
	private Dictionary userNotificationType;

	@Column(name="creation_time", nullable=false)
	private LocalDateTime creationTime;

	@Column(name="modification_time", nullable=false)
	private LocalDateTime modificationTime;

	@Column(nullable=false, length=255)
	private String value;
	
}