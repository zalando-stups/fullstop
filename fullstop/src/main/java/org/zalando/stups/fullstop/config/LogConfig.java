package org.zalando.stups.fullstop.config;


import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.zalando.logbook.HttpLogFormatter;
import org.zalando.logbook.JsonHttpLogFormatter;

import static com.fasterxml.jackson.databind.SerializationFeature.INDENT_OUTPUT;

@Configuration
public class LogConfig {

    @Bean
    public HttpLogFormatter httpFormatter(final ObjectMapper objectMapper) {
        ObjectMapper mapper = objectMapper.copy();
        mapper = mapper.disable(INDENT_OUTPUT);
        return new JsonHttpLogFormatter(mapper);
    }
    
}
