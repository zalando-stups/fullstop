package org.zalando.stups.differentnamespace;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.zalando.stups.fullstop.violation.ViolationSink;

@Configuration
public class FullstopApplicationConfiguration {

    @Bean
    public ViolationSink violationStore() {
        return new CountingViolationSink();
    }
}
