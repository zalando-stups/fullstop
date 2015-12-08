package com.unknown.comp;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.bus.EventBus;

@Configuration
public class HandlerConfig {

    @Autowired
    private EventBus eventBus;

    @Bean
    public SimpleDemonstrationViolationHandler simpleDemonstrationViolationHandler() {
        return new SimpleDemonstrationViolationHandler(eventBus);
    }

}
