package org.zalando.stups.fullstop.plugin.subnet.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.zalando.stups.fullstop.plugin.subnet.SubnetPlugin;

/**
 * @author jbellmann
 */
@Configuration
@ComponentScan(basePackageClasses = { SubnetPlugin.class })
public class SubnetPluginAutoConfiguration {
}
