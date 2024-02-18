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
import javax.persistence.NamedNativeQuery;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;


/**
 * The persistent class for the chat database table.
 * 
 */
@Entity
@Table(name = "chat")
@NamedQuery(name = "Chat.findAll", query = "SELECT c FROM Chat c")
@NamedNativeQuery(
		name = "Chat.deleteChatsByUserId", 
		query = "DELETE FROM chat WHERE chat_id IN (SELECT * FROM (SELECT chat_id FROM chat c LEFT JOIN user_group u ON c.user_group_id = u.user_group_id WHERE c.user_id = ? or c.target_user_id = ? or u.owner = ?) as t)",
		resultClass = Chat.class)
@Getter @Setter @EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Chat implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	@Column(name="chat_id", unique=true, nullable=false)
	@EqualsAndHashCode.Include
	private Long chatId;

	//bi-directional many-to-one association to Event
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="event_id", nullable=false)
	private Event event;

	@Column(nullable=false, length=256)
	private String message;

	@Column(name="modification_time", nullable=false)
	private LocalDateTime modificationTime;

	@Column(name="access_time", nullable=true)
	private LocalDateTime accessTime;

	//bi-directional many-to-one association to UserGroup
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="user_group_id", nullable=true)
	private UserGroup userGroup;

	//bi-directional many-to-one association to User
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="user_id", nullable=false)
	private User user;
	
	//bi-directional many-to-one association to User
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="target_user_id", nullable=true)
	private User targetUser;
}