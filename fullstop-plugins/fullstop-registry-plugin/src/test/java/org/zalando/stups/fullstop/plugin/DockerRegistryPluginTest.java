package org.zalando.stups.fullstop.plugin;

import com.google.common.collect.ImmutableMap;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.zalando.stups.fullstop.violation.ViolationBuilder;
import org.zalando.stups.fullstop.violation.ViolationSink;
import org.zalando.stups.pierone.client.TagSummary;

import java.time.ZonedDateTime;
import java.util.Optional;

import static java.util.Optional.empty;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.zalando.stups.fullstop.violation.ViolationMatchers.hasType;
import static org.zalando.stups.fullstop.violation.ViolationType.ARTIFACT_BUILT_FROM_DIRTY_REPOSITORY;
import static org.zalando.stups.fullstop.violation.ViolationType.IMAGE_IN_PIERONE_NOT_FOUND;
import static org.zalando.stups.fullstop.violation.ViolationType.SCM_SOURCE_JSON_MISSING;
import static org.zalando.stups.fullstop.violation.ViolationType.SCM_URL_IS_MISSING_IN_SCM_SOURCE_JSON;

public class DockerRegistryPluginTest {


    /**
     * <code>git status --porcelain</code> lists all uncommitted or untracked changes.
     */
    private static final String DIRTY_REPO_STATUS = "M pom.xml";

    /**
     * <code>git status --porcelain</code> is empty for clean repositories. (No uncommitted or untracked changes)
     */
    private static final String CLEAN_REPO_STATUS = "";

    private DockerRegistryPlugin plugin;

    private EC2InstanceContextProvider mockContextProvider;
    private ViolationSink mockViolationSink;
    private EC2InstanceContext mockContext;

    @Before
    public void setUp() throws Exception {
        mockContextProvider = mock(EC2InstanceContextProvider.class);
        mockViolationSink = mock(ViolationSink.class);
        mockContext = mock(EC2InstanceContext.class);

        when(mockContext.violation()).thenAnswer(new Answer<ViolationBuilder>() {
            @Override
            public ViolationBuilder answer(InvocationOnMock invocation) throws Throwable {
                return new ViolationBuilder();
            }
        });

        plugin = new DockerRegistryPlugin(mockContextProvider, mockViolationSink);
    }

    @After
    public void tearDown() throws Exception {
        verifyNoMoreInteractions(mockContextProvider, mockViolationSink);
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
    public void testProcessIsSkippedOnUnknownRuntime() throws Exception {
        when(mockContext.getRuntime()).thenReturn(empty());

        plugin.process(mockContext);
    }

    @Test
    public void testProcessIsSkippedOnNonDockerRuntime() throws Exception {
        when(mockContext.getRuntime()).thenReturn(Optional.of("rkt"));

        plugin.process(mockContext);
    }

    @Test
    public void testProcessIsSkippedOnMissingSource() throws Exception {
        when(mockContext.getRuntime()).thenReturn(Optional.of("Docker"));
        when(mockContext.getSource()).thenReturn(empty());

        plugin.process(mockContext);
    }

    @Test
    public void testProcessThrowsViolationAndSkipsOnMissingPieroneTag() throws Exception {
        when(mockContext.getRuntime()).thenReturn(Optional.of("Docker"));
        when(mockContext.getSource()).thenReturn(Optional.of("pierone.example.org/stups/foo:bar1"));
        when(mockContext.getPieroneTag()).thenReturn(empty());

        plugin.process(mockContext);

        verify(mockViolationSink).put(argThat(hasType(IMAGE_IN_PIERONE_NOT_FOUND)));
    }

    @Test
    public void testProcessThrowsViolationAndSkipsOnMissingScmSource() throws Exception {
        when(mockContext.getRuntime()).thenReturn(Optional.of("Docker"));
        when(mockContext.getSource()).thenReturn(Optional.of("pierone.example.org/stups/foo:bar1"));
        when(mockContext.getPieroneTag()).thenReturn(Optional.of(new TagSummary("bar1", ZonedDateTime.now(), "test_user")));
        when(mockContext.getScmSource()).thenReturn(empty());

        plugin.process(mockContext);

        verify(mockViolationSink).put(argThat(hasType(SCM_SOURCE_JSON_MISSING)));
    }

    @Test
    public void testProcessThrowsViolationsOnBadScmSourceJson() throws Exception {
        when(mockContext.getRuntime()).thenReturn(Optional.of("Docker"));
        when(mockContext.getSource()).thenReturn(Optional.of("pierone.example.org/stups/foo:bar1"));
        when(mockContext.getPieroneTag()).thenReturn(Optional.of(new TagSummary("bar1", ZonedDateTime.now(), "test_user")));
        when(mockContext.getScmSource()).thenReturn(Optional.of(ImmutableMap.of("status", DIRTY_REPO_STATUS)));

        plugin.process(mockContext);

        verify(mockViolationSink).put(argThat(hasType(SCM_URL_IS_MISSING_IN_SCM_SOURCE_JSON)));
        verify(mockViolationSink).put(argThat(hasType(ARTIFACT_BUILT_FROM_DIRTY_REPOSITORY)));
    }

    @Test
    public void testProcessWithoutViolations() throws Exception {
        when(mockContext.getRuntime()).thenReturn(Optional.of("Docker"));
        when(mockContext.getSource()).thenReturn(Optional.of("pierone.example.org/stups/foo:bar1"));
        when(mockContext.getPieroneTag()).thenReturn(Optional.of(new TagSummary("bar1", ZonedDateTime.now(), "test_user")));
        when(mockContext.getScmSource()).thenReturn(Optional.of(ImmutableMap.of(
                "status", CLEAN_REPO_STATUS,
                "url", "git@github.com:zalando-stups/fullstop.git")));

        plugin.process(mockContext);
    }

    @Test
    public void testProcessWithoutViolationsOnStatusFalse() throws Exception {
        when(mockContext.getRuntime()).thenReturn(Optional.of("Docker"));
        when(mockContext.getSource()).thenReturn(Optional.of("pierone.example.org/stups/foo:bar1"));
        when(mockContext.getPieroneTag()).thenReturn(Optional.of(new TagSummary("bar1", ZonedDateTime.now(), "test_user")));
        when(mockContext.getScmSource()).thenReturn(Optional.of(ImmutableMap.of(
                "status", "FALSE",
                "url", "git@github.com:zalando-stups/fullstop.git")));

        plugin.process(mockContext);
    }
}
