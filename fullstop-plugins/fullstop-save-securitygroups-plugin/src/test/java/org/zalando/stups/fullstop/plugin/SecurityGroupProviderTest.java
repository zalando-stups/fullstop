package org.zalando.stups.fullstop.plugin;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.ec2.AmazonEC2Client;
import com.amazonaws.services.ec2.model.DescribeSecurityGroupsRequest;
import com.amazonaws.services.ec2.model.DescribeSecurityGroupsResult;
import com.google.common.collect.Lists;
import org.assertj.core.api.Assertions;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.zalando.stups.fullstop.aws.ClientProvider;

import static org.mockito.Mockito.*;

public class SecurityGroupProviderTest {

    private ClientProvider clientProviderMock;
    private SecurityGroupProvider securityGroupProvider;
    private AmazonEC2Client amazonEC2ClientMock;
    private static Region REGION = Region.getRegion(Regions.EU_WEST_1);

    @Before
    public void setUp() throws Exception {
        clientProviderMock = mock(ClientProvider.class);
        amazonEC2ClientMock = mock(AmazonEC2Client.class);
    }

    @After
    public void tearDown() throws Exception {
        verifyNoMoreInteractions(clientProviderMock, amazonEC2ClientMock);
    }

    @Test(expected = RuntimeException.class)
    public void testNullAmazonClient() {
        when(clientProviderMock.getClient(any(), anyString(), any(Region.class))).thenReturn(null);
        securityGroupProvider = new SecurityGroupProvider(clientProviderMock);
        try {
            securityGroupProvider.getSecurityGroup(Lists.newArrayList("sg.1234"), REGION, "9876");
        } finally {
            verify(clientProviderMock).getClient(any(), anyString(), any(Region.class));
        }


    }

    @Test
    public void testAmazonException(){
        AmazonServiceException amazonServiceException = new AmazonServiceException("");
        amazonServiceException.setErrorCode("InvalidGroup.NotFound");

        when(clientProviderMock.getClient(any(), anyString(), any(Region.class))).thenReturn(amazonEC2ClientMock);
        when(amazonEC2ClientMock.describeSecurityGroups(any(DescribeSecurityGroupsRequest.class))).thenThrow(amazonServiceException);

        securityGroupProvider = new SecurityGroupProvider(clientProviderMock);
        String securityGroup = securityGroupProvider.getSecurityGroup(Lists.newArrayList("sg.1234"), REGION, "9876");

        Assertions.assertThat(securityGroup).isEqualTo(null);

        verify(clientProviderMock).getClient(any(), anyString(), any(Region.class));
        verify(amazonEC2ClientMock).describeSecurityGroups(any(DescribeSecurityGroupsRequest.class));
    }


    @Test
    public void testJsonException(){
        DescribeSecurityGroupsResult mockResult = spy(new DescribeSecurityGroupsResult());

        when(clientProviderMock.getClient(any(), anyString(), any(Region.class))).thenReturn(amazonEC2ClientMock);
        when(mockResult.getSecurityGroups()).thenThrow(new IllegalStateException());
        when(amazonEC2ClientMock.describeSecurityGroups(any(DescribeSecurityGroupsRequest.class))).thenReturn(mockResult);

        securityGroupProvider = new SecurityGroupProvider(clientProviderMock);
        String securityGroup = securityGroupProvider.getSecurityGroup(Lists.newArrayList("sg.1234"), REGION, "9876");

        Assertions.assertThat(securityGroup).isEqualTo(null);

        verify(clientProviderMock).getClient(any(), anyString(), any(Region.class));
        verify(amazonEC2ClientMock).describeSecurityGroups(any(DescribeSecurityGroupsRequest.class));
    }
}