package org.zalando.stups.fullstop.jpa.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.zalando.stups.fullstop.jpa.auditing.SpringSecurityAuditorAware;

@Configuration
@EnableJpaAuditing(auditorAwareRef = "auditorAware")
public class AuditingConfig {

    @Bean
    public AuditorAware auditorAware() {
        return new SpringSecurityAuditorAware();
    }
}
