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
package org.zalando.stups.differentnamespace;

import com.amazonaws.services.cloudtrail.processinglibrary.model.CloudTrailEvent;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.IntegrationTest;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.plugin.core.PluginRegistry;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.zalando.stups.fullstop.plugin.FullstopPlugin;
import org.zalando.stups.fullstop.plugin.RegionPlugin;
import org.zalando.stups.fullstop.plugin.config.RegionPluginProperties;
import org.zalando.stups.fullstop.violation.ViolationSink;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.zalando.stups.fullstop.events.TestCloudTrailEventSerializer.createCloudTrailEvent;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = FullstopApplication.class)
@IntegrationTest("debug=true")
@ActiveProfiles("single")
public class FullstopApplicationSingleIT {

    @Autowired
    private PluginRegistry<FullstopPlugin, CloudTrailEvent> pluginRegistry;

    @Autowired
    private RegionPlugin regionPlugin;

    @Autowired
    private RegionPluginProperties regionPluginProperties;

    @Autowired
    private ViolationSink violationSink;

    @Test
    public void testRegionPlugin() {

        assertThat(regionPluginProperties.getWhitelistedRegions()).containsOnly("us-west-1");

        List<FullstopPlugin> plugins = pluginRegistry.getPlugins();
        assertThat(plugins).isNotEmpty();
        assertThat(plugins).contains(regionPlugin);

        CloudTrailEvent cloudTrailEvent = createCloudTrailEvent("/run-instance-us-west.json");

        for (FullstopPlugin plugin : plugins) {
            plugin.processEvent(cloudTrailEvent);
        }

        assertThat(((CountingViolationSink) violationSink).getInvocationCount()).isEqualTo(0);
    }

    @Test
    public void testRegionPluginThatShouldReportViolations() {

        assertThat(regionPluginProperties.getWhitelistedRegions()).containsOnly("us-west-1");

        List<FullstopPlugin> plugins = pluginRegistry.getPlugins();
        assertThat(plugins).isNotEmpty();
        assertThat(plugins).contains(regionPlugin);

        CloudTrailEvent cloudTrailEvent = createCloudTrailEvent("/run-instance-eu-central.json");

        for (FullstopPlugin plugin : plugins) {
            plugin.processEvent(cloudTrailEvent);
        }

        assertThat(((CountingViolationSink) violationSink).getInvocationCount()).isGreaterThan(0);
    }
}
