package org.zalando.stups.fullstop.plugin.snapshot;

import com.amazonaws.services.cloudtrail.processinglibrary.model.CloudTrailEvent;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.zalando.stups.fullstop.events.UserDataProvider;
import org.zalando.stups.fullstop.violation.Violation;
import org.zalando.stups.fullstop.violation.ViolationSink;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;
import static org.zalando.stups.fullstop.events.TestCloudTrailEventSerializer.createCloudTrailEvent;

public class SnapshotSourcePluginTest {

    private UserDataProvider provider;

    private ViolationSink sink;

    private CloudTrailEvent event;

    private SnapshotSourcePlugin plugin;

    @Before
    public void setUp() {
        provider = mock(UserDataProvider.class);
        sink = mock(ViolationSink.class);
        plugin = new SnapshotSourcePlugin(provider, sink);
    }

    @After
    public void tearDown() {
        Mockito.verifyNoMoreInteractions(sink, provider);
    }

    @Test
    public void shouldNotSupportTerminateEvent() {
        event = createCloudTrailEvent("/record-termination.json");
        assertThat(plugin.supports(event)).isFalse();
    }

    @Test
    public void shouldSupportRunEvent() {
        event = createCloudTrailEvent("/record-run.json");
        assertThat(plugin.supports(event)).isTrue();
    }

    @Test
    public void shouldComplainWithoutSource() {
        event = createCloudTrailEvent("/record-run.json");
        when(provider.getUserData(any(), any(String.class), any())).thenReturn(new HashMap<String, String>());
        plugin.processEvent(event);

        verify(provider).getUserData(any(), any(String.class), any(String.class));
        verify(sink).put(any(Violation.class));
    }

    @Test
    public void shouldComplainWithSnapshotSource() {
        event = createCloudTrailEvent("/record-run.json");

        Map<String, String> userData = new HashMap<>();
        userData.put("source", "docker://registry.zalando.com/stups/yourturn:1.0-SNAPSHOT");
        when(provider.getUserData(any(), any(String.class), any())).thenReturn(userData);
        plugin.processEvent(event);

        verify(provider).getUserData(any(), any(String.class), any(String.class));
        verify(sink).put(any(Violation.class));
    }

    @Test
    public void shouldNotComplainWithSnapshotInName() {
        event = createCloudTrailEvent("/record-run.json");

        Map<String, String> userData = new HashMap<>();
        userData.put("source", "docker://registry.zalando.com/stups/SNAPSHOT:1.0");
        when(provider.getUserData(any(), any(String.class), any())).thenReturn(userData);
        plugin.processEvent(event);

        verify(provider).getUserData(any(), any(String.class), any(String.class));
        verify(sink, never()).put(any(Violation.class));
    }

    @Test
    public void shouldNotComplainWithoutSnapshotSource() {
        event = createCloudTrailEvent("/record-run.json");

        Map<String, String> userData = new HashMap<>();
        userData.put("source", "docker://registry.zalando.com/stups/yourturn:1.0");
        when(provider.getUserData(any(), any(String.class), any())).thenReturn(userData);
        plugin.processEvent(event);

        verify(provider).getUserData(any(), any(String.class), any(String.class));
        verify(sink, never()).put(any(Violation.class));
    }
}
