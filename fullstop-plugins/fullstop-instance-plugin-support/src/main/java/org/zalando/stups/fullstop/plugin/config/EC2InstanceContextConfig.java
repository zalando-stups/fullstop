/**
 * Copyright (C) 2015 Zalando SE (http://tech.zalando.com)
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.zalando.stups.fullstop.plugin.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.zalando.stups.fullstop.aws.ClientProvider;
import org.zalando.stups.fullstop.plugin.EC2InstanceContextProvider;
import org.zalando.stups.fullstop.plugin.impl.EC2InstanceContextProviderImpl;
import org.zalando.stups.fullstop.plugin.provider.AmiIdProvider;
import org.zalando.stups.fullstop.plugin.provider.AmiProvider;
import org.zalando.stups.fullstop.plugin.provider.TaupageYamlProvider;
import org.zalando.stups.fullstop.plugin.provider.impl.AmiIdProviderImpl;
import org.zalando.stups.fullstop.plugin.provider.impl.AmiProviderImpl;
import org.zalando.stups.fullstop.plugin.provider.impl.TaupageYamlProviderImpl;

@Configuration
public class EC2InstanceContextConfig {

    @ConditionalOnMissingBean
    @Bean
    EC2InstanceContextProvider contextProvider(ClientProvider clientProvider) {
        return new EC2InstanceContextProviderImpl(
                clientProvider,
                amiIdProvider(),
                amiProvider(),
                taupageYamlProvider());
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
}
