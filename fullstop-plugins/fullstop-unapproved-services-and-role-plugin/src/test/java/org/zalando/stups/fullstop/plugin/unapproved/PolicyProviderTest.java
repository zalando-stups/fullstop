package org.zalando.stups.fullstop.plugin.unapproved;

import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.identitymanagement.AmazonIdentityManagementClient;
import com.amazonaws.services.identitymanagement.model.GetRolePolicyResult;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.zalando.stups.fullstop.aws.ClientProvider;
import org.zalando.stups.fullstop.plugin.unapproved.impl.PolicyProviderImpl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

/**
 * Created by mrandi.
 */
public class PolicyProviderTest {

    private ClientProvider clientProviderMock;
    private AmazonIdentityManagementClient clientMock;

    private PolicyProvider policyProvider;

    @Before
    public void setUp() throws Exception {
        clientProviderMock = mock(ClientProvider.class);
        clientMock = mock(AmazonIdentityManagementClient.class);

        policyProvider = new PolicyProviderImpl(clientProviderMock);
    }

    @After
    public void tearDown() throws Exception {
        verifyNoMoreInteractions(clientProviderMock, clientMock);
    }

    @Test
    public void testGetPolicy() throws Exception {
        when(clientProviderMock.getClient(any(), any(), any())).thenReturn(clientMock);
        when(clientMock.getRolePolicy(any())).thenReturn(
                new GetRolePolicyResult().withPolicyDocument("%7B%22foo%22%3A%22bar%22%7D"));

        assertThat(policyProvider.getPolicy("test", Region.getRegion(Regions.AP_SOUTHEAST_2), "test")).isEqualTo("{\"foo\":\"bar\"}");

        verify(clientProviderMock).getClient(any(), any(), any());
        verify(clientMock).getRolePolicy(any());
    }
}
