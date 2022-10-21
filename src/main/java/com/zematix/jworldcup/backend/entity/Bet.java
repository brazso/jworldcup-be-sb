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
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;


/**
 * The persistent class for the bet database table.
 * 
 */
@Entity
@Table(name="bet", uniqueConstraints=@UniqueConstraint(columnNames={"user_id", "match_id"}))
@NamedQuery(name="Bet.findAll", query="SELECT b FROM Bet b")
@Getter @Setter @EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Bet implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	@Column(name="bet_id", unique=true, nullable=false)
	@EqualsAndHashCode.Include
	private Long betId;

	@NotNull
	@Min(0) @Max(99)
	@Column(name="goal_normal_by_team1", nullable=false)
	private Byte goalNormalByTeam1;

	@NotNull
	@Min(0) @Max(99)
	@Column(name="goal_normal_by_team2", nullable=false)
	private Byte goalNormalByTeam2;

	//bi-directional many-to-one association to Event
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="event_id", nullable=false)
	private Event event;

	//bi-directional many-to-one association to Match
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="match_id", nullable=false)
	private Match match;

	//bi-directional many-to-one association to User
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="user_id", nullable=false)
	private User user;
	
	@Transient
	private Integer score;
}