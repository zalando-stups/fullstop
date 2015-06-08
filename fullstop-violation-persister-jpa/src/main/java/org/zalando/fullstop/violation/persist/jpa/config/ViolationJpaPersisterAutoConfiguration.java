package org.zalando.fullstop.violation.persist.jpa.config;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.zalando.fullstop.violation.persist.jpa.ViolationJpaPersister;

import org.zalando.stups.fullstop.violation.repository.ViolationRepository;

import reactor.bus.EventBus;

/**
 * Autoconfiguration for {@link ViolationJpaPersister}.
 *
 * @author  jbellmann
 */
@Configuration
public class ViolationJpaPersisterAutoConfiguration {

    @Autowired
    private EventBus eventBus;

    @Autowired
    private ViolationRepository violationRepository;

    @Bean
    public ViolationJpaPersister violationJpaPersister() {
        return new ViolationJpaPersister(eventBus, violationRepository);
    }
}
