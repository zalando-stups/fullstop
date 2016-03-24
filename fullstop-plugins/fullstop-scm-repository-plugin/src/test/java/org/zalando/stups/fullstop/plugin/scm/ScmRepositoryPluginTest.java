package org.zalando.stups.fullstop.plugin.scm;

import com.google.common.collect.ImmutableMap;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.zalando.kontrolletti.KontrollettiOperations;
import org.zalando.kontrolletti.resources.Repository;
import org.zalando.stups.clients.kio.Application;
import org.zalando.stups.fullstop.plugin.EC2InstanceContext;
import org.zalando.stups.fullstop.plugin.EC2InstanceContextProvider;
import org.zalando.stups.fullstop.violation.ViolationBuilder;
import org.zalando.stups.fullstop.violation.ViolationSink;

import java.util.Optional;

import static java.util.Collections.singletonMap;
import static java.util.Optional.empty;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.zalando.stups.fullstop.violation.ViolationMatchers.hasType;
import static org.zalando.stups.fullstop.violation.ViolationType.*;

public class ScmRepositoryPluginTest {

    private ViolationSink mockViolationSink;

    private KontrollettiOperations mockKontrollettiOperations;

    private EC2InstanceContext mockContext;

    private Application kioApp;
    private Repository repository;
    private ScmRepositoryPlugin plugin;

    @Before
    public void setUp() throws Exception {
        final EC2InstanceContextProvider mockContextProvider = mock(EC2InstanceContextProvider.class);
        mockViolationSink = mock(ViolationSink.class);
        mockKontrollettiOperations = mock(KontrollettiOperations.class);
        mockContext = mock(EC2InstanceContext.class);

        plugin = new ScmRepositoryPlugin(mockContextProvider, mockKontrollettiOperations, mockViolationSink);

        kioApp = new Application();
        kioApp.setId("hello-world");
        kioApp.setScmUrl("git@github.com:zalando-stups/hello-world.git");

        repository = new Repository("https://github.com/zalando-stups/fullstop.git", "github.com", "zalando-stups", "fullstop");

        when(mockContext.violation()).thenReturn(new ViolationBuilder());
    }

    @After
    public void tearDown() throws Exception {
        verifyNoMoreInteractions(
                mockViolationSink,
                mockKontrollettiOperations,
                mockContext);
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
    public void testProcessEventNoViolation() throws Exception {
        final String normalizedUrl = "https://github.com/zalando-stups/fullstop.git";

        when(mockContext.getKioApplication()).thenReturn(Optional.of(kioApp));
        when(mockContext.getScmSource()).thenReturn(Optional.of(singletonMap("url", "https://github.com/hello-world/fullstop")));
        when(mockKontrollettiOperations.normalizeRepositoryUrl(anyString())).thenReturn(normalizedUrl);
        when(mockKontrollettiOperations.getRepository(anyString())).thenReturn(repository);

        plugin.process(mockContext);

        verify(mockContext).getKioApplication();
        verify(mockContext).getScmSource();
        verify(mockKontrollettiOperations).normalizeRepositoryUrl(eq("https://github.com/hello-world/fullstop"));
        verify(mockKontrollettiOperations).normalizeRepositoryUrl(eq("git@github.com:zalando-stups/hello-world.git"));
        verify(mockKontrollettiOperations).getRepository(eq(normalizedUrl));
    }

    @Test
    public void testProcessMissingApp() throws Exception {
        when(mockContext.getKioApplication()).thenReturn(empty());
        plugin.process(mockContext);
        verify(mockContext).getKioApplication();
    }

    @Test
    public void testProcessBlankKioScmUrl() throws Exception {
        when(mockContext.getKioApplication()).thenReturn(Optional.of(kioApp));
        kioApp.setScmUrl(" ");

        plugin.process(mockContext);

        verify(mockContext).getKioApplication();
        verify(mockContext).violation();
        verify(mockViolationSink).put(argThat(hasType(SCM_URL_IS_MISSING_IN_KIO)));
    }

    @Test
    public void testProcessMissingScmSource() throws Exception {
        when(mockContext.getKioApplication()).thenReturn(Optional.of(kioApp));
        when(mockContext.getScmSource()).thenReturn(empty());

        plugin.process(mockContext);

        verify(mockContext).getKioApplication();
        verify(mockContext).getScmSource();
    }

    @Test
    public void testProcessBlankScmSourceUrl() throws Exception {
        when(mockContext.getKioApplication()).thenReturn(Optional.of(kioApp));
        when(mockContext.getScmSource()).thenReturn(Optional.of(ImmutableMap.of("url", " ", "user", "unittester", "revision", "1a2b3c4d")));
        when(mockContext.getSource()).thenReturn(empty());

        plugin.process(mockContext);

        verify(mockContext).getKioApplication();
        verify(mockContext).getScmSource();
        verify(mockContext).getSource();
        verify(mockContext).violation();
        verify(mockViolationSink).put(argThat(hasType(SCM_URL_IS_MISSING_IN_SCM_SOURCE_JSON)));
    }


    @Test
    public void testProcessEventWithDifferentScmUrls() throws Exception {
        when(mockContext.getKioApplication()).thenReturn(Optional.of(kioApp));
        when(mockContext.getScmSource()).thenReturn(Optional.of(singletonMap("url", "https://github.com/hello-world/fullstop")));
        when(mockKontrollettiOperations.normalizeRepositoryUrl(anyString()))
                .thenReturn("https://github.com/zalando-stups/fullstop.git") // first call
                .thenReturn("https://github.com/stups-zalando/semistop.git"); // second call;

        plugin.process(mockContext);

        verify(mockContext).getKioApplication();
        verify(mockContext).getScmSource();
        verify(mockKontrollettiOperations).normalizeRepositoryUrl(eq("https://github.com/hello-world/fullstop"));
        verify(mockKontrollettiOperations).normalizeRepositoryUrl(eq("git@github.com:zalando-stups/hello-world.git"));
        verify(mockContext).violation();
        verify(mockViolationSink).put(argThat(hasType(SCM_URL_NOT_MATCH_WITH_KIO)));
    }

    @Test
    public void testProcessEventWithIllegalRepository() throws Exception {
        when(mockContext.getKioApplication()).thenReturn(Optional.of(kioApp));
        when(mockContext.getScmSource()).thenReturn(Optional.of(singletonMap("url", "https://github.com/hello-world/fullstop")));
        when(mockKontrollettiOperations.normalizeRepositoryUrl(anyString())).thenReturn("https://github.com/zalando-stups/fullstop.git");
        when(mockKontrollettiOperations.getRepository(anyString())).thenReturn(null);

        plugin.process(mockContext);

        verify(mockContext).getKioApplication();
        verify(mockContext).getScmSource();
        verify(mockKontrollettiOperations).normalizeRepositoryUrl(eq("https://github.com/hello-world/fullstop"));
        verify(mockKontrollettiOperations).normalizeRepositoryUrl(eq("git@github.com:zalando-stups/hello-world.git"));
        verify(mockKontrollettiOperations).getRepository(eq("https://github.com/zalando-stups/fullstop.git"));
        verify(mockContext).violation();
        verify(mockViolationSink).put(argThat(hasType(ILLEGAL_SCM_REPOSITORY)));
    }

    @Test(expected = HttpServerErrorException.class)
    public void testProcessEventWithKontrollettiServerError() throws Exception {
        when(mockContext.getKioApplication()).thenReturn(Optional.of(kioApp));
        when(mockContext.getScmSource()).thenReturn(Optional.of(singletonMap("url", "https://github.com/hello-world/fullstop")));
        when(mockKontrollettiOperations.normalizeRepositoryUrl(anyString())).thenReturn("https://github.com/zalando-stups/fullstop.git");
        when(mockKontrollettiOperations.getRepository(anyString())).thenThrow(new HttpServerErrorException(INTERNAL_SERVER_ERROR));

        try {
            plugin.process(mockContext);
        } finally {
            verify(mockContext).getKioApplication();
            verify(mockContext).getScmSource();
            verify(mockKontrollettiOperations).normalizeRepositoryUrl(eq("https://github.com/hello-world/fullstop"));
            verify(mockKontrollettiOperations).normalizeRepositoryUrl(eq("git@github.com:zalando-stups/hello-world.git"));
            verify(mockKontrollettiOperations).getRepository(eq("https://github.com/zalando-stups/fullstop.git"));
        }
    }

    @Test(expected = HttpClientErrorException.class)
    public void testProcessEventWithKontrollettiBadRequest() throws Exception {
        when(mockContext.getKioApplication()).thenReturn(Optional.of(kioApp));
        when(mockContext.getScmSource()).thenReturn(Optional.of(singletonMap("url", "https://github.com/hello-world/fullstop")));
        when(mockKontrollettiOperations.normalizeRepositoryUrl(anyString())).thenReturn("https://github.com/zalando-stups/fullstop.git");
        when(mockKontrollettiOperations.getRepository(anyString())).thenThrow(new HttpClientErrorException(BAD_REQUEST));

        try {
            plugin.process(mockContext);
        } finally {
            verify(mockContext).getKioApplication();
            verify(mockContext).getScmSource();
            verify(mockKontrollettiOperations).normalizeRepositoryUrl(eq("https://github.com/hello-world/fullstop"));
            verify(mockKontrollettiOperations).normalizeRepositoryUrl(eq("git@github.com:zalando-stups/hello-world.git"));
            verify(mockKontrollettiOperations).getRepository(eq("https://github.com/zalando-stups/fullstop.git"));
        }
    }
}
