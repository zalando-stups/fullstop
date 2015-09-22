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

import com.amazonaws.regions.Region;
import com.amazonaws.services.cloudtrail.processinglibrary.model.CloudTrailEvent;
import com.amazonaws.services.ec2.AmazonEC2Client;
import com.amazonaws.services.ec2.model.*;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.zalando.stups.fullstop.aws.ClientProvider;
import org.zalando.stups.fullstop.events.Records;
import org.zalando.stups.fullstop.events.TestCloudTrailEventData;
import org.zalando.stups.fullstop.events.UserDataProvider;
import org.zalando.stups.fullstop.violation.entity.ApplicationEntity;
import org.zalando.stups.fullstop.violation.entity.LifecycleEntity;
import org.zalando.stups.fullstop.violation.entity.VersionEntity;
import org.zalando.stups.fullstop.violation.service.impl.ApplicationLifecycleServiceImpl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Maps.newHashMap;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

/**
 * Created by gkneitschel.
 */

public class LifecyclePluginTest {

    private LifecyclePlugin plugin;

    private UserDataProvider userDataProviderMock;

    private ApplicationLifecycleServiceImpl applicationLifecycleServiceMock;

    private LocalPluginProcessor processor;

    private ClientProvider clientProviderMock;

    private DescribeImagesResult describeImagesResultMock;

    private DescribeInstancesResult describeInstancesResultMock;

    private AmazonEC2Client amazonEC2ClientMock;

    protected CloudTrailEvent buildEvent(String type) {
        List<Map<String, Object>> records = Records.fromClasspath("/record-" + type + ".json");

        return TestCloudTrailEventData.createCloudTrailEventFromMap(records.get(0));
    }

    @Before
    public void setUp() throws Exception {

        userDataProviderMock = mock(UserDataProvider.class);
        applicationLifecycleServiceMock = mock(ApplicationLifecycleServiceImpl.class);
        clientProviderMock = mock(ClientProvider.class);
        amazonEC2ClientMock = mock(AmazonEC2Client.class);

        plugin = new LifecyclePlugin(applicationLifecycleServiceMock, userDataProviderMock, clientProviderMock);
        processor = new LocalPluginProcessor(plugin);

        //Create an Image
        Image image = new Image();
        image.setName("amiName");
        describeImagesResultMock = new DescribeImagesResult();
        describeImagesResultMock.setImages(newArrayList(image));

        //create an instance
        Instance instance = new Instance();
        instance.setInstanceId("i-43210");
        Reservation reservation = new Reservation();
        reservation.setInstances(newArrayList(instance));
        describeInstancesResultMock = new DescribeInstancesResult();
        describeInstancesResultMock.setReservations(newArrayList(reservation));

        when(
                clientProviderMock.getClient(
                        any(),
                        any(String.class),
                        any(Region.class))).thenReturn(amazonEC2ClientMock);
    }

    @After
    public void tearDown() throws Exception {
        verifyNoMoreInteractions(userDataProviderMock, applicationLifecycleServiceMock, clientProviderMock);
    }

    @Test
    public void testSupports() throws Exception {
        CloudTrailEvent event = buildEvent("run");
        assertThat(plugin.supports(event)).isTrue();

        event = buildEvent("start");
        assertThat(plugin.supports(event)).isTrue();

        event = buildEvent("stop");
        assertThat(plugin.supports(event)).isTrue();

        event = buildEvent("termination");
        assertThat(plugin.supports(event)).isTrue();
    }

    @Test
    public void testProcessEvent() throws Exception {
        HashMap<Object, Object> value = newHashMap();
        value.put("application_id", "test");
        value.put("application_version", "test");

        when(
                userDataProviderMock.getUserData(
                        any(String.class),
                        any(Region.class),
                        any(String.class))).thenReturn(value);
        when(
                applicationLifecycleServiceMock.saveLifecycle(
                        any(ApplicationEntity.class),
                        any(VersionEntity.class),
                        any(LifecycleEntity.class))).thenReturn(new LifecycleEntity());

        when(amazonEC2ClientMock.describeImages(any(DescribeImagesRequest.class))).thenReturn(describeImagesResultMock);

        processor.processEvents(getClass().getResourceAsStream("/record-start.json"));

        verify(userDataProviderMock).getUserData(any(String.class), any(Region.class), any(String.class));
        verify(applicationLifecycleServiceMock).saveLifecycle(any(), any(), any());
        verify(clientProviderMock, atLeast(1)).getClient(any(), any(String.class), any(Region.class));

    }

    @Test
    public void testNullEvent() throws Exception {
        when(amazonEC2ClientMock.describeImages(any(DescribeImagesRequest.class))).thenReturn(describeImagesResultMock);
        processor.processEvents(getClass().getResourceAsStream("/record-broken.json"));
        verify(clientProviderMock, atLeast(1)).getClient(any(), any(String.class), any(Region.class));

    }

    @Test
    public void testAmiName() throws Exception {

        HashMap<Object, Object> value = newHashMap();
        value.put("application_id", "test");
        value.put("application_version", "test");

        when(
                userDataProviderMock.getUserData(
                        any(String.class),
                        any(Region.class),
                        any(String.class))).thenReturn(value);
        when(
                applicationLifecycleServiceMock.saveLifecycle(
                        any(ApplicationEntity.class),
                        any(VersionEntity.class),
                        any(LifecycleEntity.class))).thenReturn(new LifecycleEntity());


        when(amazonEC2ClientMock.describeImages(any(DescribeImagesRequest.class))).thenReturn(describeImagesResultMock);
        when(amazonEC2ClientMock.describeInstances(any(DescribeInstancesRequest.class))).thenReturn(describeInstancesResultMock);

        processor.processEvents(getClass().getResourceAsStream("/record-run.json"));

        verify(userDataProviderMock).getUserData(any(String.class), any(Region.class), any(String.class));
        verify(clientProviderMock, atLeast(1)).getClient(any(), any(String.class), any(Region.class));
        verify(applicationLifecycleServiceMock).saveLifecycle(any(), any(), any());
    }

    @Test
    public void testEmptyUserData() throws Exception {
        HashMap<Object, Object> value = newHashMap();
        value.put(null, null);
        value.put(null, null);

        when(
                userDataProviderMock.getUserData(
                        any(String.class),
                        any(Region.class),
                        any(String.class))).thenReturn(value);

        when(amazonEC2ClientMock.describeImages(any(DescribeImagesRequest.class))).thenReturn(describeImagesResultMock);

        processor.processEvents(getClass().getResourceAsStream("/record-start.json"));

        verify(userDataProviderMock).getUserData(any(String.class), any(Region.class), any(String.class));
        verify(clientProviderMock, atLeast(1)).getClient(any(), any(String.class), any(Region.class));

    }
}