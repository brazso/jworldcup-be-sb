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

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;


/**
 * The persistent class for the match database table.
 * 
 */
@Entity
@Table(name="match_")
@NamedQuery(name="Match.findAll", query="SELECT m FROM Match m")
@Getter @Setter @EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Match implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	@Column(name="match_id", unique=true, nullable=false)
	@EqualsAndHashCode.Include
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
	 * Result sign of the match by team1: 1 - if won, 0 - if draw, -1 if lost, null if not valuable
	 */
	@Transient
	private Integer resultSignByTeam1;

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
}