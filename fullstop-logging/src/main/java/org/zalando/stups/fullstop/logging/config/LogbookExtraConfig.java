package org.zalando.stups.fullstop.logging.config;

import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.zalando.logbook.BodyFilter;
import org.zalando.logbook.spring.LogbookAutoConfiguration;


@Configuration
@AutoConfigureBefore(LogbookAutoConfiguration.class)
class LogbookExtraConfig {

    @Bean
    BodyFilter hideFullBody() {
        return (contentType, message) -> "<<obfuscated>>";
    }
}
