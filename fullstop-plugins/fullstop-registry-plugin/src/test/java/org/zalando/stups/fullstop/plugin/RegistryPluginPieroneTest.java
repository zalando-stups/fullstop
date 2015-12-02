package org.zalando.stups.fullstop.plugin;

import com.amazonaws.services.cloudtrail.processinglibrary.model.CloudTrailEvent;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.zalando.stups.clients.kio.KioOperations;
import org.zalando.stups.fullstop.events.UserDataProvider;
import org.zalando.stups.fullstop.plugin.config.RegistryPluginProperties;
import org.zalando.stups.fullstop.violation.Violation;
import org.zalando.stups.fullstop.violation.ViolationSink;
import org.zalando.stups.pierone.client.PieroneOperations;
import org.zalando.stups.pierone.client.TagSummary;

import java.util.Map;

import static com.google.common.collect.Maps.newHashMap;
import static java.util.Collections.emptyMap;
import static java.util.Collections.singletonMap;
import static org.mockito.Mockito.*;
import static org.zalando.stups.fullstop.events.TestCloudTrailEventSerializer.createCloudTrailEvent;

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

    @Before
    public void setUp() {
        final RegistryPluginProperties pluginConfiguration = new RegistryPluginProperties();

        event = createCloudTrailEvent("/record.json");
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
    public void shouldNotComplainOnMissingDockerPrefixIfArtifactContainsSourceAndTagIsInPierone() {
        when(pieroneOperations.listTags(TEAM, APP)).thenReturn(singletonMap(VERSION, mock(TagSummary.class)));
        registryPlugin.validateSourceWithPierone(
                event,
                APP,
                VERSION,
                TEAM,
                "stups/yourturn",
                "stups/yourturn:1.0",
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
        when(pieroneOperations.getScmSource(TEAM, APP, VERSION)).thenReturn(null);
        registryPlugin.validateScmSource(event, TEAM, APP, VERSION, INSTANCE_ID);
        verify(pieroneOperations).getScmSource(TEAM, APP, VERSION);
        verify(violationSink).put(any(Violation.class));
    }

    @Test
    public void shouldComplainIfScmSourceIsEmpty() {
        when(pieroneOperations.getScmSource(eq(TEAM), eq(APP), eq(VERSION))).thenReturn(emptyMap());
        registryPlugin.validateScmSource(event, TEAM, APP, VERSION, INSTANCE_ID);
        verify(pieroneOperations).getScmSource(eq(TEAM), eq(APP), eq(VERSION));
        verify(violationSink).put(any(Violation.class));
    }

    @Test
    public void shouldNotComplainIfScmSourceIsNotEmpty() {
        Map<String, String> scmSource = newHashMap();
        scmSource.put("revision", "abcdef");
        scmSource.put("url", "https://github.com/zalando-stups/fullstop.git");
        when(pieroneOperations.getScmSource(TEAM, APP, VERSION)).thenReturn(scmSource);
        registryPlugin.validateScmSource(event, TEAM, APP, VERSION, INSTANCE_ID);
        verify(pieroneOperations).getScmSource(TEAM, APP, VERSION);
        verify(violationSink, never()).put(any(Violation.class));
    }
}
