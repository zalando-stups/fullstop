package org.zalando.stups.fullstop.plugin;

import static org.mockito.Mockito.mock;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.zalando.stups.clients.kio.KioOperations;
import org.zalando.stups.fullstop.clients.pierone.PieroneOperations;
import org.zalando.stups.fullstop.events.Records;
import org.zalando.stups.fullstop.events.TestCloudTrailEventData;
import org.zalando.stups.fullstop.events.UserDataProvider;
import org.zalando.stups.fullstop.plugin.config.RegistryPluginProperties;
import org.zalando.stups.fullstop.violation.Violation;
import org.zalando.stups.fullstop.violation.ViolationSink;

import com.amazonaws.services.cloudtrail.processinglibrary.model.CloudTrailEvent;

public class RegistryPluginUserdataTest {

    private KioOperations kioOperations;

    private PieroneOperations pieroneOperations;

    private CloudTrailEvent event;

    private ViolationSink violationSink;

    private UserDataProvider userDataProvider;

    private RegistryPlugin registryPlugin;

    private RegistryPluginProperties pluginConfiguration;

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
    }

    @After
    public void tearDown() {
        verifyNoMoreInteractions(userDataProvider,
                                 kioOperations,
                                 pieroneOperations,
                                 violationSink);
    }

    @Test
    public void shouldComplainWithMissingUserData() {
        when(userDataProvider.getUserData(any(),
                                          any())).thenReturn(null);
        registryPlugin.getAndValidateUserData(event,
                                              "foo");

        verify(userDataProvider).getUserData(any(),
                                             any());
        verify(violationSink).put(any(Violation.class));
    }

    @Test
    public void shouldComplainWithEmptyUserData() {
        Map userData = new HashMap();
        when(userDataProvider.getUserData(any(),
                                          any())).thenReturn(userData);

        registryPlugin.getAndValidateUserData(event,
                                              "foo");

        verify(userDataProvider).getUserData(any(),
                                             any());

        verify(violationSink).put(any(Violation.class));
    }

    @Test
    public void shouldNotComplainWithFilledUserData() {
        Map userData = new HashMap();
        userData.put("foo",
                     "bar");
        when(userDataProvider.getUserData(any(),
                                          any())).thenReturn(userData);

        registryPlugin.getAndValidateUserData(event,
                                              "foo");

        verify(userDataProvider).getUserData(any(),
                                             any());

        verify(violationSink,
               never()).put(any(Violation.class));
    }

    @Test
    public void shouldComplainWithoutApplicationId() {
        Map userData = new HashMap();
        userData.put("foo",
                     "bar");

        registryPlugin.getAndValidateApplicationId(event,
                                                   userData,
                                                   "foo");

        verify(violationSink).put(any(Violation.class));
    }

    @Test
    public void shouldNotComplainWithApplicationId() {
        Map userData = new HashMap();
        userData.put(RegistryPlugin.APPLICATION_ID,
                     "bar");

        registryPlugin.getAndValidateApplicationId(event,
                                                   userData,
                                                   "foo");

        verify(violationSink,
               never()).put(any(Violation.class));
    }

    @Test
    public void shouldComplainWithoutApplicationVersion() {
        Map userData = new HashMap();
        userData.put("foo",
                     "bar");

        registryPlugin.getAndValidateApplicationVersion(event,
                                                        userData,
                                                        "foo");

        verify(violationSink).put(any(Violation.class));
    }

    @Test
    public void shouldNotComplainWithApplicationVersion() {
        Map userData = new HashMap();
        userData.put(RegistryPlugin.APPLICATION_VERSION,
                     "bar");

        registryPlugin.getAndValidateApplicationVersion(event,
                                                        userData,
                                                        "foo");

        verify(violationSink,
               never()).put(any(Violation.class));
    }

    @Test
    public void shouldComplainWithoutSource() {
        Map userData = new HashMap();
        userData.put("foo",
                     "bar");

        registryPlugin.getAndValidateSource(event,
                                            userData,
                                            "foo");

        verify(violationSink).put(any(Violation.class));
    }

    @Test
    public void shouldNotComplainWithSource() {
        Map userData = new HashMap();
        userData.put(RegistryPlugin.SOURCE,
                     "bar");

        registryPlugin.getAndValidateSource(event,
                                            userData,
                                            "foo");

        verify(violationSink,
               never()).put(any(Violation.class));
    }
}
