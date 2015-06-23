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
import static org.mockito.Mockito.verifyNoMoreInteractions;

import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;
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

public class RegistryPluginPieroneTest {

    private KioOperations kioOperations;

    private PieroneOperations pieroneOperations;

    private CloudTrailEvent event;

    private ViolationSink violationSink;

    private UserDataProvider userDataProvider;

    private RegistryPlugin registryPlugin;

    private RegistryPluginProperties pluginConfiguration;

    private static final String APP = "fullstop";

    private static final String VERSION = "1.0";

    private static final String TEAM = "stups";

    private static final String ARTIFACT = "docker://stups/yourturn:1.0";

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
    public void shouldComplainIfSourceDoesNotMatchArtifact() {
        Map<String, String> tags = Maps.newHashMap();
        tags.put(VERSION,
                 ARTIFACT);
        when(pieroneOperations.listTags(TEAM,
                                        APP)).thenReturn(tags);
        registryPlugin.validateSourceWithPierone(event,
                                                 APP,
                                                 VERSION,
                                                 TEAM,
                                                 ARTIFACT,
                                                 "docker://stups/yourturn:2.0");
        verify(pieroneOperations).listTags(TEAM,
                                           APP);
        verify(violationSink).put(any(Violation.class));
    }

    @Test
    public void shouldComplainPieroneTagsAreEmpty() {
        Map<String, String> tags = Maps.newHashMap();
        when(pieroneOperations.listTags(TEAM,
                                        APP)).thenReturn(tags);
        registryPlugin.validateSourceWithPierone(event,
                                                 APP,
                                                 VERSION,
                                                 TEAM,
                                                 ARTIFACT,
                                                 ARTIFACT);
        verify(pieroneOperations).listTags(TEAM,
                                           APP);
        verify(violationSink).put(any(Violation.class));
    }

    @Test
    public void shouldComplainIfTagIsMissingInPierone() {
        Map<String, String> tags = Maps.newHashMap();
        tags.put("2.0",
                 "aasdf");
        when(pieroneOperations.listTags(TEAM,
                                        APP)).thenReturn(tags);
        registryPlugin.validateSourceWithPierone(event,
                                                 APP,
                                                 VERSION,
                                                 TEAM,
                                                 ARTIFACT,
                                                 ARTIFACT);
        verify(pieroneOperations).listTags(TEAM,
                                           APP);
        verify(violationSink).put(any(Violation.class));
    }

    @Test
    public void shouldNotComplainIfSourceMatchesArtifactAndTagIsInPierone() {
        Map<String, String> tags = Maps.newHashMap();
        tags.put(VERSION,
                 "docker://stups/yourturn:1.0");
        when(pieroneOperations.listTags(TEAM,
                                        APP)).thenReturn(tags);
        registryPlugin.validateSourceWithPierone(event,
                                                 APP,
                                                 VERSION,
                                                 TEAM,
                                                 ARTIFACT,
                                                 ARTIFACT);
        verify(pieroneOperations).listTags(TEAM,
                                           APP);
        verify(violationSink,
               never()).put(any(Violation.class));
    }

    @Test
    public void shouldComplainIfThereIsNoScmSource() {

        when(pieroneOperations.getScmSource(TEAM,
                                            APP,
                                            VERSION)).thenThrow(new HttpClientErrorException(HttpStatus.NOT_FOUND));
        registryPlugin.validateScmSource(event,
                                         TEAM,
                                         APP,
                                         VERSION);
        verify(pieroneOperations).getScmSource(TEAM,
                                               APP,
                                               VERSION);
        verify(violationSink).put(any(Violation.class));
    }

    @Test
    public void shouldComplainIfScmSourceIsEmpty() {
        Map<String, String> scmSource = Maps.newHashMap();
        when(pieroneOperations.getScmSource(TEAM,
                                            APP,
                                            VERSION)).thenReturn(scmSource);
        registryPlugin.validateScmSource(event,
                                         TEAM,
                                         APP,
                                         VERSION);
        verify(pieroneOperations).getScmSource(TEAM,
                                               APP,
                                               VERSION);
        verify(violationSink).put(any(Violation.class));
    }

    @Test
    public void shouldNotComplainIfScmSourceIsNotEmpty() {
        Map<String, String> scmSource = Maps.newHashMap();
        scmSource.put("revision",
                      "abcdef");
        when(pieroneOperations.getScmSource(TEAM,
                                            APP,
                                            VERSION)).thenReturn(scmSource);
        registryPlugin.validateScmSource(event,
                                         TEAM,
                                         APP,
                                         VERSION);
        verify(pieroneOperations).getScmSource(TEAM,
                                               APP,
                                               VERSION);
        verify(violationSink,
               never()).put(any(Violation.class));
    }
}
