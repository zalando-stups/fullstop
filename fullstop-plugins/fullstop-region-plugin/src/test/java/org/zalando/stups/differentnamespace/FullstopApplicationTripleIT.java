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
import com.amazonaws.services.cloudtrail.processinglibrary.model.CloudTrailEventData;
import com.amazonaws.services.cloudtrail.processinglibrary.model.internal.CloudTrailEventField;
import com.amazonaws.services.cloudtrail.processinglibrary.model.internal.UserIdentity;
import org.assertj.core.api.Assertions;
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
import org.zalando.stups.fullstop.plugin.config.RegionPluginTestCloudTrailEventData;

import java.util.List;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = FullstopApplication.class)
@IntegrationTest("debug=true")
@ActiveProfiles("triple")
public class FullstopApplicationTripleIT {

    @Autowired
    private PluginRegistry<FullstopPlugin, CloudTrailEvent> pluginRegistry;

    @Autowired
    private RegionPlugin regionPlugin;

    @Autowired
    private RegionPluginProperties regionPluginProperties;

    @Test
    public void testRegionPlugin() {

        Assertions.assertThat(regionPluginProperties.getWhitelistedRegions()).containsOnly("us-west-1", "us-east-1",
                "us-west-2");

        List<FullstopPlugin> plugins = pluginRegistry.getPlugins();
        Assertions.assertThat(plugins).isNotEmpty();
        Assertions.assertThat(plugins).contains(regionPlugin);

        CloudTrailEvent cloudTrailEvent = buildEvent();

        for (FullstopPlugin plugin : plugins) {
            plugin.processEvent(cloudTrailEvent);
        }
    }

    private CloudTrailEvent buildEvent() {
        CloudTrailEventData data = new RegionPluginTestCloudTrailEventData("/responseElements.json", "us-west-1");
        UserIdentity userIdentity = new UserIdentity();
        userIdentity.add(CloudTrailEventField.accountId.name(), "0234527346");
        data.add(CloudTrailEventField.userIdentity.name(), userIdentity);

        CloudTrailEvent event = new CloudTrailEvent(data, null);

        return event;
    }
}
