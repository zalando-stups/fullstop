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

import static org.mockito.Mockito.mock;

import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.zalando.stups.clients.kio.KioOperations;
import org.zalando.stups.fullstop.clients.pierone.PieroneOperations;
import org.zalando.stups.fullstop.events.Records;
import org.zalando.stups.fullstop.events.TestCloudTrailEventData;
import org.zalando.stups.fullstop.events.UserDataProvider;
import org.zalando.stups.fullstop.plugin.config.RegistryPluginProperties;
import org.zalando.stups.fullstop.violation.Violation;
import org.zalando.stups.fullstop.violation.ViolationSink;

import com.amazonaws.services.cloudtrail.processinglibrary.model.CloudTrailEvent;
import com.google.common.collect.Maps;

public class RegistryPluginUserdataTest {

    private KioOperations kioOperations;

    private PieroneOperations pieroneOperations;

    private CloudTrailEvent event;

    private ViolationSink violationSink;

    private UserDataProvider userDataProvider;

    private RegistryPlugin registryPlugin;

    private RegistryPluginProperties pluginConfiguration;

    protected CloudTrailEvent buildEvent() {
        List<Map<String, Object>> records = Records.fromClasspath("/record.json");

        CloudTrailEvent event = TestCloudTrailEventData.createCloudTrailEventFromMap(records.get(0));
        return event;
    }

    @Before
    public void setUp() {
        event = buildEvent();
        userDataProvider = mock(UserDataProvider.class);
        kioOperations = mock(KioOperations.class);
        pieroneOperations = mock(PieroneOperations.class);
        violationSink = mock(ViolationSink.class);
        pluginConfiguration = new RegistryPluginProperties();
        registryPlugin = new RegistryPlugin(userDataProvider,
                                            violationSink,
                                            pieroneOperations,
                                            kioOperations,
                                            pluginConfiguration);
    }

    @After
    public void tearDown() {
        verifyNoMoreInteractions(userDataProvider,
                                 kioOperations,
                                 pieroneOperations,
                                 violationSink);
    }

    @Test
    public void shouldComplainWithMissingUserData() {
        when(userDataProvider.getUserData(any(),
                                          any())).thenReturn(null);
        registryPlugin.getAndValidateUserData(event,
                                              "foo");

        verify(userDataProvider).getUserData(any(),
                                             any());
        verify(violationSink).put(any(Violation.class));
    }

    @Test
    public void shouldComplainWithEmptyUserData() {
        Map<String, String> userData = Maps.newHashMap();
        when(userDataProvider.getUserData(any(),
                                          any())).thenReturn(userData);

        registryPlugin.getAndValidateUserData(event,
                                              "foo");

        verify(userDataProvider).getUserData(any(),
                                             any());

        verify(violationSink).put(any(Violation.class));
    }

    @Test
    public void shouldNotComplainWithFilledUserData() {
        Map<String, String> userData = Maps.newHashMap();
        userData.put("foo",
                     "bar");
        when(userDataProvider.getUserData(any(),
                                          any())).thenReturn(userData);

        registryPlugin.getAndValidateUserData(event,
                                              "foo");

        verify(userDataProvider).getUserData(any(),
                                             any());

        verify(violationSink,
               never()).put(any(Violation.class));
    }

    @Test
    public void shouldComplainWithoutApplicationId() {
        Map<String, String> userData = Maps.newHashMap();
        userData.put("foo",
                     "bar");

        registryPlugin.getAndValidateApplicationId(event,
                                                   userData,
                                                   "foo");

        verify(violationSink).put(any(Violation.class));
    }

    @Test
    public void shouldNotComplainWithApplicationId() {
        Map<String, String> userData = Maps.newHashMap();
        userData.put(RegistryPlugin.APPLICATION_ID,
                     "bar");

        registryPlugin.getAndValidateApplicationId(event,
                                                   userData,
                                                   "foo");

        verify(violationSink,
               never()).put(any(Violation.class));
    }

    @Test
    public void shouldComplainWithoutApplicationVersion() {
        Map<String, String> userData = Maps.newHashMap();
        userData.put("foo",
                     "bar");

        registryPlugin.getAndValidateApplicationVersion(event,
                                                        userData,
                                                        "foo");

        verify(violationSink).put(any(Violation.class));
    }

    @Test
    public void shouldNotComplainWithApplicationVersion() {
        Map<String, String> userData = Maps.newHashMap();
        userData.put(RegistryPlugin.APPLICATION_VERSION,
                     "bar");

        registryPlugin.getAndValidateApplicationVersion(event,
                                                        userData,
                                                        "foo");

        verify(violationSink,
               never()).put(any(Violation.class));
    }

    @Test
    public void shouldComplainWithoutSource() {
        Map<String, String> userData = Maps.newHashMap();
        userData.put("foo",
                     "bar");

        registryPlugin.getAndValidateSource(event,
                                            userData,
                                            "foo");

        verify(violationSink).put(any(Violation.class));
    }

    @Test
    public void shouldNotComplainWithSource() {
        Map<String, String> userData = Maps.newHashMap();
        userData.put(RegistryPlugin.SOURCE,
                     "bar");

        registryPlugin.getAndValidateSource(event,
                                            userData,
                                            "foo");

        verify(violationSink,
               never()).put(any(Violation.class));
    }
}
