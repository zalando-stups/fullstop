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
import com.google.common.collect.Maps;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;
import org.zalando.stups.clients.kio.KioOperations;
import org.zalando.stups.fullstop.clients.pierone.PieroneOperations;
import org.zalando.stups.fullstop.clients.pierone.TagSummary;
import org.zalando.stups.fullstop.events.Records;
import org.zalando.stups.fullstop.events.TestCloudTrailEventData;
import org.zalando.stups.fullstop.events.UserDataProvider;
import org.zalando.stups.fullstop.plugin.config.RegistryPluginProperties;
import org.zalando.stups.fullstop.violation.Violation;
import org.zalando.stups.fullstop.violation.ViolationSink;

import java.util.List;
import java.util.Map;

import static java.util.Collections.emptyMap;
import static java.util.Collections.singletonMap;
import static org.mockito.Mockito.*;

public class RegistryPluginPieroneTest {

    private static final String APP = "fullstop";

    private static final String VERSION = "1.0";

    private static final String TEAM = "stups";

    private static final String ARTIFACT = "docker://stups/yourturn:1.0";
    private static final String INSTANCE_ID = "i-12345";


    private KioOperations kioOperations;

    private PieroneOperations pieroneOperations;

    private CloudTrailEvent event;

    private ViolationSink violationSink;

    private UserDataProvider userDataProvider;

    private RegistryPlugin registryPlugin;

    protected CloudTrailEvent buildEvent() {
        List<Map<String, Object>> records = Records.fromClasspath("/record.json");

        return TestCloudTrailEventData.createCloudTrailEventFromMap(records.get(0));
    }

    @Before
    public void setUp() {
        final RegistryPluginProperties pluginConfiguration = new RegistryPluginProperties();

        event = buildEvent();
        userDataProvider = mock(UserDataProvider.class);
        kioOperations = mock(KioOperations.class);
        pieroneOperations = mock(PieroneOperations.class);
        violationSink = mock(ViolationSink.class);
        registryPlugin = new RegistryPlugin(
                userDataProvider,
                violationSink,
                pieroneOperations,
                kioOperations,
                pluginConfiguration);
    }

    @After
    public void tearDown() {
        verifyNoMoreInteractions(
                userDataProvider,
                kioOperations,
                pieroneOperations,
                violationSink);
    }

    @Test
    public void shouldComplainIfArtifactDoesNotContainSource() {
        when(pieroneOperations.listTags(TEAM, APP)).thenReturn(singletonMap(VERSION, mock(TagSummary.class)));
        registryPlugin.validateSourceWithPierone(
                event,
                APP,
                VERSION,
                TEAM,
                "stups/yourturn:2.0",
                ARTIFACT,
                INSTANCE_ID);
        verify(pieroneOperations).listTags(
                TEAM,
                APP);
        verify(violationSink).put(any(Violation.class));
    }

    @Test
    public void shouldComplainPieroneTagsAreEmpty() {
        when(pieroneOperations.listTags(TEAM, APP)).thenReturn(emptyMap());
        registryPlugin.validateSourceWithPierone(
                event,
                APP,
                VERSION,
                TEAM,
                ARTIFACT,
                ARTIFACT,
                INSTANCE_ID);
        verify(pieroneOperations).listTags(TEAM, APP);
        verify(violationSink).put(any(Violation.class));
    }

    @Test
    public void shouldComplainIfTagIsMissingInPierone() {
        when(pieroneOperations.listTags(TEAM, APP)).thenReturn(singletonMap("2.0", mock(TagSummary.class)));

        registryPlugin.validateSourceWithPierone(
                event,
                APP,
                VERSION,
                TEAM,
                ARTIFACT,
                ARTIFACT,
                INSTANCE_ID);
        verify(pieroneOperations).listTags(
                TEAM,
                APP);
        verify(violationSink).put(any(Violation.class));
    }

    @Test
    public void shouldNotComplainIfArtifactContainsSourceAndTagIsInPierone() {
        when(pieroneOperations.listTags(TEAM, APP)).thenReturn(singletonMap(VERSION, mock(TagSummary.class)));
        registryPlugin.validateSourceWithPierone(
                event,
                APP,
                VERSION,
                TEAM,
                "stups/yourturn",
                ARTIFACT,
                INSTANCE_ID);
        verify(pieroneOperations).listTags(
                TEAM,
                APP);
        verify(
                violationSink,
                never()).put(any(Violation.class));
    }

    @Test
    public void shouldComplainIfThereIsNoScmSource() {

        when(
                pieroneOperations.getScmSource(
                        TEAM,
                        APP,
                        VERSION)).thenThrow(new HttpClientErrorException(HttpStatus.NOT_FOUND));
        registryPlugin.validateScmSource(
                event,
                TEAM,
                APP,
                VERSION,
                INSTANCE_ID);
        verify(pieroneOperations).getScmSource(
                TEAM,
                APP,
                VERSION);
        verify(violationSink).put(any(Violation.class));
    }

    @Test
    public void shouldComplainIfScmSourceIsEmpty() {
        Map<String, String> scmSource = Maps.newHashMap();
        when(
                pieroneOperations.getScmSource(
                        TEAM,
                        APP,
                        VERSION)).thenReturn(scmSource);
        registryPlugin.validateScmSource(
                event,
                TEAM,
                APP,
                VERSION,
                INSTANCE_ID);
        verify(pieroneOperations).getScmSource(
                TEAM,
                APP,
                VERSION);
        verify(violationSink).put(any(Violation.class));
    }

    @Test
    public void shouldNotComplainIfScmSourceIsNotEmpty() {
        Map<String, String> scmSource = Maps.newHashMap();
        scmSource.put(
                "revision",
                "abcdef");
        when(
                pieroneOperations.getScmSource(
                        TEAM,
                        APP,
                        VERSION)).thenReturn(scmSource);
        registryPlugin.validateScmSource(
                event,
                TEAM,
                APP,
                VERSION,
                INSTANCE_ID);
        verify(pieroneOperations).getScmSource(
                TEAM,
                APP,
                VERSION);
        verify(
                violationSink,
                never()).put(any(Violation.class));
    }
}
