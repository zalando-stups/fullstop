package org.zalando.stups.fullstop.jobs.elb;

import com.amazonaws.regions.Region;
import com.amazonaws.services.elasticloadbalancing.AmazonElasticLoadBalancingClient;
import com.amazonaws.services.elasticloadbalancing.model.DescribeLoadBalancersRequest;
import com.amazonaws.services.elasticloadbalancing.model.DescribeLoadBalancersResult;
import com.amazonaws.services.elasticloadbalancing.model.Instance;
import com.amazonaws.services.elasticloadbalancing.model.Listener;
import com.amazonaws.services.elasticloadbalancing.model.ListenerDescription;
import com.amazonaws.services.elasticloadbalancing.model.LoadBalancerDescription;
import com.google.common.collect.ImmutableMap;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.zalando.stups.fullstop.aws.ClientProvider;
import org.zalando.stups.fullstop.jobs.common.AccountIdSupplier;
import org.zalando.stups.fullstop.jobs.common.AmiDetailsProvider;
import org.zalando.stups.fullstop.jobs.common.AwsApplications;
import org.zalando.stups.fullstop.jobs.common.EC2InstanceProvider;
import org.zalando.stups.fullstop.jobs.common.FetchTaupageYaml;
import org.zalando.stups.fullstop.jobs.common.PortsChecker;
import org.zalando.stups.fullstop.jobs.common.SecurityGroupsChecker;
import org.zalando.stups.fullstop.jobs.config.JobsProperties;
import org.zalando.stups.fullstop.violation.ViolationSink;
import org.zalando.stups.fullstop.violation.service.ViolationService;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static com.amazonaws.regions.Region.getRegion;
import static com.amazonaws.regions.Regions.fromName;
import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Sets.newHashSet;
import static java.util.Arrays.asList;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.anyListOf;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

public class FetchElasticLoadBalancersJobTest {

    public static final String ACCOUNT_ID = "1";
    public static final String REGION1 = "eu-west-1";
    private ViolationSink violationSinkMock;

    private ClientProvider clientProviderMock;

    private AccountIdSupplier accountIdSupplierMock;

    private JobsProperties jobsPropertiesMock;

    private AmazonElasticLoadBalancingClient mockAwsELBClient;

    private DescribeLoadBalancersResult mockDescribeELBResult;

    private PortsChecker portsChecker;

    private SecurityGroupsChecker securityGroupsChecker;

    private final List<String> regions = newArrayList();

    private AwsApplications mockAwsApplications;

    private ViolationService mockViolationService;

    private FetchTaupageYaml fetchTaupageYamlMock;

    private AmiDetailsProvider mockAmiDetailsProvider;

    private EC2InstanceProvider mockEC2InstanceProvider;

    @Before
    public void setUp() throws Exception {
        this.violationSinkMock = mock(ViolationSink.class);
        this.clientProviderMock = mock(ClientProvider.class);
        this.accountIdSupplierMock = mock(AccountIdSupplier.class);
        this.jobsPropertiesMock = mock(JobsProperties.class);
        this.portsChecker = mock(PortsChecker.class);
        this.securityGroupsChecker = mock(SecurityGroupsChecker.class);
        this.mockAwsELBClient = mock(AmazonElasticLoadBalancingClient.class);
        this.mockAwsApplications = mock(AwsApplications.class);
        this.mockViolationService = mock(ViolationService.class);
        this.fetchTaupageYamlMock = mock(FetchTaupageYaml.class);
        this.mockAmiDetailsProvider = mock(AmiDetailsProvider.class);
        this.mockEC2InstanceProvider = mock(EC2InstanceProvider.class);

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

        regions.add(REGION1);

        when(clientProviderMock.getClient(any(), any(String.class), any(Region.class))).thenReturn(mockAwsELBClient);

        when(mockEC2InstanceProvider.getById(anyString(), any(Region.class), anyString()))
                .thenReturn(Optional.of(new com.amazonaws.services.ec2.model.Instance().withInstanceId("foo").withImageId("bar")));
        when(mockAmiDetailsProvider.getAmiDetails(anyString(), any(Region.class), anyString()))
                .thenReturn(ImmutableMap.of("ami_id", "bar"));
    }
    @Test
    public void testCheck() throws Exception {
        when(accountIdSupplierMock.get()).thenReturn(newHashSet(ACCOUNT_ID));
        when(jobsPropertiesMock.getWhitelistedRegions()).thenReturn(regions);
        when(portsChecker.check(any(LoadBalancerDescription.class))).thenReturn(Collections.<Integer>emptyList());
        when(securityGroupsChecker.check(any(), any(), any())).thenReturn(Collections.<String>emptySet());
        when(mockAwsELBClient.describeLoadBalancers(any(DescribeLoadBalancersRequest.class))).thenReturn(mockDescribeELBResult);
        when(mockAwsApplications.isPubliclyAccessible(anyString(), anyString(), anyListOf(String.class)))
                .thenReturn(Optional.of(false));

        final FetchElasticLoadBalancersJob fetchELBJob = new FetchElasticLoadBalancersJob(
                violationSinkMock,
                clientProviderMock,
                accountIdSupplierMock,
                jobsPropertiesMock,
                securityGroupsChecker,
                portsChecker,
                mockAwsApplications,
                mockViolationService,
                fetchTaupageYamlMock,
                mockAmiDetailsProvider,
                mockEC2InstanceProvider);

        fetchELBJob.run();

        verify(accountIdSupplierMock).get();
        verify(jobsPropertiesMock, atLeast(1)).getWhitelistedRegions();
        verify(jobsPropertiesMock).getElbAllowedPorts();
        verify(securityGroupsChecker, atLeast(1)).check(any(), any(), any());
        verify(portsChecker, atLeast(1)).check(any());
        verify(mockAwsELBClient).describeLoadBalancers(any(DescribeLoadBalancersRequest.class));
        verify(clientProviderMock).getClient(any(), any(String.class), any(Region.class));
        verify(mockAwsApplications).isPubliclyAccessible(eq(ACCOUNT_ID), eq(REGION1), eq(asList("i1", "i2")));
        verify(mockEC2InstanceProvider).getById(eq(ACCOUNT_ID), eq(getRegion(fromName(REGION1))), eq("i1"));
        verify(mockAmiDetailsProvider).getAmiDetails(eq(ACCOUNT_ID), eq(getRegion(fromName(REGION1))), eq("bar"));
    }

    @After
    public void tearDown() throws Exception {
        verifyNoMoreInteractions(violationSinkMock,
                clientProviderMock,
                accountIdSupplierMock,
                jobsPropertiesMock,
                securityGroupsChecker,
                portsChecker,
                mockAwsApplications);
    }
}
