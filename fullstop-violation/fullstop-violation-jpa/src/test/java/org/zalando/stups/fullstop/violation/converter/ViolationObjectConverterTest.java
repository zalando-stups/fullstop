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
        Object obj = singletonMap("test","this is a test");
        String result = violationObjectConverter.convertToDatabaseColumn(obj);
        assertThat(result).isEqualTo("{\"test\":\"this is a test\"}");
    }

    @Test
    public void testConvertToEntityAttribute() throws Exception {
        String value = "{\"test\":\"this is a test\"}";
        Object result = violationObjectConverter.convertToEntityAttribute(value);
        assertThat(result).isEqualTo(objectMapper.readValue("{\"test\":\"this is a test\"}", JsonNode.class));
    }

    @Test
    public void testConvertToDatabaseColumnNull() throws Exception {
        Object obj = null;
        String result = violationObjectConverter.convertToDatabaseColumn(obj);
        assertThat(result).isNull();
    }

    @Test
    public void testConvertToEntityAttributeEmpty() throws Exception {
        String value = "";
        Object result = violationObjectConverter.convertToEntityAttribute(value);
        assertThat(result).isNull();
    }

}