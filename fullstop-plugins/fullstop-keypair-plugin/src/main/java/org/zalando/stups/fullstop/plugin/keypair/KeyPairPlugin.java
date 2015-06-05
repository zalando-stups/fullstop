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
package org.zalando.stups.fullstop.plugin.keypair;

import static java.lang.String.format;

import static org.zalando.stups.fullstop.events.CloudtrailEventSupport.containsKeyNames;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.stereotype.Component;

import org.springframework.util.CollectionUtils;

import org.zalando.stups.fullstop.plugin.AbstractFullstopPlugin;
import org.zalando.stups.fullstop.violation.ViolationBuilder;
import org.zalando.stups.fullstop.violation.ViolationSink;

import com.amazonaws.services.cloudtrail.processinglibrary.model.CloudTrailEvent;
import com.amazonaws.services.cloudtrail.processinglibrary.model.CloudTrailEventData;

/**
 * @author  ljaeckel
 */
@Component
public class KeyPairPlugin extends AbstractFullstopPlugin {

    private static final Logger LOG = LoggerFactory.getLogger(KeyPairPlugin.class);

    private static final String EC2_SOURCE_EVENTS = "ec2.amazonaws.com";
    private static final String EVENT_NAME = "RunInstances";

    private final ViolationSink violationSink;

    @Autowired
    public KeyPairPlugin(final ViolationSink violationSink) {
        this.violationSink = violationSink;
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

        List<String> keyNames = containsKeyNames(event.getEventData().getRequestParameters());
        if (!CollectionUtils.isEmpty(keyNames)) {
            violationSink.put(new ViolationBuilder(format("KeyPair must be blank, but was %s", keyNames)).withEventId(
                    getCloudTrailEventId(event)).withRegion(getCloudTrailEventRegion(event)).withAccoundId(
                    getCloudTrailEventAccountId(event)).build());

        }
    }

}
