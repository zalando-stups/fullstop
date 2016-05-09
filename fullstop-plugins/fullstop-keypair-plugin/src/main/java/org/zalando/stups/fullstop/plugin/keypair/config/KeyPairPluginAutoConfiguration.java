package org.zalando.stups.fullstop.plugin.keypair.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.zalando.stups.fullstop.plugin.EC2InstanceContextProvider;
import org.zalando.stups.fullstop.plugin.keypair.KeyPairPlugin;
import org.zalando.stups.fullstop.violation.ViolationSink;

@Configuration
public class KeyPairPluginAutoConfiguration {

    @Bean
    KeyPairPlugin keyPairPlugin(final EC2InstanceContextProvider contextProvider, final ViolationSink violationSink) {
        return new KeyPairPlugin(contextProvider, violationSink);
    }
}
