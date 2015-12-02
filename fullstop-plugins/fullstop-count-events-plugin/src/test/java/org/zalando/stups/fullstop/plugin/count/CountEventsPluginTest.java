package org.zalando.stups.fullstop.plugin.count;

import com.codahale.metrics.ConsoleReporter;
import com.codahale.metrics.MetricRegistry;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.concurrent.TimeUnit;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
public class CountEventsPluginTest {

    @Autowired
    private MetricRegistry metricRegistry;

    @Test
    public void invoke() throws InterruptedException {

        CountEventsMetric metric = new CountEventsMetric(metricRegistry);
        for (int i = 0; i < 100; i++) {
            if (i % 2 == 0) {
                metric.markEvent("test2");
            }

            metric.markEvent("test");
            TimeUnit.MILLISECONDS.sleep(50);
        }

        TimeUnit.SECONDS.sleep(2);
    }

    @Configuration
    static class TestConfig {

        @Bean
        public MetricRegistry metricRegistry() {
            MetricRegistry metricRegistry = new MetricRegistry();

            ConsoleReporter reporter = ConsoleReporter.forRegistry(metricRegistry).convertRatesTo(TimeUnit.SECONDS)
                                                      .convertDurationsTo(TimeUnit.MILLISECONDS).build();
            reporter.start(2, TimeUnit.SECONDS);

            return metricRegistry;
        }

    }

}
