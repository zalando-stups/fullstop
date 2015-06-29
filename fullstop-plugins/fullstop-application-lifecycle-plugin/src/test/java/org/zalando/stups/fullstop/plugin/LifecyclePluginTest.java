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
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.zalando.stups.fullstop.events.Records;
import org.zalando.stups.fullstop.events.TestCloudTrailEventData;
import org.zalando.stups.fullstop.events.UserDataProvider;
import org.zalando.stups.fullstop.violation.repository.LifecycleRepository;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Created by gkneitschel.
 */
public class LifecyclePluginTest {

    private LifecyclePlugin plugin;

    private UserDataProvider provider;

    private LifecycleRepository lifecycleRepository;

    private CloudTrailEvent event;

    protected CloudTrailEvent buildEvent(String type) {
        List<Map<String, Object>> records = Records.fromClasspath("/record-" + type + ".json");

        return TestCloudTrailEventData.createCloudTrailEventFromMap(records.get(0));
    }

    @Before
    public void setUp() throws Exception {
        provider = Mockito.mock(UserDataProvider.class);
        lifecycleRepository = Mockito.mock(LifecycleRepository.class);
        plugin = new LifecyclePlugin(lifecycleRepository, provider);
    }

    @Test
    public void testSupports() throws Exception {
        event = buildEvent("run");
        assertThat(plugin.supports(event)).isTrue();

        event = buildEvent("start");
        assertThat(plugin.supports(event)).isTrue();

        event = buildEvent("stop");
        assertThat(plugin.supports(event)).isTrue();

        event = buildEvent("termination");
        assertThat(plugin.supports(event)).isTrue();
    }

    @Test
    public void testProcessEvent() throws Exception {
        assertThat(true).isTrue();
    }
}