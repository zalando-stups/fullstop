package org.zalando.stups.fullstop.jobs.rds;

import com.amazonaws.regions.Region;
import com.amazonaws.services.rds.AmazonRDSClient;
import com.amazonaws.services.rds.model.DBInstance;
import com.amazonaws.services.rds.model.DescribeDBInstancesRequest;
import com.amazonaws.services.rds.model.DescribeDBInstancesResult;
import com.amazonaws.services.rds.model.Endpoint;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.zalando.stups.fullstop.aws.ClientProvider;
import org.zalando.stups.fullstop.jobs.common.AccountIdSupplier;
import org.zalando.stups.fullstop.jobs.config.JobsProperties;
import org.zalando.stups.fullstop.violation.Violation;
import org.zalando.stups.fullstop.violation.ViolationSink;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Sets.newHashSet;
import static org.mockito.Mockito.*;

public class FetchRdsJobTest {

    private ClientProvider clientProviderMock;
    private JobsProperties jobsPropertiesMock;
    private ViolationSink violationSinkMock;
    private AmazonRDSClient amazonRDSClientMock;
    private DescribeDBInstancesResult describeDBInstancesResultMock;
    private AccountIdSupplier accountIdSupplierMock;

    @Before
    public void setUp() throws Exception {
        this.clientProviderMock = mock(ClientProvider.class);
        this.jobsPropertiesMock = mock(JobsProperties.class);
        this.violationSinkMock = mock(ViolationSink.class);
        this.amazonRDSClientMock = mock(AmazonRDSClient.class);
        this.accountIdSupplierMock = mock(AccountIdSupplier.class);

        when(accountIdSupplierMock.get()).thenReturn(newHashSet("54321"));

        // Jobsproperties
        when(jobsPropertiesMock.getWhitelistedRegions()).thenReturn(newArrayList("eu-west-1"));

        // Dbinstances
        final Endpoint endpoint = new Endpoint();
        endpoint.setAddress("aws.db.cn");
        final DBInstance dbInstance1 = new DBInstance();
        dbInstance1.setPubliclyAccessible(true);
        dbInstance1.setEndpoint(endpoint);
        final DBInstance dbInstance2 = new DBInstance();
        dbInstance2.setPubliclyAccessible(false);
        dbInstance2.setEndpoint(endpoint);
        describeDBInstancesResultMock = new DescribeDBInstancesResult();
        describeDBInstancesResultMock.setDBInstances(newArrayList(dbInstance1, dbInstance2));

        // clientprovider
        when(clientProviderMock.getClient(any(), any(String.class), any(Region.class))).thenReturn(amazonRDSClientMock);

    }

    @After
    public void tearDown() throws Exception {
        verifyNoMoreInteractions(accountIdSupplierMock, clientProviderMock, jobsPropertiesMock, violationSinkMock, amazonRDSClientMock);
    }

    @Test
    public void testCheck() throws Exception {
        final FetchRdsJob fetchRdsJob = new FetchRdsJob(accountIdSupplierMock, clientProviderMock, jobsPropertiesMock, violationSinkMock);
        when(amazonRDSClientMock.describeDBInstances(any(DescribeDBInstancesRequest.class))).thenReturn(describeDBInstancesResultMock);
        fetchRdsJob.run();

        verify(violationSinkMock, times(1)).put(any(Violation.class));
        verify(accountIdSupplierMock, times(1)).get();
        verify(amazonRDSClientMock, times(1)).describeDBInstances(any(DescribeDBInstancesRequest.class));
        verify(jobsPropertiesMock, times(1)).getWhitelistedRegions();
        verify(clientProviderMock, times(1)).getClient(any(), any(String.class), any(Region.class));
    }
}
