package org.zalando.stups.fullstop.plugin.snapshot.config;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.zalando.stups.fullstop.plugin.EC2InstanceContextProvider;
import org.zalando.stups.fullstop.plugin.snapshot.SnapshotSourcePlugin;
import org.zalando.stups.fullstop.violation.ViolationSink;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

@RunWith(SpringRunner.class)
@ContextConfiguration
public class SnapshotSourcePluginAutoConfigurationTest {

    @Autowired(required = false)
    private SnapshotSourcePlugin plugin;

    @Test
    public void testSnapshotSourcePlugin() throws Exception {
        assertThat(plugin).isNotNull();
    }

    @Configuration
    @EnableAutoConfiguration
    static class TestConfig {

        @Bean
        EC2InstanceContextProvider contextProvieder() {
            return mock(EC2InstanceContextProvider.class);
        }

        @Bean
        ViolationSink violationSink() {
            return mock(ViolationSink.class);
        }
    }
}
