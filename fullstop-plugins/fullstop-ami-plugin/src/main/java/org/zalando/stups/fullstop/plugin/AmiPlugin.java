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
import org.springframework.stereotype.Component;

/**
 * @author  mrandi
 */
@Component
public class AmiPlugin implements FullstopPlugin {
    private static final Logger LOG = LoggerFactory.getLogger(AmiPlugin.class);

    private static final String EC2_EVENTS = "ec2.amazonaws.com";
    private static final String RUN = "RunInstances";

    private static final String AMI = "ADBCDEF";


    @Override
    public Object processEvent(CloudTrailEvent event) {
        return getAmi(event.getEventData().getResponseElements());

    }


    @Override
    public boolean supports(CloudTrailEvent delimiter) {
        CloudTrailEventData cloudTrailEventData = delimiter.getEventData();
        String eventSource = cloudTrailEventData.getEventSource();
        String eventName = cloudTrailEventData.getEventName();

        return eventName.equals(EC2_EVENTS) && eventSource.equals(RUN);
    }

    private String getAmi(String parameters) {
        if (parameters == null) {
            return null;
        }
        return JsonPath.read(parameters, "$.InstancesSet.items[*].imageId");
    }
}
