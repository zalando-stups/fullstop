package org.zalando.stups.fullstop.violation;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.retry.annotation.EnableRetry;

@Configuration
@EnableAutoConfiguration
@EnableJpaRepositories("org.zalando.stups.fullstop")
@EntityScan("org.zalando.stups.fullstop")
@EnableJpaAuditing
@EnableRetry(proxyTargetClass = true)
public class JpaConfig {

    @Bean
    AuditorAware<String> auditorAware() {
        return () -> "unit-test";
    }
}
