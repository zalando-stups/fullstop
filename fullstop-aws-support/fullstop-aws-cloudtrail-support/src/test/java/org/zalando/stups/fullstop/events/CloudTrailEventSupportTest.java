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
import com.amazonaws.services.cloudtrail.processinglibrary.model.CloudTrailEventData;
import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.zalando.stups.fullstop.events.CloudTrailEventSupport.*;
import static org.zalando.stups.fullstop.events.TestCloudTrailEventSerializer.createCloudTrailEvent;

/**
 * @author jbellmann
 */
public class CloudTrailEventSupportTest {

    private CloudTrailEvent cloudTrailEvent;

    private CloudTrailEventData cloudTrailEventData;

    @Before
    public void setUp() {
        cloudTrailEvent = Mockito.mock(CloudTrailEvent.class);
        cloudTrailEventData = Mockito.mock(CloudTrailEventData.class);

        Mockito.when(cloudTrailEvent.getEventData()).thenReturn(cloudTrailEventData);
    }

    @Test
    public void getAmisTest() {
        List<String> instances = CloudTrailEventSupport.getInstances(createCloudTrailEvent("/responseElements.json"));
        for (String instance : instances) {
            String ami = getAmi(instance);
            Assertions.assertThat(ami).isNotEmpty();
        }

    }

    @Test
    public void getInstanceIdsTest() {
        List<String> instanceIds = getInstanceIds(createCloudTrailEvent("/responseElements.json"));
        Assertions.assertThat(instanceIds).isNotEmpty();
    }

    @Test
    public void getInstancesTest() {
        CloudTrailEvent cloudTrailEvent = createCloudTrailEvent("/responseElements.json");
        List<String> instances = getInstances(cloudTrailEvent);
        Assertions.assertThat(instances).isNotEmpty();
    }

    @Test(expected = NullPointerException.class)
    public void getInstanceIdsNullEvent() {
        List<String> instanceIds = getInstanceIds(null);
        Assertions.assertThat(instanceIds).isNotEmpty();
    }

    @Test(expected = NullPointerException.class)
    public void getInstanceIdsNullEventData() {
        List<String> instanceIds = getInstanceIds(new CloudTrailEvent(null, null));
        Assertions.assertThat(instanceIds).isNotEmpty();
    }

    @Test
    public void testNullResponseElementsAmis() {
        List<String> instanceIds = getInstanceIds(createCloudTrailEvent("/empty-responseElements.json"));
        Assertions.assertThat(instanceIds).isEmpty();
    }

    @Test
    public void EventSourcePredicateTrue() {
        when(cloudTrailEventData.getEventSource()).thenReturn("ec2.amazonaws.com");
        assertThat(EC2_EVENT.test(cloudTrailEvent)).isTrue();
    }

    @Test
    public void EventSourcePredicateFalse() {
        when(cloudTrailEventData.getEventSource()).thenReturn("ec3.amazonaws.com");
        assertThat(EC2_EVENT.test(cloudTrailEvent)).isFalse();
    }

    @Test
    public void EventNamePredicateFalse() {
        when(cloudTrailEventData.getEventName()).thenReturn("RunNothing");
        assertThat(RUN_INSTANCES.test(cloudTrailEvent)).isFalse();
    }

    @Test
    public void EventNamePredicateTrue() {
        when(cloudTrailEventData.getEventName()).thenReturn("RunInstances");
        assertThat(RUN_INSTANCES.test(cloudTrailEvent)).isTrue();
    }

    @Test(expected = IllegalArgumentException.class)
    public void nullArgumentEventSourcePredicate() {
        new EventSourcePredicate(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void nullArgumentEventNamePredicate() {
        new EventNamePredicate(null);
    }

    @Test
    public void testCloudTrailEventPredicateComposite() {
        when(cloudTrailEventData.getEventName()).thenReturn("RunInstances");
        when(cloudTrailEventData.getEventSource()).thenReturn("ec2.amazonaws.com");

        CloudTrailEventPredicate predicate = CloudTrailEventPredicate.fromSource("ec2.amazonaws.com");
        predicate = predicate.andWith(CloudTrailEventPredicate.withName("RunInstances"));

        assertThat(predicate.test(cloudTrailEvent)).isTrue();
    }

    @Test
    public void testCloudTrailEventPredicateCompositeResultsFalse() {
        when(cloudTrailEventData.getEventName()).thenReturn("RunInstances");
        when(cloudTrailEventData.getEventSource()).thenReturn("ec3.amazonaws.com");

        CloudTrailEventPredicate predicate = CloudTrailEventPredicate.fromSource("ec2.amazonaws.com");
        predicate = predicate.andWith(CloudTrailEventPredicate.withName("RunInstances"));

        assertThat(predicate.test(cloudTrailEvent)).isFalse();
    }
}
