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
import org.joda.time.DateTime;
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

    private DescribeImagesResult describeImagesResult;

    private AmazonEC2Client amazonEC2ClientMock;

    private LifecycleEntity lifecycleEntity;


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

        reset(userDataProviderMock, applicationLifecycleServiceMock, clientProviderMock, amazonEC2ClientMock);

        HashMap<Object, Object> value = newHashMap();
        value.put("application_id", "test");
        value.put("application_version", "test");

        plugin = new LifecyclePlugin(applicationLifecycleServiceMock, userDataProviderMock, clientProviderMock);
        processor = new LocalPluginProcessor(plugin);


        //Create an Image
        Image image = new Image();
        image.setName("Wooza");
        describeImagesResult = new DescribeImagesResult();
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
        when(
                clientProviderMock.getClient(
                        any(),
                        any(String.class),
                        any(Region.class))).thenReturn(amazonEC2ClientMock);

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


        when(amazonEC2ClientMock.describeImages(any(DescribeImagesRequest.class))).thenReturn(describeImagesResult);
        when(amazonEC2ClientMock.describeInstances(any(DescribeInstancesRequest.class))).thenReturn(describeInstancesResultMock);

    }

    @After
    public void tearDown() throws Exception {
        verifyNoMoreInteractions(userDataProviderMock, applicationLifecycleServiceMock, clientProviderMock, amazonEC2ClientMock);
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

        processor.processEvents(getClass().getResourceAsStream("/record-start.json"));

        verify(userDataProviderMock).getUserData(any(String.class), any(Region.class), any(String.class));
        verify(applicationLifecycleServiceMock).saveLifecycle(any(), any(), any());
        verify(clientProviderMock, atLeast(1)).getClient(any(), any(String.class), any(Region.class));
        verify(amazonEC2ClientMock).describeImages(any(DescribeImagesRequest.class));

    }

    @Test
    public void testNullEvent() throws Exception {

        processor.processEvents(getClass().getResourceAsStream("/record-broken.json"));

        verify(clientProviderMock, atLeast(1)).getClient(any(), any(String.class), any(Region.class));

    }

    @Test
    public void testAmiName() throws Exception {

        processor.processEvents(getClass().getResourceAsStream("/record-run.json"));

        verify(userDataProviderMock).getUserData(any(String.class), any(Region.class), any(String.class));
        verify(clientProviderMock, atLeast(1)).getClient(any(), any(String.class), any(Region.class));
        verify(applicationLifecycleServiceMock).saveLifecycle(any(), any(), any());
        verify(amazonEC2ClientMock).describeImages(any());
    }

    @Test
    public void testAmiIdJson() throws Exception {

        lifecycleEntity = new LifecycleEntity();
        lifecycleEntity.setImageId("ami-amishppl");
        lifecycleEntity.setImageName("Wooza");
        lifecycleEntity.setEventType("RunInstances");
        lifecycleEntity.setEventDate(new DateTime(1434616306000L));
        lifecycleEntity.setAccountId("123456789111");
        lifecycleEntity.setInstanceId("i-affenbanane");
        lifecycleEntity.setRegion("eu-west-1");

        processor.processEvents(getClass().getResourceAsStream("/record-run.json"));


        verify(userDataProviderMock).getUserData(any(String.class), any(Region.class), any(String.class));
        verify(clientProviderMock, atLeast(1)).getClient(any(), any(String.class), any(Region.class));

        ApplicationEntity applicationEntity = new ApplicationEntity("test");
        VersionEntity versionEntity = new VersionEntity("test");


        verify(applicationLifecycleServiceMock, atLeast(1)).saveLifecycle(eq(applicationEntity),eq(versionEntity),eq(lifecycleEntity));
        verify(amazonEC2ClientMock).describeImages(any());
    }

    @Test
    public void testAmiIdAmazon() throws Exception {

        lifecycleEntity = new LifecycleEntity();
        lifecycleEntity.setImageId("ami-666");
        lifecycleEntity.setImageName("Wooza");
        lifecycleEntity.setEventType("RunInstances");
        lifecycleEntity.setEventDate(new DateTime(1434616306000L));
        lifecycleEntity.setAccountId("123456789111");
        lifecycleEntity.setInstanceId("i-affenbanane");
        lifecycleEntity.setRegion("eu-west-1");

        processor.processEvents(getClass().getResourceAsStream("/record-missing-ami.json"));


        verify(userDataProviderMock).getUserData(any(String.class), any(Region.class), any(String.class));
        verify(clientProviderMock, atLeast(1)).getClient(any(), any(String.class), any(Region.class));

        ApplicationEntity applicationEntity = new ApplicationEntity("test");
        VersionEntity versionEntity = new VersionEntity("test");


        verify(applicationLifecycleServiceMock, atLeast(1)).saveLifecycle(eq(applicationEntity),eq(versionEntity),eq(lifecycleEntity));
        verify(amazonEC2ClientMock).describeImages(any());
        verify(amazonEC2ClientMock).describeInstances(any());
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


        processor.processEvents(getClass().getResourceAsStream("/record-start.json"));

        verify(userDataProviderMock).getUserData(any(String.class), any(Region.class), any(String.class));
        verify(clientProviderMock, atLeast(1)).getClient(any(), any(String.class), any(Region.class));
        verify(amazonEC2ClientMock).describeImages(any());


    }
}