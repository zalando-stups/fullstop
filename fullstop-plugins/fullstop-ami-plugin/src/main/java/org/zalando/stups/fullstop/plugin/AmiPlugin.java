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

package org.zalando.stups.fullstop.plugin;

import static org.slf4j.LoggerFactory.getLogger;

import org.slf4j.Logger;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.stereotype.Component;

import org.zalando.stups.fullstop.aws.ClientProvider;

import com.amazonaws.services.cloudtrail.processinglibrary.model.CloudTrailEvent;
import com.amazonaws.services.cloudtrail.processinglibrary.model.CloudTrailEventData;

/**
 * @author  mrandi
 */
@Component
public class AmiPlugin implements FullstopPlugin {

    private final Logger log = getLogger(getClass());

    private static final String EC2_EVENTS = "ec2.amazonaws.com";
    private static final String RUN = "RunInstances";

    private final ClientProvider clientProvider;

    @Autowired
    public AmiPlugin(final ClientProvider clientProvider) {
        this.clientProvider = clientProvider;
    }

    @Override
    public boolean supports(final CloudTrailEvent event) {
        CloudTrailEventData eventData = event.getEventData();

        String eventSource = eventData.getEventSource();
        String eventName = eventData.getEventName();

        return eventSource.equals(EC2_EVENTS) && eventName.equals(RUN);
    }

    @Override
    public Object processEvent(final CloudTrailEvent event) {

        // 1. get ami from event
        // 2. find out if this ami is whitelisted

        return null;
    }
}
