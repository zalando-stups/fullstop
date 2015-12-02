package org.zalando.stups.fullstop.plugin.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.zalando.stups.clients.kio.KioOperations;
import org.zalando.stups.fullstop.aws.ClientProvider;
import org.zalando.stups.fullstop.events.UserDataProvider;
import org.zalando.stups.fullstop.plugin.RegistryPlugin;
import org.zalando.stups.fullstop.violation.ViolationSink;
import org.zalando.stups.pierone.client.PieroneOperations;

@Configuration
@EnableConfigurationProperties({ RegistryPluginProperties.class })
public class RegistryPluginAutoConfiguration {
    @Autowired
    private ClientProvider clientProvider;

    @ConditionalOnMissingBean
    @Bean
    public UserDataProvider userDataProvider() {
        return new UserDataProvider(clientProvider);
    }

    @Bean
    RegistryPlugin registryPlugin(UserDataProvider userDataProvider, ViolationSink violationSink,
                                  PieroneOperations pieroneOperations, KioOperations kioOperations,
                                  RegistryPluginProperties registryPluginProperties) {
        return new RegistryPlugin(userDataProvider, violationSink, pieroneOperations, kioOperations, registryPluginProperties);
    }
}
