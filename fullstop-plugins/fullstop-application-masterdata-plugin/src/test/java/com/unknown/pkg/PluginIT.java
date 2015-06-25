package com.unknown.pkg;

import java.util.List;

import org.assertj.core.api.Assertions;

import org.junit.Test;

import org.junit.runner.RunWith;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.boot.test.SpringApplicationConfiguration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import org.zalando.stups.fullstop.plugin.ApplicationMasterdataPlugin;
import org.zalando.stups.fullstop.plugin.NamedValidator;
import org.zalando.stups.fullstop.plugin.config.ApplicationMasterdataPluginProperties;
import org.zalando.stups.fullstop.violation.SystemOutViolationSink;
import org.zalando.stups.fullstop.violation.ViolationSink;

import com.unknown.pkg.PluginIT.TestConfig;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = {ExampleApplication.class, TestConfig.class})
public class PluginIT {

    @Autowired
    private List<NamedValidator> namedValidators;

    @Autowired
    private ApplicationMasterdataPluginProperties applicationMasterdataPluginProperties;

    @Autowired
    private ApplicationMasterdataPlugin plugin;

    @Test
    public void testNamedValidators() {
        Assertions.assertThat(applicationMasterdataPluginProperties.getValidatorsEnabled()).contains(
            "specificationUrl");
        Assertions.assertThat(namedValidators.size()).isEqualTo(1);

        plugin.processEvent(null);
    }

    @Configuration
    static class TestConfig {

        @Bean
        public ViolationSink violationSink() {
            return new SystemOutViolationSink();
        }

    }

}
