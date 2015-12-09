package com.unknown.pkg;

import com.amazonaws.services.cloudtrail.processinglibrary.model.CloudTrailEvent;
import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.IntegrationTest;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.zalando.stups.fullstop.plugin.count.CountEventsPlugin;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = ExampleApplication.class)
@IntegrationTest({ "debug=true" })
public class PluginTest {

    @Autowired
    private CountEventsPlugin plugin;

    @Test
    public void testCreation() {
        CloudTrailEvent event = Mockito.mock(CloudTrailEvent.class);

        // plugin supports all events
        Assertions.assertThat(plugin.supports(event)).isTrue();
    }

}
