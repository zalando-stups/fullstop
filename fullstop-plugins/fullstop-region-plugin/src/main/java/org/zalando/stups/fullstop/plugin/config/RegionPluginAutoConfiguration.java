package org.zalando.stups.fullstop.plugin.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;

import org.springframework.context.annotation.Configuration;

/**
 * @author  jbellmann
 */
@Configuration
@EnableConfigurationProperties({ RegionPluginProperties.class })
public class RegionPluginAutoConfiguration { }
