/**
 * Copyright (C) 2015 Zalando SE (http://tech.zalando.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/**
 *
 * @author npiccolotto
 */
package org.zalando.stups.fullstop.events;

import java.util.Map;

import org.yaml.snakeyaml.Yaml;

import org.zalando.stups.fullstop.aws.ClientProvider;

import com.amazonaws.AmazonServiceException;

import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;

import com.amazonaws.services.cloudtrail.processinglibrary.model.CloudTrailEvent;
import com.amazonaws.services.ec2.AmazonEC2Client;
import com.amazonaws.services.ec2.model.DescribeInstanceAttributeRequest;
import com.amazonaws.services.ec2.model.DescribeInstanceAttributeResult;

import com.amazonaws.util.Base64;

public class UserDataProvider {
    public static final String USER_DATA = "userData";

    private final ClientProvider clientProvider;

    public UserDataProvider(final ClientProvider clientProvider) {
        this.clientProvider = clientProvider;
    }

    @Deprecated
    public Map getUserData(final CloudTrailEvent event, final String instanceId) throws AmazonServiceException {
        final String accountId = event.getEventData().getUserIdentity().getAccountId();
        final String region = event.getEventData().getAwsRegion();
        return getUserData(accountId, region, instanceId);
    }

    public Map getUserData(final String accountId, final String region, final String instanceId)
        throws AmazonServiceException {
        Region awsRegion = Region.getRegion(Regions.fromName(region));
        return getUserData(accountId, awsRegion, instanceId);
    }

    public Map getUserData(final String accountId, final Region region, final String instanceId)
        throws AmazonServiceException {
        AmazonEC2Client ec2Client = clientProvider.getClient(AmazonEC2Client.class, accountId, region);

        DescribeInstanceAttributeRequest describeInstanceAttributeRequest = new DescribeInstanceAttributeRequest();
        describeInstanceAttributeRequest.setInstanceId(instanceId);
        describeInstanceAttributeRequest.setAttribute(USER_DATA);

        DescribeInstanceAttributeResult describeInstanceAttributeResult;
        describeInstanceAttributeResult = ec2Client.describeInstanceAttribute(describeInstanceAttributeRequest);

        String userData = describeInstanceAttributeResult.getInstanceAttribute().getUserData();

        if (userData == null) {
            return null;
        }

        byte[] bytesUserData = Base64.decode(userData);
        String decodedUserData = new String(bytesUserData);

        Yaml yaml = new Yaml();

        return (Map) yaml.load(decodedUserData);
    }
}
