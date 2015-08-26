/**
 * Copyright (C) 2015 Zalando SE (http://tech.zalando.com)
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
import org.zalando.stups.fullstop.jobs.config.JobsProperties;
import org.zalando.stups.fullstop.jobs.elb.FetchElasticLoadBalancersJob;
import org.zalando.stups.fullstop.teams.Account;
import org.zalando.stups.fullstop.teams.TeamOperations;
import org.zalando.stups.fullstop.violation.ViolationSink;

import java.util.List;

import static com.google.common.collect.Lists.newArrayList;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

/**
 * Created by gkneitschel.
 */

public class FetchElasticLoadBalancersJobTest {

    private ViolationSink violationSinkMock;

    private ClientProvider clientProviderMock;

    private TeamOperations teamOperationsMock;

    private JobsProperties jobsPropertiesMock;

    private AmazonElasticLoadBalancingClient amazonElasticLoadBalancingClientMock;

    private DescribeLoadBalancersResult describeLoadBalancerAttributesResultMock;

    private LoadBalancerDescription loadBalancerDescription;

    private ListenerDescription listenerDescription;

    private List<Account> accounts = newArrayList();

    private List<String> regions = newArrayList();

    @Before
    public void setUp() throws Exception {
        this.violationSinkMock = mock(ViolationSink.class);
        this.clientProviderMock = mock(ClientProvider.class);
        this.teamOperationsMock = mock(TeamOperations.class);
        this.jobsPropertiesMock = mock(JobsProperties.class);
        this.amazonElasticLoadBalancingClientMock = mock(AmazonElasticLoadBalancingClient.class);

        Listener listener = new Listener("HTTPS", 80, 80);
        listenerDescription.setListener(listener);


        loadBalancerDescription.setScheme("internet-facing");
        loadBalancerDescription.setListenerDescriptions(newArrayList(listenerDescription));

        describeLoadBalancerAttributesResultMock.setLoadBalancerDescriptions(newArrayList(loadBalancerDescription));

        Account account = new Account("1", "testaccount", "test", "awesome");
        accounts.add(account);
        regions.add("eu-west-1");

        when(clientProviderMock.getClient(any(), any(String.class), any(Region.class))).thenReturn(amazonElasticLoadBalancingClientMock);
    }
    @Test
    public void testCheck() throws Exception {
        when(teamOperationsMock.getAccounts()).thenReturn(accounts);
        when(jobsPropertiesMock.getWhitelistedRegions()).thenReturn(regions);
        when(amazonElasticLoadBalancingClientMock.describeLoadBalancers(any(DescribeLoadBalancersRequest.class))).thenReturn(
                describeLoadBalancerAttributesResultMock);

        FetchElasticLoadBalancersJob fetchElasticLoadBalancersJob = new FetchElasticLoadBalancersJob(
                violationSinkMock,
                clientProviderMock,
                teamOperationsMock,
                jobsPropertiesMock);

        fetchElasticLoadBalancersJob.check();

        verify(teamOperationsMock,atLeast(1)).getAccounts();
    }

    @After
    public void tearDown() throws Exception {
        verifyNoMoreInteractions();

    }
}