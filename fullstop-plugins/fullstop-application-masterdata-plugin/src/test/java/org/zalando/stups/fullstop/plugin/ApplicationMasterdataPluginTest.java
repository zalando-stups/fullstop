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

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.zalando.stups.clients.kio.Application;
import org.zalando.stups.fullstop.violation.ViolationBuilder;
import org.zalando.stups.fullstop.violation.ViolationSink;

import java.util.Optional;

import static java.util.Optional.empty;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;
import static org.zalando.stups.fullstop.violation.ViolationMatchers.hasType;
import static org.zalando.stups.fullstop.violation.ViolationType.*;

public class ApplicationMasterdataPluginTest {

    private ApplicationMasterdataPlugin plugin;

    private EC2InstanceContextProvider mockContextProvider;
    private ViolationSink mockViolationSink;
    private EC2InstanceContext mockContext;
    private Application kioApp;

    @Before
    public void setUp() throws Exception {
        kioApp = new Application();
        mockContextProvider = mock(EC2InstanceContextProvider.class);
        mockViolationSink = mock(ViolationSink.class);
        mockContext = mock(EC2InstanceContext.class);

        plugin = new ApplicationMasterdataPlugin(mockContextProvider, mockViolationSink);

        when(mockContext.violation()).thenReturn(new ViolationBuilder());
    }

    @After
    public void tearDown() throws Exception {
        verifyNoMoreInteractions(mockViolationSink, mockContextProvider, mockContext);
    }

    @Test
    public void testSupportsEventName() throws Exception {
        assertThat(plugin.supportsEventName().test("RunInstances")).isTrue();
        assertThat(plugin.supportsEventName().test("StartInstances")).isTrue();
        assertThat(plugin.supportsEventName().test("TerminateInstances")).isFalse();
        assertThat(plugin.supportsEventName().test("StopInstances")).isFalse();
        assertThat(plugin.supportsEventName().test("Foobar")).isFalse();
    }

    @Test
    public void testSkipOnMissingAppId() throws Exception {
        when(mockContext.getApplicationId()).thenReturn(empty());

        plugin.process(mockContext);

        verify(mockContext).getApplicationId();
    }

    @Test
    public void testMissingKioApp() throws Exception {
        when(mockContext.getApplicationId()).thenReturn(Optional.of("my-app"));
        when(mockContext.getKioApplication()).thenReturn(empty());

        plugin.process(mockContext);

        verify(mockContext).getApplicationId();
        verify(mockContext).getKioApplication();
        verify(mockContext).violation();
        verify(mockViolationSink).put(argThat(hasType(APPLICATION_NOT_PRESENT_IN_KIO)));
    }

    @Test
    public void testMissingUrl() throws Exception {
        when(mockContext.getApplicationId()).thenReturn(Optional.of("my-app"));
        when(mockContext.getKioApplication()).thenReturn(Optional.of(kioApp));
        kioApp.setSpecificationType(null);
        kioApp.setSpecificationUrl(" ");

        plugin.process(mockContext);

        verify(mockContext).getApplicationId();
        verify(mockContext).getKioApplication();
        verify(mockContext, times(2)).violation();
        verify(mockViolationSink).put(argThat(hasType(SPEC_TYPE_IS_MISSING_IN_KIO)));
        verify(mockViolationSink).put(argThat(hasType(SPEC_URL_IS_MISSING_IN_KIO)));
    }

    @Test
    public void testHappyCase() throws Exception {
        when(mockContext.getApplicationId()).thenReturn(Optional.of("my-app"));
        when(mockContext.getKioApplication()).thenReturn(Optional.of(kioApp));
        kioApp.setSpecificationType("Github Issues");
        kioApp.setSpecificationUrl("https://github.com/zalando-stups/fullstop/issues");

        plugin.process(mockContext);

        verify(mockContext).getApplicationId();
        verify(mockContext).getKioApplication();
    }
}
