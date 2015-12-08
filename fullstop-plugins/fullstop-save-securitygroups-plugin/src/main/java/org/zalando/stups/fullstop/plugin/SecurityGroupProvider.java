package org.zalando.stups.fullstop.plugin;

import com.amazonaws.AmazonClientException;
import com.amazonaws.regions.Region;
import com.amazonaws.services.ec2.AmazonEC2Client;
import com.amazonaws.services.ec2.model.DescribeSecurityGroupsRequest;
import com.amazonaws.services.ec2.model.DescribeSecurityGroupsResult;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zalando.stups.fullstop.aws.ClientProvider;

import java.util.List;

/**
 * @author jbellmann
 */
public class SecurityGroupProvider {

    private final Logger log = LoggerFactory.getLogger(SecurityGroupProvider.class);

    private final ClientProvider clientProvider;

    public SecurityGroupProvider(final ClientProvider clientProvider) {
        this.clientProvider = clientProvider;
    }

    public String getSecurityGroup(final List<String> securityGroupIds, final Region region, final String accountId) {

        DescribeSecurityGroupsResult result = null;
        ObjectMapper objectMapper = new ObjectMapper();
        String securityGroups = null;

        AmazonEC2Client amazonEC2Client = clientProvider.getClient(AmazonEC2Client.class, accountId, region);

        if (amazonEC2Client == null) {
            throw new RuntimeException(
                    String.format(
                            "Somehow we could not create an Client with accountId: %s and region: %s", accountId,
                            region.toString()));
        }
        else {

            try {
                DescribeSecurityGroupsRequest request = new DescribeSecurityGroupsRequest();
                request.setGroupIds(securityGroupIds);
                result = amazonEC2Client.describeSecurityGroups(request);
            }
            catch (AmazonClientException e) {
                log.error(e.getMessage());
            }

            try {
                securityGroups = objectMapper.writeValueAsString(result);
            }
            catch (JsonProcessingException e) {
                log.error(e.getMessage());
            }

            return securityGroups;
        }
    }

}
