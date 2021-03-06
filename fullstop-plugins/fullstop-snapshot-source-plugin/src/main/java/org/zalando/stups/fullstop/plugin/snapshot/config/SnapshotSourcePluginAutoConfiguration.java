package org.zalando.stups.fullstop.plugin.snapshot.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.zalando.stups.fullstop.plugin.EC2InstanceContextProvider;
import org.zalando.stups.fullstop.plugin.snapshot.SnapshotSourcePlugin;
import org.zalando.stups.fullstop.violation.ViolationSink;

@Configuration
public class SnapshotSourcePluginAutoConfiguration {

    @Bean
    public SnapshotSourcePlugin snapshotSourcePlugin(
            final EC2InstanceContextProvider contextProvider,
            final ViolationSink violationSink) {
        return new SnapshotSourcePlugin(contextProvider, violationSink);
    }
}
