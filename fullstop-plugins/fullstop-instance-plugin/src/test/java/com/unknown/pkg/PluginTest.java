package com.unknown.pkg;

import org.assertj.core.api.Assertions;

import org.junit.Test;

import org.junit.runner.RunWith;

import org.mockito.Mockito;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.boot.test.IntegrationTest;
import org.springframework.boot.test.SpringApplicationConfiguration;

import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import org.zalando.stups.fullstop.plugin.instance.RunInstancePlugin;

import com.amazonaws.services.cloudtrail.processinglibrary.model.CloudTrailEvent;

/**
 * @author  jbellmann
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = {ExampleApplication.class})
@IntegrationTest({ "debug=true" })
public class PluginTest {

    @Autowired
    private RunInstancePlugin plugin;

    @Test
    public void testPlugin() {
        CloudTrailEvent event = Mockito.mock(CloudTrailEvent.class);

        Assertions.assertThat(plugin.supports(event)).isTrue();
    }
}
