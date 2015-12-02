package org.zalando.stups.fullstop.plugin.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.zalando.stups.fullstop.plugin.RegionPlugin;

/**
 * @author jbellmann
 */
@Configuration
@ComponentScan(basePackageClasses = { RegionPlugin.class })
@EnableConfigurationProperties({ RegionPluginProperties.class })
public class RegionPluginAutoConfiguration {
}
