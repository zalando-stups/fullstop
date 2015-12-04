package org.zalando.stups.fullstop.plugin.taupageyaml.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.zalando.stups.fullstop.plugin.EC2InstanceContextProvider;
import org.zalando.stups.fullstop.plugin.taupageyaml.TaupageYamlPlugin;
import org.zalando.stups.fullstop.violation.ViolationSink;

/**
 * @author clohmann
 */
@Configuration
public class TaupageYamlPluginAutoConfiguration {

    @Bean
    public TaupageYamlPlugin taupageYamlPlugin(final EC2InstanceContextProvider contextProvider,
                                               final ViolationSink violationSink) {
        return new TaupageYamlPlugin(contextProvider, violationSink);
    }
}
