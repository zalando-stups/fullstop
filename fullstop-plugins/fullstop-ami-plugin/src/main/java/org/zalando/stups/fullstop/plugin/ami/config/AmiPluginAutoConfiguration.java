package org.zalando.stups.fullstop.plugin.ami.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.zalando.stups.fullstop.plugin.EC2InstanceContextProvider;
import org.zalando.stups.fullstop.plugin.ami.AmiPlugin;
import org.zalando.stups.fullstop.violation.ViolationSink;

@Configuration
public class AmiPluginAutoConfiguration {

    @Bean
    AmiPlugin amiPlugin(final EC2InstanceContextProvider contextProvider,
                        final ViolationSink violationSink) {
        return new AmiPlugin(contextProvider, violationSink);
    }
}
