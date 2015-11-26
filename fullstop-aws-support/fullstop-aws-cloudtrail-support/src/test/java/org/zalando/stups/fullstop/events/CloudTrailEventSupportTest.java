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

import com.amazonaws.services.cloudtrail.processinglibrary.model.CloudTrailEvent;
import org.junit.Test;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.zalando.stups.fullstop.events.CloudTrailEventSupport.*;
import static org.zalando.stups.fullstop.events.TestCloudTrailEventSerializer.createCloudTrailEvent;

/**
 * @author jbellmann
 */
public class CloudTrailEventSupportTest {

    @Test
    public void getAmisTest() {
        List<String> instances = getInstances(createCloudTrailEvent("/responseElements.json"));
        for (String instance : instances) {
            Optional<String> ami = getAmi(instance);
            assertThat(ami).isPresent();
            assertThat(ami.get()).isNotEmpty();
        }
    }

    @Test
    public void getInstanceIdsTest() {
        List<String> instanceIds = getInstanceIds(createCloudTrailEvent("/responseElements.json"));
        assertThat(instanceIds).isNotEmpty();
    }

    @Test
    public void getInstancesTest() {
        CloudTrailEvent cloudTrailEvent = createCloudTrailEvent("/responseElements.json");
        List<String> instances = getInstances(cloudTrailEvent);
        assertThat(instances).isNotEmpty();
    }

    @Test(expected = NullPointerException.class)
    public void getInstanceIdsNullEvent() {
        List<String> instanceIds = getInstanceIds(null);
        assertThat(instanceIds).isNotEmpty();
    }

    @Test(expected = NullPointerException.class)
    public void getInstanceIdsNullEventData() {
        List<String> instanceIds = getInstanceIds(new CloudTrailEvent(null, null));
        assertThat(instanceIds).isNotEmpty();
    }

    @Test
    public void testNullResponseElementsAmis() {
        List<String> instanceIds = getInstanceIds(createCloudTrailEvent("/empty-responseElements.json"));
        assertThat(instanceIds).isEmpty();
    }
}
