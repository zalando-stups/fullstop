package org.zalando.fullstop.violation.persist.jpa.config;

import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.StatelessKieSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.metrics.CounterService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.web.config.EnableSpringDataWebSupport;
import org.zalando.fullstop.violation.persist.jpa.ViolationJpaPersister;
import org.zalando.stups.fullstop.violation.repository.ViolationRepository;
import org.zalando.stups.fullstop.violation.repository.ViolationTypeRepository;
import reactor.bus.EventBus;

/**
 * Autoconfiguration for {@link ViolationJpaPersister}.
 *
 * @author jbellmann
 */
@Configuration
@EnableJpaRepositories("org.zalando.stups.fullstop.violation.repository")
@EnableSpringDataWebSupport
public class ViolationJpaPersisterAutoConfiguration {

    @Autowired
    private EventBus eventBus;

    @Autowired
    private ViolationRepository violationRepository;

    @Autowired
    private ViolationTypeRepository violationTypeRepository;

    @Autowired
    private CounterService counterService;

    @Autowired
    private StatelessKieSession kieSession;

    @Bean
    public ViolationJpaPersister violationJpaPersister() {
        return new ViolationJpaPersister(eventBus, violationRepository, violationTypeRepository, counterService,
                kieSession);
    }
}
