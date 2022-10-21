package com.zematix.jworldcup.backend.entity;

import java.io.Serializable;

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
 * The persistent class for the user_of_event database table.
 * 
 */
@Entity
@Table(name="user_of_event")
@NamedQuery(name="UserOfEvent.findAll", query="SELECT u FROM UserOfEvent u")
@Getter @Setter
public class UserOfEvent implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	@Column(name="user_of_event_id", unique=true, nullable=false)
	private Long userOfEventId;

	//bi-directional many-to-one association to User
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="user_id", nullable=false)
	private User user;

	//bi-directional many-to-one association to Event
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="event_id", nullable=false)
	private Event event;

	//bi-directional many-to-one association to Team
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="fav_group_team_id")
	private Team favouriteGroupTeam;

	//bi-directional many-to-one association to Team
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="fav_knockout_team_id")
	private Team favouriteKnockoutTeam;
}