package org.zalando.stups.fullstop.violation.store.slf4j;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * To enable simple configuration in Spring-Boot-Environment.
 *
 * @author  jbellmann
 */
@Configuration
public class Slf4jViolationStoreAutoconfiguration {

    @Bean
// @Conditional(Bea)
    public Slf4jViolationStore slf4jViolationStore() {
        return new Slf4jViolationStore();
    }

}
