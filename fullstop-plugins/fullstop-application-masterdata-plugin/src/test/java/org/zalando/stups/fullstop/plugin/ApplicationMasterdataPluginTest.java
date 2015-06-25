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

import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Map;

import org.assertj.core.util.Lists;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.zalando.stups.clients.kio.Application;
import org.zalando.stups.clients.kio.KioOperations;
import org.zalando.stups.clients.kio.NotFoundException;
import org.zalando.stups.fullstop.events.Records;
import org.zalando.stups.fullstop.events.TestCloudTrailEventData;
import org.zalando.stups.fullstop.events.UserDataProvider;
import org.zalando.stups.fullstop.plugin.config.ApplicationMasterdataPluginProperties;
import org.zalando.stups.fullstop.violation.Violation;
import org.zalando.stups.fullstop.violation.ViolationSink;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.cloudtrail.processinglibrary.model.CloudTrailEvent;
import com.google.common.collect.Maps;

public class ApplicationMasterdataPluginTest {
    private static final String APP = "test";

    private ApplicationMasterdataPlugin plugin;

    private KioOperations kioOperations;

    private UserDataProvider userDataProvider;

    private ApplicationMasterdataPluginProperties pluginProperties;

    private List<NamedValidator> validators;

    private ViolationSink violationSink;

    private CloudTrailEvent event;

    private CloudTrailEvent buildEvent() {
        List<Map<String, Object>> records = Records.fromClasspath("/record.json");

        CloudTrailEvent event = TestCloudTrailEventData.createCloudTrailEventFromMap(records.get(0));
        return event;
    }

    private void mockUserData(boolean empty) {
        Map<String, String> userData = Maps.newHashMap();
        if (!empty) {
            userData.put("application_id",
                         APP);
        }
        when(userDataProvider.getUserData(any(),
                                          any())).thenReturn(userData);
    }

    @Before
    public void setUp() {
        event = buildEvent();
        kioOperations = mock(KioOperations.class);
        userDataProvider = mock(UserDataProvider.class);
        validators = Lists.newArrayList(new DocumentationUrlValidator(),
                                        new ScmUrlValidator(),
                                        new SpecificationUrlValidator());
        violationSink = mock(ViolationSink.class);
        pluginProperties = new ApplicationMasterdataPluginProperties();
        pluginProperties.setValidatorsEnabled(Lists.newArrayList("scm_url",
                                                                 "specification_url",
                                                                 "documentation_url"));
        plugin = new ApplicationMasterdataPlugin(kioOperations,
                                                 userDataProvider,
                                                 pluginProperties,
                                                 validators,
                                                 violationSink);
        plugin.init();
    }

    @After
    public void tearDown() {
        verifyNoMoreInteractions(kioOperations,
                                 userDataProvider,
                                 violationSink);
    }

    @Test
    public void shouldSupportRunEvent() {
        assertTrue(plugin.supports(event));
    }

    @Test
    public void shouldComplainOnException() {
        when(userDataProvider.getUserData(any(),
                                          any())).thenThrow(new AmazonServiceException("foo"));
        plugin.processEvent(event);
        verify(userDataProvider).getUserData(any(),
                                             any());
        verify(violationSink).put(any(Violation.class));
    }

    @Test
    public void shouldComplainOnEmptyUserdata() {
        mockUserData(true);
        plugin.processEvent(event);
        verify(userDataProvider).getUserData(any(),
                                             any());
        verify(violationSink).put(any(Violation.class));
    }

    @Test
    public void shouldComplainOnNullUserdata() {
        when(userDataProvider.getUserData(any(),
                                          any())).thenReturn(null);
        plugin.processEvent(event);
        verify(userDataProvider).getUserData(any(),
                                             any());
        verify(violationSink).put(any(Violation.class));
    }

    @Test
    public void shouldComplainOnApplicationNotFound() {
        mockUserData(false);
        when(kioOperations.getApplicationById(APP)).thenThrow(new NotFoundException());
        plugin.processEvent(event);
        verify(userDataProvider).getUserData(any(),
                                             any());
        verify(kioOperations).getApplicationById(APP);
        verify(violationSink).put(any(Violation.class));
    }

    @Test
    public void shouldComplainAboutMissingScmUrl() {
        mockUserData(false);
        Application app = new Application();
        app.setDocumentationUrl("foo");
        app.setSpecificationUrl("bar");
        when(kioOperations.getApplicationById(APP)).thenReturn(app);
        plugin.processEvent(event);
        verify(userDataProvider).getUserData(any(),
                                             any());
        verify(kioOperations).getApplicationById(APP);
        verify(violationSink).put(any(Violation.class));
    }

    @Test
    public void shouldComplainAboutMissingDocumentationUrl() {
        mockUserData(false);
        Application app = new Application();
        app.setScmUrl("foo");
        app.setSpecificationUrl("bar");
        when(kioOperations.getApplicationById(APP)).thenReturn(app);
        plugin.processEvent(event);
        verify(userDataProvider).getUserData(any(),
                                             any());
        verify(kioOperations).getApplicationById(APP);
        verify(violationSink).put(any(Violation.class));
    }

    @Test
    public void shouldComplainAboutMissingSpecificationUrl() {
        mockUserData(false);
        Application app = new Application();
        app.setDocumentationUrl("foo");
        app.setScmUrl("bar");
        when(kioOperations.getApplicationById(APP)).thenReturn(app);
        plugin.processEvent(event);
        verify(userDataProvider).getUserData(any(),
                                             any());
        verify(kioOperations).getApplicationById(APP);
        verify(violationSink).put(any(Violation.class));
    }

    @Test
    public void shouldNotComplainInHappyCase() {
        mockUserData(false);
        Application app = new Application();
        app.setDocumentationUrl("foo");
        app.setSpecificationUrl("bar");
        app.setScmUrl("baz");
        when(kioOperations.getApplicationById(APP)).thenReturn(app);
        plugin.processEvent(event);
        verify(userDataProvider).getUserData(any(),
                                             any());
        verify(kioOperations).getApplicationById(APP);
        verify(violationSink,
               never()).put(any(Violation.class));
    }
}
