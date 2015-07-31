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

import com.amazonaws.services.cloudtrail.processinglibrary.model.CloudTrailEvent;
import com.amazonaws.services.cloudtrail.processinglibrary.model.CloudTrailEventData;
import com.unknown.pkg.PluginTest.TestConfig;
import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.zalando.stups.fullstop.aws.ClientProvider;
import org.zalando.stups.fullstop.plugin.ami.AmiPlugin;
import org.zalando.stups.fullstop.violation.NoOpViolationSink;
import org.zalando.stups.fullstop.violation.ViolationSink;

/**
 * @author jbellmann
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = { ExampleApplication.class, TestConfig.class })
public class PluginTest {

    @Autowired
    private AmiPlugin plugin;

    @Test
    public void testPluginCreated() {
        CloudTrailEvent event = getMockedEvent();

        Assertions.assertThat(plugin.supports(event)).isTrue();

    }

    protected CloudTrailEvent getMockedEvent() {
        CloudTrailEvent event = Mockito.mock(CloudTrailEvent.class);
        CloudTrailEventData eventData = Mockito.mock(CloudTrailEventData.class);
        Mockito.when(event.getEventData()).thenReturn(eventData);
        Mockito.when(eventData.getEventSource()).thenReturn("ec2.amazonaws.com");
        Mockito.when(eventData.getEventName()).thenReturn("RunInstances");
        return event;
    }

    @Configuration
    static class TestConfig {

        @Bean
        public ViolationSink violationSink() {
            return new NoOpViolationSink();
        }

        @Bean
        public ClientProvider clientProvider() {
            return Mockito.mock(ClientProvider.class);
        }
    }
}
