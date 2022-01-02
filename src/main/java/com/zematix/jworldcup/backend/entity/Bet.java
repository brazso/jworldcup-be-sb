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


/**
 * The persistent class for the bet database table.
 * 
 */
@Entity
@Table(name="bet", uniqueConstraints=@UniqueConstraint(columnNames={"user_id", "match_id"}))
@NamedQuery(name="Bet.findAll", query="SELECT b FROM Bet b")
public class Bet implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	@Column(name="bet_id", unique=true, nullable=false)
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

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((betId == null) ? 0 : betId.hashCode());
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
		if (!(obj instanceof Bet)) // use instanceof instead of getClass, due to hibernate creating proxys of subclasses that are lazy-loaded
			return false;
		Bet other = (Bet) obj;
		if (betId == null) {
			if (other.getBetId() != null) // remember to change to getter at other, simple property may result always null
				return false;
		} else if (!betId.equals(other.getBetId())) // remember to change to getter at other, simple property may result always null
			return false;
		return true;
	}

	public Long getBetId() {
		return this.betId;
	}

	public void setBetId(Long betId) {
		this.betId = betId;
	}

	public Byte getGoalNormalByTeam1() {
		return this.goalNormalByTeam1;
	}

	public void setGoalNormalByTeam1(Byte goalNormalByTeam1) {
		this.goalNormalByTeam1 = goalNormalByTeam1;
	}

	public Byte getGoalNormalByTeam2() {
		return this.goalNormalByTeam2;
	}

	public void setGoalNormalByTeam2(Byte goalNormalByTeam2) {
		this.goalNormalByTeam2 = goalNormalByTeam2;
	}

	public Event getEvent() {
		return this.event;
	}

	public void setEvent(Event event) {
		this.event = event;
	}

	public Match getMatch() {
		return this.match;
	}

	public void setMatch(Match match) {
		this.match = match;
	}

	public User getUser() {
		return this.user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public Integer getScore() {
		return score;
	}

	public void setScore(Integer score) {
		this.score = score;
	}

}