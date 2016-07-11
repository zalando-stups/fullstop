package org.zalando.stups.fullstop.plugin.unapproved;

import com.amazonaws.regions.Region;
import com.amazonaws.services.identitymanagement.AmazonIdentityManagementClient;
import com.amazonaws.services.identitymanagement.model.AttachedPolicy;
import com.amazonaws.services.identitymanagement.model.GetRolePolicyResult;
import com.amazonaws.services.identitymanagement.model.ListAttachedRolePoliciesResult;
import com.amazonaws.services.identitymanagement.model.ListRolePoliciesResult;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.zalando.stups.fullstop.aws.ClientProvider;
import org.zalando.stups.fullstop.plugin.unapproved.impl.PolicyProviderImpl;

import static com.amazonaws.regions.Regions.US_EAST_1;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

public class PolicyProviderTest {

    private AmazonIdentityManagementClient clientMock;

    private PolicyProvider policyProvider;

    @Before
    public void setUp() throws Exception {
        clientMock = mock(AmazonIdentityManagementClient.class);

        final ClientProvider clientProviderMock = mock(ClientProvider.class);
        when(clientProviderMock.getClient(any(), any(), any())).thenReturn(clientMock);

        policyProvider = new PolicyProviderImpl(clientProviderMock);

    }

    @After
    public void tearDown() throws Exception {
        verifyNoMoreInteractions(clientMock);
    }

    @Test
    public void testGetRolePolicies() throws Exception {
        when(clientMock.listAttachedRolePolicies(any()))
                .thenReturn(new ListAttachedRolePoliciesResult().withAttachedPolicies(
                        new AttachedPolicy().withPolicyName("bar1"),
                        new AttachedPolicy().withPolicyName("bar2")));
        when(clientMock.listRolePolicies(any()))
                .thenReturn(new ListRolePoliciesResult().withPolicyNames("foo", "bar"));
        when(clientMock.getRolePolicy(any()))
                .thenReturn(new GetRolePolicyResult().withPolicyDocument("%7B%22hello%22%3A%22world%22%7D"));

        final RolePolicies rolePolicies = policyProvider.getRolePolicies("foo", Region.getRegion(US_EAST_1), "123456789012");
        assertThat(rolePolicies).isNotNull();
        assertThat(rolePolicies.getAttachedPolicyNames()).containsOnly("bar1", "bar2");
        assertThat(rolePolicies.getInlinePolicyNames()).containsOnly("foo", "bar");
        assertThat(rolePolicies.getMainPolicy()).isEqualTo("{\"hello\":\"world\"}");

        verify(clientMock).listAttachedRolePolicies(any());
        verify(clientMock).listRolePolicies(any());
        verify(clientMock).getRolePolicy(any());
    }

    @Test
    public void testGetRolePoliciesWhenMainPolicyIsMissing() throws Exception {
        when(clientMock.listAttachedRolePolicies(any())).thenReturn(new ListAttachedRolePoliciesResult());
        when(clientMock.listRolePolicies(any())).thenReturn(new ListRolePoliciesResult());

        final RolePolicies rolePolicies = policyProvider.getRolePolicies("foo", Region.getRegion(US_EAST_1), "123456789012");
        assertThat(rolePolicies).isNotNull();
        assertThat(rolePolicies.getAttachedPolicyNames()).isEmpty();
        assertThat(rolePolicies.getInlinePolicyNames()).isEmpty();

        verify(clientMock).listAttachedRolePolicies(any());
        verify(clientMock).listRolePolicies(any());
    }
}
