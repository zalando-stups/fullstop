package org.zalando.stups.fullstop.plugin.config;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.zalando.stups.fullstop.plugin.ApplicationMasterdataPlugin;
import org.zalando.stups.fullstop.plugin.EC2InstanceContextProvider;
import org.zalando.stups.fullstop.violation.ViolationSink;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
public class ApplicationMasterdataPluginAutoConfigurationTest {

    @Autowired(required = false)
    private ApplicationMasterdataPlugin plugin;

    @Test
    public void testApplicationMasterdataPlugin() throws Exception {
        assertThat(plugin).isNotNull();
    }

    @Configuration
    @EnableAutoConfiguration
    static class TestConfig {

        @Bean
        EC2InstanceContextProvider contextProvider(){
            return mock(EC2InstanceContextProvider.class);
        }

        @Bean
        ViolationSink violationSink(){
            return mock(ViolationSink.class);
        }
    }
}
