package com.zematix.jworldcup.backend.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.session.SessionInformation;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.stereotype.Service;
import org.springframework.web.context.annotation.ApplicationScope;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.zematix.jworldcup.backend.entity.Chat;
import com.zematix.jworldcup.backend.entity.Event;
import com.zematix.jworldcup.backend.entity.UserGroup;
import com.zematix.jworldcup.backend.model.UserCertificate;

/**
 * Helper class to get values related to the application.
 * The base values mostly come from application property file.
 * It also contains some cached values. 
 */
@ApplicationScope
@Service
@Configuration
public class ApplicationService extends ServiceBase {
	@Inject
	private MatchService matchService;
	
	@Inject
	private EventService eventService;
	
	@Inject
	private UserGroupService userGroupService;

	@Inject
	private ChatService chatService;
	
	@Autowired
	SessionRegistry sessionRegistry;

	@Value("${app.shortName}")
	private String appShortName;

	@Value("${app.version.number}")
	private String appVersionNumber;

	@Value("${app.version.date}")
	private String appVersionDateString;

	@Value("${app.cheat.dateTime:#{null}}")
	private String appCheatDateTimeString;
	
	@Value("${app.emailAddr}")
	private String appEmailAddr;
	
	/**
	 * Cached map containing event completions in percent.
	 * Key is the {@link Event#eventId}, value is an integer between 0 and 100.
	 * Because of loader method may throw @{link ServiceException} no lambda function
	 * could be used despite {@link CacheLoader#from} supports it.
	 */
	private LoadingCache<Long, Integer> eventCompletionPercentCache = CacheBuilder.newBuilder()
			.build(new CacheLoader<Long, Integer>() {
				public Integer load(Long eventId) throws Exception {
					return matchService.retriveMatchesAccomplishedInPercent(eventId);
				}
			});

	/**
	 * Cached list containing topUsers.
	 * It may contain max 1 elements with {@link ApplicationService#appShortName} key value.
	 */
	private LoadingCache<String, List<UserCertificate>> topUsersCache = CacheBuilder.newBuilder()
			.maximumSize(1)
			.build(new CacheLoader<String, List<UserCertificate>>() {
				public List<UserCertificate> load(String unused) throws Exception {
					return userGroupService.retrieveTopUsers();
				}
			});
	
	/**
	 * Cached list of used trigger date values belongs to scheduled retrieveMatchResultsJob by eventId as key.
	 * Adding elements to the cache takes place manually.
	 */
	private LoadingCache<Long, List<LocalDateTime>> retrieveMatchResultsJobTriggerStartTimesCache = CacheBuilder.newBuilder()
			.build(new CacheLoader<Long, List<LocalDateTime>>() {
				public List<LocalDateTime> load(Long eventId) throws Exception {
					return new ArrayList<>(); // refresh makes the list empty
				}
			});
	
	/**
	 * Cached list of chat objects belongs to key userGroup. The latter can be 
	 * virtual one with UserGroup.EVERYBODY_USER_GROUP_ID ID value, where eventId 
	 * is a must, so only userGropId cannot be used as key.
	 */
	private LoadingCache<UserGroup, List<Chat>> chatsByUserGroupCache = CacheBuilder.newBuilder()
			.build(new CacheLoader<UserGroup, List<Chat>>() {
				public List<Chat> load(UserGroup userGroup) throws Exception {
					return chatService.retrieveChats(userGroup);
				}
			});
	
	/**
	 * Initialization of cached fields
	 */
	@PostConstruct
	public void initApplication() {
		logger.info("ApplicationService: init");;
		// initializes cachedEventCompletionPercentCache
		List<Event> events = eventService.findAllEvents();
		events.stream().forEach(e -> refreshEventCompletionPercentCache(e.getEventId()));
		
		// initializes topUsersCache - not used because the synchronously called loader function might be slow and does use applicationService (infinite loop)
		// so it is placed to the SchedulerService, see {@link ApplicationService#databaseMaintenanceJob()} method
//		try {
//			List<UserCertificate> topUsers = userGroupService.retrieveTopUsers();
//			topUsersCache.put(appShortName, topUsers);
//		} catch (ServiceException e) {
//			consumeServiceException(e);
//			throw new IllegalStateException(e.getMessage()); // fatal case 
//		}
		
		// initializes refreshRetrieveMatchResultsJobTriggerStartTimesCache
		events.stream().forEach(e -> refreshRetrieveMatchResultsJobTriggerStartTimesCache(e.getEventId()));
	}

	/**
	 * @return application short name 
	 */
	public String getAppShortName() {
		return appShortName;
	}

	/**
	 * @return application version number as {@link String} 
	 */
	public String getAppVersionNumber() {
		return appVersionNumber;
	}

	/**
	 * @return application version date from yyyy-MM-dd format 
	 */
	public LocalDate getAppVersionDate() {
		LocalDate appVersionDate = null; 
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
		try {
			appVersionDate = LocalDate.parse(appVersionDateString, formatter);
		} catch (DateTimeParseException e) {
			logger.error(String.format("Unsupported date value as app.version.date = %s in configuration file.", appVersionDateString));
		}
		return appVersionDate;
	}

	/**
	 * For test purpose a cheat datetime can be set in configuration.
	 * 
	 * @return application cheat date time or {@null} unless exists
	 */
	public LocalDateTime getAppCheatDateTime() {
		LocalDateTime appCheatDateTime = null; 
		if (appCheatDateTimeString != null) {
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

			try {
				appCheatDateTime = LocalDateTime.parse(appCheatDateTimeString, formatter);
			} catch (DateTimeParseException e) {
				logger.error(String.format("Unsupported date value as app.cheat.dateTime = %s in configuration file.", appCheatDateTimeString));
			}
		}
		return appCheatDateTime;
	}

	/**
	 * Returns the actual datetime. However if cheat datetime is set in configuration, 
	 * that value is returned.
	 * 
	 * @return cheat datetime if exists, otherwise the actual datetime 
	 */
	public LocalDateTime getActualDateTime() {
		LocalDateTime appCheatDateTime = getAppCheatDateTime(); 
		return appCheatDateTime != null ? appCheatDateTime : LocalDateTime.now();
	}

	/**
	 * @return application contact email address 
	 */
	public String getAppEmailAddr() {
		return appEmailAddr;
	}

	/**
	 * Gets or loads complete percent values belongs to the given {@code eventId} parameter
	 * in cache.
	 */
	public Integer getEventCompletionPercentCache(Long eventId) {
		return eventCompletionPercentCache.getUnchecked(eventId);
	}

	/**
	 * Refreshes (or loads if does not exist yet) percent value in the {@link ApplicationService#eventCompletionPercentCache} 
	 * cache using the given {@code eventId} parameter as key.
	 * 
	 * @param eventId
	 */
	public void refreshEventCompletionPercentCache(Long eventId) {
		eventCompletionPercentCache.refresh(eventId);
	}
	
	/**
	 * Gets or loads topUsers list in cache.
	 */
	public List<UserCertificate> getTopUsersCache() {
		return topUsersCache.getUnchecked(appShortName);
	}
	
	/**
	 * Refreshes (or loads if does not exist yet) topUsers list in the {@link ApplicationService#topUsersCache} 
	 * cache using the given {@code eventId} parameter as key.
	 * 
	 * @param eventId
	 */
	public void refreshTopUsersCache() {
		topUsersCache.refresh(appShortName);
	}

	/**
	 * Gets or loads topUsers list in cache.
	 */
	public LoadingCache<Long, List<LocalDateTime>> getRetrieveMatchResultsJobTriggerStartTimesCache() {
		return retrieveMatchResultsJobTriggerStartTimesCache;
	}

	/**
	 * Refreshes retrieveMatchResultsJobTriggerStartTimesCache belongs to the given eventId.
	 * 
	 * @param eventId
	 */
	public void refreshRetrieveMatchResultsJobTriggerStartTimesCache(Long eventId) {
		retrieveMatchResultsJobTriggerStartTimesCache.refresh(eventId);
	}
	
	/**
	 * Gets or loads chatsByUserGroupCache list in cache.
	 */
	public LoadingCache<UserGroup, List<Chat>> getChatsByUserGroupCache() {
		return chatsByUserGroupCache;
	}

	/**
	 * Refreshes chatsByUserGroupCache belongs to the given userGroup.
	 * 
	 * @param userGroup
	 */
	public void refreshChatsByUserGroupCache(UserGroup userGroup) {
		chatsByUserGroupCache.refresh(userGroup);
	}
	
	/**
	 * Retrieves eventId values of completed events.
	 * @return eventId list of all completed Event entities
	 */
	public List<Long> getCompletedEventIds() {
		List<Long> eventIds = new ArrayList<>();
		eventIds.addAll(this.eventCompletionPercentCache.asMap().keySet());
		eventIds = eventIds.stream().filter(e -> this.eventCompletionPercentCache.getUnchecked(e) == 100).toList();
		return eventIds;
	}

	/**
	 * Retrieves a list of all logged in users
	 * @return all logged in users
	 */
	public List<org.springframework.security.core.userdetails.User> getAllAuthenticatedPrincipals() {
		return sessionRegistry.getAllPrincipals().stream()
				  .filter(org.springframework.security.core.userdetails.User.class::isInstance)
			      .filter(u -> !sessionRegistry.getAllSessions(u, false).isEmpty()) // excludes expired sessions
			      .map(e -> (org.springframework.security.core.userdetails.User)e)
			      .toList();
	}
	
	public List<SessionInformation> getAllAuthenticatedSessions(org.springframework.security.core.userdetails.User user) {
		return sessionRegistry.getAllSessions(user, false);
	}
	
}
