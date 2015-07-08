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
package org.zalando.stups.fullstop.plugin.config;

import com.amazonaws.services.cloudtrail.processinglibrary.exceptions.CallbackException;
import com.amazonaws.services.cloudtrail.processinglibrary.model.CloudTrailEvent;
import com.amazonaws.services.cloudtrail.processinglibrary.model.CloudTrailEventData;
import com.amazonaws.services.cloudtrail.processinglibrary.model.internal.CloudTrailEventField;
import com.amazonaws.services.cloudtrail.processinglibrary.model.internal.UserIdentity;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.zalando.stups.fullstop.events.Records;
import org.zalando.stups.fullstop.events.TestCloudTrailEventData;
import org.zalando.stups.fullstop.plugin.LocalPluginProcessor;
import org.zalando.stups.fullstop.plugin.RegionPlugin;
import org.zalando.stups.fullstop.violation.SystemOutViolationSink;
import org.zalando.stups.fullstop.violation.Violation;
import org.zalando.stups.fullstop.violation.ViolationSink;

import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.*;

/**
 * @author jbellmann
 */
public class RegionPluginTest {

    private ViolationSink violationSink = new SystemOutViolationSink();

    ;

    private RegionPluginProperties regionPluginProperties;

    @Before
    public void setUp() {
        violationSink = Mockito.spy(violationSink);
        regionPluginProperties = new RegionPluginProperties();
    }

    @Test
    public void testWhitelistedRegion() {
        CloudTrailEventData data = new RegionPluginTestCloudTrailEventData("/responseElements.json", "eu-west-1");
        UserIdentity userIdentity = new UserIdentity();
        userIdentity.add(CloudTrailEventField.accountId.name(), "0234527346");
        data.add(CloudTrailEventField.userIdentity.name(), userIdentity);

        CloudTrailEvent event = new CloudTrailEvent(data, null);

        //
        RegionPlugin plugin = new RegionPlugin(violationSink, regionPluginProperties);
        plugin.processEvent(event);

        verify(violationSink, never()).put(Mockito.any(Violation.class));
    }

    @Test
    public void testNonWhitelistedRegion() {

        CloudTrailEvent event = buildEvent("withResp");

        //
        RegionPlugin plugin = new RegionPlugin(violationSink, regionPluginProperties);
        plugin.processEvent(event);

        verify(violationSink, atLeastOnce()).put(Mockito.any(Violation.class));
    }

    @Test
    public void testWithLocalPluginProcessor() throws CallbackException {
        RegionPlugin plugin = new RegionPlugin(violationSink, regionPluginProperties);
        LocalPluginProcessor lpp = new LocalPluginProcessor(plugin);
        lpp.processEvents(getClass().getResourceAsStream("/record-run.json"));
    }

    protected CloudTrailEvent buildEvent(final String type) {
        List<Map<String, Object>> records = Records.fromClasspath("/record-" + type + ".json");

        return new CloudTrailEvent(new TestCloudTrailEventData(records.get(0), "/responseElements.json"), null);

        // return TestCloudTrailEventData.createCloudTrailEventFromMap(records.get(0));
    }

}
