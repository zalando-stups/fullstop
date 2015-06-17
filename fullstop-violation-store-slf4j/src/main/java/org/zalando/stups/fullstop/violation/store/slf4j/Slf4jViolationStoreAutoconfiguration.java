/**
 * Copyright 2015 Zalando SE
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.zalando.stups.fullstop.violation.store.slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * To enable simple configuration in Spring-Boot-Environment.
 *
 * @author jbellmann
 */
@Configuration
@EnableConfigurationProperties({ Slf4jViolationStoreProperties.class })
public class Slf4jViolationStoreAutoconfiguration {

    @Autowired
    private Slf4jViolationStoreProperties slf4jViolationStoreProperties;

    @ConditionalOnMissingBean
    @Bean
    public Slf4jViolationStore slf4jViolationStore() {
        return new Slf4jViolationStore(slf4jViolationStoreProperties.getLoggernames());
    }

}
