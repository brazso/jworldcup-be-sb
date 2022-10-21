package com.zematix.jworldcup.backend.entity;

import java.io.Serializable;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.Table;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;


/**
 * The persistent class for the round database table.
 * 
 */
@Entity
@Table(name="round")
@NamedQuery(name="Round.findAll", query="SELECT r FROM Round r")
@Getter @Setter @EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Round implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	@Column(name="round_id", unique=true, nullable=false)
	@EqualsAndHashCode.Include
	private Long roundId;

	@Column(name="is_groupmatch", nullable=false)
	private Byte isGroupmatch;

	@Column(name="is_overtime")
	private Byte isOvertime;

	@Column(nullable=false, length=50)
	private String name;

	//bi-directional many-to-one association to Match
	@OneToMany(mappedBy="round")
	@OrderBy("matchN ASC")
	private List<Match> matches;

	//bi-directional many-to-one association to Event
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="event_id", nullable=false)
	private Event event;

	public Boolean getIsGroupmatchAsBoolean() {
		return this.isGroupmatch == null ? null : this.isGroupmatch == 1;
	}

	public void setIsGroupmatchAsBoolean(Boolean isGroupmatch) {
		this.isGroupmatch = isGroupmatch == null ? null : (isGroupmatch.booleanValue() ? (byte) 1 : (byte) 0);
	}

	public Boolean getIsOvertimeAsBoolean() {
		return this.isOvertime == null ? null : this.isOvertime == 1;
	}

	public void setIsOvertimeAsBoolean(Boolean isOvertime) {
		this.isOvertime = isOvertime == null ? null : (isOvertime.booleanValue() ? (byte) 1 : (byte) 0);
	}

	public Match addMatch(Match match) {
		getMatches().add(match);
		match.setRound(this);

		return match;
	}

	public Match removeMatch(Match match) {
		getMatches().remove(match);
		match.setRound(null);

		return match;
	}
}