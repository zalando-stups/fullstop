package org.zalando.stups.fullstop.plugin.scm;

import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.google.common.collect.ImmutableMap;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.zalando.kontrolletti.KontrollettiOperations;
import org.zalando.kontrolletti.resources.Repository;
import org.zalando.stups.clients.kio.Application;
import org.zalando.stups.clients.kio.KioOperations;
import org.zalando.stups.clients.kio.NotFoundException;
import org.zalando.stups.pierone.client.PieroneOperations;
import org.zalando.stups.fullstop.events.UserDataProvider;
import org.zalando.stups.fullstop.plugin.LocalPluginProcessor;
import org.zalando.stups.fullstop.violation.Violation;
import org.zalando.stups.fullstop.violation.ViolationSink;

import static java.util.Collections.singletonMap;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

public class ScmRepositoryPluginTest {

    private static final Region EU_WEST_1 = Region.getRegion(Regions.EU_WEST_1);

    private LocalPluginProcessor processor;

    private ViolationSink mockViolationSink;

    private KioOperations mockKioOperations;

    private PieroneOperations mockPieroneOperations;

    private KontrollettiOperations mockKontrollettiOperations;

    private UserDataProvider mockUserDataProvider;

    private Application kioApp;
    private Repository repository;

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
        kioApp.setTeamId("stups");

        repository = new Repository("https://github.com/zalando-stups/fullstop.git", "github.com", "zalando-stups", "fullstop");
    }

    @After
    public void tearDown() throws Exception {
        verifyNoMoreInteractions(
                mockViolationSink,
                mockKioOperations,
                mockPieroneOperations,
                mockKontrollettiOperations,
                mockUserDataProvider);
    }

    @Test
    public void testProcessEventNoViolation() throws Exception {
        final String normalizedUrl = "https://github.com/zalando-stups/fullstop.git";

        when(mockUserDataProvider.getUserData(anyString(), any(Region.class), anyString()))
                .thenReturn(
                        ImmutableMap.of(
                                "source", "hello-world:0.1",
                                "application_id", "hello-world"));
        when(mockKioOperations.getApplicationById(anyString())).thenReturn(kioApp);
        when(mockPieroneOperations.getScmSource(anyString(), anyString(), anyString()))
                .thenReturn(singletonMap("url", "https://github.com/hello-world/fullstop"));
        when(mockKontrollettiOperations.normalizeRepositoryUrl(anyString())).thenReturn(
                normalizedUrl);
        when(mockKontrollettiOperations.getRepository(anyString())).thenReturn(repository);

        processor.processEvents(getClass().getResourceAsStream("/run-instance-record.json"));

        verify(mockUserDataProvider).getUserData(eq("123456789111"), eq(EU_WEST_1), eq("i-affenbanane"));
        verify(mockKioOperations).getApplicationById(eq("hello-world"));
        verify(mockPieroneOperations).getScmSource(eq("stups"), eq("hello-world"), eq("0.1"));
        verify(mockKontrollettiOperations).normalizeRepositoryUrl(eq("https://github.com/hello-world/fullstop"));
        verify(mockKontrollettiOperations).normalizeRepositoryUrl(eq("git@github.com:zalando-stups/hello-world.git"));
        verify(mockKontrollettiOperations).getRepository(eq(normalizedUrl));
    }

    @Test
    public void testProcessEventMissingUserData() throws Exception {
        when(mockUserDataProvider.getUserData(anyString(), any(Region.class), anyString())).thenReturn(null);

        processor.processEvents(getClass().getResourceAsStream("/run-instance-record.json"));

        verify(mockUserDataProvider).getUserData(eq("123456789111"), eq(EU_WEST_1), eq("i-affenbanane"));
    }

    @Test
    public void testProcessEventMissingUserDataSource() throws Exception {
        when(mockUserDataProvider.getUserData(anyString(), any(Region.class), anyString()))
                .thenReturn(
                        ImmutableMap.of(
                                "application_id", "hello-world"));

        processor.processEvents(getClass().getResourceAsStream("/run-instance-record.json"));

        verify(mockUserDataProvider).getUserData(eq("123456789111"), eq(EU_WEST_1), eq("i-affenbanane"));
    }

    @Test
    public void testProcessEventIllegalUserDataSource() throws Exception {
        when(mockUserDataProvider.getUserData(anyString(), any(Region.class), anyString()))
                .thenReturn(
                        ImmutableMap.of(
                                "source", "foobar",
                                "application_id", "hello-world"));

        processor.processEvents(getClass().getResourceAsStream("/run-instance-record.json"));

        verify(mockUserDataProvider).getUserData(eq("123456789111"), eq(EU_WEST_1), eq("i-affenbanane"));
    }

    @Test
    public void testProcessEventMissingUserDataAppId() throws Exception {
        when(mockUserDataProvider.getUserData(anyString(), any(Region.class), anyString()))
                .thenReturn(
                        ImmutableMap.of(
                                "source", "hello-world:0.1"));

        processor.processEvents(getClass().getResourceAsStream("/run-instance-record.json"));

        verify(mockUserDataProvider).getUserData(eq("123456789111"), eq(EU_WEST_1), eq("i-affenbanane"));
    }

    @Test
    public void testProcessEventMissingKioApp() throws Exception {
        when(mockUserDataProvider.getUserData(anyString(), any(Region.class), anyString()))
                .thenReturn(
                        ImmutableMap.of(
                                "source", "hello-world:0.1",
                                "application_id", "hello-world"));
        when(mockKioOperations.getApplicationById(anyString())).thenThrow(new NotFoundException());

        processor.processEvents(getClass().getResourceAsStream("/run-instance-record.json"));

        verify(mockUserDataProvider).getUserData(eq("123456789111"), eq(EU_WEST_1), eq("i-affenbanane"));
        verify(mockKioOperations).getApplicationById(eq("hello-world"));
    }

    @Test
    public void testProcessEventMissingKioScmUrl() throws Exception {
        when(mockUserDataProvider.getUserData(anyString(), any(Region.class), anyString()))
                .thenReturn(
                        ImmutableMap.of(
                                "source", "hello-world:0.1",
                                "application_id", "hello-world"));
        when(mockKioOperations.getApplicationById(anyString())).thenReturn(kioApp);
        kioApp.setScmUrl("");

        processor.processEvents(getClass().getResourceAsStream("/run-instance-record.json"));

        verify(mockUserDataProvider).getUserData(eq("123456789111"), eq(EU_WEST_1), eq("i-affenbanane"));
        verify(mockKioOperations).getApplicationById(eq("hello-world"));

        verify(mockViolationSink).put(any(Violation.class));
    }

    @Test
    public void testProcessEventMissingPieroneScmSource() throws Exception {
        when(mockUserDataProvider.getUserData(anyString(), any(Region.class), anyString()))
                .thenReturn(
                        ImmutableMap.of(
                                "source", "hello-world:0.1",
                                "application_id", "hello-world"));
        when(mockKioOperations.getApplicationById(anyString())).thenReturn(kioApp);
        when(mockPieroneOperations.getScmSource(anyString(), anyString(), anyString())).thenReturn(null);

        processor.processEvents(getClass().getResourceAsStream("/run-instance-record.json"));

        verify(mockUserDataProvider).getUserData(eq("123456789111"), eq(EU_WEST_1), eq("i-affenbanane"));
        verify(mockKioOperations).getApplicationById(eq("hello-world"));
        verify(mockPieroneOperations).getScmSource(eq("stups"), eq("hello-world"), eq("0.1"));
    }

    @Test
    public void testProcessEventMissingPieroneScmSourceUrl() throws Exception {
        when(mockUserDataProvider.getUserData(anyString(), any(Region.class), anyString()))
                .thenReturn(
                        ImmutableMap.of(
                                "source", "hello-world:0.1",
                                "application_id", "hello-world"));
        when(mockKioOperations.getApplicationById(anyString())).thenReturn(kioApp);
        when(mockPieroneOperations.getScmSource(anyString(), anyString(), anyString()))
                .thenReturn(singletonMap("url", ""));

        processor.processEvents(getClass().getResourceAsStream("/run-instance-record.json"));

        verify(mockUserDataProvider).getUserData(eq("123456789111"), eq(EU_WEST_1), eq("i-affenbanane"));
        verify(mockKioOperations).getApplicationById(eq("hello-world"));
        verify(mockPieroneOperations).getScmSource(eq("stups"), eq("hello-world"), eq("0.1"));

        verify(mockViolationSink).put(any(Violation.class));
    }

    @Test
    public void testProcessEventWithDifferentScmUrls() throws Exception {
        when(mockUserDataProvider.getUserData(anyString(), any(Region.class), anyString()))
                .thenReturn(
                        ImmutableMap.of(
                                "source", "hello-world:0.1",
                                "application_id", "hello-world"));
        when(mockKioOperations.getApplicationById(anyString())).thenReturn(kioApp);
        when(mockPieroneOperations.getScmSource(anyString(), anyString(), anyString()))
                .thenReturn(singletonMap("url", "https://github.com/hello-world/fullstop"));
        when(mockKontrollettiOperations.normalizeRepositoryUrl(anyString()))
                .thenReturn("https://github.com/zalando-stups/fullstop.git") // first call
                .thenReturn("https://github.com/stups-zalando/semistop.git"); // second call;

        processor.processEvents(getClass().getResourceAsStream("/run-instance-record.json"));

        verify(mockUserDataProvider).getUserData(eq("123456789111"), eq(EU_WEST_1), eq("i-affenbanane"));
        verify(mockKioOperations).getApplicationById(eq("hello-world"));
        verify(mockPieroneOperations).getScmSource(eq("stups"), eq("hello-world"), eq("0.1"));
        verify(mockKontrollettiOperations).normalizeRepositoryUrl(eq("https://github.com/hello-world/fullstop"));
        verify(mockKontrollettiOperations).normalizeRepositoryUrl(eq("git@github.com:zalando-stups/hello-world.git"));

        verify(mockViolationSink).put(any(Violation.class));
    }

    @Test
    public void testProcessWithIllegalRepository() throws Exception {
        final String normalizedUrl = "https://github.com/zalando-stups/fullstop.git";

        when(mockUserDataProvider.getUserData(anyString(), any(Region.class), anyString()))
                .thenReturn(
                        ImmutableMap.of(
                                "source", "hello-world:0.1",
                                "application_id", "hello-world"));
        when(mockKioOperations.getApplicationById(anyString())).thenReturn(kioApp);
        when(mockPieroneOperations.getScmSource(anyString(), anyString(), anyString()))
                .thenReturn(singletonMap("url", "https://github.com/hello-world/fullstop"));
        when(mockKontrollettiOperations.normalizeRepositoryUrl(anyString())).thenReturn(
                normalizedUrl);
        when(mockKontrollettiOperations.getRepository(anyString())).thenReturn(null);

        processor.processEvents(getClass().getResourceAsStream("/run-instance-record.json"));

        verify(mockUserDataProvider).getUserData(eq("123456789111"), eq(EU_WEST_1), eq("i-affenbanane"));
        verify(mockKioOperations).getApplicationById(eq("hello-world"));
        verify(mockPieroneOperations).getScmSource(eq("stups"), eq("hello-world"), eq("0.1"));
        verify(mockKontrollettiOperations).normalizeRepositoryUrl(eq("https://github.com/hello-world/fullstop"));
        verify(mockKontrollettiOperations).normalizeRepositoryUrl(eq("git@github.com:zalando-stups/hello-world.git"));
        verify(mockKontrollettiOperations).getRepository(eq(normalizedUrl));

        verify(mockViolationSink).put(any(Violation.class));
    }
}
