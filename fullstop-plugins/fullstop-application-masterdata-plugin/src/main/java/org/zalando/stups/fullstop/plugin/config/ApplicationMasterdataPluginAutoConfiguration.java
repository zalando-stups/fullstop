package org.zalando.stups.fullstop.plugin.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import org.zalando.stups.fullstop.plugin.ApplicationMasterdataPlugin;

/**
 * @author  jbellmann
 */
@Configuration
@ComponentScan(basePackageClasses = {ApplicationMasterdataPlugin.class})
@EnableConfigurationProperties({ ApplicationMasterdataPluginProperties.class })
public class ApplicationMasterdataPluginAutoConfiguration { }
