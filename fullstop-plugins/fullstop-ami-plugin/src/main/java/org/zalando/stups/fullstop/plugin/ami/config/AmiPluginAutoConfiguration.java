package org.zalando.stups.fullstop.plugin.ami.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import org.zalando.stups.fullstop.plugin.ami.AmiPlugin;

/**
 * @author  jbellmann
 */
@Configuration
@ComponentScan(basePackageClasses = {AmiPlugin.class})
public class AmiPluginAutoConfiguration { }
