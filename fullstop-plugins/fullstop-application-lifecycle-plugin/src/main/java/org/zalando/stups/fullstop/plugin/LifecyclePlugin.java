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

import static com.google.common.collect.Lists.*;
import static java.util.function.Predicate.isEqual;
import static org.zalando.stups.fullstop.events.CloudTrailEventSupport.getAccountId;
import static org.zalando.stups.fullstop.events.CloudTrailEventSupport.getAmi;
import static org.zalando.stups.fullstop.events.CloudTrailEventSupport.getEventTime;
import static org.zalando.stups.fullstop.events.CloudTrailEventSupport.getInstanceId;
import static org.zalando.stups.fullstop.events.CloudTrailEventSupport.getInstances;
import static org.zalando.stups.fullstop.events.CloudTrailEventSupport.getRegion;
import static org.zalando.stups.fullstop.events.CloudTrailEventSupport.getRunInstanceTime;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.regions.Region;
import com.amazonaws.services.cloudtrail.processinglibrary.model.CloudTrailEvent;
import com.amazonaws.services.cloudtrail.processinglibrary.model.CloudTrailEventData;
import com.amazonaws.services.ec2.AmazonEC2Client;
import com.amazonaws.services.ec2.model.*;
import com.google.common.collect.Lists;
import com.jayway.jsonpath.PathNotFoundException;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.zalando.stups.fullstop.aws.ClientProvider;
import org.zalando.stups.fullstop.events.UserDataProvider;
import org.zalando.stups.fullstop.violation.entity.ApplicationEntity;
import org.zalando.stups.fullstop.violation.entity.LifecycleEntity;
import org.zalando.stups.fullstop.violation.entity.VersionEntity;
import org.zalando.stups.fullstop.violation.service.impl.ApplicationLifecycleServiceImpl;

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
        final CloudTrailEventData cloudTrailEventData = event.getEventData();
        final String eventSource = cloudTrailEventData.getEventSource();
        final String eventName = cloudTrailEventData.getEventName();

        return eventSource.equals(EC2_SOURCE_EVENTS) &&
                Stream.of(RUN_EVENT_NAME, START_EVENT_NAME, STOP_EVENT_NAME, TERMINATE_EVENT_NAME)
                      .anyMatch(isEqual(eventName));
    }

    @Override
    public void processEvent(final CloudTrailEvent event) {
        final List<String> instances = getInstances(event);
        final Region region = getRegion(event);
        final String accountId = getAccountId(event);
        final String eventName = event.getEventData().getEventName();

        final AmazonEC2Client amazonEC2Client = clientProvider.getClient(
                AmazonEC2Client.class, event.getEventData().getAccountId(), region);

        for (final String instance : instances) {
            final String instanceId = getInstanceId(instance);
            final DateTime eventDate = getLifecycleDate(event, instance);
            final LifecycleEntity lifecycleEntity = new LifecycleEntity();

            if (eventName.equals(RUN_EVENT_NAME)) {
                String amiId = null;

                try {
                     amiId = getAmiId(amazonEC2Client, instanceId);
                } catch (PathNotFoundException e){
                    LOG.warn("no amiId found for instance {}", instance);
                }

                if (amiId != null) {
                    String amiName = getAmiName(amazonEC2Client, amiId);
                    lifecycleEntity.setImageId(amiId);
                    lifecycleEntity.setImageName(amiName);
                }
            }

            lifecycleEntity.setEventType(eventName);
            lifecycleEntity.setEventDate(eventDate);
            lifecycleEntity.setAccountId(accountId);
            lifecycleEntity.setRegion(region.getName());
            lifecycleEntity.setInstanceId(instanceId);

            final Map userData;
            try {
                userData = userDataProvider.getUserData(
                        accountId,
                        region,
                        instanceId);
                if (userData == null) {
                    LOG.warn(
                            "No userData found for instance {}. Skip processing the {} lifecycle event.",
                            instance,
                            eventName);
                    return;
                }
            }
            catch (final AmazonServiceException e) {
                LOG.warn(
                        "Could not fetch userData for instance {}. Reason: {}. Skip processing the {} lifecycle event",
                        instance,
                        e.toString(),
                        eventName);
                return;
            }

            final Optional<VersionEntity> versionEntity = getEntity(
                    userData,
                    "application_version",
                    VersionEntity::new);
            if (!versionEntity.isPresent()) {
                LOG.warn(
                        "Missing 'application_version' in user data for instance {}. Skip processing the {} lifecycle event",
                        instance,
                        eventName);
                return;
            }

            final Optional<ApplicationEntity> applicationEntity = getEntity(
                    userData,
                    "application_id",
                    ApplicationEntity::new);
            if (!applicationEntity.isPresent()) {
                LOG.warn(
                        "Missing 'application_id' in user data for instance {}. Skip processing the {} lifecycle event",
                        instance,
                        eventName);
                return;
            }

            applicationLifecycleService.saveLifecycle(applicationEntity.get(), versionEntity.get(), lifecycleEntity);
        }

    }

    private String getAmiId(AmazonEC2Client amazonEC2Client, String instance) {
        List<String> instances = newArrayList();
        DescribeInstancesRequest describeInstancesRequest = new DescribeInstancesRequest();
        DescribeInstancesResult describeInstancesResult;

        try {
            describeInstancesResult = amazonEC2Client.describeInstances(describeInstancesRequest.withInstanceIds(instance));
        } catch (final AmazonServiceException e){
            LOG.warn("Lifecycle plugin: cannot fetch ami id. Reason {}", e.toString());
            return null;
        }
        for (Reservation reservation : describeInstancesResult.getReservations()) {
            for (Instance reservationInstance : reservation.getInstances()) {
                instances.add(reservationInstance.getInstanceId());
            }

        }
        if (instances.size()>1){
            LOG.warn("Lifecycle plugin: more than one instance returned for instanceId {}", instance);
        }

        return instances.get(0);

    }

    private String getAmiName(AmazonEC2Client amazonEC2Client, String ami) {
        DescribeImagesRequest describeImagesRequest = new DescribeImagesRequest();
        DescribeImagesResult describeImagesResult;
        try{
            describeImagesResult = amazonEC2Client.describeImages(describeImagesRequest.withImageIds(ami));
        }
        catch (final AmazonServiceException e) {
            LOG.warn("Lifecycle plugin: cannot fetch ami name. Reason {}", e.toString());
            return null;
        }
        return describeImagesResult.getImages().get(0).getName();
    }

    private <T> Optional<T> getEntity(Map userData, String attribute, Function<String, T> constructor) {
        return Optional.ofNullable(userData.get(attribute))
                       .map(Object::toString)
                       .map(constructor);
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
