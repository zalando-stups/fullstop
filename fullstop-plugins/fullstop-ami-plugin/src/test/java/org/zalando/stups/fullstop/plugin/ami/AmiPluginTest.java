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
package org.zalando.stups.fullstop.plugin.ami;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.zalando.stups.fullstop.plugin.EC2InstanceContext;
import org.zalando.stups.fullstop.plugin.EC2InstanceContextProvider;
import org.zalando.stups.fullstop.violation.ViolationBuilder;
import org.zalando.stups.fullstop.violation.ViolationSink;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;
import static org.zalando.stups.fullstop.violation.ViolationMatchers.hasType;
import static org.zalando.stups.fullstop.violation.ViolationType.WRONG_AMI;

public class AmiPluginTest {

    private EC2InstanceContextProvider mockContextProvider;
    private ViolationSink mockViolationSink;
    private AmiPlugin amiPlugin;
    private EC2InstanceContext mockContext;

    @Before
    public void setUp() throws Exception {
        mockContextProvider = mock(EC2InstanceContextProvider.class);
        mockViolationSink = mock(ViolationSink.class);
        mockContext = mock(EC2InstanceContext.class);

        amiPlugin = new AmiPlugin(mockContextProvider, mockViolationSink);

        when(mockContext.violation()).thenReturn(new ViolationBuilder());
        when(mockContext.getAmi()).thenReturn(Optional.empty());
        when(mockContext.getAmiId()).thenReturn(Optional.empty());
    }

    @After
    public void tearDown() throws Exception {
        verifyNoMoreInteractions(mockContextProvider, mockViolationSink, mockContext);
    }

    @Test
    public void testSupportsEventName() throws Exception {
        assertThat(amiPlugin.supportsEventName().test("RunInstances")).isTrue();
        assertThat(amiPlugin.supportsEventName().test("StartInstances")).isFalse();
        assertThat(amiPlugin.supportsEventName().test("TerminateInstances")).isFalse();
        assertThat(amiPlugin.supportsEventName().test("StopInstances")).isFalse();
        assertThat(amiPlugin.supportsEventName().test("Foobar")).isFalse();
    }

    @Test
    public void testIsTaupageAmi() throws Exception {
        when(mockContext.isTaupageAmi()).thenReturn(Optional.of(true));

        amiPlugin.process(mockContext);

        verify(mockContext).isTaupageAmi();
    }

    @Test
    public void testUnknownTaupageAmi() throws Exception {
        when(mockContext.isTaupageAmi()).thenReturn(Optional.empty());

        amiPlugin.process(mockContext);

        verify(mockContext).isTaupageAmi();
        verify(mockContext).getAmiId();
        verify(mockContext).violation();
        verify(mockContext).getAmi();
        verify(mockViolationSink).put(argThat(hasType(WRONG_AMI)));
    }


    @Test
    public void testIsNotTaupageAmi() throws Exception {
        when(mockContext.isTaupageAmi()).thenReturn(Optional.of(false));

        amiPlugin.process(mockContext);

        verify(mockContext).isTaupageAmi();
        verify(mockContext).getAmiId();
        verify(mockContext).violation();
        verify(mockContext).getAmi();
        verify(mockViolationSink).put(argThat(hasType(WRONG_AMI)));
    }
}
