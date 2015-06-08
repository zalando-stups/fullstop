package com.unknown.comp;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.zalando.stups.fullstop.violation.reactor.EventBusViolationHandler;

import reactor.bus.EventBus;

@Configuration
public class HandlerConfig {

    @Autowired
    private EventBus eventBus;

    @Bean
    public SimpleDemonstrationViolationHandler simpleDemonstrationViolationHandler() {
        return new SimpleDemonstrationViolationHandler();
    }

    @Bean
    public EventBusViolationHandler eventBusViolationHandler() {
        return new EventBusViolationHandler(eventBus, simpleDemonstrationViolationHandler());
    }

}
