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
    private Repositories mockRepositories;
    private Repository validRepo;
    private Repository unallowedRepo;
    private Repository anotherRepo;

    @Before
    public void setUp() throws Exception {
        final EC2InstanceContextProvider mockContextProvider = mock(EC2InstanceContextProvider.class);
        mockViolationSink = mock(ViolationSink.class);
        mockContext = mock(EC2InstanceContext.class);
        mockRepositories = mock(Repositories.class);
        ScmRepositoryPluginProperties properties = new ScmRepositoryPluginProperties();
        plugin = new ScmRepositoryPlugin(mockContextProvider, mockRepositories, mockViolationSink, properties);

        kioApp = new Application();
        kioApp.setId("hello-world");
        kioApp.setScmUrl("git@github.com:zalando-stups/hello-world.git");

        validRepo = new Repository("github.com", "zalando-stups", "fullstop");
        unallowedRepo = new Repository("github.com", "zalando-foo", "fullstop");
        anotherRepo = new Repository("github.com", "zalando-stups", "semistop");

        properties.setHosts(ImmutableMap.of(
                "github.com", "^(?:zalando|zalando-stups)$",
                "github.my.company.com", "^.+$"
        ));

        when(mockContext.violation()).thenReturn(new ViolationBuilder());
        when(mockContext.getApplicationId()).thenReturn(Optional.of("fullstop"));
    }

    @After
    public void tearDown() throws Exception {
        verifyNoMoreInteractions(
                mockViolationSink,
                mockRepositories,
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
        when(mockContext.getKioApplication()).thenReturn(Optional.of(kioApp));
        when(mockContext.getScmSource()).thenReturn(Optional.of(singletonMap("url", "https://github.com/zalando-stups/fullstop")));
        when(mockRepositories.parse(anyString())).thenReturn(validRepo);

        plugin.process(mockContext);

        verify(mockContext).getKioApplication();
        verify(mockContext).getScmSource();
        verify(mockRepositories).parse(eq("https://github.com/zalando-stups/fullstop"));
        verify(mockRepositories).parse(eq("git@github.com:zalando-stups/hello-world.git"));
    }


    @Test
    public void testProcessMissingScmSource() throws Exception {
        when(mockContext.getScmSource()).thenReturn(empty());

        plugin.process(mockContext);

        verify(mockContext).getScmSource();
    }

    @Test
    public void testProcessBlankScmSourceUrl() throws Exception {
        when(mockContext.getScmSource()).thenReturn(Optional.of(ImmutableMap.of("url", " ", "user", "unittester", "revision", "1a2b3c4d")));
        when(mockContext.getSource()).thenReturn(empty());

        plugin.process(mockContext);

        verify(mockContext).getScmSource();
        verify(mockContext).getSource();
        verify(mockContext).getApplicationId();
        verify(mockContext).violation();
        verify(mockViolationSink).put(argThat(hasType(SCM_URL_IS_MISSING_IN_SCM_SOURCE_JSON)));
    }

    @Test
    public void testUnparseableScmSourceUrl() throws Exception {
        final String badScmUrl = "https://google.com";
        when(mockContext.getScmSource()).thenReturn(Optional.of(singletonMap("url", badScmUrl)));
        when(mockRepositories.parse(eq(badScmUrl))).thenThrow(new UnknownScmUrlException(badScmUrl));

        plugin.process(mockContext);

        verify(mockContext).getScmSource();
        verify(mockContext).getApplicationId();
        verify(mockContext).violation();
        verify(mockRepositories).parse(eq(badScmUrl));
        verify(mockViolationSink).put(argThat(hasType(ILLEGAL_SCM_REPOSITORY)));
    }


    @Test
    public void testUnallowedOwners() throws Exception {
        when(mockContext.getScmSource()).thenReturn(Optional.of(singletonMap("url", unallowedRepo.toString())));
        when(mockContext.getKioApplication()).thenReturn(empty());
        when(mockRepositories.parse(eq(unallowedRepo.toString()))).thenReturn(unallowedRepo);

        plugin.process(mockContext);

        verify(mockContext).getScmSource();
        verify(mockContext).getApplicationId();
        verify(mockContext).violation();
        verify(mockContext).getKioApplication();
        verify(mockRepositories).parse(eq(unallowedRepo.toString()));
        verify(mockViolationSink).put(argThat(hasType(ILLEGAL_SCM_REPOSITORY)));
    }


    @Test
    public void testProcessMissingApp() throws Exception {
        when(mockContext.getScmSource()).thenReturn(Optional.of(singletonMap("url", validRepo.toString())));
        when(mockContext.getKioApplication()).thenReturn(empty());
        when(mockRepositories.parse(eq(validRepo.toString()))).thenReturn(validRepo);

        plugin.process(mockContext);

        verify(mockContext).getScmSource();
        verify(mockContext).getKioApplication();
        verify(mockRepositories).parse(eq(validRepo.toString()));
    }

    @Test
    public void testProcessBlankKioScmUrl() throws Exception {
        when(mockContext.getScmSource()).thenReturn(Optional.of(singletonMap("url", validRepo.toString())));
        when(mockRepositories.parse(eq(validRepo.toString()))).thenReturn(validRepo);
        when(mockContext.getKioApplication()).thenReturn(Optional.of(kioApp));
        kioApp.setScmUrl(" ");

        plugin.process(mockContext);

        verify(mockContext).getScmSource();
        verify(mockRepositories).parse(eq(validRepo.toString()));
        verify(mockContext).getKioApplication();
        verify(mockContext).violation();
        verify(mockViolationSink).put(argThat(hasType(SCM_URL_IS_MISSING_IN_KIO)));
    }

    @Test
    public void testProcessEventWithDifferentScmUrls() throws Exception {
        when(mockContext.getKioApplication()).thenReturn(Optional.of(kioApp));
        when(mockContext.getScmSource()).thenReturn(Optional.of(singletonMap("url", validRepo.toString())));
        when(mockRepositories.parse(eq(validRepo.toString()))).thenReturn(validRepo);
        when(mockRepositories.parse(eq(kioApp.getScmUrl()))).thenReturn(anotherRepo);

        plugin.process(mockContext);

        verify(mockContext).getKioApplication();
        verify(mockContext).getScmSource();
        verify(mockRepositories).parse(eq(validRepo.toString()));
        verify(mockRepositories).parse(eq(kioApp.getScmUrl()));
        verify(mockContext).violation();
        verify(mockViolationSink).put(argThat(hasType(SCM_URL_NOT_MATCH_WITH_KIO)));
    }

    @Test
    public void testProcessEventWithUnparseableKioScmUrl() throws Exception {
        when(mockContext.getKioApplication()).thenReturn(Optional.of(kioApp));
        when(mockContext.getScmSource()).thenReturn(Optional.of(singletonMap("url", validRepo.toString())));
        when(mockRepositories.parse(eq(validRepo.toString()))).thenReturn(validRepo);
        when(mockRepositories.parse(eq(kioApp.getScmUrl()))).thenThrow(new UnknownScmUrlException(kioApp.getScmUrl()));

        plugin.process(mockContext);

        verify(mockContext).getKioApplication();
        verify(mockContext).getScmSource();
        verify(mockRepositories).parse(eq(validRepo.toString()));
        verify(mockRepositories).parse(eq(kioApp.getScmUrl()));
        verify(mockContext).violation();
        verify(mockViolationSink).put(argThat(hasType(SCM_URL_NOT_MATCH_WITH_KIO)));
    }
}
