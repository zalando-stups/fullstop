package org.zalando.stups.fullstop.plugin.unapproved;

import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.identitymanagement.AmazonIdentityManagementClient;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.zalando.stups.fullstop.aws.ClientProvider;
import org.zalando.stups.fullstop.plugin.unapproved.impl.PolicyProviderImpl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

/**
 * Created by mrandi.
 */
public class PolicyProviderTest {

    private ClientProvider clientProviderMock;

    private PolicyProvider policyProvider;

    @Before
    public void setUp() throws Exception {
        clientProviderMock = mock(ClientProvider.class);
        policyProvider = new PolicyProviderImpl(clientProviderMock);
    }

    @After
    public void tearDown() throws Exception {
        verifyNoMoreInteractions(clientProviderMock);
    }

    @Test
    public void testGetPolicy() throws Exception {

        when(clientProviderMock.getClient(any(), any(), any())).thenReturn(new AmazonIdentityManagementClient());

        policyProvider.getPolicy("test", Region.getRegion(Regions.AP_SOUTHEAST_2), "test");

        verify(clientProviderMock).getClient(any(), any(), any());
    }

    @Test
    public void testGetPolicyException() throws Exception {

        when(clientProviderMock.getClient(any(), any(), any())).thenReturn(null);

        try {
            policyProvider.getPolicy("test", Region.getRegion(Regions.AP_SOUTHEAST_2), "test");
        }
        catch (final RuntimeException e) {
            assertThat(
                    e.getMessage()
                     .startsWith("Somehow we could not create an AmazonIdentityManagementClient with accountId:"));
        }

        verify(clientProviderMock).getClient(any(), any(), any());
    }
}