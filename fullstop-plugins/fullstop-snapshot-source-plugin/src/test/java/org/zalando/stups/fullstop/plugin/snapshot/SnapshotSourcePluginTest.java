package org.zalando.stups.fullstop.plugin.snapshot;

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
import static org.zalando.stups.fullstop.violation.ViolationType.EC2_WITH_A_SNAPSHOT_IMAGE;

public class SnapshotSourcePluginTest {

    private ViolationSink mockViolationSink;
    private EC2InstanceContextProvider mockContextProvider;
    private EC2InstanceContext mockContext;

    private SnapshotSourcePlugin plugin;

    @Before
    public void setUp() {
        mockViolationSink = mock(ViolationSink.class);
        mockContextProvider = mock(EC2InstanceContextProvider.class);
        mockContext = mock(EC2InstanceContext.class);
        plugin = new SnapshotSourcePlugin(mockContextProvider, mockViolationSink);

        when(mockContext.violation()).thenReturn(new ViolationBuilder());
    }

    @After
    public void tearDown() {
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
    public void testMissingSource() throws Exception {
        when(mockContext.getSource()).thenReturn(Optional.empty());

        plugin.process(mockContext);

        verify(mockContext).getSource();
    }

    @Test
    public void testFixVersion() throws Exception {
        when(mockContext.getSource()).thenReturn(Optional.of("docker://registry.zalando.com/stups/SNAPSHOT:1.0"));

        plugin.process(mockContext);

        verify(mockContext).getSource();
    }

    @Test
    public void testSnapshotInName() throws Exception {
        when(mockContext.getSource()).thenReturn(Optional.of("docker://registry.zalando.com/stups/SNAPSHOT:1.0"));

        plugin.process(mockContext);

        verify(mockContext).getSource();
    }

    @Test
    public void testSnapshotArtifact() throws Exception {
        when(mockContext.getSource()).thenReturn(Optional.of("docker://registry.zalando.com/stups/yourturn:1.0-SNAPSHOT"));

        plugin.process(mockContext);

        verify(mockContext).getSource();
        verify(mockContext).violation();
        verify(mockViolationSink).put(argThat(hasType(EC2_WITH_A_SNAPSHOT_IMAGE)));
    }
}
