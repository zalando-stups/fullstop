package org.zalando.stups.fullstop.plugin.unapproved.impl;

import com.amazonaws.regions.Region;
import com.amazonaws.services.identitymanagement.AmazonIdentityManagementClient;
import com.amazonaws.services.identitymanagement.model.GetRolePolicyRequest;
import com.amazonaws.services.identitymanagement.model.GetRolePolicyResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.zalando.stups.fullstop.aws.ClientProvider;
import org.zalando.stups.fullstop.plugin.unapproved.PolicyProvider;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Optional;

@Service
public class PolicyProviderImpl implements PolicyProvider {

    private final ClientProvider clientProvider;

    @Autowired
    public PolicyProviderImpl(final ClientProvider clientProvider) {
        this.clientProvider = clientProvider;
    }

    @Override
    public String getPolicy(final String roleName, final Region region, final String accountId) {
        final AmazonIdentityManagementClient iamClient = clientProvider
                .getClient(AmazonIdentityManagementClient.class, accountId, region);

        // assuming that there is an inline policy with the same name as the role itself
        return Optional.of(new GetRolePolicyRequest().withRoleName(roleName).withPolicyName(roleName))
                .map(iamClient::getRolePolicy)
                .map(GetRolePolicyResult::getPolicyDocument)
                .map(PolicyProviderImpl::urlDecode)
                .orElse(null);
    }

    private static String urlDecode(String input) {
        try {
            return URLDecoder.decode(input, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new IllegalStateException(e);
        }
    }

}
