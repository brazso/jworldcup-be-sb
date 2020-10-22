package com.zematix.jworldcup.backend.entity;

import java.io.Serializable;
import javax.persistence.*;
import java.util.List;


/**
 * The persistent class for the round database table.
 * 
 */
@Entity
@Table(name="round")
@NamedQuery(name="Round.findAll", query="SELECT r FROM Round r")
public class Round implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	@Column(name="round_id", unique=true, nullable=false)
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

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((roundId == null) ? 0 : roundId.hashCode());
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
		if (!(obj instanceof Round)) // use instanceof instead of getClass, due to hibernate creating proxys of subclasses that are lazy-loaded
			return false;
		Round other = (Round) obj;
		if (roundId == null) {
			if (other.getRoundId() != null) // remember to change to getter at other, simple property may result always null
				return false;
		} else if (!roundId.equals(other.getRoundId())) // remember to change to getter at other, simple property may result always null
			return false;
		return true;
	}

	public Long getRoundId() {
		return this.roundId;
	}

	public void setRoundId(Long roundId) {
		this.roundId = roundId;
	}

	public Byte getIsGroupmatch() {
		return this.isGroupmatch;
	}

	public void setIsGroupmatch(Byte isGroupmatch) {
		this.isGroupmatch = isGroupmatch;
	}

	public Boolean getIsGroupmatchAsBoolean() {
		return this.isGroupmatch == null ? null : this.isGroupmatch == 1;
	}

	public void setIsGroupmatchAsBoolean(Boolean isGroupmatch) {
		this.isGroupmatch = isGroupmatch == null ? null : (isGroupmatch.booleanValue() ? (byte) 1 : (byte) 0);
	}

	public Byte getIsOvertime() {
		return this.isOvertime;
	}

	public void setIsOvertime(Byte isOvertime) {
		this.isOvertime = isOvertime;
	}

	public Boolean getIsOvertimeAsBoolean() {
		return this.isOvertime == null ? null : this.isOvertime == 1;
	}

	public void setIsOvertimeAsBoolean(Boolean isOvertime) {
		this.isOvertime = isOvertime == null ? null : (isOvertime.booleanValue() ? (byte) 1 : (byte) 0);
	}

	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<Match> getMatches() {
		return this.matches;
	}

	public void setMatches(List<Match> matches) {
		this.matches = matches;
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

	public Event getEvent() {
		return this.event;
	}

	public void setEvent(Event event) {
		this.event = event;
	}

}