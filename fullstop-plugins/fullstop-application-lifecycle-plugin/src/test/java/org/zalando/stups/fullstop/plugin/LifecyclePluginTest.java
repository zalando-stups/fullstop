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
package org.zalando.stups.fullstop.plugin;

import com.amazonaws.services.cloudtrail.processinglibrary.model.CloudTrailEvent;
import com.amazonaws.services.cloudtrail.processinglibrary.model.CloudTrailEventData;
import com.amazonaws.services.ec2.model.*;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.zalando.stups.fullstop.violation.entity.ApplicationEntity;
import org.zalando.stups.fullstop.violation.entity.LifecycleEntity;
import org.zalando.stups.fullstop.violation.entity.VersionEntity;
import org.zalando.stups.fullstop.violation.service.ApplicationLifecycleService;

import java.util.Date;
import java.util.HashMap;
import java.util.Optional;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Maps.newHashMap;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;
import static org.zalando.stups.fullstop.events.TestCloudTrailEventSerializer.createCloudTrailEvent;
import static org.zalando.stups.fullstop.plugin.AbstractEC2InstancePlugin.RUN_INSTANCES;
import static org.zalando.stups.fullstop.plugin.AbstractEC2InstancePlugin.START_INSTANCES;

public class LifecyclePluginTest {

    private LifecyclePlugin plugin;

    private ApplicationLifecycleService applicationLifecycleServiceMock;

    private EC2InstanceContextProvider contextProviderMock;
    private EC2InstanceContext contextMock;

    @Before
    public void setUp() throws Exception {
        contextProviderMock = mock(EC2InstanceContextProvider.class);
        applicationLifecycleServiceMock = mock(ApplicationLifecycleService.class);
        contextMock = mock(EC2InstanceContext.class);

        HashMap<Object, Object> value = newHashMap();
        value.put("application_id", "test");
        value.put("application_version", "test");

        plugin = new LifecyclePlugin(contextProviderMock, applicationLifecycleServiceMock);

        //Create an Image
        Image image = new Image();
        image.setName("Wooza");
        DescribeImagesResult describeImagesResult = new DescribeImagesResult();
        describeImagesResult.setImages(newArrayList(image));

        //create an instance
        Instance instance = new Instance();
        instance.setInstanceId("i-43210");
        instance.setImageId("ami-666");
        Reservation reservation = new Reservation();
        reservation.setInstances(newArrayList(instance));
        DescribeInstancesResult describeInstancesResultMock = new DescribeInstancesResult();
        describeInstancesResultMock.setReservations(newArrayList(reservation));

        //Mocked calls


        when(applicationLifecycleServiceMock.saveLifecycle(
                any(ApplicationEntity.class),
                any(VersionEntity.class),
                any(LifecycleEntity.class)))
                .thenReturn(new LifecycleEntity());

    }

    @After
    public void tearDown() throws Exception {
        verifyNoMoreInteractions(contextProviderMock, applicationLifecycleServiceMock);
    }

    @Test
    public void testSupports() throws Exception {
        assertThat(plugin.supports(createCloudTrailEvent("/record-run.json"))).isTrue();

        assertThat(plugin.supports(createCloudTrailEvent("/record-start.json"))).isTrue();

        assertThat(plugin.supports(createCloudTrailEvent("/record-stop.json"))).isTrue();

        assertThat(plugin.supports(createCloudTrailEvent("/record-termination.json"))).isTrue();
    }

    @Test
    public void testProcessRunInstanceEvent() throws Exception {
        when(contextMock.getEventName()).thenReturn(RUN_INSTANCES);
        when(contextMock.getInstanceJson()).thenReturn(
                "{\"launchTime\": 1434616306000}");
        when(contextMock.getAmiId()).thenReturn(Optional.of("ami-01234567"));
        when(contextMock.getAmi().map(Image::getName)).thenReturn(Optional.of("Foobar"));
        when(contextMock.getApplicationId()).thenReturn(Optional.of("hello-world"));
        when(contextMock.getVersionId()).thenReturn(Optional.of("1.0"));

        plugin.process(contextMock);

        verify(applicationLifecycleServiceMock).saveLifecycle(any(), any(), any());
    }

    @Test
    public void testMissingApplicationId() throws Exception {
        when(contextMock.getEventName()).thenReturn(RUN_INSTANCES);
        when(contextMock.getInstanceJson()).thenReturn(
                "{\"launchTime\": 1434616306000}");
        when(contextMock.getAmiId()).thenReturn(Optional.of("ami-01234567"));
        when(contextMock.getAmi().map(Image::getName)).thenReturn(Optional.of("Foobar"));
        when(contextMock.getApplicationId()).thenReturn(Optional.empty());
        when(contextMock.getVersionId()).thenReturn(Optional.of("1.0"));

        plugin.process(contextMock);
    }

    @Test
    public void testMissingVersionId() throws Exception {
        when(contextMock.getEventName()).thenReturn(RUN_INSTANCES);
        when(contextMock.getInstanceJson()).thenReturn(
                "{\"launchTime\": 1434616306000}");
        when(contextMock.getAmiId()).thenReturn(Optional.of("ami-01234567"));
        when(contextMock.getAmi().map(Image::getName)).thenReturn(Optional.of("Foobar"));
        when(contextMock.getApplicationId()).thenReturn(Optional.of("hello-world"));
        when(contextMock.getVersionId()).thenReturn(Optional.empty());

        plugin.process(contextMock);
    }

    @Test
    public void testProcessStartInstanceEvent() throws Exception {
        final CloudTrailEventData eventData = mock(CloudTrailEventData.class);
        when(eventData.getEventTime()).thenReturn(new Date());
        when(contextMock.getEventName()).thenReturn(START_INSTANCES);
        when(contextMock.getEvent()).thenReturn(new CloudTrailEvent(eventData, null));
        when(contextMock.getAmiId()).thenReturn(Optional.of("ami-01234567"));
        when(contextMock.getAmi().map(Image::getName)).thenReturn(Optional.of("Foobar"));
        when(contextMock.getApplicationId()).thenReturn(Optional.of("hello-world"));
        when(contextMock.getVersionId()).thenReturn(Optional.of("1.0"));

        plugin.process(contextMock);

        verify(applicationLifecycleServiceMock).saveLifecycle(any(), any(), any());
    }
}
