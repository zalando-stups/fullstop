package org.zalando.stups.fullstop.plugin.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.zalando.stups.fullstop.plugin.ApplicationRegistryPlugin;
import org.zalando.stups.fullstop.plugin.DockerRegistryPlugin;
import org.zalando.stups.fullstop.plugin.EC2InstanceContextProvider;
import org.zalando.stups.fullstop.violation.ViolationSink;

@Configuration
@EnableConfigurationProperties({ RegistryPluginProperties.class })
public class RegistryPluginAutoConfiguration {

    @Bean
    ApplicationRegistryPlugin registryPlugin(
            final EC2InstanceContextProvider contextProvider,
            final ViolationSink violationSink,
            final RegistryPluginProperties registryPluginProperties) {
        return new ApplicationRegistryPlugin(contextProvider, violationSink, registryPluginProperties);
    }

    @Bean
    DockerRegistryPlugin dockerRegistryPlugin(
            final EC2InstanceContextProvider contextProvider,
            final ViolationSink violationSink) {
        return new DockerRegistryPlugin(contextProvider, violationSink);
    }
}
