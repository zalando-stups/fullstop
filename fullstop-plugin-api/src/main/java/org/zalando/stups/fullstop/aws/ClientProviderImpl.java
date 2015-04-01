/**
 * Copyright 2015 Zalando SE
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.zalando.stups.fullstop.aws;

import com.amazonaws.services.s3.AmazonS3Client;
import org.springframework.stereotype.Service;

import com.amazonaws.auth.BasicSessionCredentials;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;

import com.amazonaws.regions.Region;

import com.amazonaws.services.ec2.AmazonEC2Client;
import com.amazonaws.services.route53.AmazonRoute53Client;
import com.amazonaws.services.securitytoken.AWSSecurityTokenServiceClient;
import com.amazonaws.services.securitytoken.model.AssumeRoleRequest;
import com.amazonaws.services.securitytoken.model.AssumeRoleResult;

/**
 * //TODO, every call to get__Client is a requestToAWS and costs money.
 *
 * @author  mrandi
 */
@Service
public class ClientProviderImpl implements ClientProvider {

    @Override
    public AmazonEC2Client getEC2Client(final String accountId, final Region region) {

        AmazonEC2Client amazonEC2Client = new AmazonEC2Client(getTemporaryCredentials(accountId));
        amazonEC2Client.setRegion(region);

        return amazonEC2Client;
    }

    @Override
    public AmazonRoute53Client getRoute53Client(final String accountId, final Region region) {
        throw new UnsupportedOperationException("Not implemented yet!");
    }

    @Override
    public AmazonS3Client getS3Client(final String accountId, final Region region){
        AmazonS3Client amazonS3Client = new AmazonS3Client(getTemporaryCredentials(accountId));
        amazonS3Client.setRegion(region);

        return amazonS3Client;
    }

    private BasicSessionCredentials getTemporaryCredentials(final String accountId) {
        AWSSecurityTokenServiceClient stsClient = new AWSSecurityTokenServiceClient(new ProfileCredentialsProvider());

        AssumeRoleRequest assumeRequest = new AssumeRoleRequest().withRoleArn("arn:aws:iam::" + accountId
                                                                         + ":role/fullstop").withDurationSeconds(3600)
                                                                 .withRoleSessionName("fullstop");

        AssumeRoleResult assumeResult = stsClient.assumeRole(assumeRequest);

        return new BasicSessionCredentials(assumeResult.getCredentials().getAccessKeyId(),
                assumeResult.getCredentials().getSecretAccessKey(), assumeResult.getCredentials().getSessionToken());
    }
}
