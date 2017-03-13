package org.zalando.stups.fullstop.plugin.scm;

import com.google.common.collect.ImmutableMap;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.zalando.stups.clients.kio.Application;
import org.zalando.stups.fullstop.plugin.EC2InstanceContext;
import org.zalando.stups.fullstop.plugin.EC2InstanceContextProvider;
import org.zalando.stups.fullstop.plugin.scm.config.ScmRepositoryPluginProperties;
import org.zalando.stups.fullstop.violation.ViolationBuilder;
import org.zalando.stups.fullstop.violation.ViolationSink;

import java.util.Optional;

import static java.util.Collections.singletonMap;
import static java.util.Optional.empty;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.zalando.stups.fullstop.violation.ViolationMatchers.hasType;
import static org.zalando.stups.fullstop.violation.ViolationType.ILLEGAL_SCM_REPOSITORY;
import static org.zalando.stups.fullstop.violation.ViolationType.SCM_URL_IS_MISSING_IN_KIO;
import static org.zalando.stups.fullstop.violation.ViolationType.SCM_URL_IS_MISSING_IN_SCM_SOURCE_JSON;
import static org.zalando.stups.fullstop.violation.ViolationType.SCM_URL_NOT_MATCH_WITH_KIO;

public class ScmRepositoryPluginTest {

    private ViolationSink mockViolationSink;

    private EC2InstanceContext mockContext;

    private Application kioApp;
    private ScmRepositoryPlugin plugin;
    private Repositories mockRepoUrls;
    private ScmRepositoryPluginProperties properties;

    @Before
    public void setUp() throws Exception {
        final EC2InstanceContextProvider mockContextProvider = mock(EC2InstanceContextProvider.class);
        mockViolationSink = mock(ViolationSink.class);
        mockContext = mock(EC2InstanceContext.class);
        mockRepoUrls = mock(Repositories.class);
        properties = new ScmRepositoryPluginProperties();
        plugin = new ScmRepositoryPlugin(mockContextProvider, mockRepoUrls, mockViolationSink, properties);

        kioApp = new Application();
        kioApp.setId("hello-world");
        kioApp.setScmUrl("git@github.com:zalando-stups/hello-world.git");

        when(mockContext.violation()).thenReturn(new ViolationBuilder());
    }

    @After
    public void tearDown() throws Exception {
        verifyNoMoreInteractions(
                mockViolationSink,
                mockRepoUrls,
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

//    @Test
//    public void testProcessEventNoViolation() throws Exception {
//        final String normalizedUrl = "https://github.com/zalando-stups/fullstop.git";
//
//        when(mockContext.getKioApplication()).thenReturn(Optional.of(kioApp));
//        when(mockContext.getScmSource()).thenReturn(Optional.of(singletonMap("url", "https://github.com/hello-world/fullstop")));
//        when(mockRepoUrls.normalize(anyString())).thenReturn(normalizedUrl);
//
//        plugin.process(mockContext);
//
//        verify(mockContext).getKioApplication();
//        verify(mockContext).getScmSource();
//        verify(mockRepoUrls).normalize(eq("https://github.com/hello-world/fullstop"));
//        verify(mockRepoUrls).normalize(eq("git@github.com:zalando-stups/hello-world.git"));
//    }

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


//    @Test
//    public void testProcessEventWithDifferentScmUrls() throws Exception {
//        when(mockContext.getKioApplication()).thenReturn(Optional.of(kioApp));
//        when(mockContext.getScmSource()).thenReturn(Optional.of(singletonMap("url", "https://github.com/hello-world/fullstop")));
//        when(mockRepoUrls.normalize(anyString()))
//                .thenReturn("https://github.com/zalando-stups/fullstop.git") // first call
//                .thenReturn("https://github.com/stups-zalando/semistop.git"); // second call;
//
//        plugin.process(mockContext);
//
//        verify(mockContext).getKioApplication();
//        verify(mockContext).getScmSource();
//        verify(mockRepoUrls).normalize(eq("https://github.com/hello-world/fullstop"));
//        verify(mockRepoUrls).normalize(eq("git@github.com:zalando-stups/hello-world.git"));
//        verify(mockContext).violation();
//        verify(mockViolationSink).put(argThat(hasType(SCM_URL_NOT_MATCH_WITH_KIO)));
//    }
//
//    @Test
//    public void testProcessEventWithIllegalRepository() throws Exception {
//        when(mockContext.getKioApplication()).thenReturn(Optional.of(kioApp));
//        when(mockContext.getScmSource()).thenReturn(Optional.of(singletonMap("url", "https://github.com/hello-world/fullstop")));
//        when(mockRepoUrls.normalize(anyString())).thenReturn("https://github.com/zalando-stups/fullstop.git");
//        properties.setAllowedReposRegex("^https:\\/\\/bitbucket\\.org\\/.+$");
//
//        plugin.process(mockContext);
//
//        verify(mockContext).getKioApplication();
//        verify(mockContext).getScmSource();
//        verify(mockRepoUrls).normalize(eq("https://github.com/hello-world/fullstop"));
//        verify(mockRepoUrls).normalize(eq("git@github.com:zalando-stups/hello-world.git"));
//        verify(mockContext).violation();
//        verify(mockViolationSink).put(argThat(hasType(ILLEGAL_SCM_REPOSITORY)));
//    }
//
//    @Test
//    public void testProcessBadScmSourceUrl() throws Exception {
//        final String normalizedUrl = "https://github.com/zalando-stups/fullstop.git";
//
//        when(mockContext.getKioApplication()).thenReturn(Optional.of(kioApp));
//        when(mockContext.getScmSource()).thenReturn(Optional.of(singletonMap("url", "git:https://github.com/hello-world/fullstop")));
//        when(mockRepoUrls.normalize(anyString())).thenReturn(normalizedUrl);
//
//        plugin.process(mockContext);
//
//        verify(mockContext).getKioApplication();
//        verify(mockContext).getScmSource();
//        verify(mockRepoUrls).normalize(eq("https://github.com/hello-world/fullstop"));
//        verify(mockRepoUrls).normalize(eq("git@github.com:zalando-stups/hello-world.git"));
//    }
}
