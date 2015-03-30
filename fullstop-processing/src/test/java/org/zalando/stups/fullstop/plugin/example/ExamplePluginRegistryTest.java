/**
 * Copyright 2015 Zalando SE
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.zalando.stups.fullstop.plugin.example;

import java.util.ArrayList;
import java.util.List;

import org.assertj.core.api.Assertions;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import org.mockito.Mockito;

import org.springframework.plugin.core.PluginRegistry;
import org.springframework.plugin.core.SimplePluginRegistry;

import org.zalando.stups.fullstop.PluginEventsProcessor;
import org.zalando.stups.fullstop.plugin.FullstopPlugin;

import com.amazonaws.services.cloudtrail.processinglibrary.exceptions.CallbackException;
import com.amazonaws.services.cloudtrail.processinglibrary.model.CloudTrailEvent;
import com.amazonaws.services.cloudtrail.processinglibrary.model.CloudTrailEventData;

import com.google.common.collect.Lists;

/**
 * Shows how it works with the registry itself.
 *
 * @author  jbellmann
 */
@Ignore
public class ExamplePluginRegistryTest {

    private CloudTrailEvent event;
    private CloudTrailEventData eventData;

    private PluginRegistry<FullstopPlugin, CloudTrailEvent> pluginRegistry;

    @Before
    public void setUp() {
        event = Mockito.mock(CloudTrailEvent.class);
        eventData = Mockito.mock(CloudTrailEventData.class);

        List<FullstopPlugin> plugins = new ArrayList<>();
        plugins.add(new ExamplePlugin());
        pluginRegistry = SimplePluginRegistry.create(plugins);
    }

    @Test
    public void pluginDoesNotSupportEvent() {
        Mockito.when(eventData.getEventName()).thenReturn("Persist");
        Mockito.when(eventData.getEventSource()).thenReturn("ec2.amazonaws.com");
        Mockito.when(event.getEventData()).thenReturn(eventData);

        List<FullstopPlugin> pluginsThatSupportsTheEvent = this.pluginRegistry.getPluginsFor(event);

        Assertions.assertThat(pluginsThatSupportsTheEvent).isEmpty();

    }

    @Test
    public void pluginSupportsEvent() {
        Mockito.when(eventData.getEventName()).thenReturn("Delete");
        Mockito.when(eventData.getEventSource()).thenReturn("ec2.amazonaws.com");
        Mockito.when(event.getEventData()).thenReturn(eventData);

        List<FullstopPlugin> pluginsThatSupportsTheEvent = this.pluginRegistry.getPluginsFor(event);

        Assertions.assertThat(pluginsThatSupportsTheEvent).isNotEmpty();

    }

    @Test
    public void eventsProcessor() {
        Mockito.when(eventData.getEventName()).thenReturn("Delete");
        Mockito.when(eventData.getEventSource()).thenReturn("ec2.amazonaws.com");
        Mockito.when(event.getEventData()).thenReturn(eventData);

        PluginEventsProcessor processor = new PluginEventsProcessor(pluginRegistry);

        try {
            processor.process(Lists.newArrayList(event));
        } catch (CallbackException e) {
            e.printStackTrace();
            Assert.fail();
        }
    }
}
