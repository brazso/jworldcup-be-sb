package com.zematix.jworldcup.backend.entity;

import java.io.Serializable;
import javax.persistence.*;
import java.util.List;


/**
 * The persistent class for the team database table.
 * 
 */
@Entity
@Table(name="team")
@NamedQuery(name="Team.findAll", query="SELECT t FROM Team t")
public class Team implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	@Column(name="team_id", unique=true, nullable=false)
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

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((teamId == null) ? 0 : teamId.hashCode());
		return result;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 * Some modifications have been done due to hibernate proxy subclasses
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof Team)) // use instanceof instead of getClass, due to hibernate creating proxys of subclasses that are lazy-loaded
			return false;
		Team other = (Team) obj;
		if (teamId == null) {
			if (other.getTeamId() != null) // remember to change to getter at other, simple property may result always null
				return false;
		} else if (!teamId.equals(other.getTeamId())) // remember to change to getter at other, simple property may result always null
			return false;
		return true;
	}

	public Long getTeamId() {
		return this.teamId;
	}

	public void setTeamId(Long teamId) {
		this.teamId = teamId;
	}

	public Short getFifaPoints() {
		return this.fifaPoints;
	}

	public void setFifaPoints(Short fifaPoints) {
		this.fifaPoints = fifaPoints;
	}

	public String getFlag() {
		return this.flag;
	}

	public void setFlag(String flag) {
		this.flag = flag;
	}

	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Long getWsId() {
		return this.wsId;
	}

	public void setWsId(Long wsId) {
		this.wsId = wsId;
	}

	public List<Group> getGroups1() {
		return this.groups1;
	}

	public void setGroups1(List<Group> groups1) {
		this.groups1 = groups1;
	}

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

	public List<Group> getGroups2() {
		return this.groups2;
	}

	public void setGroups2(List<Group> groups2) {
		this.groups2 = groups2;
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

	public List<Match> getMatches1() {
		return this.matches1;
	}

	public void setMatches1(List<Match> matches1) {
		this.matches1 = matches1;
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

	public List<Match> getMatches2() {
		return this.matches2;
	}

	public void setMatches2(List<Match> matches2) {
		this.matches2 = matches2;
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

	public Event getEvent() {
		return this.event;
	}

	public void setEvent(Event event) {
		this.event = event;
	}

	public Group getGroup() {
		return this.group;
	}

	public void setGroup(Group group) {
		this.group = group;
	}

//	public List<User> getUsers1() {
//		return this.users1;
//	}
//
//	public void setUsers1(List<User> users1) {
//		this.users1 = users1;
//	}
//
//	public User addUsers1(User users1) {
//		getUsers1().add(users1);
//		users1.setFavouriteGroupTeam(this);
//
//		return users1;
//	}
//
//	public User removeUsers1(User users1) {
//		getUsers1().remove(users1);
//		users1.setFavouriteGroupTeam(null);
//
//		return users1;
//	}
//
//	public List<User> getUsers2() {
//		return this.users2;
//	}
//
//	public void setUsers2(List<User> users2) {
//		this.users2 = users2;
//	}
//
//	public User addUsers2(User users2) {
//		getUsers2().add(users2);
//		users2.setFavouriteKnockoutTeam(this);
//
//		return users2;
//	}
//
//	public User removeUsers2(User users2) {
//		getUsers2().remove(users2);
//		users2.setFavouriteKnockoutTeam(null);
//
//		return users2;
//	}

}