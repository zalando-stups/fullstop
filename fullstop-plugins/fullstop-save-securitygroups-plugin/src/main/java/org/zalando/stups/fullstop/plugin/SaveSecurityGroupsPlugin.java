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

import static org.joda.time.DateTimeZone.UTC;

import static org.zalando.stups.fullstop.events.CloudtrailEventSupport.SECURITY_GROUP_IDS_JSON_PATH;
import static org.zalando.stups.fullstop.events.CloudtrailEventSupport.getAccountId;
import static org.zalando.stups.fullstop.events.CloudtrailEventSupport.getInstanceIds;
import static org.zalando.stups.fullstop.events.CloudtrailEventSupport.getInstanceLaunchTime;
import static org.zalando.stups.fullstop.events.CloudtrailEventSupport.getRegion;
import static org.zalando.stups.fullstop.events.CloudtrailEventSupport.read;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;

import java.util.List;

import org.joda.time.DateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import org.springframework.stereotype.Component;

import org.zalando.stups.fullstop.s3.S3Service;

import com.amazonaws.regions.Region;

import com.amazonaws.services.cloudtrail.processinglibrary.model.CloudTrailEvent;
import com.amazonaws.services.cloudtrail.processinglibrary.model.CloudTrailEventData;
import com.amazonaws.services.s3.model.ObjectMetadata;

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;

/**
 * @author  gkneitschel
 */
@Component
public class SaveSecurityGroupsPlugin extends AbstractFullstopPlugin {

    private static final Logger LOG = LoggerFactory.getLogger(SaveSecurityGroupsPlugin.class);

    private static final String EC2_SOURCE_EVENTS = "ec2.amazonaws.com";
    private static final String EVENT_NAME = "RunInstances";
    public static final String SECURITY_GROUPS = "security-groups-";
    public static final String JSON = ".json";

    @Value("${fullstop.instanceData.bucketName}")
    private String bucketName;

    private final S3Service s3Writer;

    private final SecurityGroupProvider securityGroupProvider;

    @Autowired
    public SaveSecurityGroupsPlugin(final SecurityGroupProvider securityGroupProvider, final S3Service s3Writer) {
        this.securityGroupProvider = securityGroupProvider;
        this.s3Writer = s3Writer;
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
        DateTime instanceLaunchTime = new DateTime(getInstanceLaunchTime(event).get(0));

        String securityGroup = getSecurityGroup(securityGroupIds, region, accountId);

        String prefix = Paths.get(accountId, region.getName(), instanceLaunchTime.toString("YYYY"),
                instanceLaunchTime.toString("MM"), instanceLaunchTime.toString("dd")).toString() + "/";

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

            // TODO, I do not understand what is going on here and why
            for (String instanceBucket : instanceBuckets) {
                List<String> currentBucket = Lists.newArrayList(Splitter.on('-').limit(3).trimResults()
                            .omitEmptyStrings().split(instanceBucket));

                String currentBucketName = currentBucket.get(0) + "-" + currentBucket.get(1);
                DateTime currentBucketDate = new DateTime(currentBucket.get(2), UTC);

                // TODO we should use absolute values
                if (instanceBucketNameControlElement != null || instanceBootTimeControlElement != null) {
                    if (instanceLaunchTime.getMillis() - currentBucketDate.getMillis()
                            < instanceLaunchTime.getMillis() - instanceBootTimeControlElement.getMillis()) {

                        instanceBucketNameControlElement = currentBucketName;
                        instanceBootTimeControlElement = currentBucketDate;
                    }
                } else {
                    instanceBucketNameControlElement = currentBucketName;
                    instanceBootTimeControlElement = currentBucketDate;
                }
            }

            prefix = prefix + instanceBucketNameControlElement + "-" + instanceBootTimeControlElement;
            writeToS3(securityGroup, prefix, instanceId);
        }
    }

    public String getSecurityGroup(final List<String> securityGroupIds, final Region region, final String accountId) {
        return securityGroupProvider.getSecurityGroup(securityGroupIds, region, accountId);
    }

    protected List<String> readSecurityGroupIds(final CloudTrailEvent cloudTrailEvent) {
        return read(cloudTrailEvent, SECURITY_GROUP_IDS_JSON_PATH, true);
    }

    protected void writeToS3(final String content, final String prefix, final String instanceId) {
        InputStream stream = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentLength(content.length());

        String fileName = instanceId + SECURITY_GROUPS + new DateTime(UTC) + JSON;
        s3Writer.putObjectToS3(bucketName, fileName, prefix, metadata, stream);
    }

    protected List<String> listS3Objects(final String buckestName, final String prefix) {
        return s3Writer.listS3Objects(buckestName, prefix);
    }

}
