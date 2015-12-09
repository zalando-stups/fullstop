package org.zalando.stups.fullstop.violation.converter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

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
            logger.info("Error parsing object metadata: {}. The value will be written as string", obj);
            return objectMapper.toString();
        }
    }

    @Override
    public Object convertToEntityAttribute(final String value) {
        if (StringUtils.hasText(value)) {
            return value;
        }

        return null;
    }
}
