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

import com.amazonaws.services.cloudtrail.processinglibrary.model.CloudTrailEvent;
import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.zalando.stups.fullstop.events.TestCloudTrailEventData;

import java.util.List;

import static org.zalando.stups.fullstop.events.CloudtrailEventSupport.*;

/**
 * @author jbellmann
 */
public class SecurityGroupTest {

    @Test
    public void testReadingPrivateIpAddress() {
        CloudTrailEvent event = new CloudTrailEvent(new TestCloudTrailEventData("/responseElements.json"), null);

        List<String> result = read(event, PRIVATE_IP_JSON_PATH, true);

        Assertions.assertThat(result).isNotEmpty();
        Assertions.assertThat(result).contains("172.31.12.21");
    }

    @Test
    public void testReadingPublicIpAddress() {
        CloudTrailEvent event = new CloudTrailEvent(new TestCloudTrailEventData("/responseElements.json"), null);

        List<String> result = read(event, PUBLIC_IP_JSON_PATH, true);

        Assertions.assertThat(result).isEmpty();
    }

}
