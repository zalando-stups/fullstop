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

import com.amazonaws.services.cloudtrail.processinglibrary.model.CloudTrailEvent;
import com.amazonaws.services.cloudtrail.processinglibrary.model.CloudTrailEventData;
import com.jayway.jsonpath.JsonPath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;


/**
 * @author gkneitschel
 */
@Component
public class RegionPlugin implements FullstopPlugin {

    private static final Logger LOG = LoggerFactory.getLogger(
            RegionPlugin.class);

    private static final String EC2_SOURCE_EVENTS = "ec2.amazonaws.com";
    private static final String EVENT_NAME = "RunInstances";


    @Value("${fullstop.plugin.properties.whitelistedRegions}")
    private String whitelistedRegions;


    @Override
    public boolean supports(final CloudTrailEvent event) {
        CloudTrailEventData cloudTrailEventData = event.getEventData();
        String eventSource = cloudTrailEventData.getEventSource();
        String eventName = cloudTrailEventData.getEventName();

        return eventSource.equals(EC2_SOURCE_EVENTS) &&
                eventName.equals(EVENT_NAME);
    }

    @Override
    public Object processEvent(final CloudTrailEvent event) {
        String parameters = event.getEventData().getResponseElements();

        String region = event.getEventData().getAwsRegion();
        String instances = getInstanceIds(parameters).toString();

        if (!whitelistedRegions.equals(region)) {
            LOG.error("Region: EC2 instances " + instances +
                    " are running in the wrong region! (" + region + ")");

            return "Region: EC2 instances " + instances +
                    " are running in the wrong region! (" + region + ")";
        }
        LOG.info("Region: correct region set.");
        return "Region: correct region set.";
    }
    

    private List<String> getInstanceIds(final String parameters) {

        if (parameters == null) {
            return null;
        }

        return JsonPath.read(parameters, "$.instancesSet.items[*].instanceId");
    }
}
