package org.zalando.stups.fullstop.violation;

import com.opentable.db.postgres.embedded.EmbeddedPostgreSQL;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.orm.jpa.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.retry.annotation.EnableRetry;
import org.zalando.stups.fullstop.violation.service.ApplicationLifecycleService;
import org.zalando.stups.fullstop.violation.service.impl.ApplicationLifecycleServiceImpl;

import javax.sql.DataSource;
import java.io.IOException;

@Configuration
@EnableAutoConfiguration
@EnableJpaRepositories("org.zalando.stups.fullstop.violation.repository")
@EntityScan("org.zalando.stups.fullstop.violation")
@EnableJpaAuditing
@EnableRetry(proxyTargetClass = true)
public class EmbeddedPostgresJpaConfig {

    @Bean
    DataSource dataSource() throws IOException {
        return embeddedPostgres().getPostgresDatabase();
    }

    @Bean
    EmbeddedPostgreSQL embeddedPostgres() throws IOException {
        return EmbeddedPostgreSQL.start();
    }

    @Bean
    AuditorAware<String> auditorAware() {
        return () -> "unit-test";
    }

    @Bean
    ApplicationLifecycleService applicationLifecycleService() {
        return new ApplicationLifecycleServiceImpl();
    }
}
