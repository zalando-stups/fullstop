package org.zalando.stups.fullstop.plugin;

import com.amazonaws.services.cloudtrail.processinglibrary.exceptions.CallbackException;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.zalando.stups.fullstop.aws.ClientProvider;
import org.zalando.stups.fullstop.plugin.config.RegionPluginProperties;
import org.zalando.stups.fullstop.plugin.impl.EC2InstanceContextProviderImpl;
import org.zalando.stups.fullstop.plugin.provider.*;
import org.zalando.stups.fullstop.violation.SystemOutViolationSink;
import org.zalando.stups.fullstop.violation.Violation;
import org.zalando.stups.fullstop.violation.ViolationSink;

import java.util.Map;

import static java.util.Optional.of;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;
import static org.zalando.stups.fullstop.events.TestCloudTrailEventSerializer.createCloudTrailEvent;

/**
 * @author jbellmann
 */
public class RegionPluginTest {

    private ViolationSink violationSink = new SystemOutViolationSink();
    private RegionPlugin plugin;
    private RegionPluginProperties regionPluginProperties;
    private EC2InstanceContextProvider contextProvider;
    private TaupageYamlProvider taupageYamlProvider;

    @Before
    public void setUp() {
        final ClientProvider clientProvider = mock(ClientProvider.class);
        final AmiIdProvider amiIdProvider = mock(AmiIdProvider.class);
        final AmiProvider amiProvider = mock(AmiProvider.class);
        final KioApplicationProvider kioApplicationProvider = mock(KioApplicationProvider.class);
        final KioVersionProvider kioVersionProvider = mock(KioVersionProvider.class);
        final KioApprovalProvider kioApprovalProvider = mock(KioApprovalProvider.class);
        final PieroneTagProvider pieroneTagProvider = mock(PieroneTagProvider.class);
        final ScmSourceProvider scmSourceProvider = mock(ScmSourceProvider.class);

        taupageYamlProvider = mock(TaupageYamlProvider.class);

        contextProvider = new EC2InstanceContextProviderImpl(clientProvider,
                amiIdProvider,
                amiProvider,
                taupageYamlProvider,
                "blub",
                Lists.newArrayList("1234"),
                kioApplicationProvider,
                kioVersionProvider,
                kioApprovalProvider,
                pieroneTagProvider,
                scmSourceProvider);

        violationSink = Mockito.spy(violationSink);
        regionPluginProperties = new RegionPluginProperties();
        plugin = new RegionPlugin(contextProvider, violationSink, regionPluginProperties);
    }

    @Test
    public void testWhitelistedRegion() {
        plugin.processEvent(createCloudTrailEvent("/responseElements.json"));

        verify(violationSink, never()).put(any(Violation.class));
    }

    @Test
    public void testNonWhitelistedRegion() {
        Map<String, String> taupageYaml = Maps.newHashMap();
        taupageYaml.put("application_id", "test123");
        taupageYaml.put("application_version", "0.12");
        when(taupageYamlProvider.apply(
                contextProvider.instancesIn(createCloudTrailEvent("/run-instance-us-west.json")).get(0))).
                thenReturn(of(taupageYaml));
        plugin.processEvent(createCloudTrailEvent("/run-instance-us-west.json"));

        verify(violationSink, atLeastOnce()).put(any(Violation.class));
    }

    @Test
    public void testWithLocalPluginProcessor() throws CallbackException {
        RegionPlugin plugin = new RegionPlugin(contextProvider, violationSink, regionPluginProperties);
        LocalPluginProcessor lpp = new LocalPluginProcessor(plugin);
        lpp.processEvents(getClass().getResourceAsStream("/record-run.json"));
    }
}
