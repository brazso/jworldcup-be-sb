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

    @Bean
    public CacheManager cacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager(
        		CACHE_USER_BY_LOGIN_NAME, CACHE_USER_OF_EVENT, CACHE_USER_GROUPS
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
