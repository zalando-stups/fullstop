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
package org.zalando.stups.fullstop.plugin.unapproved;

import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.cloudtrail.processinglibrary.model.CloudTrailEvent;
import org.assertj.core.api.Assertions;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.zalando.stups.fullstop.events.Records;
import org.zalando.stups.fullstop.events.TestCloudTrailEventData;
import org.zalando.stups.fullstop.violation.Violation;
import org.zalando.stups.fullstop.violation.ViolationSink;

import java.util.List;
import java.util.Map;

import static com.google.common.collect.Lists.newArrayList;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

/**
 * Created by mrandi.
 */
public class UnapprovedServicesAndRolePluginTest {

    private PolicyProvider policyProviderMock;

    private ViolationSink violationSinkMock;

    private PolicyTemplateCaching policyTemplateCachingMock;

    private CloudTrailEvent event;


    private UnapprovedServicesAndRolePlugin plugin;

    @Before
    public void setUp() throws Exception {
        policyProviderMock = mock(PolicyProvider.class);
        violationSinkMock = mock(ViolationSink.class);
        policyTemplateCachingMock = mock(PolicyTemplateCaching.class);

        event = buildEvent();

        plugin = new UnapprovedServicesAndRolePlugin(
                policyProviderMock,
                violationSinkMock,
                policyTemplateCachingMock);

    }

    @After
    public void tearDown() throws Exception {
        verifyNoMoreInteractions(policyProviderMock, violationSinkMock, policyTemplateCachingMock);
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

        when(policyTemplateCachingMock.getS3Objects()).thenReturn(newArrayList("mint-worker-b17-AppServerRole-W5WX8WewafwO2MEWZ"));

        boolean result = plugin.supports(event);
        Assertions.assertThat(result).isTrue();

        verify(policyTemplateCachingMock).getS3Objects();
    }

    @Test
    public void testProcessEvent() throws Exception {

        String roleName = "mint-worker-b17-AppServerRole-W5WX8WewafwO2MEWZ";
        Region region = Region.getRegion(Regions.fromName("us-east-1"));
        String accountId = "XXXX123XXX";

        when(policyProviderMock.getPolicy(roleName, region, accountId)).thenReturn(anyString());
        when(policyTemplateCachingMock.getPolicyTemplate(any())).thenReturn("");

        plugin.processEvent(event);

        verify(policyProviderMock).getPolicy(any(), any(), any());
        verify(policyTemplateCachingMock).getPolicyTemplate(any());
        verify(violationSinkMock).put(any(Violation.class));

    }
}