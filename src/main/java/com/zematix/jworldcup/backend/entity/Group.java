package com.zematix.jworldcup.backend.entity;

import java.io.Serializable;
import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import lombok.Getter;
import lombok.Setter;


/**
 * The persistent class for the group_ database table.
 * 
 */
@Entity
@Table(name="group_")
@NamedQuery(name="Group.findAll", query="SELECT g FROM Group g")
@Getter @Setter
public class Group implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	@Column(name="group_id", unique=true, nullable=false)
	private Long groupId;

	@Column(nullable=false, length=1)
	private String name;

	//bi-directional many-to-one association to Event
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="event_id", nullable=false)
	private Event event;

	//bi-directional many-to-one association to Team
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="team1_id")
	private Team team1;

	//bi-directional many-to-one association to Team
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="team2_id")
	private Team team2;

	//bi-directional many-to-one association to Team
	@OneToMany(mappedBy="group")
	private List<Team> teams;

	public Team addTeam(Team team) {
		getTeams().add(team);
		team.setGroup(this);

		return team;
	}

	public Team removeTeam(Team team) {
		getTeams().remove(team);
		team.setGroup(null);

		return team;
	}

}