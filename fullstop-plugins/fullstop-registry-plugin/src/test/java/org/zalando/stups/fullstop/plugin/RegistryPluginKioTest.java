package org.zalando.stups.fullstop.plugin;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.zalando.stups.clients.kio.Application;
import org.zalando.stups.clients.kio.KioOperations;
import org.zalando.stups.clients.kio.NotFoudException;
import org.zalando.stups.clients.kio.Version;
import org.zalando.stups.fullstop.clients.pierone.PieroneOperations;
import org.zalando.stups.fullstop.events.Records;
import org.zalando.stups.fullstop.events.TestCloudTrailEventData;
import org.zalando.stups.fullstop.events.UserDataProvider;
import org.zalando.stups.fullstop.plugin.config.RegistryPluginProperties;
import org.zalando.stups.fullstop.violation.Violation;
import org.zalando.stups.fullstop.violation.ViolationSink;

import com.amazonaws.services.cloudtrail.processinglibrary.model.CloudTrailEvent;

public class RegistryPluginKioTest {

    private KioOperations kioOperations;

    private PieroneOperations pieroneOperations;

    private CloudTrailEvent event;

    private ViolationSink violationSink;

    private UserDataProvider userDataProvider;

    private RegistryPlugin registryPlugin;

    private RegistryPluginProperties pluginConfiguration;

    private Application application;

    private Version version;

    private static final String APPLICATION_ID = "fullstop";

    private static final String APPLICATION_VERSION = "1.0";

    protected CloudTrailEvent buildEvent() {
        List<Map<String, Object>> records = Records.fromClasspath("/record.json");

        CloudTrailEvent event = TestCloudTrailEventData.createCloudTrailEventFromMap(records.get(0));
        return event;
    }

    @Before
    public void setUp() {
        event = buildEvent();
        userDataProvider = mock(UserDataProvider.class);
        kioOperations = mock(KioOperations.class);
        pieroneOperations = mock(PieroneOperations.class);
        violationSink = mock(ViolationSink.class);
        pluginConfiguration = new RegistryPluginProperties();
        registryPlugin = new RegistryPlugin(userDataProvider,
                                            violationSink,
                                            pieroneOperations,
                                            kioOperations,
                                            pluginConfiguration);
        application = new Application();
        application.setId(APPLICATION_ID);

        version = new Version();
        version.setApplicationId(APPLICATION_ID);
        version.setId(APPLICATION_VERSION);
    }

    @After
    public void tearDown() {
        verifyNoMoreInteractions(userDataProvider,
                                 kioOperations,
                                 pieroneOperations,
                                 violationSink);
    }

    @Test
    public void shouldComplainWhenApplicationNotFound() {
        when(kioOperations.getApplicationById(APPLICATION_ID)).thenThrow(new NotFoudException());

        registryPlugin.getAndValidateApplicationFromKio(event,
                                                        APPLICATION_ID);
        verify(kioOperations).getApplicationById(APPLICATION_ID);
        verify(violationSink).put(any(Violation.class));
    }

    @Test
    public void shouldNotComplainWhenApplicationFound() {
        when(kioOperations.getApplicationById(APPLICATION_ID)).thenReturn(application);

        registryPlugin.getAndValidateApplicationFromKio(event,
                                                        APPLICATION_ID);
        verify(kioOperations).getApplicationById(APPLICATION_ID);
        verify(violationSink,
               never()).put(any(Violation.class));
    }

    @Test
    public void shouldComplainWhenVersionNotFound() {
        when(kioOperations.getApplicationVersion(APPLICATION_ID,
                                                 APPLICATION_VERSION)).thenThrow(new NotFoudException());

        registryPlugin.getAndValidateApplicationVersionFromKio(event,
                                                               APPLICATION_ID,
                                                               APPLICATION_VERSION);

        verify(kioOperations).getApplicationVersion(APPLICATION_ID,
                                                    APPLICATION_VERSION);
        verify(violationSink).put(any(Violation.class));
    }

    @Test
    public void shouldNotComplainWhenVersionFound() {
        when(kioOperations.getApplicationVersion(APPLICATION_ID,
                                                 APPLICATION_VERSION)).thenReturn(version);

        registryPlugin.getAndValidateApplicationVersionFromKio(event,
                                                               APPLICATION_ID,
                                                               APPLICATION_VERSION);

        verify(kioOperations).getApplicationVersion(APPLICATION_ID,
                                                    APPLICATION_VERSION);
        verify(violationSink,
               never()).put(any(Violation.class));
    }
}
