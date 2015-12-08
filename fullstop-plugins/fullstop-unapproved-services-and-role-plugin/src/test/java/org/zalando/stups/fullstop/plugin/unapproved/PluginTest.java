package org.zalando.stups.fullstop.plugin.unapproved;

import com.amazonaws.services.cloudtrail.processinglibrary.model.CloudTrailEvent;
import com.amazonaws.services.cloudtrail.processinglibrary.model.CloudTrailEventData;
import org.assertj.core.api.Assertions;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.zalando.stups.fullstop.aws.ClientProvider;
import org.zalando.stups.fullstop.plugin.AbstractPluginTest;
import org.zalando.stups.fullstop.violation.NoOpViolationSink;
import org.zalando.stups.fullstop.violation.ViolationSink;

/**
 * @author jbellmann
 */
@Ignore
public class PluginTest extends AbstractPluginTest {

    @Autowired
    private UnapprovedServicesAndRolePlugin plugin;

    @Test
    public void testPlugin() {
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
