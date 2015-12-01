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

import com.amazonaws.services.ec2.model.Image;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.zalando.stups.fullstop.plugin.EC2InstanceContext;
import org.zalando.stups.fullstop.plugin.EC2InstanceContextProvider;
import org.zalando.stups.fullstop.violation.ViolationBuilder;
import org.zalando.stups.fullstop.violation.ViolationSink;

import java.util.Optional;

import static com.google.common.collect.Sets.newHashSet;
import static java.util.Collections.emptySet;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;
import static org.zalando.stups.fullstop.violation.ViolationMatchers.hasType;
import static org.zalando.stups.fullstop.violation.ViolationType.WRONG_AMI;

public class AmiPluginTest {

    private EC2InstanceContextProvider mockContextProvider;
    private ViolationSink mockViolationSink;
    private WhiteListedAmiProvider mockWhiteListedAmiProvider;
    private AmiPlugin amiPlugin;
    private EC2InstanceContext mockContext;

    @Before
    public void setUp() throws Exception {
        mockContextProvider = mock(EC2InstanceContextProvider.class);
        mockViolationSink = mock(ViolationSink.class);
        mockWhiteListedAmiProvider = mock(WhiteListedAmiProvider.class);
        mockContext = mock(EC2InstanceContext.class);

        amiPlugin = new AmiPlugin(mockContextProvider, mockViolationSink, mockWhiteListedAmiProvider);

        when(mockContext.violation()).thenReturn(new ViolationBuilder());
        when(mockContext.getAmi().map(Image::getName)).thenReturn(Optional.empty());
    }

    @After
    public void tearDown() throws Exception {
        verifyNoMoreInteractions(mockContextProvider, mockViolationSink, mockWhiteListedAmiProvider, mockContext);
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
    public void testSkipOnEmptyWhiteListedAmis() throws Exception {
        when(mockWhiteListedAmiProvider.apply(any())).thenReturn(emptySet());

        amiPlugin.process(mockContext);

        verify(mockWhiteListedAmiProvider).apply(same(mockContext));
    }

    @Test
    public void testSkipOnMIssingAmiId() throws Exception {
        when(mockWhiteListedAmiProvider.apply(any())).thenReturn(newHashSet("0815", "4711"));
        when(mockContext.getAmiId()).thenReturn(Optional.empty());

        amiPlugin.process(mockContext);

        verify(mockWhiteListedAmiProvider).apply(same(mockContext));
        verify(mockContext).getAmiId();
    }

    @Test
    public void testSupportedAmi() throws Exception {
        when(mockWhiteListedAmiProvider.apply(any())).thenReturn(newHashSet("0815", "4711"));
        when(mockContext.getAmiId()).thenReturn(Optional.of("4711"));

        amiPlugin.process(mockContext);

        verify(mockWhiteListedAmiProvider).apply(same(mockContext));
        verify(mockContext).getAmiId();
    }

    @Test
    public void testUnsupportedAmi() throws Exception {
        when(mockWhiteListedAmiProvider.apply(any())).thenReturn(newHashSet("0815", "4711"));
        when(mockContext.getAmiId()).thenReturn(Optional.of("1234"));

        amiPlugin.process(mockContext);

        verify(mockWhiteListedAmiProvider).apply(same(mockContext));
        verify(mockContext).getAmiId();
        verify(mockContext).violation();
        verify(mockContext).getAmi().map(Image::getName);
        verify(mockViolationSink).put(argThat(hasType(WRONG_AMI)));
    }
}
