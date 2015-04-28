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

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import org.springframework.stereotype.Component;

import org.zalando.stups.fullstop.violation.ViolationStore;
import org.zalando.stups.fullstop.violation.ViolationStoreDomain;

import com.amazonaws.services.cloudtrail.processinglibrary.model.CloudTrailEvent;
import com.amazonaws.services.cloudtrail.processinglibrary.model.CloudTrailEventData;

import com.google.common.collect.Lists;

import com.jayway.jsonpath.JsonPath;

/**
 * @author  gkneitschel
 */
@Component
public class RegionPlugin extends AbstractFullstopPlugin {

    private static final Logger LOG = LoggerFactory.getLogger(RegionPlugin.class);

    private static final String EC2_SOURCE_EVENTS = "ec2.amazonaws.com";
    private static final String EVENT_NAME = "RunInstances";

    private final ViolationStore violationStore;

    @Value("${fullstop.plugins.region.whitelistedRegions}")
    private String whitelistedRegions;

    @Autowired
    public RegionPlugin(final ViolationStore violationStore) {
        this.violationStore = violationStore;
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

        // Check Auto-Scaling, seems to be null on Auto-Scaling-Event

        String region = event.getEventData().getAwsRegion();
        List<String> instances = getInstanceIds(event);
        if (instances.isEmpty()) {
            LOG.error("No instanceIds found, maybe autoscaling?");
        }

        if (!whitelistedRegions.equals(region)) {
            LOG.error("Region: EC2 instances " + instances + " are running in the wrong region! (" + region + ")");

        }

        LOG.info("Region: correct region set.");
    }

    private List<String> getInstanceIds(final CloudTrailEvent event) {

        String parameters = event.getEventData().getResponseElements();

        if (parameters == null) {
            return Lists.newArrayList();
        }

        List<String> instanceIds = new ArrayList<>();
        try {
            instanceIds = JsonPath.read(parameters, "$.instancesSet.items[*].instanceId");
            return instanceIds;
        } catch (Exception e) {
            violationStore.save(new ViolationStoreDomain(event.getEventData().getAccountId(),
                    event.getEventData().getAwsRegion(), "Cannot find InstanceIds in JSON " + e));
        }

        return instanceIds;
    }
}
