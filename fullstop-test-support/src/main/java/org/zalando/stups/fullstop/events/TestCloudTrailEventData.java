/**
 * Copyright (C) 2015 Zalando SE (http://tech.zalando.com)
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.zalando.stups.fullstop.events;

import com.amazonaws.services.cloudtrail.processinglibrary.model.CloudTrailEvent;
import com.amazonaws.services.cloudtrail.processinglibrary.model.CloudTrailEventData;
import com.amazonaws.services.cloudtrail.processinglibrary.model.internal.CloudTrailEventField;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Creates {@link CloudTrailEvent}s with data from classpath-resources.
 *
 * @author jbellmann
 */
public class TestCloudTrailEventData extends CloudTrailEventData {

    private Map<String, Object> data = new LinkedHashMap<String, Object>();

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
    public Object get(final String key) {
        return data.get(key);
    }

    @Override
    public UUID getEventId() {
        Object value = data.get(CloudTrailEventField.eventID.name());
        if (value == null) {
            return UUID.randomUUID();
        }
        else {
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
    public String getResponseElements() {
        return getResponseElementsFromClasspath(responseElementsResource);
    }

    protected String getResponseElementsFromClasspath(final String resource) {
        try {
            return new String(Files.readAllBytes(java.nio.file.Paths.get(getClass().getResource(resource).toURI())));
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
        catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

}
