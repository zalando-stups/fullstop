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

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.mockito.Mockito.*;

import org.zalando.stups.clients.kio.Approval;
import org.zalando.stups.clients.kio.KioOperations;
import org.zalando.stups.clients.kio.Version;
import org.zalando.stups.fullstop.clients.pierone.PieroneOperations;
import org.zalando.stups.fullstop.events.Records;
import org.zalando.stups.fullstop.events.TestCloudTrailEventData;
import org.zalando.stups.fullstop.events.UserDataProvider;
import org.zalando.stups.fullstop.plugin.config.RegistryPluginProperties;
import org.zalando.stups.fullstop.violation.Violation;
import org.zalando.stups.fullstop.violation.ViolationSink;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class RegistryPluginApprovalsTest {

    private static final String APPLICATION_ID = "fullstop";

    private static final String APPLICATION_VERSION = "1.0";

    private KioOperations kioOperations;

    private PieroneOperations pieroneOperations;

    private CloudTrailEvent event;

    private ViolationSink violationSink;

    private UserDataProvider userDataProvider;

    private RegistryPlugin registryPlugin;

    private RegistryPluginProperties pluginConfiguration;

    private Version version;

    protected CloudTrailEvent buildEvent() {
        List<Map<String, Object>> records = Records.fromClasspath("/record.json");

        CloudTrailEvent event = TestCloudTrailEventData.createCloudTrailEventFromMap(records.get(0));
        return event;
    }

    protected List<Approval> buildDefaultApprovals() {
        return pluginConfiguration.getDefaultApprovals()
                                  .stream()
                                  .map(type -> {
                                      Approval approval = new Approval();
                                      approval.setApprovalType(type);
                                      approval.setUserId(Math.random() * 100 + "");
                                      approval.setApprovedAt(new Date());
                                      return approval;
                                  })
                                  .collect(Collectors.toList());
    }

    protected Approval buildTestApproval(String user) {
        Approval approval = new Approval();
        approval.setUserId(user != null ? user : "npiccolotto");
        approval.setApprovedAt(new Date());
        approval.setApprovalType(pluginConfiguration.getTestApproval());
        return approval;
    }

    protected Approval buildCodeApproval(String user) {
        Approval approval = new Approval();
        approval.setUserId(user != null ? user : "npiccolotto");
        approval.setApprovedAt(new Date());
        approval.setApprovalType(pluginConfiguration.getCodeApproval());
        return approval;
    }

    protected Approval buildDeployApproval(String user) {
        Approval approval = new Approval();
        approval.setUserId(user != null ? user : "npiccolotto");
        approval.setApprovedAt(new Date());
        approval.setApprovalType(pluginConfiguration.getDeployApproval());
        return approval;
    }

    @Before
    public void setUp() {
        userDataProvider = mock(UserDataProvider.class);
        kioOperations = mock(KioOperations.class);
        pieroneOperations = mock(PieroneOperations.class);
        violationSink = mock(ViolationSink.class);
        pluginConfiguration = new RegistryPluginProperties();
        registryPlugin = new RegistryPlugin(userDataProvider,
                                            violationSink,
                                            pieroneOperations,
                                            kioOperations,
                                            pluginConfiguration);
        // test version
        version = new Version();
        version.setApplicationId(APPLICATION_ID);
        version.setId(APPLICATION_VERSION);
    }

    @After
    public void tearDown() {
        verifyNoMoreInteractions(userDataProvider,
                                 kioOperations,
                                 pieroneOperations,
                                 violationSink);
    }

    @Test
    public void shouldComplainWithoutAnyApprovals() {
        event = buildEvent();

        when(kioOperations.getApplicationApprovals(APPLICATION_ID,
                                                   APPLICATION_VERSION)).thenReturn(new LinkedList<Approval>());
        registryPlugin.validateContainsDefaultApprovals(version,
                                                        event);

        verify(kioOperations).getApplicationApprovals(APPLICATION_ID,
                                                      APPLICATION_VERSION);
        verify(violationSink).put(any(Violation.class));
    }

    @Test
    public void shouldComplainAboutMissingDefaultApprovals() {
        // build test event
        event = buildEvent();

        // build test approvals
        List<Approval> approvals = new LinkedList<Approval>();
        approvals.add(buildTestApproval(null));
        approvals.add(buildCodeApproval(null));
        // mock kio operations
        when(kioOperations.getApplicationApprovals(APPLICATION_ID,
                                                   APPLICATION_VERSION)).thenReturn(approvals);

        // run validation
        registryPlugin.validateContainsDefaultApprovals(version,
                                                        event);
        // ensure kio operations was called
        verify(kioOperations).getApplicationApprovals(APPLICATION_ID,
                                                      APPLICATION_VERSION);
        // ensure a violation was created
        verify(violationSink).put(any(Violation.class));
    }

    @Test
    public void shouldNotComplainWithDefaultApprovals() {
        // build test event
        event = buildEvent();

        // build test approvals
        List<Approval> approvals = buildDefaultApprovals();
        // mock kio operations
        when(kioOperations.getApplicationApprovals(APPLICATION_ID,
                                                   APPLICATION_VERSION)).thenReturn(approvals);

        // run validation
        registryPlugin.validateContainsDefaultApprovals(version,
                                                        event);
        // ensure kio operations was called
        verify(kioOperations).getApplicationApprovals(APPLICATION_ID,
                                                      APPLICATION_VERSION);
        // ensure a violation was created
        verify(violationSink,
               never()).put(any(Violation.class));
    }

    @Test
    public void shouldNotComplainWithFourEyesPrinciple() {
        // build test event
        event = buildEvent();

        // build test approvals
        List<Approval> approvals = buildDefaultApprovals();
        // mock kio operations
        when(kioOperations.getApplicationApprovals(APPLICATION_ID,
                                                   APPLICATION_VERSION)).thenReturn(approvals);

        // run validation
        registryPlugin.validateFourEyesPrinciple(version,
                                                 event);
        // ensure kio operations was called
        verify(kioOperations).getApplicationApprovals(APPLICATION_ID,
                                                      APPLICATION_VERSION);
        // ensure a violation was created
        verify(violationSink,
               never()).put(any(Violation.class));
    }

    @Test
    public void shouldComplainAboutViolatedFourEyesPrinciple() {
        // build test event
        event = buildEvent();

        // build test approvals
        List<Approval> approvals = new LinkedList<Approval>();
        approvals.add(buildTestApproval(null));
        approvals.add(buildCodeApproval(null));
        // mock kio operations
        when(kioOperations.getApplicationApprovals(APPLICATION_ID,
                                                   APPLICATION_VERSION)).thenReturn(approvals);

        // run validation
        registryPlugin.validateFourEyesPrinciple(version,
                                                 event);
        // ensure kio operations was called
        verify(kioOperations).getApplicationApprovals(APPLICATION_ID,
                                                      APPLICATION_VERSION);
        // ensure a violation was created
        verify(violationSink).put(any(Violation.class));
    }

}
