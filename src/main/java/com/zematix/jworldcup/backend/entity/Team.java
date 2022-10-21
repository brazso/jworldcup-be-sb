package com.zematix.jworldcup.backend.entity;

import java.io.Serializable;
import javax.persistence.*;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.util.List;


/**
 * The persistent class for the team database table.
 * 
 */
@Entity
@Table(name="team")
@NamedQuery(name="Team.findAll", query="SELECT t FROM Team t")
@Getter @Setter @EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Team implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	@Column(name="team_id", unique=true, nullable=false)
	@EqualsAndHashCode.Include
	private Long teamId;

	@Column(name="fifa_points")
	private Short fifaPoints;

	@Column(nullable=false, length=3)
	private String flag;

	@Column(nullable=false, length=50)
	private String name;

	@Column(name="ws_id")
	private Long wsId;

	//bi-directional many-to-one association to Group
	@OneToMany(mappedBy="team1")
	private List<Group> groups1;

	//bi-directional many-to-one association to Group
	@OneToMany(mappedBy="team2")
	private List<Group> groups2;

	//bi-directional many-to-one association to Match
	@OneToMany(mappedBy="team1")
	private List<Match> matches1;

	//bi-directional many-to-one association to Match
	@OneToMany(mappedBy="team2")
	private List<Match> matches2;

	//bi-directional many-to-one association to Event
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="event_id", nullable=false)
	private Event event;

	//bi-directional many-to-one association to Group
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="group_id", nullable=false)
	private Group group;

	public Group addGroups1(Group groups1) {
		getGroups1().add(groups1);
		groups1.setTeam1(this);

		return groups1;
	}

	public Group removeGroups1(Group groups1) {
		getGroups1().remove(groups1);
		groups1.setTeam1(null);

		return groups1;
	}

	public Group addGroups2(Group groups2) {
		getGroups2().add(groups2);
		groups2.setTeam2(this);

		return groups2;
	}

	public Group removeGroups2(Group groups2) {
		getGroups2().remove(groups2);
		groups2.setTeam2(null);

		return groups2;
	}

	public Match addMatches1(Match matches1) {
		getMatches1().add(matches1);
		matches1.setTeam1(this);

		return matches1;
	}

	public Match removeMatches1(Match matches1) {
		getMatches1().remove(matches1);
		matches1.setTeam1(null);

		return matches1;
	}

	public Match addMatches2(Match matches2) {
		getMatches2().add(matches2);
		matches2.setTeam2(this);

		return matches2;
	}

	public Match removeMatches2(Match matches2) {
		getMatches2().remove(matches2);
		matches2.setTeam2(null);

		return matches2;
	}
}