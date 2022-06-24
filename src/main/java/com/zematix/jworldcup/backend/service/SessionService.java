package com.zematix.jworldcup.backend.service;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;

import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Service;
import org.springframework.web.context.annotation.SessionScope;

import com.google.common.collect.Streams;
import com.zematix.jworldcup.backend.emun.SessionDataModificationFlag;
import com.zematix.jworldcup.backend.entity.Chat;
import com.zematix.jworldcup.backend.entity.Event;
import com.zematix.jworldcup.backend.entity.User;
import com.zematix.jworldcup.backend.entity.UserGroup;
import com.zematix.jworldcup.backend.entity.UserOfEvent;
import com.zematix.jworldcup.backend.exception.ServiceException;
import com.zematix.jworldcup.backend.model.ParameterizedMessage;
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
	private EventService eventService;
	
	@Inject
	private ChatService chatService;

	@Inject
	private UserGroupService userGroupService;

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
		String message = ParameterizedMessage.create("header.label.welcome", user.getLoginName()).buildMessage(msgs, locale);
//		String message = msgs.getMessage("header.label.welcome", new String[]{user.getLoginName()}, locale); // same result
		
		// refresh event
		event = eventService.findLastEventByUserId(user.getUserId());
		
		// refresh userOfEvent
		this.getUserOfEvent();
		
		// refresh userGroups
		this.getUserGroups();

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
		}

		logger.info("newsLineChatMessage {}", message);
		this.setNewsLine(message);
	}

	/**
	 * Merges given sessionDataClient into this instance and returns the latter one wrapped into SessionData.
	 * 
	 * @param sessionDataClient - sessionData comes from client
	 * @param localUpdateMap - some this instance properties can be updated from the caller
	 * @return merged instance
	 */
	public SessionData refreshSessionData(SessionData sessionDataClient, Map<String, Object> localUpdateMap) {
		SessionData sessionData = new SessionData(id);

		sessionData.setAppShortName(getAppShortName());
		sessionData.setAppVersionNumber(getAppVersionNumber());
		sessionData.setAppVersionDate(getAppVersionDate());
		sessionData.setAppEmailAddr(getAppEmailAddr());
		
		// actualDateTime always comes from local
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

		// user always comes from local
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
		else { // check users' online flag modifications
			List<String> from = sessionDataClient.getUserGroups().stream().flatMap(ug -> ug.getUsers().stream())
					.distinct().map(u -> u.getLoginName() + u.getIsOnline()).toList();
			List<String> to = sessionData.getUserGroups().stream().flatMap(ug -> ug.getUsers().stream()).distinct()
					.map(u -> u.getLoginName() + u.getIsOnline()).toList();
			if (!from.equals(to)) {
				sessionData.getModificationSet().add(SessionDataModificationFlag.USER_GROUPS);
			}
		}
		
		// eventCompletionPercent comes from local
		sessionData.setEventCompletionPercent(getEventCompletionPercent());
		if (sessionDataClient == null || !sessionData.getEventCompletionPercent().equals(sessionDataClient.getEventCompletionPercent())) {
			sessionData.getModificationSet().add(SessionDataModificationFlag.EVENT_COMPLETION_PERCENT);
		}
		
		// completedEventIds comes from local
		sessionData.setCompletedEventIds(getCompletedEventIds());
		if (sessionDataClient == null || !sessionData.getCompletedEventIds().equals(sessionDataClient.getCompletedEventIds())) {
			sessionData.getModificationSet().add(SessionDataModificationFlag.COMPLETED_EVENT_IDS);
		}
		
		// eventTriggerStartTimes comes from local
		sessionData.setEventTriggerStartTimes(getCachedRetrieveMatchResultsJobTriggerStartTimes());
		if (sessionDataClient == null || !sessionData.getEventTriggerStartTimes().equals(sessionDataClient.getEventTriggerStartTimes())) {
			sessionData.getModificationSet().add(SessionDataModificationFlag.EVENT_TRIGGER_START_TIMES);
		}
		
		// newsLine comes from local
		if (localUpdateMap.get("newsLine") != null) {
			setNewsLine((String)localUpdateMap.get("newsLine"));
		}
		sessionData.setNewsLine(getNewsLine());
		if (sessionDataClient == null || !sessionData.getNewsLine().equals(sessionDataClient.getNewsLine())) {
			sessionData.getModificationSet().add(SessionDataModificationFlag.NEWS_LINE);
		}
		
		return sessionData;
	}
	
	public SessionData refreshSessionData(SessionData sessionDataClient) {
		return refreshSessionData(sessionDataClient, new HashMap<>());
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
	 * Retrieves cached {@link UserOfEvent} instance. Unless it is found,
	 * it is read from database. Creates an empty instance if it is 
	 * not in the database either.
	 * 
	 * @return cached/database/empty userOfEvent
	 */
	public UserOfEvent getUserOfEvent() {
		if (this.event == null || this.user == null) {
			this.userOfEvent = null;
		}
		else {
			if (this.userOfEvent == null 
					|| (this.userOfEvent.getEvent().getEventId() != this.event.getEventId() || this.userOfEvent.getUser().getUserId() != this.user.getUserId())) {
				
				UserOfEvent userOfEvent = null;
				try {
					userOfEvent = userService.retrieveUserOfEvent(this.event.getEventId(), this.user.getUserId()); // cached method
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
	 * Retrieves cached list of {@link UserGroup} instances. Unless it is found,
	 * it is read from database. Creates an empty instance if it is 
	 * not in the database either.
	 * 
	 * @return cached/database/empty userGroups
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
	 * Updates {@link ChatMessage#newsLine with the sent chat message from push object} 
	 */
	public void updateNewsLine() { // TODO
//		Map<String, String> params = getExternalContext().getRequestParameterMap();
//		ChatMessage chatMessage = new ChatMessage(params.get("message"), params.get("user"),
//				params.get("userGroup"), Long.valueOf(params.get("userGroupId")));
//		String message = String.format("[%s -> %s] %s", chatMessage.getUser(), chatMessage.getUserGroup(),
//				chatMessage.getMessage());
//		logger.info("newsLineChatMessage {}", message);
//		this.setNewsLine(message);
	}
	
	/**
	 * Generates newsLine message from this session instance.
	 */
	public String generateNewsLine() {
		String result = ParameterizedMessage.create("header.label.welcome", user.getLoginName()).buildMessage(msgs, locale);
		if (isEventFinished()) {
			Event nextEvent = eventService.findNextEvent();
			if (nextEvent == null) {
				result = ParameterizedMessage.create("newsLine.noNextEvent", user.getLoginName()).buildMessage(msgs, locale);
			}
			else {
				long days = getActualDateTime().until(nextEvent.getStartTime(), ChronoUnit.DAYS);
				result = ParameterizedMessage.create("newsLine.nextEvent" + (days < 2 ? "1" : ""), days, nextEvent.getShortDescWithYear()).buildMessage(msgs, locale);
			}
		}
		return result;
	}
}
