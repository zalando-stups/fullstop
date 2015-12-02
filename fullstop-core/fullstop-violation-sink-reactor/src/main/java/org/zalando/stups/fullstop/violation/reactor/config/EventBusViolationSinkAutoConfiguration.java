package org.zalando.stups.fullstop.violation.reactor.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.metrics.CounterService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.zalando.stups.fullstop.violation.reactor.EventBusViolationSink;
import reactor.Environment;
import reactor.bus.EventBus;

/**
 * @author jbellmann
 */
@Configuration
public class EventBusViolationSinkAutoConfiguration {

    @Autowired
    private CounterService counterService;

    @Bean
    public EventBusViolationSink eventBusViolationSink() {
        return new EventBusViolationSink(eventBus(), counterService);
    }

    @Bean
    public EventBus eventBus() {
        Environment.initialize();

        return EventBus.create(Environment.get());
    }
}
