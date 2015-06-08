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
package org.zalando.stups.fullstop.plugin;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.regions.Region;
import com.amazonaws.services.cloudtrail.processinglibrary.model.CloudTrailEvent;
import com.amazonaws.services.cloudtrail.processinglibrary.model.CloudTrailEventData;
import com.amazonaws.services.ec2.AmazonEC2Client;
import com.amazonaws.services.ec2.model.DescribeSecurityGroupsRequest;
import com.amazonaws.services.ec2.model.DescribeSecurityGroupsResult;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.ListObjectsRequest;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.zalando.stups.fullstop.aws.ClientProvider;
import org.zalando.stups.fullstop.s3.S3Writer;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.List;

import static org.joda.time.DateTimeZone.UTC;
import static org.zalando.stups.fullstop.events.CloudtrailEventSupport.*;

/**
 * @author gkneitschel
 */
@Component
public class SaveSecurityGroupsPlugin extends AbstractFullstopPlugin {

    private static final Logger LOG = LoggerFactory.getLogger(SaveSecurityGroupsPlugin.class);

    private static final String EC2_SOURCE_EVENTS = "ec2.amazonaws.com";
    private static final String EVENT_NAME = "RunInstances";
    public static final String SECURITY_GROUPS = "SecurityGroups-";
    public static final String JSON = ".json";
    private final ClientProvider cachingClientProvider;

    @Value("${fullstop.instanceData.bucketName}")
    private String bucketName;

    @Autowired
    private S3Writer s3Writer;


    @Autowired
    public SaveSecurityGroupsPlugin(final ClientProvider cachingClientProvider) {
        this.cachingClientProvider = cachingClientProvider;
    }

    @Override
    public boolean supports(final CloudTrailEvent event) {
        CloudTrailEventData cloudTrailEventData = event.getEventData();
        String eventSource = cloudTrailEventData.getEventSource();
        String eventName = cloudTrailEventData.getEventName();

        return eventSource.equals(EC2_SOURCE_EVENTS) && eventName.equals(EVENT_NAME);
    }

    @Override
    public void processEvent(final CloudTrailEvent event) {

        List<String> securityGroupIds = readSecurityGroupIds(event);

        Region region = getRegion(event);
        String accountId = getAccountId(event);
        List<String> instanceIds = getInstanceIds(event);
        DateTime instanceLaunchTime = new DateTime(getInstanceLaunchTime(event));

        String securityGroup = getSecurityGroup(securityGroupIds, region, accountId);

        String prefix = Paths.get(accountId, region.getName(), instanceLaunchTime.toString("YYYY"),
                instanceLaunchTime.toString("MM"), instanceLaunchTime.toString("dd"))
                .toString() + "/";

        List<String> s3InstanceObjects = listS3Objects(bucketName, prefix);


        for (String instanceId : instanceIds) {

            List<String> instanceBuckets = Lists.newArrayList();

            for (String s3InstanceObject : s3InstanceObjects) {
                String s = Paths.get(s3InstanceObject).getFileName().toString();
                if (s.startsWith(instanceId)) {
                    instanceBuckets.add(s);
                }
            }

            if (instanceBuckets.isEmpty()) {
                continue;
            }

            String instanceBucketNameControlElement = null;
            DateTime instanceBootTimeControlElement = null;

            for (String instanceBucket : instanceBuckets) {

                List<String> currentBucket = Lists.newArrayList(Splitter.on('-')
                        .limit(3)
                        .trimResults()
                        .omitEmptyStrings()
                        .split(instanceBucket));

                String currentBucketName = currentBucket.get(0) + "-" + currentBucket.get(1);
                DateTime currentBucketDate = new DateTime(currentBucket.get(2), UTC);

                //TODO we should use absolute values
                if (instanceBucketNameControlElement != null || instanceBootTimeControlElement != null) {
                    if (instanceLaunchTime.getMillis() - currentBucketDate.getMillis() <
                            instanceLaunchTime.getMillis() - instanceBootTimeControlElement.getMillis()) {

                        instanceBucketNameControlElement = currentBucketName;
                        instanceBootTimeControlElement = currentBucketDate;
                    }
                } else {
                    instanceBucketNameControlElement = currentBucketName;
                    instanceBootTimeControlElement = currentBucketDate;
                }
            }
            prefix = prefix + instanceBucketNameControlElement + "-" + instanceBootTimeControlElement;
            writeToS3(securityGroup, prefix);
        }
    }

    public String getSecurityGroup(List<String> securityGroupIds, Region region, String accountId) {

        DescribeSecurityGroupsResult result = null;
        ObjectMapper objectMapper = new ObjectMapper();
        String securityGroups = null;

        AmazonEC2Client amazonEC2Client = cachingClientProvider.getClient(AmazonEC2Client.class, accountId, region);

        if (amazonEC2Client == null) {
            throw new RuntimeException(String.format(
                    "Somehow we could not create an Client with accountId: %s and region: %s", accountId,
                    region.toString()));
        } else {

            try {
                DescribeSecurityGroupsRequest request = new DescribeSecurityGroupsRequest();
                request.setGroupIds(securityGroupIds);
                result = amazonEC2Client.describeSecurityGroups(request);
            } catch (AmazonClientException e) {
                LOG.error(e.getMessage());
            }
            try {
                securityGroups = objectMapper.writeValueAsString(result);
            } catch (JsonProcessingException e) {
                LOG.error(e.getMessage());
            }
            return securityGroups;
        }
    }

    protected List<String> readSecurityGroupIds(final CloudTrailEvent cloudTrailEvent) {

        return read(cloudTrailEvent, SECURITY_GROUP_IDS_JSON_PATH, true);
    }


    private void writeToS3(String content, String prefix) {
        InputStream stream = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentLength(content.length());
        String fileName = SECURITY_GROUPS + new DateTime(UTC) + JSON;
        s3Writer.putObjectToS3(bucketName, fileName, prefix, metadata, stream);
    }

    private List<String> listS3Objects(String bucketName, String prefix) {
        final List<String> commonPrefixes = Lists.newArrayList();

        AmazonS3Client s3client = new AmazonS3Client();

        try {
            System.out.println("Listing objects");

            ListObjectsRequest listObjectsRequest = new ListObjectsRequest()
                    .withDelimiter("/")
                    .withBucketName(bucketName)
                    .withPrefix(prefix);


            ObjectListing objectListing;

            do {
                objectListing = s3client.listObjects(listObjectsRequest);
                commonPrefixes.addAll(objectListing.getCommonPrefixes());
                for (S3ObjectSummary objectSummary :
                        objectListing.getObjectSummaries()) {
                    System.out.println(" - " + objectSummary.getKey() + "  " +
                            "(size = " + objectSummary.getSize() +
                            ")");
                }
                listObjectsRequest.setMarker(objectListing.getNextMarker());
            } while (objectListing.isTruncated());

        } catch (AmazonServiceException ase) {
            System.out.println("Caught an AmazonServiceException, " +
                    "which means your request made it " +
                    "to Amazon S3, but was rejected with an error response " +
                    "for some reason.");
            System.out.println("Error Message:    " + ase.getMessage());
            System.out.println("HTTP Status Code: " + ase.getStatusCode());
            System.out.println("AWS Error Code:   " + ase.getErrorCode());
            System.out.println("Error Type:       " + ase.getErrorType());
            System.out.println("Request ID:       " + ase.getRequestId());
        } catch (AmazonClientException ace) {
            System.out.println("Caught an AmazonClientException, " +
                    "which means the client encountered " +
                    "an internal error while trying to communicate" +
                    " with S3, " +
                    "such as not being able to access the network.");
            System.out.println("Error Message: " + ace.getMessage());
        }

        return commonPrefixes;
    }
}
