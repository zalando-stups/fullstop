package org.zalando.stups.fullstop.violation.converter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;
import java.io.IOException;
import java.util.Collections;

@Converter(autoApply = true)
public class ViolationObjectConverter implements AttributeConverter<Object, String> {

    private ObjectMapper objectMapper = new ObjectMapper();

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public String convertToDatabaseColumn(final Object obj) {
        if (obj == null) {
            return null;
        }

        try {
            return objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            logger.info("Could not parse object metadata: {}. The value will be converted as string. Reason: {}", obj, e);
            return obj.toString();
        }
    }

    @Override
    public Object convertToEntityAttribute(final String value) {
        if (StringUtils.hasText(value)) {
            try {
                return objectMapper.readValue(value, JsonNode.class);
            } catch (IOException e) {
                logger.info("Could not parse value metadata: {}. The value will be converted as map. Reason: {}", value, e);
                return Collections.singletonMap("data", value);
            }
        }
        return null;
    }
}
