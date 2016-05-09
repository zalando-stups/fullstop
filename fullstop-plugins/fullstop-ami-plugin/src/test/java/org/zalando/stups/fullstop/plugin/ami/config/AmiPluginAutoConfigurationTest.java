package org.zalando.stups.fullstop.plugin.ami.config;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.zalando.stups.fullstop.plugin.EC2InstanceContextProvider;
import org.zalando.stups.fullstop.plugin.ami.AmiPlugin;
import org.zalando.stups.fullstop.violation.ViolationSink;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;


@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration
public class AmiPluginAutoConfigurationTest {

    @Autowired(required = false)
    private AmiPlugin amiPlugin;

    @Test
    public void testAmiPlugin() throws Exception {
        assertThat(amiPlugin).isNotNull();
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
