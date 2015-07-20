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
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
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

    private CloudTrailEvent event;

    protected CloudTrailEvent buildEvent(String type) {
        List<Map<String, Object>> records = Records.fromClasspath("/record-" + type + ".json");

        return TestCloudTrailEventData.createCloudTrailEventFromMap(records.get(0));
    }

    @Before
    public void setUp() throws Exception {

        userDataProviderMock = mock(UserDataProvider.class);
        applicationLifecycleServiceMock = mock(ApplicationLifecycleServiceImpl.class);
        plugin = new LifecyclePlugin(applicationLifecycleServiceMock, userDataProviderMock);
        processor = new LocalPluginProcessor(plugin);
    }

    @After
    public void tearDown() throws Exception {
        verifyNoMoreInteractions(userDataProviderMock, applicationLifecycleServiceMock);
    }



    @Test
    public void testSupports() throws Exception {
        event = buildEvent("run");
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

        when(userDataProviderMock.getUserData(any(String.class), any(Region.class), any(String.class))).thenReturn(value);
        when(applicationLifecycleServiceMock.saveLifecycle(
                any(ApplicationEntity.class),
                any(VersionEntity.class),
                any(LifecycleEntity.class))).thenReturn(new LifecycleEntity());

        processor.processEvents(getClass().getResourceAsStream("/record-start.json"));

        verify(userDataProviderMock, atLeast(2)).getUserData(any(String.class), any(Region.class), any(String.class));
        verify(applicationLifecycleServiceMock).saveLifecycle(any(), any(), any());
    }

    @Test
    public void testNullEvent() throws Exception{
        processor.processEvents(getClass().getResourceAsStream("/record-broken.json"));
    }
}