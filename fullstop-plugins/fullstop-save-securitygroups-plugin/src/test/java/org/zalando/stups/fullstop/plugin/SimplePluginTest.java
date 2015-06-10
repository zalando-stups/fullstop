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

import java.util.List;
import java.util.Map;

import org.assertj.core.api.Assertions;

import org.junit.Test;

import org.zalando.stups.fullstop.events.Records;
import org.zalando.stups.fullstop.events.TestCloudTrailEventData;

import com.amazonaws.services.cloudtrail.processinglibrary.model.CloudTrailEvent;

public class SimplePluginTest {

    @Test
    public void createCloudTrailEvent() {

        List<Map<String, Object>> records = Records.fromClasspath("/record.json");

        Map<String, Object> record = records.get(0);
        System.out.println(record.toString());

        CloudTrailEvent event = TestCloudTrailEventData.createCloudTrailEventFromMap(records.get(0));

        // we expect RunInstance and ec2 as source not, autoscaling
        SaveSecurityGroupsPlugin plugin = new SaveSecurityGroupsPlugin(null);
        boolean result = plugin.supports(event);
        Assertions.assertThat(result).isFalse();
    }

}
