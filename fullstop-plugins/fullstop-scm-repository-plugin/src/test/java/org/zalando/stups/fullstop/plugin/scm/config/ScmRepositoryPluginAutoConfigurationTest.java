package org.zalando.stups.fullstop.plugin.scm.config;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.zalando.stups.fullstop.plugin.EC2InstanceContextProvider;
import org.zalando.stups.fullstop.plugin.scm.ScmRepositoryPlugin;
import org.zalando.stups.fullstop.violation.ViolationSink;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
import static org.mockito.Mockito.mock;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration
public class ScmRepositoryPluginAutoConfigurationTest {

    @Autowired(required = false)
    private ScmRepositoryPlugin scmRepositoryPlugin;

    @Autowired(required = false)
    private ScmRepositoryPluginProperties properties;

    @Test
    public void testScmRepositoryPlugin() throws Exception {
        assertThat(scmRepositoryPlugin).isNotNull();
    }

    @Test
    public void testPropertiesMapping() throws Exception {
        assertThat(properties).isNotNull();
        assertThat(properties.getHosts()).containsOnly(
                entry("github.my.company.com", "^.+$"),
                entry("github.com", "^(?:zalando|zalando-stups)$"));
    }

    @Configuration
    @EnableAutoConfiguration
    static class TestConfig {

        @Bean
        ViolationSink violationSink() {
            return mock(ViolationSink.class);
        }

        @Bean
        EC2InstanceContextProvider ec2InstanceContextProvider() {
            return mock(EC2InstanceContextProvider.class);
        }
    }
}
