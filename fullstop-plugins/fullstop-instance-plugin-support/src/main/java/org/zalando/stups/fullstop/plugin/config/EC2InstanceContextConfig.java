/**
 * Copyright (C) 2015 Zalando SE (http://tech.zalando.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.zalando.stups.fullstop.plugin.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.zalando.stups.fullstop.aws.ClientProvider;
import org.zalando.stups.fullstop.plugin.EC2InstanceContextProvider;
import org.zalando.stups.fullstop.plugin.impl.EC2InstanceContextProviderImpl;
import org.zalando.stups.fullstop.plugin.provider.*;
import org.zalando.stups.fullstop.plugin.provider.impl.*;

@Configuration
public class EC2InstanceContextConfig {

    @ConditionalOnMissingBean
    @Bean
    EC2InstanceContextProvider contextProvider(ClientProvider clientProvider,
                                               @Value("${fullstop.plugins.ami.amiNameStartWith}") final String taupageNamePrefix,
                                               @Value("${fullstop.plugins.ami.whitelistedAmiAccount}") final String taupageOwner) {
        return new EC2InstanceContextProviderImpl(
                clientProvider,
                amiIdProvider(),
                amiProvider(),
                taupageYamlProvider(),
                taupageNamePrefix,
                taupageOwner,
                kioApplicationProvider(),
                kioVersionProvider(),
                kioApprovalProvider()
        );
    }

    @Bean
    AmiIdProvider amiIdProvider() {
        return new AmiIdProviderImpl();
    }

    @Bean
    AmiProvider amiProvider() {
        return new AmiProviderImpl();
    }

    @Bean
    TaupageYamlProvider taupageYamlProvider() {
        return new TaupageYamlProviderImpl();
    }

    @Bean
    KioApplicationProvider kioApplicationProvider() {
        return new KioApplicationProviderImpl();
    }

    @Bean
    KioVersionProvider kioVersionProvider() {
        return new KioVersionProviderImpl();
    }

    @Bean
    KioApprovalProvider kioApprovalProvider() {
        return new KioApprovalProviderImpl();
    }
}
