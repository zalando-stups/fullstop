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
package org.zalando.stups.fullstop.jobs;

import com.amazonaws.regions.Region;
import com.amazonaws.services.elasticloadbalancing.AmazonElasticLoadBalancingClient;
import com.amazonaws.services.elasticloadbalancing.model.*;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.zalando.stups.fullstop.aws.ClientProvider;
import org.zalando.stups.fullstop.jobs.common.AwsApplications;
import org.zalando.stups.fullstop.jobs.common.PortsChecker;
import org.zalando.stups.fullstop.jobs.common.SecurityGroupsChecker;
import org.zalando.stups.fullstop.jobs.config.JobsProperties;
import org.zalando.stups.fullstop.jobs.elb.FetchElasticLoadBalancersJob;
import org.zalando.stups.fullstop.teams.Account;
import org.zalando.stups.fullstop.teams.TeamOperations;
import org.zalando.stups.fullstop.violation.Violation;
import org.zalando.stups.fullstop.violation.ViolationSink;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Sets.newHashSet;
import static java.util.Arrays.asList;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

public class FetchElasticLoadBalancersJobTest {

    public static final String ACCOUNT_ID = "1";
    public static final String REGION1 = "eu-west-1";
    private ViolationSink violationSinkMock;

    private ClientProvider clientProviderMock;

    private TeamOperations teamOperationsMock;

    private JobsProperties jobsPropertiesMock;

    private AmazonElasticLoadBalancingClient mockAwsELBClient;

    private DescribeLoadBalancersResult mockDescribeELBResult;

    private PortsChecker portsChecker;

    private SecurityGroupsChecker securityGroupsChecker;

    private List<Account> accounts = newArrayList();

    private List<String> regions = newArrayList();

    private AwsApplications mockAwsApplications;

    @Before
    public void setUp() throws Exception {
        this.violationSinkMock = mock(ViolationSink.class);
        this.clientProviderMock = mock(ClientProvider.class);
        this.teamOperationsMock = mock(TeamOperations.class);
        this.jobsPropertiesMock = mock(JobsProperties.class);
        this.portsChecker = mock(PortsChecker.class);
        this.securityGroupsChecker = mock(SecurityGroupsChecker.class);
        this.mockAwsELBClient = mock(AmazonElasticLoadBalancingClient.class);
        mockAwsApplications = mock(AwsApplications.class);

        final Listener listener = new Listener("HTTPS", 80, 80);

        final ListenerDescription listenerDescription = new ListenerDescription();
        listenerDescription.setListener(listener);

        final LoadBalancerDescription publicELB = new LoadBalancerDescription();
        publicELB.setScheme("internet-facing");
        publicELB.setListenerDescriptions(newArrayList(listenerDescription));
        publicELB.setCanonicalHostedZoneName("test.com");
        publicELB.setInstances(asList(new Instance("i1"), new Instance("i2")));

        final LoadBalancerDescription privateELB = new LoadBalancerDescription();
        privateELB.setScheme("internal");
        privateELB.setCanonicalHostedZoneName("internal.org");

        mockDescribeELBResult = new DescribeLoadBalancersResult();
        mockDescribeELBResult.setLoadBalancerDescriptions(newArrayList(publicELB, privateELB));

        final Account account = new Account(ACCOUNT_ID, "testaccount", "test", "awesome");
        accounts.add(account);
        regions.add(REGION1);

        when(clientProviderMock.getClient(any(), any(String.class), any(Region.class))).thenReturn(mockAwsELBClient);
    }
    @Test
    public void testCheck() throws Exception {
        when(teamOperationsMock.getAccounts()).thenReturn(accounts);
        when(jobsPropertiesMock.getWhitelistedRegions()).thenReturn(regions);
        when(portsChecker.check(any(LoadBalancerDescription.class))).thenReturn(Collections.<Integer>emptyList());
        when(securityGroupsChecker.check(any(), any(), any())).thenReturn(Collections.<String>emptySet());
        when(mockAwsELBClient.describeLoadBalancers(any(DescribeLoadBalancersRequest.class))).thenReturn(mockDescribeELBResult);
        when(mockAwsApplications.isPubliclyAccessible(anyString(), anyString(), anyListOf(String.class)))
                .thenReturn(Optional.of(false));

        final FetchElasticLoadBalancersJob fetchELBJob = new FetchElasticLoadBalancersJob(
                violationSinkMock,
                clientProviderMock,
                teamOperationsMock,
                jobsPropertiesMock,
                securityGroupsChecker,
                portsChecker,
                mockAwsApplications);

        fetchELBJob.check();

        verify(teamOperationsMock,atLeast(1)).getAccounts();
        verify(jobsPropertiesMock, atLeast(1)).getWhitelistedRegions();
        verify(securityGroupsChecker, atLeast(1)).check(any(), any(), any());
        verify(portsChecker, atLeast(1)).check(any());
        verify(mockAwsELBClient).describeLoadBalancers(any(DescribeLoadBalancersRequest.class));
        verify(clientProviderMock).getClient(any(), any(String.class), any(Region.class));
        verify(mockAwsApplications).isPubliclyAccessible(eq(ACCOUNT_ID), eq(REGION1), eq(asList("i1", "i2")));
    }

    @After
    public void tearDown() throws Exception {
        verifyNoMoreInteractions(violationSinkMock,
                clientProviderMock,
                teamOperationsMock,
                jobsPropertiesMock,
                securityGroupsChecker,
                portsChecker,
                mockAwsApplications);
    }
}
