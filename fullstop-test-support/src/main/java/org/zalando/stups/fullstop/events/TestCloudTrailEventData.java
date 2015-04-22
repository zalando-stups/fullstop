/**
 * Copyright 2015 Zalando SE
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.zalando.stups.fullstop.events;

import java.io.IOException;

import java.net.URISyntaxException;

import java.nio.file.Files;

import com.amazonaws.services.cloudtrail.processinglibrary.model.CloudTrailEvent;
import com.amazonaws.services.cloudtrail.processinglibrary.model.CloudTrailEventData;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;

/**
 * Creates {@link CloudTrailEvent}s with data from classpath-resources.
 *
 * @author  jbellmann
 */
public class TestCloudTrailEventData extends CloudTrailEventData {

    private String resource;

    public TestCloudTrailEventData(final String classpathResource) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(classpathResource),
            "'classpathResource' should never be null or empty.");
        this.resource = classpathResource;
    }

    @Override
    public String getResponseElements() {
        return getResponseElementsFromClasspath(resource);
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

    public static CloudTrailEvent createCloudTrailEvent(final String classpathResource) {
        return new CloudTrailEvent(new TestCloudTrailEventData(classpathResource), null);
    }

}
