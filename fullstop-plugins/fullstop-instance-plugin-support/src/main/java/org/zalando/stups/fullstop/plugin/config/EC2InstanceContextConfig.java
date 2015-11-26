package org.zalando.stups.fullstop.plugin.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.zalando.stups.fullstop.aws.ClientProvider;
import org.zalando.stups.fullstop.plugin.EC2InstanceContextProvider;
import org.zalando.stups.fullstop.plugin.impl.EC2InstanceContextProviderImpl;

@Configuration
public class EC2InstanceContextConfig {

    @ConditionalOnMissingBean
    @Bean
    EC2InstanceContextProvider contextProvider(ClientProvider clientProvider) {
        return new EC2InstanceContextProviderImpl(clientProvider);
    }

}
