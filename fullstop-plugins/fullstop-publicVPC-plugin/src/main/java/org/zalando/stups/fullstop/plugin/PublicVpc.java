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

import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.cloudtrail.processinglibrary.model.CloudTrailEvent;
import com.amazonaws.services.cloudtrail.processinglibrary.model.CloudTrailEventData;
import com.amazonaws.services.ec2.AmazonEC2Client;
import com.amazonaws.services.ec2.model.DescribeVpcsResult;
import com.amazonaws.services.ec2.model.Vpc;
import com.jayway.jsonpath.JsonPath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.zalando.stups.fullstop.aws.ClientProvider;

import java.util.ArrayList;
import java.util.List;

/**
 * @author mrandi
 */
@Component
public class PublicVpc extends AbstractFullstopPlugin {

    private static final Logger LOG = LoggerFactory.getLogger(PublicVpc.class);

    private static final String EC2_SOURCE_EVENTS = "ec2.amazonaws.com";
    private static final String EVENT_NAME = "RunInstances";

    private final ClientProvider cachingClientProvider;

    @Value("${fullstop.processor.properties.whitelistedAmiAccount}")
    private String whitelistedAmiAccount;

    @Autowired
    public PublicVpc(final ClientProvider cachingClientProvider) {
        this.cachingClientProvider = cachingClientProvider;
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

        String parameters = event.getEventData().getResponseElements();

        List<String> instanceIds = getInstanceId(parameters);
        List<Vpc> vpcs = new ArrayList<Vpc>();

        for (String instanceId : instanceIds) {
            AmazonEC2Client amazonEC2Client = cachingClientProvider.getClient(AmazonEC2Client.class, event.getEventData().getAccountId(), Region.getRegion(Regions.fromName(event.getEventData().getAwsRegion())));
            DescribeVpcsResult describeVpcsResult = amazonEC2Client.describeVpcs();
            vpcs = describeVpcsResult.getVpcs();
        }
        LOG.info("VPCs: " + vpcs.toString());


    }


    private List<String> getInstanceId(final String parameters) {
        if (parameters == null) {
            return null;
        }

        return JsonPath.read(parameters, "$.instancesSet.items[*].instanceId");
    }
}
