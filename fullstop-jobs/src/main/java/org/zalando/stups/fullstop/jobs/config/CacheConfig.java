package org.zalando.stups.fullstop.jobs.config;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CachingConfigurerSupport;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.guava.GuavaCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import static com.google.common.cache.CacheBuilderSpec.parse;

@Configuration
@EnableCaching
public class CacheConfig extends CachingConfigurerSupport {

    @Bean
    public CacheManager oneDayTTLCacheManager() {
        return buildCacheManager("maximumSize=1000,expireAfterWrite=1d");
    }

    @Bean
    @Primary
    public CacheManager twoHoursTTLCacheManager() {
        return buildCacheManager("maximumSize=1000,expireAfterWrite=2h");
    }

    private static CacheManager buildCacheManager(String spec) {
        final GuavaCacheManager cacheManager = new GuavaCacheManager();
        cacheManager.setCacheBuilderSpec(parse(spec));
        return cacheManager;
    }

}
