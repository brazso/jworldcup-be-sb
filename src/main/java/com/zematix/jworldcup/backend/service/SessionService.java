package com.zematix.jworldcup.backend.service;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.UUID;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Service;
import org.springframework.web.context.annotation.SessionScope;

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
	 * Initialization of some private fields
	 */
	@PostConstruct
	public void initApplicationSession() {
		locale = Locale.getDefault();
//		event = eventService.findLastEvent();
		// user cannot be initialized here, see getUser cached method
	}

	private void initApplicationSessionAfterUserInitialized() {
		String message = ParameterizedMessage.create("header.label.welcome", user.getLoginName()).buildMessage(msgs, locale);
//		String message = msgs.getMessage("header.label.welcome", new String[]{user.getLoginName()}, locale); // same result
		
		// refresh event
		event = eventService.findLastEventByUserId(user.getUserId());
		
		// refresh userOfEvent
		this.getUserOfEvent();

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

	public SessionData refreshSessionData(SessionData sessionData) {
		if (sessionData == null) {
			sessionData = new SessionData(id);
		}
		sessionData.setId(id);
		sessionData.setAppShortName(getAppShortName());
		sessionData.setAppVersionNumber(getAppVersionNumber());
		sessionData.setAppVersionDate(getAppVersionDate());
		sessionData.setActualDateTime(getActualDateTime());
		sessionData.setAppEmailAddr(getAppEmailAddr());
		
		// locale normally comes from client (input)
		if (sessionData.getLocale() != null) {
			setLocale(sessionData.getLocale());
		}
		else { // but if missing it comes from local 
			sessionData.setLocale(getLocale());
		}

		// user may come only from local
		sessionData.setUser(getUser());
		
		// event normally comes from client (input)
		if (sessionData.getEvent() != null) {
			setEvent(sessionData.getEvent());
		}
		else { // but if missing it comes from local 
			sessionData.setEvent(getEvent());
		}
		
		sessionData.setUserOfEvent(getUserOfEvent());
		
		sessionData.setEventCompletionPercent(getEventCompletionPercent());
		sessionData.setCompletedEventIds(getCompletedEventIds());
		sessionData.setEventTriggerStartTimes(getCachedRetrieveMatchResultsJobTriggerStartTimes());
		sessionData.setNewsLine(getNewsLine());
		
		return sessionData;
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
	 * Retrieves cached {@link User} instance. Unless it is found,
	 * it is read from database.
	 * 
	 * @return cached/database user
	 */
	public User getUser() {
		var authenticatedUser = userDetailsService.getAuthenticatedUser();
		if (authenticatedUser == null) {
			this.user = null;
		}
		else {
			String loginName = authenticatedUser.getUsername();
			if (this.user == null || !loginName.equals(this.user.getLoginName())) {
				var user = userService.findUserByLoginName(authenticatedUser.getUsername()); // user.getRoles() also fetched
				if (user == null) {
					// authenticated user must be in the database, so this is supposed to be a dead code
					throw new IllegalStateException(String.format("User with loginName \"%s\" is not found in the database.", loginName));
				}
				this.user = user;
				initApplicationSessionAfterUserInitialized();
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
					userOfEvent = userService.retrieveUserOfEvent(this.event.getEventId(), this.user.getUserId());
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
		return applicationService.getRetrieveMatchResultsJobTriggerStartTimesCache().getIfPresent(this.event.getEventId());
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
}
