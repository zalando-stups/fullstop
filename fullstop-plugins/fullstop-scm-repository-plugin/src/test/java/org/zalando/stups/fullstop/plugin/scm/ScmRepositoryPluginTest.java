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
package org.zalando.stups.fullstop.plugin.scm;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.amazonaws.services.cloudtrail.processinglibrary.model.CloudTrailEvent;
import com.google.common.collect.ImmutableMap;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.zalando.kontrolletti.KontrollettiOperations;
import org.zalando.stups.clients.kio.Application;
import org.zalando.stups.clients.kio.KioOperations;
import org.zalando.stups.fullstop.clients.pierone.PieroneOperations;
import org.zalando.stups.fullstop.events.UserDataProvider;
import org.zalando.stups.fullstop.plugin.LocalPluginProcessor;
import org.zalando.stups.fullstop.violation.Violation;
import org.zalando.stups.fullstop.violation.ViolationSink;

public class ScmRepositoryPluginTest {

    private LocalPluginProcessor processor;

    private ViolationSink mockViolationSink;

    private KioOperations mockKioOperations;

    private PieroneOperations mockPieroneOperations;

    private KontrollettiOperations mockKontrollettiOperations;

    private UserDataProvider mockUserDataProvider;

    private Application kioApp;

    @Before
    public void setUp() throws Exception {
        mockViolationSink = mock(ViolationSink.class);
        mockKioOperations = mock(KioOperations.class);
        mockPieroneOperations = mock(PieroneOperations.class);
        mockKontrollettiOperations = mock(KontrollettiOperations.class);
        mockUserDataProvider = mock(UserDataProvider.class);

        final ScmRepositoryPlugin plugin = new ScmRepositoryPlugin(
                mockViolationSink,
                mockKioOperations,
                mockPieroneOperations,
                mockKontrollettiOperations,
                mockUserDataProvider);

        processor = new LocalPluginProcessor(plugin);

        kioApp = new Application();
        kioApp.setScmUrl("git@github.com:zalando-stups/hello-world.git");
    }

    @After
    public void tearDown() throws Exception {
        //        verifyNoMoreInteractions(
        //                mockViolationSink,
        //                mockKioOperations,
        //                mockPieroneOperations,
        //                mockKontrollettiOperations,
        //                mockUserDataProvider);
    }

    @Test
    public void testProcessEventNoViolation() throws Exception {
        when(mockUserDataProvider.getUserData(any(CloudTrailEvent.class), anyString()))
                .thenReturn(
                        ImmutableMap.of(
                                "source", "stups/hello-world:0.1",
                                "application_id", "hello-world"));
        when(mockKioOperations.getApplicationById(anyString())).thenReturn(kioApp);
        when(mockPieroneOperations.getScmSource(anyString(), anyString(), anyString()))
                .thenReturn(ImmutableMap.of("url", "https://github.com/hello-world/fullstop"));
        when(mockKontrollettiOperations.normalizeRepositoryUrl(anyString())).thenReturn(
                "https://github.com/zalando-stups/fullstop.git");
        processor.processEvents(getClass().getResourceAsStream("/run-instance-record.json"));

        verify(mockViolationSink, never()).put(any(Violation.class));
    }
}