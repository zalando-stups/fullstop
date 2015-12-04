package org.zalando.stups.fullstop.plugin.taupageyaml.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.zalando.stups.fullstop.plugin.taupageyaml.TaupageYamlPlugin;

/**
 * @author clohmann
 */
@Configuration
@ComponentScan(basePackageClasses = {TaupageYamlPlugin.class})
public class TaupageYamlPluginAutoConfiguration {
}
