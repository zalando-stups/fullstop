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

import com.amazonaws.services.cloudtrail.processinglibrary.model.CloudTrailEvent;
import com.amazonaws.services.cloudtrail.processinglibrary.model.CloudTrailEventData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.zalando.stups.fullstop.events.UserDataProvider;
import org.zalando.stups.fullstop.violation.repository.LifecycleRepository;

/**
 * Created by gkneitschel.
 */
public class LifecyclePlugin extends AbstractFullstopPlugin {

    private static final Logger LOG = LoggerFactory.getLogger(LifecyclePlugin.class);

    private static final String EC2_SOURCE_EVENTS = "ec2.amazonaws.com";

    private static final String RUN_EVENT_NAME = "RunInstances";

    private static final String START_EVENT_NAME = "StartInstances";

    private static final String STOP_EVENT_NAME = "StopInstances";

    private static final String TERMINATE_EVENT_NAME = "TerminateInstances";

    private LifecycleRepository lifecycleRepository;

    private UserDataProvider userDataProvider;

    @Autowired
    public LifecyclePlugin(final LifecycleRepository lifecycleRepository, UserDataProvider userDataProvider) {
        this.lifecycleRepository = lifecycleRepository;
        this.userDataProvider = userDataProvider;
    }

    @Override
    public boolean supports(CloudTrailEvent event) {
        CloudTrailEventData cloudTrailEventData = event.getEventData();
        String eventSource = cloudTrailEventData.getEventSource();
        String eventName = cloudTrailEventData.getEventName();

        return eventSource.equals(EC2_SOURCE_EVENTS) &&
                (eventName.equals(RUN_EVENT_NAME) ||
                        eventName.equals(START_EVENT_NAME) ||
                        eventName.equals(STOP_EVENT_NAME) ||
                        eventName.equals(TERMINATE_EVENT_NAME)
                );
    }

    @Override
    public void processEvent(CloudTrailEvent event) {

    }

    private String getApplicationVersion() {
        return "";
    }
}
