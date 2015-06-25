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
package com.unknown.pkg;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.assertj.core.api.Assertions;

import org.junit.Test;

import org.junit.runner.RunWith;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.boot.test.SpringApplicationConfiguration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import org.zalando.stups.fullstop.plugin.ApplicationMasterdataPlugin;
import org.zalando.stups.fullstop.plugin.NamedValidator;
import org.zalando.stups.fullstop.plugin.config.ApplicationMasterdataPluginProperties;
import org.zalando.stups.fullstop.violation.Violation;
import org.zalando.stups.fullstop.violation.ViolationSink;

import com.unknown.pkg.PluginIT.TestConfig;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = { ExampleApplication.class, TestConfig.class })
public class PluginIT {

    @Autowired
    private List<NamedValidator> namedValidators;

    @Autowired
    private ApplicationMasterdataPluginProperties applicationMasterdataPluginProperties;

    @Autowired
    private ApplicationMasterdataPlugin plugin;

    @Autowired
    private CountingViolationSink violationSink;

    @Test
    public void testNamedValidators() {
        Assertions.assertThat(applicationMasterdataPluginProperties.getValidatorsEnabled())
                  .contains(
                            "specificationUrl");
        Assertions.assertThat(namedValidators.size())
                  .isEqualTo(1);

        plugin.processEvent(null);

        Assertions.assertThat(violationSink.getViolationCount())
                  .isGreaterThan(0);
    }

    @Configuration
    static class TestConfig {

        @Bean
        public ViolationSink violationSink() {
            return new CountingViolationSink();
        }

    }

    static class CountingViolationSink implements ViolationSink {
        private final AtomicInteger counter = new AtomicInteger();

        @Override
        public void put(final Violation violation) {
            counter.incrementAndGet();
        }

        public int getViolationCount() {
            return counter.get();
        }
    }

}
