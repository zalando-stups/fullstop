/**
 * Copyright (C) 2015 Zalando SE (http://tech.zalando.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.zalando.stups.fullstop.plugin;

import com.amazonaws.services.cloudtrail.processinglibrary.exceptions.CallbackException;
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
    private EC2InstanceContextProviderImpl contextProvider;

    @Before
    public void setUp() {
        final ClientProvider clientProvider = mock(ClientProvider.class);
        final AmiIdProvider amiIdProvider = mock(AmiIdProvider.class);
        final AmiProvider amiProvider = mock(AmiProvider.class);
        final TaupageYamlProvider taupageYamlProvider = mock(TaupageYamlProvider.class);
        final KioApplicationProvider kioApplicationProvider = mock(KioApplicationProvider.class);
        final KioVersionProvider kioVersionProvider = mock(KioVersionProvider.class);
        final KioApprovalProvider kioApprovalProvider = mock(KioApprovalProvider.class);
        
        contextProvider = new EC2InstanceContextProviderImpl(clientProvider,
                amiIdProvider,
                amiProvider,
                taupageYamlProvider,
                "blub",
                "34234",
                kioApplicationProvider,
                kioVersionProvider,
                kioApprovalProvider);

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
