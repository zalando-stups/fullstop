package org.zalando.stups.fullstop.jobs.config;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CachingConfigurerSupport;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.guava.GuavaCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static com.google.common.cache.CacheBuilderSpec.parse;

@Configuration
@EnableCaching
public class CacheConfig extends CachingConfigurerSupport {

    @Bean
    public CacheManager oneDayTTLCacheManager() {
        final GuavaCacheManager cacheManager = new GuavaCacheManager();
        cacheManager.setCacheBuilderSpec(parse("maximumSize=1000,expireAfterWrite=1d"));
        return cacheManager;
    }

}
