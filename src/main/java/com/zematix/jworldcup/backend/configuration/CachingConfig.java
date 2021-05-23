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
	public static final String USER_DETAILS_CACHE = "userDetails";

    @Bean
    public CacheManager cacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager(
        		USER_DETAILS_CACHE
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
