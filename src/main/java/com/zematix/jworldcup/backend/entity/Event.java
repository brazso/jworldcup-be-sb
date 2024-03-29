package com.zematix.jworldcup.backend.entity;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;


/**
 * The persistent class for the event database table.
 * 
 */
@Entity
@Table(name="event", uniqueConstraints=@UniqueConstraint(columnNames={"year", "short_desc"}))
@NamedQuery(name="Event.findAll", query="SELECT e FROM Event e")
@Getter @Setter @EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Event implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	@Column(name="event_id", unique=true, nullable=false)
	@EqualsAndHashCode.Include
	private Long eventId;

	@Column(nullable=false, length=100)
	private String description;

	@Column(nullable=false, length=50)
	private String location;

	@Column(name="short_desc", nullable=false, length=10)
	private String shortDesc;

	@Column(nullable=false)
	private Short year;

	@Column(name="organizer", nullable=false, length=10)
	private String organizer;

	//bi-directional many-to-one association to Bet
	@OneToMany(mappedBy="event")
	private List<Bet> bets;

	//bi-directional many-to-one association to Group
	@OneToMany(mappedBy="event")
	private List<Group> groups;

	//bi-directional many-to-one association to Match
	@OneToMany(mappedBy="event")
	private List<Match> matches;

	//bi-directional many-to-one association to Round
	@OneToMany(mappedBy="event")
	private List<Round> rounds;

	//bi-directional many-to-one association to Team
	@OneToMany(mappedBy="event")
	private List<Team> teams;

	//bi-directional many-to-one association to WebService
	@OneToMany(mappedBy="event")
	private List<WebService> webServices;

	//bi-directional many-to-one association to UserOfEvent
	@OneToMany(mappedBy="event")
	private List<UserOfEvent> userOfEvents;

	//bi-directional many-to-one association to UserOfEvent
	@OneToMany(mappedBy="event")
	private List<UserGroup> userGroups = new ArrayList<>();
	
	//bi-directional many-to-one association to Chat
	@OneToMany(mappedBy="event")
	private List<Chat> chats;
	
	/**
	 * Start datetime of the first (group) match belongs to the event.
	 */
	@Transient
	private LocalDateTime startTime;

	/**
	 * Start datetime of the first knockout match belongs to the event.
	 */
	@Transient
	private LocalDateTime knockoutStartTime;

	/**
	 * Start datetime of the last match belongs to the event.
	 */
	@Transient
	private LocalDateTime endTime;

	public Bet addBet(Bet bet) {
		getBets().add(bet);
		bet.setEvent(this);

		return bet;
	}

	public Bet removeBet(Bet bet) {
		getBets().remove(bet);
		bet.setEvent(null);

		return bet;
	}

	public Group addGroup(Group group) {
		getGroups().add(group);
		group.setEvent(this);

		return group;
	}

	public Group removeGroup(Group group) {
		getGroups().remove(group);
		group.setEvent(null);

		return group;
	}

	public Match addMatch(Match match) {
		getMatches().add(match);
		match.setEvent(this);

		return match;
	}

	public Match removeMatch(Match match) {
		getMatches().remove(match);
		match.setEvent(null);

		return match;
	}

	public Round addRound(Round round) {
		getRounds().add(round);
		round.setEvent(this);

		return round;
	}

	public Round removeRound(Round round) {
		getRounds().remove(round);
		round.setEvent(null);

		return round;
	}

	public Team addTeam(Team team) {
		getTeams().add(team);
		team.setEvent(this);

		return team;
	}

	public Team removeTeam(Team team) {
		getTeams().remove(team);
		team.setEvent(null);

		return team;
	}

	public WebService addWebService(WebService webService) {
		getWebServices().add(webService);
		webService.setEvent(this);

		return webService;
	}

	public WebService removeWebService(WebService webService) {
		getWebServices().remove(webService);
		webService.setEvent(null);

		return webService;
	}

	public UserOfEvent addUserOfEvent(UserOfEvent userOfEvent) {
		getUserOfEvents().add(userOfEvent);
		userOfEvent.setEvent(this);

		return userOfEvent;
	}

	public UserOfEvent removeUserOfEvent(UserOfEvent userOfEvent) {
		getUserOfEvents().remove(userOfEvent);
		userOfEvent.setEvent(null);

		return userOfEvent;
	}

	public UserGroup addUserGroup(UserGroup userGroup) {
		getUserGroups().add(userGroup);
		userGroup.setEvent(this);

		return userGroup;
	}

	public UserGroup removeUserGroup(UserGroup userGroup) {
		getUserGroups().remove(userGroup);
		userGroup.setEvent(null);

		return userGroup;
	}

	public Chat addChat(Chat chat) {
		getChats().add(chat);
		chat.setEvent(this);

		return chat;
	}

	public Chat removeChat(Chat chat) {
		getChats().remove(chat);
		chat.setEvent(null);

		return chat;
	}

	// calculated getter fields

	/**
	 * Returns short event description, e.g. WC2014
	 * @return short event description
	 */
	public String getShortDescWithYear() {
		return getShortDesc() + getYear(); 
	}
}