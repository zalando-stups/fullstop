package org.zalando.stups.fullstop.plugin.unapproved.impl;

import com.amazonaws.regions.Region;
import com.amazonaws.services.identitymanagement.AmazonIdentityManagementClient;
import com.amazonaws.services.identitymanagement.model.AttachedPolicy;
import com.amazonaws.services.identitymanagement.model.GetRolePolicyRequest;
import com.amazonaws.services.identitymanagement.model.GetRolePolicyResult;
import com.amazonaws.services.identitymanagement.model.ListAttachedRolePoliciesRequest;
import com.amazonaws.services.identitymanagement.model.ListAttachedRolePoliciesResult;
import com.amazonaws.services.identitymanagement.model.ListRolePoliciesRequest;
import com.amazonaws.services.identitymanagement.model.ListRolePoliciesResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.zalando.stups.fullstop.aws.AwsRequestUtil;
import org.zalando.stups.fullstop.aws.ClientProvider;
import org.zalando.stups.fullstop.plugin.unapproved.PolicyProvider;
import org.zalando.stups.fullstop.plugin.unapproved.RolePolicies;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Collections;
import java.util.Optional;
import java.util.Set;

import static java.util.stream.Collectors.toSet;

@Service
public class PolicyProviderImpl implements PolicyProvider {

    private static final String EMPTY_JSON = "{}";
    private final ClientProvider clientProvider;

    @Autowired
    public PolicyProviderImpl(final ClientProvider clientProvider) {
        this.clientProvider = clientProvider;
    }

    @Override
    public RolePolicies getRolePolicies(String roleName, Region region, String accountId) {
        final AmazonIdentityManagementClient iamClient = clientProvider
                .getClient(AmazonIdentityManagementClient.class, accountId, region);
        final Set<String> attachedPolicyNames = fetchAttachedPolicyNames(roleName, iamClient);
        final Set<String> inlinePolicyNames = fetchInlinePolicyNames(roleName, iamClient);
        // assuming that there is an inline policy with the same name as the role itself
        final String mainPolicy = inlinePolicyNames.contains(roleName) ? fetchMainPolicy(roleName, iamClient) : EMPTY_JSON;

        return new RolePolicies(attachedPolicyNames, inlinePolicyNames, mainPolicy);
    }

    private String fetchMainPolicy(String roleName, AmazonIdentityManagementClient iamClient) {
        return Optional.of(new GetRolePolicyRequest().withRoleName(roleName).withPolicyName(roleName))
                .map((request) -> AwsRequestUtil.performRequest(() -> iamClient.getRolePolicy(request)))
                .map(GetRolePolicyResult::getPolicyDocument)
                .map(PolicyProviderImpl::urlDecode)
                .orElse(EMPTY_JSON);
    }

    private Set<String> fetchInlinePolicyNames(String roleName, AmazonIdentityManagementClient iamClient) {
        return Optional.of(new ListRolePoliciesRequest().withRoleName(roleName))
                .map((request) -> AwsRequestUtil.performRequest(() -> iamClient.listRolePolicies(request)))
                .map(ListRolePoliciesResult::getPolicyNames)
                .map(nameList -> nameList.stream().collect(toSet()))
                .orElseGet(Collections::emptySet);
    }

    private Set<String> fetchAttachedPolicyNames(String roleName, AmazonIdentityManagementClient iamClient) {
        return Optional.of(new ListAttachedRolePoliciesRequest().withRoleName(roleName))
                .map((request) -> AwsRequestUtil.performRequest(() -> iamClient.listAttachedRolePolicies(request)))
                .map(ListAttachedRolePoliciesResult::getAttachedPolicies)
                .map(attachedPolicies -> attachedPolicies.stream().map(AttachedPolicy::getPolicyName).collect(toSet()))
                .orElseGet(Collections::emptySet);
    }

    private static String urlDecode(String input) {
        try {
            return URLDecoder.decode(input, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new IllegalStateException(e);
        }
    }

}
