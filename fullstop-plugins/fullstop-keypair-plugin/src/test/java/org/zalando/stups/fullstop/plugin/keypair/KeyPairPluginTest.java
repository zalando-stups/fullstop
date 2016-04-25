package org.zalando.stups.fullstop.plugin.keypair;

import com.amazonaws.services.ec2.model.Image;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.zalando.stups.fullstop.plugin.EC2InstanceContext;
import org.zalando.stups.fullstop.plugin.EC2InstanceContextProvider;
import org.zalando.stups.fullstop.violation.Violation;
import org.zalando.stups.fullstop.violation.ViolationBuilder;
import org.zalando.stups.fullstop.violation.ViolationSink;

import java.util.Optional;
import java.util.function.Predicate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

public class KeyPairPluginTest {

    private ViolationSink mockViolationSink;
    private KeyPairPlugin keyPairPlugin;
    private EC2InstanceContext mockContext;

    @Before
    public void setUp() throws Exception {
        final EC2InstanceContextProvider mockContextProvider = mock(EC2InstanceContextProvider.class);
        mockViolationSink = mock(ViolationSink.class);
        mockContext = mock(EC2InstanceContext.class);
        keyPairPlugin = new KeyPairPlugin(mockContextProvider, mockViolationSink);

        when(mockContext.violation()).thenReturn(new ViolationBuilder());
    }

    @After
    public void tearDown() throws Exception {
        verifyNoMoreInteractions(mockViolationSink, mockContext);
    }

    @Test
    public void testSupportsEventName() throws Exception {
        final Predicate<? super String> supportsEventName = keyPairPlugin.supportsEventName();
        assertThat(supportsEventName.test("Foobar")).isFalse();
        assertThat(supportsEventName.test("RunInstances")).isTrue();
        assertThat(supportsEventName.test("StartInstances")).isFalse();
        assertThat(supportsEventName.test("StopInstances")).isFalse();
        assertThat(supportsEventName.test("TerminateInstances")).isFalse();
    }

    @Test
    public void testProcessWithKeypair() throws Exception {
        when(mockContext.getAmiId()).thenReturn(Optional.of("ami-123456"));
        when(mockContext.getAmi()).thenReturn(Optional.of(new Image().withName("An AMI").withOwnerId("me")));
        when(mockContext.getInstanceJson()).thenReturn("{\"keyName\": \"the-key\"}");
        keyPairPlugin.process(mockContext);

        verify(mockContext).getInstanceJson();
        verify(mockContext).violation();
        verify(mockContext, times(2)).getAmi();
        verify(mockContext).getAmiId();
        verify(mockViolationSink).put(any(Violation.class));
    }

    @Test
    public void testProcessNullKeypair() throws Exception {
        when(mockContext.getInstanceJson()).thenReturn("{\"keyName\": null}");
        keyPairPlugin.process(mockContext);

        verify(mockContext).getInstanceJson();
        verify(mockViolationSink, never()).put(any(Violation.class));
    }

    @Test
    public void testProcessBlankKeypair() throws Exception {
        when(mockContext.getInstanceJson()).thenReturn("{\"keyName\": \" \"}");
        keyPairPlugin.process(mockContext);

        verify(mockContext).getInstanceJson();
        verify(mockViolationSink, never()).put(any(Violation.class));
    }

    @Test
    public void testProcessMissingKeypair() throws Exception {
        when(mockContext.getInstanceJson()).thenReturn("{}");
        keyPairPlugin.process(mockContext);

        verify(mockContext).getInstanceJson();
        verify(mockViolationSink, never()).put(any(Violation.class));
    }
}
