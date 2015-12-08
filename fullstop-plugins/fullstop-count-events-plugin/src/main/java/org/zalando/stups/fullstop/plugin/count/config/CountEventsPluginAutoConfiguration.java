package org.zalando.stups.fullstop.plugin.count.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.zalando.stups.fullstop.plugin.count.CountEventsPlugin;

/**
 * Nothing special here.
 *
 * @author jbellmann
 */
@Configuration
@ComponentScan(basePackageClasses = { CountEventsPlugin.class })
public class CountEventsPluginAutoConfiguration {
}
