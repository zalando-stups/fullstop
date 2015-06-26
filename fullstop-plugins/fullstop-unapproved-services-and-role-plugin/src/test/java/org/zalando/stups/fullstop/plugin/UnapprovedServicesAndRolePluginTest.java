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

import com.amazonaws.auth.policy.Policy;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.cloudtrail.processinglibrary.model.CloudTrailEvent;
import org.assertj.core.api.Assertions;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.zalando.stups.fullstop.events.Records;
import org.zalando.stups.fullstop.events.TestCloudTrailEventData;
import org.zalando.stups.fullstop.violation.ViolationStore;

import java.util.List;
import java.util.Map;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

/**
 * Created by mrandi.
 */
public class UnapprovedServicesAndRolePluginTest {

    private PolicyProvider policyProviderMock;

    private ViolationStore violationStoreMock;

    private CloudTrailEvent event;

    private UnapprovedServicesAndRolePlugin plugin;

    @Before
    public void setUp() throws Exception {
        policyProviderMock = mock(PolicyProvider.class);
        violationStoreMock = mock(ViolationStore.class);

        event = buildEvent();

        plugin = new UnapprovedServicesAndRolePlugin(
                policyProviderMock,
                violationStoreMock);

    }

    @After
    public void tearDown() throws Exception {
        verifyNoMoreInteractions(policyProviderMock, violationStoreMock);
    }

    protected CloudTrailEvent buildEvent() {
        List<Map<String, Object>> records = Records.fromClasspath("/record.json");

        Map<String, Object> record = records.get(0);
        System.out.println(record.toString());

        TestCloudTrailEventData eventData = new TestCloudTrailEventData(record);

        System.out.println(eventData.toString());

        return new CloudTrailEvent(eventData, null);
    }

    @Test
    public void testSupports() throws Exception {

        boolean result = plugin.supports(event);
        Assertions.assertThat(result).isTrue();
    }

    @Test
    public void testProcessEvent() throws Exception {

        String roleName = "roleName";
        Region region = Region.getRegion(Regions.fromName("eu-west-1"));
        String accountId = "accountId";

        when(policyProviderMock.getPolicy(roleName, region, accountId)).thenReturn(new Policy());

        plugin.processEvent(event);

        verify(policyProviderMock).getPolicy(any(), any(), any());

    }
}