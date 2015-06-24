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
package org.zalando.fullstop.violation.persist.jpa.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.metrics.CounterService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.zalando.fullstop.violation.persist.jpa.ViolationJpaPersister;
import org.zalando.stups.fullstop.violation.repository.ViolationRepository;
import reactor.bus.EventBus;

/**
 * Autoconfiguration for {@link ViolationJpaPersister}.
 *
 * @author jbellmann
 */
@Configuration
public class ViolationJpaPersisterAutoConfiguration {

    @Autowired
    private EventBus eventBus;

    @Autowired
    private ViolationRepository violationRepository;

    @Autowired
    private CounterService counterService;

    @Bean
    public ViolationJpaPersister violationJpaPersister() {
        return new ViolationJpaPersister(eventBus, violationRepository, counterService);
    }
}
