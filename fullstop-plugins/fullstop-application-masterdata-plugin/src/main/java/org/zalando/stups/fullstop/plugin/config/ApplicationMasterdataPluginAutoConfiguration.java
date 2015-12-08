package org.zalando.stups.fullstop.plugin.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.zalando.stups.fullstop.plugin.ApplicationMasterdataPlugin;
import org.zalando.stups.fullstop.plugin.EC2InstanceContextProvider;
import org.zalando.stups.fullstop.violation.ViolationSink;

@Configuration
public class ApplicationMasterdataPluginAutoConfiguration {

    @Bean
    ApplicationMasterdataPlugin applicationMasterdataPlugin(
            final EC2InstanceContextProvider contextProvider,
            final ViolationSink violationSink) {
        return new ApplicationMasterdataPlugin(contextProvider, violationSink);
    }
}
