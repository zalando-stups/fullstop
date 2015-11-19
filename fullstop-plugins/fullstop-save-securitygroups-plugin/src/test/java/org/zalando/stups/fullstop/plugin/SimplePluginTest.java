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

import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.zalando.stups.fullstop.events.TestCloudTrailEventSerializer;
import org.zalando.stups.fullstop.s3.S3Service;

import static org.mockito.Mockito.mock;

public class SimplePluginTest {

    @Test
    public void createCloudTrailEvent() {
        SecurityGroupProvider provider = mock(SecurityGroupProvider.class);
        S3Service writer = mock(S3Service.class);

        // we expect RunInstance and ec2 as source not, autoscaling
        SaveSecurityGroupsPlugin plugin = new SaveSecurityGroupsPlugin(provider, writer);
        boolean result = plugin.supports(TestCloudTrailEventSerializer.createCloudTrailEvent("/record.json"));
        Assertions.assertThat(result).isFalse();
    }
}
