package org.zalando.stups.fullstop.plugin.lambda.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.zalando.stups.fullstop.plugin.lambda.LambdaPlugin;


@Configuration
@ComponentScan(basePackageClasses = { LambdaPlugin.class })
@EnableConfigurationProperties({ LambdaPluginProperties.class })

public class LambdaPluginAutoConfiguration {
}
