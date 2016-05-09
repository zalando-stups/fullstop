package org.zalando.stups.fullstop.plugin.example;

import com.amazonaws.services.cloudtrail.processinglibrary.exceptions.CallbackException;
import com.amazonaws.services.cloudtrail.processinglibrary.model.CloudTrailEvent;
import com.amazonaws.services.cloudtrail.processinglibrary.model.CloudTrailEventData;
import com.google.common.collect.Lists;
import org.assertj.core.api.Assertions;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.springframework.plugin.core.PluginRegistry;
import org.springframework.plugin.core.SimplePluginRegistry;
import org.zalando.stups.fullstop.PluginEventsProcessor;
import org.zalando.stups.fullstop.plugin.FullstopPlugin;

import java.util.ArrayList;
import java.util.List;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * Shows how it works with the registry itself.
 *
 * @author jbellmann
 */
@Ignore
public class ExamplePluginRegistryTest {

    private CloudTrailEvent event;

    private CloudTrailEventData eventData;

    private PluginRegistry<FullstopPlugin, CloudTrailEvent> pluginRegistry;

    private final Logger log = getLogger(getClass());

    @Before
    public void setUp() {
        event = Mockito.mock(CloudTrailEvent.class);
        eventData = Mockito.mock(CloudTrailEventData.class);

        final List<FullstopPlugin> plugins = new ArrayList<>();
        plugins.add(new ExamplePlugin());
        pluginRegistry = SimplePluginRegistry.create(plugins);
    }

    @Test
    public void pluginDoesNotSupportEvent() {
        Mockito.when(eventData.getEventName()).thenReturn("Persist");
        Mockito.when(eventData.getEventSource()).thenReturn("ec2.amazonaws.com");
        Mockito.when(event.getEventData()).thenReturn(eventData);

        final List<FullstopPlugin> pluginsThatSupportsTheEvent = this.pluginRegistry.getPluginsFor(event);

        Assertions.assertThat(pluginsThatSupportsTheEvent).isEmpty();

    }

    @Test
    public void pluginSupportsEvent() {
        Mockito.when(eventData.getEventName()).thenReturn("Delete");
        Mockito.when(eventData.getEventSource()).thenReturn("ec2.amazonaws.com");
        Mockito.when(event.getEventData()).thenReturn(eventData);

        final List<FullstopPlugin> pluginsThatSupportsTheEvent = this.pluginRegistry.getPluginsFor(event);

        Assertions.assertThat(pluginsThatSupportsTheEvent).isNotEmpty();

    }

    @Test
    public void eventsProcessor() {
        Mockito.when(eventData.getEventName()).thenReturn("Delete");
        Mockito.when(eventData.getEventSource()).thenReturn("ec2.amazonaws.com");
        Mockito.when(event.getEventData()).thenReturn(eventData);

        final PluginEventsProcessor processor = new PluginEventsProcessor(pluginRegistry);

        try {
            processor.process(Lists.newArrayList(event));
        } catch (final CallbackException e) {
            log.error(e.getMessage(), e);
            Assert.fail();
        }
    }
}
