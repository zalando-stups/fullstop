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

import com.amazonaws.services.cloudtrail.processinglibrary.model.CloudTrailEvent;
import org.assertj.core.api.Assertions;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.zalando.stups.fullstop.plugin.unapproved.config.UnapprovedServicesAndRoleProperties;
import org.zalando.stups.fullstop.violation.Violation;
import org.zalando.stups.fullstop.violation.ViolationSink;

import static com.google.common.collect.Lists.newArrayList;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;
import static org.zalando.stups.fullstop.events.TestCloudTrailEventSerializer.createCloudTrailEvent;

/**
 * Created by mrandi.
 */
public class UnapprovedServicesAndRolePluginTest {

    private PolicyProvider policyProviderMock;

    private ViolationSink violationSinkMock;

    private PolicyTemplatesProvider policyTemplatesProviderMock;

    private CloudTrailEvent event;

    private UnapprovedServicesAndRolePlugin plugin;

    @Before
    public void setUp() throws Exception {
        policyProviderMock = mock(PolicyProvider.class);
        violationSinkMock = mock(ViolationSink.class);
        policyTemplatesProviderMock = mock(PolicyTemplatesProvider.class);

        event = createCloudTrailEvent("/record.json");

        plugin = new UnapprovedServicesAndRolePlugin(
                policyProviderMock,
                violationSinkMock,
                policyTemplatesProviderMock,
                new UnapprovedServicesAndRoleProperties());

    }

    @After
    public void tearDown() throws Exception {
        verifyNoMoreInteractions(policyProviderMock, violationSinkMock, policyTemplatesProviderMock);
    }

    @Test
    public void testSupports() throws Exception {

        when(policyTemplatesProviderMock.getPolicyTemplateNames()).thenReturn(
                newArrayList(
                        "mint-worker-b17-AppServerRole-W5WX8WewafwO2MEWZ"));

        boolean result = plugin.supports(event);
        Assertions.assertThat(result).isTrue();

        verify(policyTemplatesProviderMock).getPolicyTemplateNames();
    }

    @Test
    public void testProcessEvent() throws Exception {

        when(policyProviderMock.getPolicy(any(), any(), any())).thenReturn("{\"eventVersion\": \"1\"}");
        when(policyTemplatesProviderMock.getPolicyTemplate(any())).thenReturn("{\"eventVersion\": \"2\"}");

        plugin.processEvent(event);

        verify(policyProviderMock).getPolicy(any(), any(), any());
        verify(policyTemplatesProviderMock).getPolicyTemplate(any());
        verify(violationSinkMock).put(any(Violation.class));

    }

    @Test
    public void testProcessEvent2() throws Exception {

        when(policyProviderMock.getPolicy(any(), any(), any())).thenReturn(
                "{\"Version\":\"2012-10-17\",\"Statement\":[{\"Sid\":\"\",\"Effect\":\"Allow\",\"Principal\":{\"Federated\":\"arn:aws:iam::123:xxx-provider/Shibboleth\"},\"Action\":\"sts:AssumeRoleWithxxx\",\"Condition\":{\"StringEquals\":{\"xxx:au\":\"https://signin.aws.amazon.com/xxx\"}}}]}");
        when(policyTemplatesProviderMock.getPolicyTemplate(any())).thenReturn(
                "{\"Statement\":[{\"Sid\":\"\",\"Effect\":\"Allow\",\"Principal\":{\"Federated\":\"arn:aws:iam::123:xxx-provider/Shibboleth\"},\"Action\":\"sts:AssumeRoleWithxxx\",\"Condition\":{\"StringEquals\":{\"xxx:au\":\"https://signin.aws.amazon.com/xxx\"}}}],\"Version\":\"2012-10-17\"}");

        plugin.processEvent(event);

        verify(policyProviderMock).getPolicy(any(), any(), any());
        verify(policyTemplatesProviderMock).getPolicyTemplate(any());
    }
}
