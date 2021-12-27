package com.zematix.jworldcup.backend.entity;

import java.io.Serializable;
import java.time.LocalDateTime;
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
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;


/**
 * The persistent class for the match database table.
 * 
 */
@Entity
@Table(name="match_")
@NamedQuery(name="Match.findAll", query="SELECT m FROM Match m")
public class Match implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	@Column(name="match_id", unique=true, nullable=false)
	private Long matchId;

	@Min(0) @Max(99)
	@Column(name="goal_extra_by_team1")
	private Byte goalExtraByTeam1;

	@Min(0) @Max(99)
	@Column(name="goal_extra_by_team2")
	private Byte goalExtraByTeam2;

	//@NotNull
	@Min(0) @Max(99)
	@Column(name="goal_normal_by_team1")
	private Byte goalNormalByTeam1;

	//@NotNull
	@Min(0) @Max(99)
	@Column(name="goal_normal_by_team2")
	private Byte goalNormalByTeam2;

	@Min(0) @Max(99)
	@Column(name="goal_penalty_by_team1")
	private Byte goalPenaltyByTeam1;

	@Min(0) @Max(99)
	@Column(name="goal_penalty_by_team2")
	private Byte goalPenaltyByTeam2;

	@Column(name="match_n", nullable=false)
	private Short matchN;

	@Column(name="participants_rule", length=10)
	private String participantsRule;

	@Column(name="start_time", nullable=false)
	private LocalDateTime startTime;

	//bi-directional many-to-one association to Bet
	@OneToMany(mappedBy="match")
	private List<Bet> bets;

	//bi-directional many-to-one association to Event
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="event_id", nullable=false)
	private Event event;

	//bi-directional many-to-one association to Round
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="round_id", nullable=false)
	private Round round;

	//bi-directional many-to-one association to Team
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="team1_id")
	private Team team1;

	//bi-directional many-to-one association to Team
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="team2_id")
	private Team team2;
	
	/**
	 * Result of the match by team1: 1 - if won, 0 - if draw, -1 if lost
	 */
	@Transient
	private Integer resultByTeam1;
	
	/**
	 * Result of the match by team2: 1 - if won, 0 - if draw, -1 if lost
	 */	@Transient
	private Integer resultByTeam2;
	

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((matchId == null) ? 0 : matchId.hashCode());
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
		if (!(obj instanceof Match)) // use instanceof instead of getClass, due to hibernate creating proxys of subclasses that are lazy-loaded
			return false;
		Match other = (Match) obj;
		if (matchId == null) {
			if (other.getMatchId() != null) // remember to change to getter at other, simple property may result always null
				return false;
		} else if (!matchId.equals(other.getMatchId())) // remember to change to getter at other, simple property may result always null
			return false;
		return true;
	}

	public Long getMatchId() {
		return this.matchId;
	}

	public void setMatchId(Long matchId) {
		this.matchId = matchId;
	}

	public Byte getGoalExtraByTeam1() {
		return this.goalExtraByTeam1;
	}

	public void setGoalExtraByTeam1(Byte goalExtraByTeam1) {
		this.goalExtraByTeam1 = goalExtraByTeam1;
	}

	public Byte getGoalExtraByTeam2() {
		return this.goalExtraByTeam2;
	}

	public void setGoalExtraByTeam2(Byte goalExtraByTeam2) {
		this.goalExtraByTeam2 = goalExtraByTeam2;
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

	public Byte getGoalPenaltyByTeam1() {
		return this.goalPenaltyByTeam1;
	}

	public void setGoalPenaltyByTeam1(Byte goalPenaltyByTeam1) {
		this.goalPenaltyByTeam1 = goalPenaltyByTeam1;
	}

	public Byte getGoalPenaltyByTeam2() {
		return this.goalPenaltyByTeam2;
	}

	public void setGoalPenaltyByTeam2(Byte goalPenaltyByTeam2) {
		this.goalPenaltyByTeam2 = goalPenaltyByTeam2;
	}

	public Short getMatchN() {
		return this.matchN;
	}

	public void setMatchN(Short matchN) {
		this.matchN = matchN;
	}

	public String getParticipantsRule() {
		return this.participantsRule;
	}

	public void setParticipantsRule(String participantsRule) {
		this.participantsRule = participantsRule;
	}

	public LocalDateTime getStartTime() {
		return this.startTime;
	}

	public void setStartTime(LocalDateTime startTime) {
		this.startTime = startTime;
	}

	public List<Bet> getBets() {
		return this.bets;
	}

	public void setBets(List<Bet> bets) {
		this.bets = bets;
	}

	public Bet addBet(Bet bet) {
		getBets().add(bet);
		bet.setMatch(this);

		return bet;
	}

	public Bet removeBet(Bet bet) {
		getBets().remove(bet);
		bet.setMatch(null);

		return bet;
	}

	public Event getEvent() {
		return this.event;
	}

	public void setEvent(Event event) {
		this.event = event;
	}

	public Round getRound() {
		return this.round;
	}

	public void setRound(Round round) {
		this.round = round;
	}

	public Team getTeam1() {
		return this.team1;
	}

	public void setTeam1(Team team1) {
		this.team1 = team1;
	}

	public Team getTeam2() {
		return this.team2;
	}

	public void setTeam2(Team team2) {
		this.team2 = team2;
	}

	public Integer getResultByTeam1() {
		return resultByTeam1;
	}

	public void setResultByTeam1(Integer resultByTeam1) {
		this.resultByTeam1 = resultByTeam1;
	}

	public Integer getResultByTeam2() {
		return resultByTeam2;
	}

	public void setResultByTeam2(Integer resultByTeam2) {
		this.resultByTeam2 = resultByTeam2;
	}

}