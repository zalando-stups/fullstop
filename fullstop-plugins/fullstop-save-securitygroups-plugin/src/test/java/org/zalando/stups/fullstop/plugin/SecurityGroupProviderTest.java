package org.zalando.stups.fullstop.plugin;

import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.ec2.AmazonEC2Client;
import com.google.common.collect.Lists;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.zalando.stups.fullstop.aws.ClientProvider;

import static org.mockito.Mockito.*;

public class SecurityGroupProviderTest {

    private ClientProvider clientProviderMock;
    private SecurityGroupProvider securityGroupProvider;
    private static Region REGION = Region.getRegion(Regions.EU_WEST_1);

    @Before
    public void setUp() throws Exception {
        amazonEC2ClientMock = mock(AmazonEC2Client.class);
        clientProviderMock = mock(ClientProvider.class);


    }

    @After
    public void tearDown() throws Exception {
        verifyNoMoreInteractions(clientProviderMock);
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
    public void testGetSecurityGroup() throws Exception {

    }
}