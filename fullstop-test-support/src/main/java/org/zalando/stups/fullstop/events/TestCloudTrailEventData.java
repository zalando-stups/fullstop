/**
 * Copyright (C) 2015 Zalando SE (http://tech.zalando.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.zalando.stups.fullstop.events;

import java.io.IOException;
import java.io.StringWriter;

import java.net.URISyntaxException;

import java.nio.file.Files;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.services.cloudtrail.processinglibrary.model.CloudTrailEvent;
import com.amazonaws.services.cloudtrail.processinglibrary.model.CloudTrailEventData;
import com.amazonaws.services.cloudtrail.processinglibrary.model.internal.CloudTrailEventField;
import com.amazonaws.services.cloudtrail.processinglibrary.model.internal.UserIdentity;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Creates {@link CloudTrailEvent}s with data from classpath-resources.
 *
 * @author  jbellmann
 */
public class TestCloudTrailEventData extends CloudTrailEventData {

    private static final Logger LOG = LoggerFactory.getLogger(TestCloudTrailEventData.class);

    private Map<String, Object> data = new LinkedHashMap<String, Object>();

    private ObjectMapper mapper;

    private String responseElementsResource;

    public TestCloudTrailEventData(final Map<String, Object> data) {
        this.data = data;
    }

    public TestCloudTrailEventData(final String responseElementsResource) {
        this.responseElementsResource = responseElementsResource;
    }

    public TestCloudTrailEventData(final Map<String, Object> data, final String responseElementsResource) {
        this.data = data;
        this.responseElementsResource = responseElementsResource;
    }

    public static CloudTrailEvent createCloudTrailEventFromMap(final Map<String, Object> content) {
        return new CloudTrailEvent(new TestCloudTrailEventData(content), null);
    }

    public static CloudTrailEvent createCloudTrailEvent(final String string) {
        return new CloudTrailEvent(new TestCloudTrailEventData(new LinkedHashMap<String, Object>(), string), null);
    }

    @Override
    public UserIdentity getUserIdentity() {
        Map<String, Object> value = (Map<String, Object>) this.data.get(CloudTrailEventField.userIdentity.name());
        UserIdentity ui = new UserIdentity();
        for (Map.Entry<String, Object> entry : value.entrySet()) {
            ui.add(entry.getKey(), entry.getValue());
        }

        return ui;
    }

    @Override
    public Object get(final String key) {
        return data.get(key);
    }

    @Override
    public UUID getEventId() {
        Object value = data.get(CloudTrailEventField.eventID.name());
        if (value == null) {
            return UUID.randomUUID();
        } else {
            if (value instanceof UUID) {
                return (UUID) value;
            }

            if (value instanceof String) {

                return UUID.fromString((String) value);
            }
        }

        throw new RuntimeException("NO-UUID-FOUND");
    }

    @Override
    public void add(final String key, final Object value) {
        this.data.put(key, value);
    }

    @Override
    public String getRequestParameters() {
        if (data.get("requestParameters") != null) {
            Object responseElements = data.get("requestParameters");

            if (mapper == null) {
                mapper = new ObjectMapper();
            }

            StringWriter writer = new StringWriter();
            try {
                mapper.writeValue(writer, responseElements);
                writer.flush();
                writer.close();
                return writer.toString();
            } catch (JsonGenerationException e) {
                throw new RuntimeException(e.getMessage(), e);
            } catch (JsonMappingException e) {
                throw new RuntimeException(e.getMessage(), e);
            } catch (IOException e) {
                throw new RuntimeException(e.getMessage(), e);
            } finally {
                try {
                    writer.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return "";
    }

    @Override
    public String getResponseElements() {
        if (data.get("responseElements") != null) {
            Object responseElements = data.get("responseElements");

            if (mapper == null) {
                mapper = new ObjectMapper();
            }

            StringWriter writer = new StringWriter();
            try {
                mapper.writeValue(writer, responseElements);
                writer.flush();
                writer.close();
                return writer.toString();
            } catch (JsonGenerationException e) {
                throw new RuntimeException(e.getMessage(), e);
            } catch (JsonMappingException e) {
                throw new RuntimeException(e.getMessage(), e);
            } catch (IOException e) {
                throw new RuntimeException(e.getMessage(), e);
            } finally {
                try {
                    writer.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } else if (responseElementsResource != null) {
            return getResponseElementsFromClasspath(responseElementsResource);
        }

        return "";
    }

    protected String getResponseElementsFromClasspath(final String resource) {
        try {
            return new String(Files.readAllBytes(java.nio.file.Paths.get(getClass().getResource(resource).toURI())));
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

}
