package org.zalando.stups.fullstop.plugin.snapshot.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.zalando.stups.fullstop.aws.ClientProvider;
import org.zalando.stups.fullstop.events.UserDataProvider;
import org.zalando.stups.fullstop.plugin.snapshot.SnapshotSourcePlugin;

/**
 * @author jbellmann
 */
@Configuration
@ComponentScan(basePackageClasses = { SnapshotSourcePlugin.class })
public class SnapshotSourcePluginAutoConfiguration {

    @Autowired
    private ClientProvider clientProvider;

    @ConditionalOnMissingBean
    @Bean
    public UserDataProvider userDataProvider() {
        return new UserDataProvider(clientProvider);
    }
}
