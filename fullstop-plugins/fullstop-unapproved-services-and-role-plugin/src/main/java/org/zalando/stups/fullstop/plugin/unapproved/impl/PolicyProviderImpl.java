package org.zalando.stups.fullstop.plugin.unapproved.impl;

import com.amazonaws.AmazonClientException;
import com.amazonaws.regions.Region;
import com.amazonaws.services.identitymanagement.AmazonIdentityManagementClient;
import com.amazonaws.services.identitymanagement.model.GetRoleRequest;
import com.amazonaws.services.identitymanagement.model.GetRoleResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.zalando.stups.fullstop.aws.ClientProvider;
import org.zalando.stups.fullstop.plugin.unapproved.PolicyProvider;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

/**
 * @author mrandi
 */
@Service
public class PolicyProviderImpl implements PolicyProvider {

    private final Logger log = LoggerFactory.getLogger(PolicyProviderImpl.class);

    private final ClientProvider clientProvider;

    @Autowired
    public PolicyProviderImpl(final ClientProvider clientProvider) {
        this.clientProvider = clientProvider;
    }

    @Override public String getPolicy(final String roleName, final Region region, final String accountId) {

        AmazonIdentityManagementClient iamClient = clientProvider
                .getClient(AmazonIdentityManagementClient.class, accountId, region);

        if (iamClient == null) {
            throw new RuntimeException(
                    String.format(
                            "Somehow we could not create an AmazonIdentityManagementClient with accountId: %s and region: %s",
                            accountId,
                            region.toString()));
        }
        else {

            String assumeRolePolicyDocument = null;
            try {
                GetRoleRequest getRoleRequest = new GetRoleRequest();
                getRoleRequest.setRoleName(roleName);

                GetRoleResult role = iamClient.getRole(getRoleRequest);

                if (role != null && role.getRole() != null && role.getRole().getAssumeRolePolicyDocument() != null) {
                    try {
                        assumeRolePolicyDocument = URLDecoder.decode(
                                role.getRole().getAssumeRolePolicyDocument(),
                                "UTF-8");
                    }
                    catch (UnsupportedEncodingException e) {
                        log.warn("Could not decode policy document for role: {}", roleName);
                    }
                }
                else {
                    return null;
                }

            }
            catch (AmazonClientException e) {
                log.error(e.getMessage());
            }

            return assumeRolePolicyDocument;

        }
    }

}
