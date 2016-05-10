package org.zalando.stups.fullstop.violation.converter;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;

import static java.util.Collections.singletonMap;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Created by mrandi.
 */
public class ViolationObjectConverterTest {

    private ViolationObjectConverter violationObjectConverter;
    private ObjectMapper objectMapper;

    @Before
    public void setUp() throws Exception {
        violationObjectConverter = new ViolationObjectConverter();
        objectMapper = new ObjectMapper();
    }

    @Test
    public void testConvertToDatabaseColumn() throws Exception {
        final Object obj = singletonMap("test","this is a test");
        final String result = violationObjectConverter.convertToDatabaseColumn(obj);
        assertThat(result).isEqualTo("{\"test\":\"this is a test\"}");
    }

    @Test
    public void testConvertToEntityAttribute() throws Exception {
        final String value = "{\"test\":\"this is a test\"}";
        final Object result = violationObjectConverter.convertToEntityAttribute(value);
        assertThat(result).isEqualTo(objectMapper.readValue("{\"test\":\"this is a test\"}", JsonNode.class));
    }

    @Test
    public void testConvertToDatabaseColumnNull() throws Exception {
        final Object obj = null;
        final String result = violationObjectConverter.convertToDatabaseColumn(obj);
        assertThat(result).isNull();
    }

    @Test
    public void testConvertToEntityAttributeEmpty() throws Exception {
        final String value = "";
        final Object result = violationObjectConverter.convertToEntityAttribute(value);
        assertThat(result).isNull();
    }

}