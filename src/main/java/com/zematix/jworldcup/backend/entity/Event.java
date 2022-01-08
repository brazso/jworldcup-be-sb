package com.zematix.jworldcup.backend.entity;

import java.io.Serializable;
import java.time.LocalDateTime;
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


/**
 * The persistent class for the event database table.
 * 
 */
@Entity
@Table(name="event", uniqueConstraints=@UniqueConstraint(columnNames={"year", "short_desc"}))
@NamedQuery(name="Event.findAll", query="SELECT e FROM Event e")
public class Event implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	@Column(name="event_id", unique=true, nullable=false)
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
	private List<UserGroup> userGroups;
	
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

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((eventId == null) ? 0 : eventId.hashCode());
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
		if (!(obj instanceof Event)) // use instanceof instead of getClass, due to hibernate creating proxys of subclasses that are lazy-loaded
			return false;
		Event other = (Event) obj;
		if (eventId == null) {
			if (other.getEventId() != null) // remember to change to getter at other, simple property may result always null
				return false;
		} else if (!eventId.equals(other.getEventId())) // remember to change to getter at other, simple property may result always null
			return false;
		return true;
	}

	public Long getEventId() {
		return this.eventId;
	}

	public void setEventId(Long eventId) {
		this.eventId = eventId;
	}

	public String getDescription() {
		return this.description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getLocation() {
		return this.location;
	}

	public void setLocation(String location) {
		this.location = location;
	}

	public String getShortDesc() {
		return this.shortDesc;
	}

	public void setShortDesc(String shortDesc) {
		this.shortDesc = shortDesc;
	}

	public Short getYear() {
		return this.year;
	}

	public void setYear(Short year) {
		this.year = year;
	}

	public String getOrganizer() {
		return organizer;
	}

	public void setOrganizer(String organizer) {
		this.organizer = organizer;
	}

	public List<Bet> getBets() {
		return this.bets;
	}

	public void setBets(List<Bet> bets) {
		this.bets = bets;
	}

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

	public List<Group> getGroups() {
		return this.groups;
	}

	public void setGroups(List<Group> groups) {
		this.groups = groups;
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

	public List<Match> getMatches() {
		return this.matches;
	}

	public void setMatches(List<Match> matches) {
		this.matches = matches;
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

	public List<Round> getRounds() {
		return this.rounds;
	}

	public void setRounds(List<Round> rounds) {
		this.rounds = rounds;
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

	public List<Team> getTeams() {
		return this.teams;
	}

	public void setTeams(List<Team> teams) {
		this.teams = teams;
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

	public List<WebService> getWebServices() {
		return this.webServices;
	}

	public void setWebServices(List<WebService> webServices) {
		this.webServices = webServices;
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

	public List<UserOfEvent> getUserOfEvents() {
		return this.userOfEvents;
	}

	public void setUserOfEvents(List<UserOfEvent> userOfEvents) {
		this.userOfEvents = userOfEvents;
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

	public List<UserGroup> getUserGroups() {
		return this.userGroups;
	}

	public void setUserGroups(List<UserGroup> userGroups) {
		this.userGroups = userGroups;
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

	public List<Chat> getChats() {
		return chats;
	}

	public void setChats(List<Chat> chats) {
		this.chats = chats;
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

	/**
	 * @return the startTime
	 */
	public LocalDateTime getStartTime() {
		return startTime;
	}

	/**
	 * @param startTime the startTime to set
	 */
	public void setStartTime(LocalDateTime startTime) {
		this.startTime = startTime;
	}

	public LocalDateTime getKnockoutStartTime() {
		return knockoutStartTime;
	}

	public void setKnockoutStartTime(LocalDateTime knockoutStartTime) {
		this.knockoutStartTime = knockoutStartTime;
	}

	/**
	 * @return the endTime
	 */
	public LocalDateTime getEndTime() {
		return endTime;
	}

	/**
	 * @param endTime the endTime to set
	 */
	public void setEndTime(LocalDateTime endTime) {
		this.endTime = endTime;
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