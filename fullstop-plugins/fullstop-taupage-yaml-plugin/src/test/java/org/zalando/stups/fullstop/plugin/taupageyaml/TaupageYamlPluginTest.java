package org.zalando.stups.fullstop.plugin.taupageyaml;

import static com.google.common.collect.Maps.newHashMap;
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.zalando.stups.fullstop.violation.ViolationMatchers.hasType;
import static org.zalando.stups.fullstop.violation.ViolationType.MISSING_APPLICATION_ID_IN_USER_DATA;
import static org.zalando.stups.fullstop.violation.ViolationType.MISSING_APPLICATION_VERSION_IN_USER_DATA;
import static org.zalando.stups.fullstop.violation.ViolationType.MISSING_SOURCE_IN_USER_DATA;

import java.util.Map;
import java.util.Optional;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.zalando.stups.fullstop.plugin.EC2InstanceContext;
import org.zalando.stups.fullstop.plugin.EC2InstanceContextProvider;
import org.zalando.stups.fullstop.plugin.provider.TaupageYamlProvider;
import org.zalando.stups.fullstop.violation.ViolationBuilder;
import org.zalando.stups.fullstop.violation.ViolationSink;

public class TaupageYamlPluginTest {

    private ViolationSink mockViolationSink;
    private EC2InstanceContextProvider mockContextProvider;
    private EC2InstanceContext mockContext;
    private TaupageYamlPlugin plugin;

    @Before
    public void setUp() throws Exception {

        mockViolationSink = mock(ViolationSink.class);
        mockContextProvider = mock(EC2InstanceContextProvider.class);
        mockContext = mock(EC2InstanceContext.class);
        plugin = new TaupageYamlPlugin(mockContextProvider, mockViolationSink);

        when(mockContext.violation()).thenReturn(new ViolationBuilder());
    }

    @After
    public void tearDown() throws Exception {
        verifyNoMoreInteractions(mockViolationSink, mockContextProvider, mockContext);
    }

    @Test
    public void testSupportsEventName() throws Exception {

    }

    @Test
    public void testProcessNoViolations() throws Exception {
        when(mockContext.isTaupageAmi()).thenReturn(Optional.of(true));
        when(mockContext.getTaupageYaml()).thenReturn(Optional.of(newHashMap()));
        when(mockContext.getApplicationId()).thenReturn(Optional.of("1234"));
        when(mockContext.getVersionId()).thenReturn(Optional.of("1.0.0.FINAL"));
        when(mockContext.getSource()).thenReturn(Optional.of("source"));

        plugin.process(mockContext);

        verify(mockContext).isTaupageAmi();
        verify(mockContext).getTaupageYaml();
        verify(mockContext).getApplicationId();
        verify(mockContext).getVersionId();
        verify(mockContext).getSource();
    }

    @Test
    public void testProcessNoTaupageAmi() throws Exception {
        when(mockContext.isTaupageAmi()).thenReturn(Optional.of(false));

        plugin.process(mockContext);

        verify(mockContext).isTaupageAmi();
    }

    @Test
    public void testProcessNullableTaupageAmi() throws Exception {
        when(mockContext.isTaupageAmi()).thenReturn(Optional.empty());

        plugin.process(mockContext);

        verify(mockContext).isTaupageAmi();
    }

    @Test
    public void testProcessMissingAppIdViolation() throws Exception {
        when(mockContext.isTaupageAmi()).thenReturn(Optional.of(true));
        when(mockContext.getTaupageYaml()).thenReturn(Optional.of(newHashMap()));
        when(mockContext.getVersionId()).thenReturn(Optional.of("1.0.0.FINAL"));
        when(mockContext.getSource()).thenReturn(Optional.of("source"));

        when(mockContext.getApplicationId()).thenReturn(Optional.empty());

        plugin.process(mockContext);

        verify(mockContext).isTaupageAmi();
        verify(mockContext).getTaupageYaml();
        verify(mockContext).getApplicationId();
        verify(mockContext).getVersionId();
        verify(mockContext).getSource();
        verify(mockContext).violation();
        verify(mockViolationSink).put(argThat(hasType(MISSING_APPLICATION_ID_IN_USER_DATA)));
    }

    @Test
    public void testProcessMissingAppVersionViolation() throws Exception {
        when(mockContext.isTaupageAmi()).thenReturn(Optional.of(true));
        when(mockContext.getApplicationId()).thenReturn(Optional.of("1234"));
        when(mockContext.getTaupageYaml()).thenReturn(Optional.of(newHashMap()));
        when(mockContext.getSource()).thenReturn(Optional.of("source"));

        when(mockContext.getVersionId()).thenReturn(Optional.empty());

        plugin.process(mockContext);

        verify(mockContext).isTaupageAmi();
        verify(mockContext).getTaupageYaml();
        verify(mockContext).getApplicationId();
        verify(mockContext).getVersionId();
        verify(mockContext).getSource();
        verify(mockContext).violation();
        verify(mockViolationSink).put(argThat(hasType(MISSING_APPLICATION_VERSION_IN_USER_DATA)));
    }

    @Test
    public void testProcessMissingSourceViolation() throws Exception {
        when(mockContext.isTaupageAmi()).thenReturn(Optional.of(true));
        when(mockContext.getApplicationId()).thenReturn(Optional.of("1234"));
        when(mockContext.getTaupageYaml()).thenReturn(Optional.of(newHashMap()));
        when(mockContext.getVersionId()).thenReturn(Optional.of("1.0.0.FINAL"));

        when(mockContext.getSource()).thenReturn(Optional.empty());

        plugin.process(mockContext);

        verify(mockContext).isTaupageAmi();
        verify(mockContext).getTaupageYaml();
        verify(mockContext).getApplicationId();
        verify(mockContext).getVersionId();
        verify(mockContext).getSource();
        verify(mockContext).violation();
        verify(mockViolationSink).put(argThat(hasType(MISSING_SOURCE_IN_USER_DATA)));
    }
}
