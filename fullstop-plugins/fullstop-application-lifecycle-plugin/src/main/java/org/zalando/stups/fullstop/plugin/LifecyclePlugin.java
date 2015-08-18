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

import com.amazonaws.AmazonServiceException;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.cloudtrail.processinglibrary.model.CloudTrailEvent;
import com.amazonaws.services.cloudtrail.processinglibrary.model.CloudTrailEventData;
import com.amazonaws.services.ec2.AmazonEC2Client;
import com.amazonaws.services.ec2.model.DescribeImagesRequest;
import com.amazonaws.services.ec2.model.DescribeImagesResult;
import com.jayway.jsonpath.PathNotFoundException;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.zalando.stups.fullstop.aws.CachingClientProvider;
import org.zalando.stups.fullstop.aws.ClientProvider;
import org.zalando.stups.fullstop.events.CloudTrailEventSupport;
import org.zalando.stups.fullstop.events.UserDataProvider;
import org.zalando.stups.fullstop.violation.entity.ApplicationEntity;
import org.zalando.stups.fullstop.violation.entity.LifecycleEntity;
import org.zalando.stups.fullstop.violation.entity.VersionEntity;
import org.zalando.stups.fullstop.violation.service.impl.ApplicationLifecycleServiceImpl;

import java.util.List;
import java.util.Map;

import static org.zalando.stups.fullstop.events.CloudTrailEventSupport.*;

/**
 * Created by gkneitschel.
 */
@Component
public class LifecyclePlugin extends AbstractFullstopPlugin {

    private static final Logger LOG = LoggerFactory.getLogger(LifecyclePlugin.class);

    private static final String EC2_SOURCE_EVENTS = "ec2.amazonaws.com";

    private static final String RUN_EVENT_NAME = "RunInstances";

    private static final String START_EVENT_NAME = "StartInstances";

    private static final String STOP_EVENT_NAME = "StopInstances";

    private static final String TERMINATE_EVENT_NAME = "TerminateInstances";

    private ApplicationLifecycleServiceImpl applicationLifecycleService;

    private UserDataProvider userDataProvider;

    private ClientProvider clientProvider;

    @Autowired
    public LifecyclePlugin(final ApplicationLifecycleServiceImpl applicationLifecycleService,
            final UserDataProvider userDataProvider, ClientProvider clientProvider) {
        this.applicationLifecycleService = applicationLifecycleService;
        this.userDataProvider = userDataProvider;
        this.clientProvider = clientProvider;
    }

    @Override
    public boolean supports(final CloudTrailEvent event) {
        CloudTrailEventData cloudTrailEventData = event.getEventData();
        String eventSource = cloudTrailEventData.getEventSource();
        String eventName = cloudTrailEventData.getEventName();

        return eventSource.equals(EC2_SOURCE_EVENTS)
                && (eventName.equals(RUN_EVENT_NAME) || eventName.equals(START_EVENT_NAME)
                || eventName.equals(STOP_EVENT_NAME) || eventName.equals(TERMINATE_EVENT_NAME));
    }

    @Override
    public void processEvent(final CloudTrailEvent event) {
        List<String> instances = CloudTrailEventSupport.getInstances(event);
        String region = getRegionAsString(event);
        AmazonEC2Client amazonEC2Client = clientProvider
                .getClient(
                        AmazonEC2Client.class, event.getEventData().getAccountId(),CloudTrailEventSupport.getRegion(event));
        for (String instance : instances) {
            String amiId = null;

            try {
                 amiId = getAmi(instance);
            } catch (PathNotFoundException e){
                LOG.warn("no amiId found for instance {}", instance);
            }

            DateTime eventDate = getLifecycleDate(event, instance);
            LifecycleEntity lifecycleEntity = new LifecycleEntity();

            if (amiId != null) {
                String amiName = getAmiName(amazonEC2Client, amiId);
                lifecycleEntity.setImageId(amiId);
                lifecycleEntity.setImageName(amiName);
            }


            lifecycleEntity.setEventType(event.getEventData().getEventName());
            lifecycleEntity.setEventDate(eventDate);
            lifecycleEntity.setAccountId(getAccountId(event));
            lifecycleEntity.setRegion(region);
            lifecycleEntity.setInstanceId(CloudTrailEventSupport.getInstanceId(instance));

            ApplicationEntity applicationEntity;
            VersionEntity versionEntity;
            try {

                String versionName = getVersionName(event, instance);
                versionEntity = new VersionEntity(versionName);

                String applicationName = getApplicationName(event, instance);
                applicationEntity = new ApplicationEntity(applicationName);
            }
            catch (AmazonServiceException e) {
                LOG.warn("Could not get version/application for lifecycle event.");
                return;
            }

            if (versionEntity.getName() == null || applicationEntity.getName() == null) {
                LOG.warn("Lifecycle: UserData does not contain application name or version!");
                return;
            }
            applicationLifecycleService.saveLifecycle(applicationEntity, versionEntity, lifecycleEntity);
        }

    }

    private String getAmiName(AmazonEC2Client amazonEC2Client, String ami) {
        DescribeImagesRequest describeImagesRequest = new DescribeImagesRequest();
        DescribeImagesResult describeImagesResult = null;
        try{
            describeImagesResult = amazonEC2Client.describeImages(describeImagesRequest.withImageIds(ami));
        } catch (AmazonServiceException e){
            LOG.warn("Lifecycle plugin: cannot fetch ami name. Reason {}", e.toString());
            return null;
        }
        return describeImagesResult.getImages().get(0).getName();
    }

    private String getApplicationName(final CloudTrailEvent event, final String instance) {
        String instanceId = getInstanceId(instance);
        Map userData = userDataProvider.getUserData(getAccountId(event), getRegion(event), instanceId);
        if (userData == null || userData.get("application_id") == null) {
            return null;
        }
        return userData.get("application_id").toString();
    }

    private String getVersionName(final CloudTrailEvent event, final String instance) {
        String instanceId = getInstanceId(instance);

        Map userData = userDataProvider.getUserData(getAccountId(event), getRegion(event), instanceId);
        if (userData == null || userData.get("application_version") == null) {
            return null;
        }
        return userData.get("application_version").toString();

    }

    private DateTime getLifecycleDate(final CloudTrailEvent event, final String instance) {

        String eventName = event.getEventData().getEventName();

        if (eventName.equals(RUN_EVENT_NAME)) {
            return getRunInstanceTime(instance);
        }
        else {
            return getEventTime(event);
        }
    }

}
