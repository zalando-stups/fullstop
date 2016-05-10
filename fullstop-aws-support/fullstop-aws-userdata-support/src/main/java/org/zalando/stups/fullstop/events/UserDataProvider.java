/**
 *
 * @author npiccolotto
 */
package org.zalando.stups.fullstop.events;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.ec2.AmazonEC2Client;
import com.amazonaws.services.ec2.model.DescribeInstanceAttributeRequest;
import com.amazonaws.services.ec2.model.DescribeInstanceAttributeResult;
import com.amazonaws.util.Base64;
import org.yaml.snakeyaml.Yaml;
import org.zalando.stups.fullstop.aws.ClientProvider;

import java.util.Map;

public class UserDataProvider {
    public static final String USER_DATA = "userData";

    private final ClientProvider clientProvider;

    public UserDataProvider(final ClientProvider clientProvider) {
        this.clientProvider = clientProvider;
    }

    public Map getUserData(final String accountId, final String region, final String instanceId)
            throws AmazonServiceException {
        final Region awsRegion = Region.getRegion(Regions.fromName(region));
        return getUserData(accountId, awsRegion, instanceId);
    }

    public Map getUserData(final String accountId, final Region region, final String instanceId)
            throws AmazonServiceException {
        final AmazonEC2Client ec2Client = clientProvider.getClient(AmazonEC2Client.class, accountId, region);

        final DescribeInstanceAttributeRequest describeInstanceAttributeRequest = new DescribeInstanceAttributeRequest();
        describeInstanceAttributeRequest.setInstanceId(instanceId);
        describeInstanceAttributeRequest.setAttribute(USER_DATA);

        final DescribeInstanceAttributeResult describeInstanceAttributeResult;
        describeInstanceAttributeResult = ec2Client.describeInstanceAttribute(describeInstanceAttributeRequest);

        final String userData = describeInstanceAttributeResult.getInstanceAttribute().getUserData();

        if (userData == null) {
            return null;
        }

        final byte[] bytesUserData = Base64.decode(userData);
        final String decodedUserData = new String(bytesUserData);

        final Object yamlData = new Yaml().load(decodedUserData);

        return yamlData instanceof Map ? (Map) yamlData : null;
    }
}
