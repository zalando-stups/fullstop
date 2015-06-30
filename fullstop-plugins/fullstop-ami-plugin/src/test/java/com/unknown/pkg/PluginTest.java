package com.unknown.pkg;

import org.assertj.core.api.Assertions;

import org.junit.Test;

import org.junit.runner.RunWith;

import org.mockito.Mockito;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.IntegrationTest;
import org.springframework.boot.test.SpringApplicationConfiguration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import org.zalando.stups.fullstop.aws.ClientProvider;
import org.zalando.stups.fullstop.plugin.ami.AmiPlugin;
import org.zalando.stups.fullstop.violation.NoOpViolationSink;
import org.zalando.stups.fullstop.violation.ViolationSink;

import com.amazonaws.services.cloudtrail.processinglibrary.model.CloudTrailEvent;

/**
 * @author  jbellmann
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration
@IntegrationTest({ "debug=true" })
public class PluginTest {

    @Autowired
    private AmiPlugin plugin;

    @Test
    public void testPluginCreated() {
        CloudTrailEvent event = Mockito.mock(CloudTrailEvent.class);

        Assertions.assertThat(plugin.supports(event)).isTrue();

    }

    @Configuration
    @EnableAutoConfiguration
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
