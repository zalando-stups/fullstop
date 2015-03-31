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

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.stereotype.Component;

import com.amazonaws.services.cloudtrail.processinglibrary.model.CloudTrailEvent;
import com.amazonaws.services.cloudtrail.processinglibrary.model.CloudTrailEventData;

import com.google.common.collect.Lists;

import com.jayway.jsonpath.JsonPath;

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
    public boolean supports(final CloudTrailEvent event) {
        CloudTrailEventData cloudTrailEventData = event.getEventData();
        String eventSource = cloudTrailEventData.getEventSource();
        String eventName = cloudTrailEventData.getEventName();

        return eventName.equals(EC2_EVENTS) && eventSource.equals(RUN);
    }

    @Override
    public Object processEvent(final CloudTrailEvent event) {
        List<String> ami = getAmi(event.getEventData().getResponseElements());

        final List<String> whitelistedAmi = Lists.newArrayList();
        whitelistedAmi.add(AMI);

        boolean hasChanged = ami.retainAll(whitelistedAmi);

        if (hasChanged) {
            LOG.info("blublbu ami not valid");
        }

        return null;

    }

    private List<String> getAmi(final String parameters) {
        if (parameters == null) {
            return null;
        }

        return JsonPath.read(parameters, "$.InstancesSet.items[*].imageId");
    }
}
