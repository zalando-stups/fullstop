package org.zalando.stups.fullstop.plugin.unapproved.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.zalando.stups.fullstop.plugin.unapproved.UnapprovedServicesAndRolePlugin;

/**
 * @author jbellmann
 */
@Configuration
@ComponentScan(basePackageClasses = { UnapprovedServicesAndRolePlugin.class })
@EnableConfigurationProperties({ UnapprovedServicesAndRoleProperties.class })
public class UnapprovedServiceAutoConfiguration {
}
