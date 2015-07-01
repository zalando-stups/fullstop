package org.zalando.stups.fullstop.plugin.instance.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import org.zalando.stups.fullstop.plugin.instance.RunInstancePlugin;

/**
 * @author  jbellmann
 */
@Configuration
@ComponentScan(basePackageClasses = {RunInstancePlugin.class})
public class InstancePluginAutoConfiguration { }
