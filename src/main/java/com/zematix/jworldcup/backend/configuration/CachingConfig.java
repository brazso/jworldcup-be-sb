package com.zematix.jworldcup.backend.configuration;

import java.util.concurrent.TimeUnit;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.github.benmanes.caffeine.cache.Caffeine;

@Configuration
@EnableCaching
public class CachingConfig {
	public static final String CACHE_USER_BY_LOGIN_NAME = "userByLoginName";
	public static final String CACHE_USER_OF_EVENT = "userOfEvent";
	public static final String CACHE_USER_GROUPS = "userGroups";
	public static final String CACHE_EVENT_START_TIME = "eventStartTime";
	public static final String CACHE_EVENT_KNOCKOUT_START_TIME = "eventKnockoutStartTime";
	public static final String CACHE_EVENT_END_TIME = "eventEndTime";
	public static final String CACHE_FAVOURITE_GROUP_TEAMS = "favouriteGroupTeams";
	public static final String CACHE_FAVOURITE_KNOCKOUT_TEAMS = "favouriteKnockoutTeams";

    @Bean
    CacheManager cacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager(
        		CACHE_USER_BY_LOGIN_NAME, CACHE_USER_OF_EVENT, CACHE_USER_GROUPS, 
        		CACHE_EVENT_START_TIME, CACHE_EVENT_KNOCKOUT_START_TIME, CACHE_EVENT_END_TIME,
        		CACHE_FAVOURITE_GROUP_TEAMS, CACHE_FAVOURITE_KNOCKOUT_TEAMS
        );
        cacheManager.setCaffeine(Caffeine.newBuilder()
          .initialCapacity(200)
          .maximumSize(500)
          .expireAfterAccess(5, TimeUnit.MINUTES)
//          .weakKeys() // resulting cache will use identity (==) comparison to determine equality of keys similar to IdentityHashMap
          .recordStats());
        return cacheManager;
    }
}
