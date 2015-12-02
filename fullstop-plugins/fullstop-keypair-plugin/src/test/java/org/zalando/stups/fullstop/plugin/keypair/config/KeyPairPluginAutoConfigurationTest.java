package org.zalando.stups.fullstop.plugin.keypair.config;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.zalando.stups.fullstop.plugin.EC2InstanceContextProvider;
import org.zalando.stups.fullstop.plugin.keypair.KeyPairPlugin;
import org.zalando.stups.fullstop.violation.ViolationSink;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
public class KeyPairPluginAutoConfigurationTest {

    @Autowired(required = false)
    private KeyPairPlugin keyPairPlugin;

    @Test
    public void testKeyPairPlugin() throws Exception {
        assertThat(keyPairPlugin).isNotNull();
    }

    @Configuration
    @EnableAutoConfiguration
    static class TestConfig {

        @Bean
        ViolationSink violationSink() {
            return mock(ViolationSink.class);
        }

        @Bean
        EC2InstanceContextProvider contextProvider() {
            return mock(EC2InstanceContextProvider.class);
        }
    }
}
