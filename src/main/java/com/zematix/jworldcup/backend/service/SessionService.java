package com.zematix.jworldcup.backend.service;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.UUID;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;

import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;
import org.springframework.lang.NonNull;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.context.annotation.SessionScope;

import com.zematix.jworldcup.backend.emun.SessionDataModificationFlag;
import com.zematix.jworldcup.backend.emun.SessionDataOperationFlag;
import com.zematix.jworldcup.backend.entity.Chat;
import com.zematix.jworldcup.backend.entity.Event;
import com.zematix.jworldcup.backend.entity.Match;
import com.zematix.jworldcup.backend.entity.User;
import com.zematix.jworldcup.backend.entity.UserGroup;
import com.zematix.jworldcup.backend.entity.UserOfEvent;
import com.zematix.jworldcup.backend.exception.ServiceException;
import com.zematix.jworldcup.backend.model.HeaderMessage;
import com.zematix.jworldcup.backend.model.HeaderMessageList;
import com.zematix.jworldcup.backend.model.ParameterizedMessage;
import com.zematix.jworldcup.backend.model.PublishedEvent;
import com.zematix.jworldcup.backend.model.SessionData;
import com.zematix.jworldcup.backend.model.UserCertificate;

/**
 * Helper class to get values related to the logged in session.
 * Session (instead of application) scoped because its {@link Locale},
 * {@link Event} and {@link User} fields in fact belongs to the session, not the
 * application itself.
 */
@SessionScope
@Service
@Configuration
public class SessionService extends ServiceBase {
	private final String id = UUID.randomUUID().toString();
	
	@Inject
	private ApplicationService applicationService;

	@Inject
	private JwtUserDetailsService userDetailsService;

	@Inject
	private UserService userService;

	@Inject
	private UserOfEventService userOfEventService;

	@Inject
	private EventService eventService;
	
	@Inject
	private ChatService chatService;

	@Inject
	private UserGroupService userGroupService;
	
	@Inject
	private TeamService teamService;
	
	@Inject
	private MessageSource msgs;

	/**
	 * {@Link Local} object used in the application
	 */
	private Locale locale;
	
	/**
	 * {@link Event} object used in the application
	 */
	private Event event;
	
	/**
	 * {@User} object logged in inside the application
	 */
	private User user;
	
	/**
	 * {@UserOfEvent} object belongs to the logged in {@link ApplicationBean#user}
	 */
	private UserOfEvent userOfEvent;
	
	/**
	 * Content of the news line scrolling left in top panel
	 */
	private String newsLine;

	/**
	 * Stored separately because at logout/scheduler, without authentication, it must be used.
	 */
	private String username;

	/**
	 * List of userGroups belongs to this.event and this.user
	 */
	private List<UserGroup> userGroups = new ArrayList<>();

	/**
	 * List of news messages to be displayed on UI header as newsLine.
	 */
//	private Queue<String> headerMessages = new LinkedList<>();
	private HeaderMessageList headerMessages = new HeaderMessageList();
	
	/**
	 * Initialization of some private fields
	 */
	@PostConstruct
	public void initSession() {
		logger.info("SessionService.postConstruct");
		
		locale = Locale.getDefault();
//		event = eventService.findLastEvent();
		// user cannot be initialized here, see getUser cached method
		
//		// store local id into session
//		HttpSession session = WebContextHolder.get().getSession();
//		session.setAttribute("sessionServiceId", id);
	}

	@PreDestroy
	public void destroySession() {
		logger.info("SessionService.preDestroy");
	}
	
	private void initSessionAfterUserInitialized() {
		// refresh event
		event = eventService.findLastEventByUserId(user.getUserId());
		
		// refresh userOfEvent
		this.getUserOfEvent();
		
		// refresh userGroups
		this.getUserGroups();

		this.initHeaderMessages();
	}

	/**
	 * After an user logged in, start header messages are generated.
	 */
	private void initHeaderMessages() {
		this.headerMessages.clear();
		
		String message = ParameterizedMessage.create("header.label.welcome", user.getLoginName()).buildMessage(msgs, locale);
//		String message = msgs.getMessage("header.label.welcome", new String[]{user.getLoginName()}, locale); // same result
		HeaderMessage headerMessage = HeaderMessage.builder().message(message).priority(1).creationTime(getActualDateTime()).build();
		this.headerMessages.push(headerMessage);

		// initialize newsLine
		Chat chat = null;
		try {
			chat = chatService.retrieveLatestChat(event.getEventId(), user.getUserId());
		} catch (ServiceException e) {
			logger.error("Problem retrieving latest chat message for event #{} of user #{}!", event.getEventId(),
					user.getUserId(), e);
		}
		if (chat != null) {
			String userGroupName = chat.getUserGroup().isEverybody() ? /*msgs.getString("userGroups.name.Everybody")*/ UserGroup.EVERYBODY_NAME: chat.getUserGroup().getName();
			message = String.format("[%s -> %s] %s", chat.getUser().getLoginName(), 
					userGroupName, chat.getMessage());
			headerMessage = HeaderMessage.builder().message(message).priority(5).creationTime(getActualDateTime()).build();
			this.headerMessages.push(headerMessage);
		}
		
		if (isEventFinished()) {
			Event nextEvent = eventService.findNextEvent();
			if (nextEvent == null) {
				message = ParameterizedMessage.create("newsLine.noNextEvent", user.getLoginName()).buildMessage(msgs, locale);
			}
			else {
				long days = getActualDateTime().until(nextEvent.getStartTime(), ChronoUnit.DAYS);
				message = ParameterizedMessage.create("newsLine.nextEvent" + (days < 2 ? "1" : ""), days, nextEvent.getShortDescWithYear()).buildMessage(msgs, locale);
			}
			headerMessage = HeaderMessage.builder().message(message).priority(3).creationTime(getActualDateTime()).build();
			this.headerMessages.push(headerMessage);
		}
	}
	
	/**
	 * Header messages are generated for the logged in user.
	 */
	public void generateHeaderMessages() {
		HeaderMessage headerMessage;
		if (applicationService.getActualDateTime().isBefore(this.event.getStartTime()) && this.userOfEvent.getFavouriteGroupTeam() == null) {
			String message = ParameterizedMessage.create("header.label.missing_group_favourite_team", this.event.getShortDescWithYear()).buildMessage(msgs, locale);
			headerMessage = HeaderMessage.builder().message(message).priority(2).creationTime(getActualDateTime()).build();
			this.headerMessages.push(headerMessage);
		}
		if (applicationService.getActualDateTime().isBefore(this.event.getKnockoutStartTime()) && this.userOfEvent.getFavouriteKnockoutTeam() == null &&
				!teamService.retrieveFavouriteKnockoutTeams(this.event.getEventId()).isEmpty()) {
			String message = ParameterizedMessage.create("header.label.missing_knockout_favourite_team", this.event.getShortDescWithYear()).buildMessage(msgs, locale);
			headerMessage = HeaderMessage.builder().message(message).priority(2).creationTime(getActualDateTime()).build();
			this.headerMessages.push(headerMessage);
		}
	}
	
	/**
	 * Merges given sessionDataClient into this instance and returns the latter one wrapped into SessionData.
	 * 
	 * @param sessionDataClient - sessionData comes from client
	 * @return merged instance
	 */
	public SessionData refreshSessionData(SessionData sessionDataClient) {
		SessionData sessionData = new SessionData(id);
		if (sessionDataClient != null && sessionDataClient.getOperationFlag() != null) {
			sessionData.setOperationFlag(sessionDataClient.getOperationFlag());
		}

		sessionData.setAppShortName(getAppShortName());
		sessionData.setAppVersionNumber(getAppVersionNumber());
		sessionData.setAppVersionDate(getAppVersionDate());
		sessionData.setAppEmailAddr(getAppEmailAddr());
		
		// actualDateTime always comes from local/server
		sessionData.setActualDateTime(getActualDateTime());
		if (sessionDataClient == null || !sessionData.getActualDateTime().equals(sessionDataClient.getActualDateTime())) {
			sessionData.getModificationSet().add(SessionDataModificationFlag.ACTUAL_DATE_TIME);
		}
		
		// locale normally comes from client
		if (sessionDataClient == null || !getLocale().equals(sessionDataClient.getLocale())) {
			sessionData.getModificationSet().add(SessionDataModificationFlag.LOCALE);
		}
		if (sessionDataClient != null && sessionDataClient.getLocale() != null) {
			setLocale(sessionDataClient.getLocale());
		}
		sessionData.setLocale(getLocale());

		// user always comes from local/server
		sessionData.setUser(getUser());
		if (sessionDataClient == null || !sessionData.getUser().equals(sessionDataClient.getUser())) {
			sessionData.getModificationSet().add(SessionDataModificationFlag.USER);
		}
		
		// event normally comes from client
		if (sessionDataClient == null || !getEvent().equals(sessionDataClient.getEvent())) {
			sessionData.getModificationSet().add(SessionDataModificationFlag.EVENT);
		}
		if (sessionDataClient != null && sessionDataClient.getEvent() != null) {
			setEvent(sessionDataClient.getEvent());
		}
		sessionData.setEvent(getEvent());
		
		sessionData.setUserOfEvent(getUserOfEvent()); // UserOfEventDto has no user and event fields inside
//		if (sessionDataClient == null || !sessionData.getUserOfEvent().equals(sessionDataClient.getUserOfEvent())) {
//			sessionData.getModificationSet().add(SessionDataModificationFlag.USER_OF_EVENT);
//		}
		
		sessionData.setUserGroups(getUserGroups());
		if (sessionDataClient == null || !sessionData.getUserGroups().equals(sessionDataClient.getUserGroups())) {
			sessionData.getModificationSet().add(SessionDataModificationFlag.USER_GROUPS);
		}
		else { // check users' active flag modifications
			List<String> from = sessionDataClient.getUserGroups().stream().flatMap(ug -> ug.getUsers().stream())
					.distinct().map(u -> u.getLoginName() + u.getIsActive()).toList();
			List<String> to = sessionData.getUserGroups().stream().flatMap(ug -> ug.getUsers().stream()).distinct()
					.map(u -> u.getLoginName() + u.getIsActive()).toList();
			if (!from.equals(to)) {
				sessionData.getModificationSet().add(SessionDataModificationFlag.USER_GROUPS);
			}
		}
		
		// eventCompletionPercent comes from local/server
		sessionData.setEventCompletionPercent(getEventCompletionPercent());
		if (sessionDataClient == null || !sessionData.getEventCompletionPercent().equals(sessionDataClient.getEventCompletionPercent())) {
			sessionData.getModificationSet().add(SessionDataModificationFlag.EVENT_COMPLETION_PERCENT);
		}
		
		// completedEventIds comes from local/server
		sessionData.setCompletedEventIds(getCompletedEventIds());
		if (sessionDataClient == null || !sessionData.getCompletedEventIds().equals(sessionDataClient.getCompletedEventIds())) {
			sessionData.getModificationSet().add(SessionDataModificationFlag.COMPLETED_EVENT_IDS);
		}
		
		// eventTriggerStartTimes comes from local/server
		sessionData.setEventTriggerStartTimes(getCachedRetrieveMatchResultsJobTriggerStartTimes());
		if (sessionDataClient == null || !sessionData.getEventTriggerStartTimes().equals(sessionDataClient.getEventTriggerStartTimes())) {
			sessionData.getModificationSet().add(SessionDataModificationFlag.EVENT_TRIGGER_START_TIMES);
		}
		
		// newsLine comes from server, in fact it is written here
		if (sessionData.getOperationFlag() == SessionDataOperationFlag.SERVER) {
			HeaderMessage headerMessage = getHeaderMessages().pop();
			setNewsLine(headerMessage != null ? headerMessage.getMessage() : null);
		}
		sessionData.setNewsLine(getNewsLine());
		if (sessionDataClient == null || (sessionData.getNewsLine() != null && !sessionData.getNewsLine().equals(sessionDataClient.getNewsLine()))) {
			sessionData.getModificationSet().add(SessionDataModificationFlag.NEWS_LINE);
		}
		
		return sessionData;
	}
	
	/**
	 * @return session id
	 */
	public String getId() {
		return this.id;
	}
	
	/**
	 * @return application short name
	 */
	public String getAppShortName() {
		return applicationService.getAppShortName();
	}

	/**
	 * @return the application version number as {@link String}
	 */
	public String getAppVersionNumber() {
		return applicationService.getAppVersionNumber();
	}

	/**
	 * @return the application version date
	 */
	public LocalDate getAppVersionDate() {
		return applicationService.getAppVersionDate();
	}

	/**
	 * Returns the actual datetime. However if cheat datetime is set in configuration, 
	 * that value is returned.
	 * 
	 * @return cheat datetime if exists, otherwise the actual datetime
	 */
	public LocalDateTime getActualDateTime() {
		return applicationService.getActualDateTime();
	}
	
	/**
	 * For test purpose a cheat datetime can be set in configuration.
	 * 
	 * @return application cheat date time or {@code null} unless exists
	 */
	public LocalDateTime getAppCheatDateTime() {
		return applicationService.getAppCheatDateTime();
	}

	/**
	 * Returns the contact email address of the application.
	 * 
	 * @return contact email address of the application
	 */
	public String getAppEmailAddr() {
		return applicationService.getAppEmailAddr();
	}
	
	/**
	 * Return the actual {@link Locale} used in the application.
	 * 
	 * @return actual {@link Locale} object
	 */
	public Locale getLocale() {
		return locale;
	}

	public void setLocale(Locale locale) {
		this.locale = locale;
	}
	
	public String getLanguage() {
		return locale.getLanguage();
	}

	public void setLanguage(String language) {
		locale = new Locale(language);
	}

	/**
	 * Return the actual {@link Event} used in the application.
	 * 
	 * @return actual {@Event} object
	 */
	public Event getEvent() {
		return event;
	}

	public void setEvent(Event event) {
		this.event = event;
	}
	
	/**
	 * Retrieves authenticated {@link User} instance.
	 * 
	 * @return authenticated user
	 */
	public User getUser() {
		if (this.user != null) {
			return this.user;
		}

		var authenticatedUser = userDetailsService.getAuthenticatedUser();
		String loginName = authenticatedUser != null ? authenticatedUser.getUsername() : this.username;
		if (loginName == null) {
			this.user = null;
			this.username = null;
		}
		else {
			var user = userService.findUserByLoginName(loginName); // cached method
			if (user == null) {
				// authenticated user must exist in the database, so this is supposed to be a dead code
				throw new IllegalStateException(String.format("User with loginName \"%s\" is not found in the database.", loginName));
			}
			user.setLoginTime(applicationService.getActualDateTime());
			this.user = user;
			this.username = loginName;
			if (authenticatedUser != null) {
				initSessionAfterUserInitialized();
			}
		}	
		return this.user;
	}

	public void setUser(User user) {
		this.user = user;
	}
	
	/**
	 * Retrieves {@link UserOfEvent} instance from database. Creates an empty 
	 * instance if it is not found the database.
	 * 
	 * @return database/empty userOfEvent
	 */
	public UserOfEvent getUserOfEvent() {
		if (this.event == null || this.user == null) {
			this.userOfEvent = null;
		}
		else {
			UserOfEvent userOfEvent = null;
			try {
				userOfEvent = userOfEventService.retrieveUserOfEvent(this.event.getEventId(), this.user.getUserId()); // cached method
			} catch (ServiceException e) {
				consumeServiceException(e);
				throw new IllegalStateException(e.getMessage()); // fatal case 
			}
			if (userOfEvent == null) {
				// create empty UserOfEvent element to use as cached value
				userOfEvent = new UserOfEvent();
				userOfEvent.setEvent(this.event);
				userOfEvent.setUser(this.user);
			}
			this.userOfEvent = userOfEvent;
		}
		return this.userOfEvent;
	}

	public void setUserOfEvent(UserOfEvent userOfEvent) {
		this.userOfEvent = userOfEvent;
	}

	public String getNewsLine() {
		return newsLine;
	}

	public void setNewsLine(String newsLine) {
		this.newsLine = newsLine;
	}

	public String getUsername() {
		return this.username;
	}

	/**
	 * Retrieves a list of {@link UserGroup} instances. Creates an empty instance if it is 
	 * not found in the database.
	 * 
	 * @return database/empty userGroups
	 */
	public List<UserGroup> getUserGroups() {
		if (this.event == null || this.user == null) {
			this.userGroups = new ArrayList<>();
		}
		else {
			List<UserGroup> userGroups = new ArrayList<>();
			try {
				userGroups = userGroupService.retrieveUserGroups(this.event.getEventId(), this.user.getUserId(), true); // cached method
				userGroups.forEach(userGroup -> {
					userGroup.getUsers().forEach(user -> {
						user.setIsOnline(applicationService.getAllAuthenticatedPrincipals().stream()
								.anyMatch(principal -> principal.getUsername().equals(user.getLoginName())));
						user.setIsActive(Boolean.TRUE.equals(user.getIsOnline()) && applicationService
								.getLastAppearancebyUserCache().getIfPresent(user.getLoginName()) != null);
					});
				});
			} catch (ServiceException e) {
				consumeServiceException(e);
				throw new IllegalStateException(e.getMessage()); // fatal case 
			}
			this.userGroups = userGroups;
		}

		return this.userGroups;
	}
	
	public HeaderMessageList getHeaderMessages() {
		return this.headerMessages;
	}
	
	/**
	 * Retrieves the {@link SimpleDateFormat#SHORT} date format belongs to the locale.
	 * For example locale named "en" returns "mm/dd/yy"
	 * 
	 * @return {@link SimpleDateFormat#SHORT} date format as {@link String} belongs to the locale
	 */
	public SimpleDateFormat shortDateFormat() {
		SimpleDateFormat dateFormat = (SimpleDateFormat) SimpleDateFormat
			 .getDateInstance(SimpleDateFormat.SHORT, locale);
		dateFormat.setTimeZone(TimeZone.getTimeZone(getUser().getZoneId()));
		return dateFormat;
	}
	
	/**
	 * Retrieves {@link ApplicationBean#shortDateFormat() date format as pattern 
	 * 
	 * @return shortDateFormat as pattern
	 */
	public String shortDatePattern() {
		return shortDateFormat().toPattern();
	}
	
	/**
	 * Retrieves the {@link SimpleDateFormat#SHORT} date format 
	 * belongs to the locale. For example locale named "en" returns "mm/dd/yy hh:mm XM"
	 * 
	 * @return {@link SimpleDateFormat#SHORT} date format as {@link String} belongs to the locale
	 */
	public SimpleDateFormat shortDateTimeFormat() {
		SimpleDateFormat dateFormat = (SimpleDateFormat) SimpleDateFormat
			 .getDateTimeInstance(SimpleDateFormat.SHORT, SimpleDateFormat.SHORT, locale);
		dateFormat.setTimeZone(TimeZone.getTimeZone(getUser().getZoneId()));
		return dateFormat;
	}
	
	/**
	 * Retrieves {@link ApplicationBean#shortDateTimeFormat() date format as pattern 
	 * 
	 * @return shortDateTimeFormat as pattern
	 */
	public String shortDateTimePattern() {
		return shortDateTimeFormat().toPattern();
	}
	
	/**
	 * Retrieves the {@link SimpleDateFormat#MEDIUM} date format belongs to the locale.
	 * For example locale named "en" returns "MMM d, yyyy"
	 * 
	 * @return {@link SimpleDateFormat#MEDIUM} date format as {@link String} belongs to the locale
	 */
	public SimpleDateFormat mediumDateFormat() {
		SimpleDateFormat dateFormat = (SimpleDateFormat) SimpleDateFormat
			 .getDateInstance(SimpleDateFormat.MEDIUM, locale);
		dateFormat.setTimeZone(TimeZone.getTimeZone(getUser().getZoneId()));
		return dateFormat;
	}
	
	/**
	 * Retrieves {@link ApplicationBean#mediumDateFormat() date format as pattern 
	 * 
	 * @return mediumDateFormat as pattern
	 */
	public String mediumDatePattern() {
		return mediumDateFormat().toPattern();
	}
	
	/**
	 * Retrieves event completion which is a percent value based on the finished/completed matches.
	 * 
	 * @return event completion in percent
	 */
	public Integer getEventCompletionPercent() {
		Integer percent = null;

		if (this.event != null) {
			percent = applicationService.getEventCompletionPercentCache(this.event.getEventId());
		}
		
		return percent;
	}
	
	/**
	 * Renders rgb color to the value stored in the actual {@link Event#completionPercent},
	 * where 0 means rgb(0,255,0) and 100 means (255,0,0). Transition goes from green to red.
	 * 
	 * @return rgb color rendered to actual event completion
	 */
	public String getEventCompletionPercentClassRGBColor() {
		String rgbColor = null;

		Integer percent = getEventCompletionPercent();
		if (percent  != null) {
			int red = (int) Math.round(percent*2.55);
			int green = (int) Math.round((100-percent)*2.55);
			int blue = 0;
		
			rgbColor = String.format("rgb(%d,%d,%d)", red, green, blue);
		}
		
		return rgbColor;
	}
	
	/**
	 * Renders class name to the value stored in the actual {@link Event#completionPercent}
	 * 
	 * @return class name rendered to actual event completion
	 */
	public String getEventCompletionPercentClass() {
		String clazz = "";

		Integer percent = getEventCompletionPercent();
		if (percent != null) {
			if (percent<75) {
				clazz = "lightgreenBar";
			}
			else if (percent>=75 && percent<100) {
				clazz = "greenBar";
			}
			else {
				clazz = "redBar";
			}
		}
		
		return clazz;
	}

	/**
	 * Returns {@code true} if the actual event is finished. It means that all of its matches
	 * are finished/completed. Otherwise {@code false} is returned.
	 *  
	 * @return true if the actual event is finished
	 */
	public boolean isEventFinished() {
		Integer percent = getEventCompletionPercent();
		return percent != null && percent == 100;
	}

	/**
	 * Returns cached list containing topUsers.
	 * @return
	 */
	public List<UserCertificate> getCachedTopUsers() {
		return applicationService.getTopUsersCache();
	}

	/**
	 * Returns cached list containing chats belongs to given userGroup.
	 * @return
	 */
	public List<Chat> getChatsByUserGroupCache(UserGroup userGroup) {
		return applicationService.getChatsByUserGroupCache().getUnchecked(userGroup);
	}

	/**
	 * Refreshes chatsByUserGroupCache belongs to the given userGroup.
	 * @param userGroup
	 */
	public void refreshChatsByUserGroupCache(UserGroup userGroup) {
		applicationService.refreshChatsByUserGroupCache(userGroup);
	}

	
	/**
	 * Retrieves eventId values of completed events.
	 * @return eventId list of all completed Event entities
	 */
	public List<Long> getCompletedEventIds() {
		return applicationService.getCompletedEventIds();
	}

	/**
	 * Returns cached list containing scheduled retrieval match result job trigger start times
	 * belongs to the actual event.
	 * @return list containing scheduled retrieval match result job trigger start times of actual event
	 */
	public List<LocalDateTime> getCachedRetrieveMatchResultsJobTriggerStartTimes() {
		var result = applicationService.getRetrieveMatchResultsJobTriggerStartTimesCache().getIfPresent(this.event.getEventId());
		return result != null ? result : new ArrayList<>(0); 
	}

	/**
	 * Invoked from {@link MatchService#saveMatch(Long, boolean, Boolean, LocalDateTime, Byte, Byte, Byte, Byte, Byte, Byte)
	 * when a match result is saved.
	 * @param event - contains the saved match
	 */
	@Async
	@EventListener(condition = "#event.success")
	public void onUpdateMatchEvent(@NonNull PublishedEvent/*<Match>*/ event) {
		if (!event.getEntity().getClass().equals(Match.class)) {
			return;
		}
		
		Match match = (Match)event.getEntity();
		logger.info("onUpdateMatchEvent matchId: {}", match.getMatchId());

		String teamName1 = ParameterizedMessage.create("team."+match.getTeam1().getName()).buildMessage(msgs, locale);
		String teamName2 = ParameterizedMessage.create("team."+match.getTeam2().getName()).buildMessage(msgs, locale);
		String message = ParameterizedMessage.create("header.label.match_result", teamName1, teamName2,
				match.getGoalNormalByTeam1(), match.getGoalNormalByTeam2()).buildMessage(msgs, locale);
		HeaderMessage headerMessage = HeaderMessage.builder().message(message).priority(5).creationTime(getActualDateTime()).build();
		this.headerMessages.push(headerMessage);
	}

	/**
	 * Invoked from {@link MatchService#saveMatch(Long, boolean, Boolean, LocalDateTime, Byte, Byte, Byte, Byte, Byte, Byte)
	 * when a match result is saved.
	 * @param event - contains the saved match
	 */
	@Async
	@EventListener(condition = "#event.success")
	public void onSendPrivateChat(@NonNull PublishedEvent/*<Chat>*/ event) {
		if (!event.getEntity().getClass().equals(Chat.class)) {
			return;
		}
		
		Chat chat = (Chat)event.getEntity();
		logger.info("onSendPrivateChat chatId: {}", chat.getChatId());

		String message = ParameterizedMessage.create("header.label.private_chat", chat.getUser().getLoginName())
				.buildMessage(msgs, locale);
		HeaderMessage headerMessage = HeaderMessage.builder().message(message).priority(2).creationTime(getActualDateTime()).build();
		this.headerMessages.push(headerMessage);
	}
}
