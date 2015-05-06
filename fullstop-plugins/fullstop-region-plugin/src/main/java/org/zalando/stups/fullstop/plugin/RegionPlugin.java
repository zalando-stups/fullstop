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

import static org.zalando.stups.fullstop.events.CloudTrailEventPredicate.fromSource;
import static org.zalando.stups.fullstop.events.CloudTrailEventPredicate.withName;
import static org.zalando.stups.fullstop.events.CloudtrailEventSupport.getAccountId;
import static org.zalando.stups.fullstop.events.CloudtrailEventSupport.getInstanceIds;
import static org.zalando.stups.fullstop.events.CloudtrailEventSupport.getRegionAsString;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.stereotype.Component;

import org.zalando.stups.fullstop.events.CloudTrailEventPredicate;
import org.zalando.stups.fullstop.plugin.config.RegionPluginProperties;
import org.zalando.stups.fullstop.violation.Violation;
import org.zalando.stups.fullstop.violation.ViolationStore;

import com.amazonaws.services.cloudtrail.processinglibrary.model.CloudTrailEvent;

/**
 * @author  gkneitschel
 */
@Component
public class RegionPlugin extends AbstractFullstopPlugin {

    private static final Logger LOG = LoggerFactory.getLogger(RegionPlugin.class);

    private static final String EC2_SOURCE_EVENTS = "ec2.amazonaws.com";
    private static final String EVENT_NAME = "RunInstances";

    private final ViolationStore violationStore;

    private CloudTrailEventPredicate eventFilter = fromSource(EC2_SOURCE_EVENTS).andWith(withName(EVENT_NAME));

// @Value("${fullstop.plugins.region.whitelistedRegions}")
// private String whitelistedRegions;

    private final RegionPluginProperties regionPluginProperties;

    @Autowired
    public RegionPlugin(final ViolationStore violationStore, final RegionPluginProperties regionPluginProperties) {
        this.violationStore = violationStore;
        this.regionPluginProperties = regionPluginProperties;
    }

    @Override
    public boolean supports(final CloudTrailEvent event) {
// CloudTrailEventData cloudTrailEventData = event.getEventData();
// String eventSource = cloudTrailEventData.getEventSource();
// String eventName = cloudTrailEventData.getEventName();
//
// return eventSource.equals(EC2_SOURCE_EVENTS) && eventName.equals(EVENT_NAME);

        return eventFilter.test(event);
    }

    @Override
    public void processEvent(final CloudTrailEvent event) {

        // Check Auto-Scaling, seems to be null on Auto-Scaling-Event

        String accountId = getAccountId(event);
        String region = getRegionAsString(event);
        List<String> instances = getInstanceIds(event);

        // List<String> instances = getInstanceIds(event);
        if (instances.isEmpty()) {
            LOG.error("No instanceIds found, maybe autoscaling?");
        }

// if (!whitelistedRegions.equals(region)) {
// LOG.error("Region: EC2 instances " + instances + " are running in the wrong region! (" + region + ")");
//
// }

        if (!regionPluginProperties.getWhitelistedRegions().contains(region)) {
            String message = String.format("Region: EC2 instances %s are running in the wrong region! (%s)",
                    instances.toString(), region);
            Violation violation = new Violation(accountId, region, message);
            violationStore.save(violation);
        }

        // Do we need this?
        LOG.info("Region: correct region set.");
    }

// private List<String> getInstanceIds(final CloudTrailEvent event) {
//
// String parameters = event.getEventData().getResponseElements();
//
// if (parameters == null) {
// return Lists.newArrayList();
// }
//
// List<String> instanceIds = new ArrayList<>();
// try {
// instanceIds = JsonPath.read(parameters, "$.instancesSet.items[*].instanceId");
// return instanceIds;
// } catch (Exception e) {
// violationStore.save(new Violation(getAccountId(event), getRegionAsString(event),
// "Cannot find InstanceIds in JSON " + e));
// }
//
// return instanceIds;
// }
}
