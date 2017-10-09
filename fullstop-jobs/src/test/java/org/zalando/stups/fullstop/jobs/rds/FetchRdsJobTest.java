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
import org.mockito.ArgumentCaptor;
import org.zalando.stups.fullstop.aws.ClientProvider;
import org.zalando.stups.fullstop.jobs.common.AccountIdSupplier;
import org.zalando.stups.fullstop.jobs.config.JobsProperties;
import org.zalando.stups.fullstop.jobs.exception.JobExceptionHandler;
import org.zalando.stups.fullstop.violation.Violation;
import org.zalando.stups.fullstop.violation.ViolationSink;

import java.util.Map;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Sets.newHashSet;
import static org.junit.Assert.assertArrayEquals;
import static org.mockito.Mockito.*;

public class FetchRdsJobTest {

    private ClientProvider clientProviderMock;
    private JobsProperties jobsPropertiesMock;
    private ViolationSink violationSinkMock;
    private AmazonRDSClient amazonRDSClientMock;
    private DescribeDBInstancesResult describeDBInstancesResultMock;
    private AccountIdSupplier accountIdSupplierMock;
    private JobExceptionHandler exceptionHandlerMock;

    @Before
    public void setUp() throws Exception {
        this.clientProviderMock = mock(ClientProvider.class);
        this.jobsPropertiesMock = mock(JobsProperties.class);
        this.violationSinkMock = mock(ViolationSink.class);
        this.amazonRDSClientMock = mock(AmazonRDSClient.class);
        this.accountIdSupplierMock = mock(AccountIdSupplier.class);
        this.exceptionHandlerMock = mock(JobExceptionHandler.class);

        when(accountIdSupplierMock.get()).thenReturn(newHashSet("54321"));

        // Jobsproperties
        when(jobsPropertiesMock.getWhitelistedRegions()).thenReturn(newArrayList("eu-west-1"));

        // Dbinstances
        final Endpoint endpoint = new Endpoint();
        endpoint.setAddress("aws.db.cn");
        final Endpoint endpoint2 = new Endpoint();
        endpoint2.setAddress("aws.db2.cn");
        final DBInstance dbInstance1 = new DBInstance();
        dbInstance1.setPubliclyAccessible(true);
        dbInstance1.setEndpoint(endpoint);
        final DBInstance dbInstance2 = new DBInstance();
        dbInstance2.setPubliclyAccessible(false);
        dbInstance2.setEndpoint(endpoint);
        final DBInstance dbInstance3 = new DBInstance();
        dbInstance3.setPubliclyAccessible(true);
        dbInstance3.setEndpoint(endpoint2);
        describeDBInstancesResultMock = new DescribeDBInstancesResult();
        describeDBInstancesResultMock.setDBInstances(newArrayList(dbInstance1, dbInstance2, dbInstance3));

        // clientprovider
        when(clientProviderMock.getClient(any(), any(String.class), any(Region.class))).thenReturn(amazonRDSClientMock);

    }

    @After
    public void tearDown() throws Exception {
        verifyNoMoreInteractions(accountIdSupplierMock, clientProviderMock, jobsPropertiesMock, violationSinkMock, amazonRDSClientMock);
    }

    @Test
    public void testCheck() throws Exception {
        final FetchRdsJob fetchRdsJob = new FetchRdsJob(accountIdSupplierMock, clientProviderMock, jobsPropertiesMock, violationSinkMock, exceptionHandlerMock);
        when(amazonRDSClientMock.describeDBInstances(any(DescribeDBInstancesRequest.class))).thenReturn(describeDBInstancesResultMock);
        fetchRdsJob.run();

        ArgumentCaptor<Violation> violations = ArgumentCaptor.forClass(Violation.class);
        verify(violationSinkMock, times(2)).put(violations.capture());
        verify(accountIdSupplierMock, times(1)).get();
        verify(amazonRDSClientMock, times(1)).describeDBInstances(any(DescribeDBInstancesRequest.class));
        verify(jobsPropertiesMock, times(1)).getWhitelistedRegions();
        verify(clientProviderMock, times(1)).getClient(any(), any(String.class), any(Region.class));

        // Regression test for #479: Make sure that the metadata lists the correct endpoints.
        assertArrayEquals(new String[] {"aws.db.cn", "aws.db2.cn"},
                violations.getAllValues().stream()
                        .map(v -> ((Map<String, Object>) v.getMetaInfo()).get("unsecuredDatabase"))
                        .toArray());
    }
}
