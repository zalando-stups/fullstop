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
import com.amazonaws.services.cloudtrail.processinglibrary.model.CloudTrailEvent;
import com.amazonaws.services.cloudtrail.processinglibrary.model.CloudTrailEventData;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
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

    @Autowired
    public LifecyclePlugin(final ApplicationLifecycleServiceImpl applicationLifecycleService,
            final UserDataProvider userDataProvider) {
        this.applicationLifecycleService = applicationLifecycleService;
        this.userDataProvider = userDataProvider;
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
        for (String instance : instances) {
            DateTime eventDate = getLifecycleDate(event, instance);

            LifecycleEntity lifecycleEntity = new LifecycleEntity();
            lifecycleEntity.setEventType(event.getEventData().getEventName());
            lifecycleEntity.setEventDate(eventDate);
            lifecycleEntity.setRegion(region);
            lifecycleEntity.setInstanceId(CloudTrailEventSupport.getSingleInstance(instance));

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

            applicationLifecycleService.saveLifecycle(applicationEntity, versionEntity, lifecycleEntity);
        }

    }

    private String getApplicationName(final CloudTrailEvent event, final String instance) {
        String instanceId = getSingleInstance(instance);
        Map userData = userDataProvider.getUserData(getAccountId(event), getRegion(event), instanceId);
        return userData.get("application_id").toString();
    }

    private String getVersionName(final CloudTrailEvent event, final String instance) {
        String instanceId = getSingleInstance(instance);

        Map userData = userDataProvider.getUserData(getAccountId(event), getRegion(event), instanceId);
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
