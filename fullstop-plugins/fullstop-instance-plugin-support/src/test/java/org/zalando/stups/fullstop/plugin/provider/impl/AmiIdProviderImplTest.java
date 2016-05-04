package org.zalando.stups.fullstop.plugin.provider.impl;

import com.amazonaws.AmazonClientException;
import com.amazonaws.services.ec2.AmazonEC2Client;
import com.amazonaws.services.ec2.model.DescribeInstancesRequest;
import com.amazonaws.services.ec2.model.DescribeInstancesResult;
import com.amazonaws.services.ec2.model.Instance;
import com.amazonaws.services.ec2.model.Reservation;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.zalando.stups.fullstop.plugin.EC2InstanceContext;
import org.zalando.stups.fullstop.plugin.provider.AmiIdProvider;

import java.util.Optional;

import static com.google.common.collect.Lists.newArrayList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class AmiIdProviderImplTest {

    public static final String INSTANCE_ID = "i-fwer234";
    public static final String IMAGE_ID = "ami-243d";
    private AmiIdProvider amiIdProvider;
    private EC2InstanceContext ec2InstanceContextMock;
    private AmazonEC2Client amazonEC2ClientMock;

    @Before
    public void setUp() {

        amiIdProvider = new AmiIdProviderImpl();

        ec2InstanceContextMock = mock(EC2InstanceContext.class);

        amazonEC2ClientMock = mock(AmazonEC2Client.class);

    }

    @After
    public void tearDown() {
        verifyNoMoreInteractions(ec2InstanceContextMock, amazonEC2ClientMock);
    }

    @Test
    public void testAmiIdFound() throws Exception {

        when(ec2InstanceContextMock.getInstanceJson()).thenReturn("{\"imageId\":\"123\"}");

        final Optional<String> result = amiIdProvider.apply(ec2InstanceContextMock);

        assertThat(result).isPresent();

        verify(ec2InstanceContextMock).getInstanceJson();
    }

    @Test
    public void testAmiIdNotFound() throws Exception {

        when(ec2InstanceContextMock.getInstanceJson()).thenReturn("{json here");
        when(ec2InstanceContextMock.getInstanceId()).thenReturn(INSTANCE_ID);
        when(ec2InstanceContextMock.getClient(eq(AmazonEC2Client.class))).thenReturn(amazonEC2ClientMock);

        final DescribeInstancesRequest describeInstancesRequest = new DescribeInstancesRequest().withInstanceIds(INSTANCE_ID);
        when(amazonEC2ClientMock.describeInstances(eq(describeInstancesRequest)))
                .thenReturn(new DescribeInstancesResult()
                        .withReservations(newArrayList(
                                new Reservation().withInstances(newArrayList(
                                        new Instance()
                                                .withInstanceId(INSTANCE_ID)
                                                .withImageId(IMAGE_ID)
                                ))
                        )));

        final Optional<String> result = amiIdProvider.apply(ec2InstanceContextMock);

        assertThat(result).isPresent();

        verify(ec2InstanceContextMock).getInstanceJson();
        verify(ec2InstanceContextMock).getInstanceId();
        verify(ec2InstanceContextMock).getClient(eq(AmazonEC2Client.class));
        verify(amazonEC2ClientMock).describeInstances(eq(describeInstancesRequest));
    }

    @Test
    public void testAmiIdNotFoundInAWS() throws Exception {

        when(ec2InstanceContextMock.getInstanceJson()).thenReturn("{json here");
        when(ec2InstanceContextMock.getInstanceId()).thenReturn(INSTANCE_ID);
        when(ec2InstanceContextMock.getClient(eq(AmazonEC2Client.class))).thenReturn(amazonEC2ClientMock);

        final DescribeInstancesRequest describeInstancesRequest = new DescribeInstancesRequest().withInstanceIds(INSTANCE_ID);
        when(amazonEC2ClientMock.describeInstances(eq(describeInstancesRequest)))
                .thenReturn(new DescribeInstancesResult()
                        .withReservations(newArrayList(
                                new Reservation().withInstances(newArrayList(
                                        new Instance()
                                                .withInstanceId("another id")
                                                .withImageId(IMAGE_ID)
                                ))
                        )));

        final Optional<String> result = amiIdProvider.apply(ec2InstanceContextMock);

        assertThat(result).isEmpty();

        verify(ec2InstanceContextMock).getInstanceJson();
        verify(ec2InstanceContextMock).getInstanceId();
        verify(ec2InstanceContextMock).getClient(eq(AmazonEC2Client.class));
        verify(amazonEC2ClientMock).describeInstances(eq(describeInstancesRequest));
    }

    @Test
    public void testAmiIdAWSException() throws Exception {

        when(ec2InstanceContextMock.getInstanceJson()).thenReturn("{json here");
        when(ec2InstanceContextMock.getInstanceId()).thenReturn(INSTANCE_ID);
        when(ec2InstanceContextMock.getClient(eq(AmazonEC2Client.class)))
                .thenThrow(new AmazonClientException("oops, I did it again... Britney"));


        final Optional<String> result = amiIdProvider.apply(ec2InstanceContextMock);

        assertThat(result).isEmpty();

        verify(ec2InstanceContextMock).getInstanceJson();
        verify(ec2InstanceContextMock).getInstanceId();
        verify(ec2InstanceContextMock).getClient(eq(AmazonEC2Client.class));
    }

}