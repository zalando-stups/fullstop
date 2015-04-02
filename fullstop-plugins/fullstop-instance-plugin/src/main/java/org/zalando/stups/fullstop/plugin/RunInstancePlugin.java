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
import com.amazonaws.services.ec2.model.DescribeSecurityGroupsRequest;
import com.amazonaws.services.ec2.model.DescribeSecurityGroupsResult;
import com.amazonaws.services.ec2.model.SecurityGroup;
import com.jayway.jsonpath.JsonPath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.zalando.stups.fullstop.aws.ClientProvider;

import java.util.ArrayList;
import java.util.List;

/**
 * This plugin only handles EC-2 Events where name of event starts with "Delete".
 *
 * @author jbellmann
 */
@Component
public class RunInstancePlugin implements FullstopPlugin {

    private static final Logger LOG = LoggerFactory.getLogger(RunInstancePlugin.class);

    private static final String EC2_SOURCE_EVENTS = "ec2.amazonaws.com";
    private static final String EVENT_NAME = "RunInstances";

    private final ClientProvider clientProvider;

    @Autowired
    public RunInstancePlugin(final ClientProvider clientProvider) {
        this.clientProvider = clientProvider;
    }

    @Override
    public boolean supports(final CloudTrailEvent event) {
        CloudTrailEventData eventData = event.getEventData();

        String eventSource = eventData.getEventSource();
        String eventName = eventData.getEventName();

        return eventSource.equals(EC2_SOURCE_EVENTS) && eventName.equals(EVENT_NAME);
    }

    @Override
    public Object processEvent(final CloudTrailEvent event) {

        String parameters = event.getEventData().getResponseElements();

        List<String> securityGroupId = getSecuritygroup(parameters);
        AmazonEC2Client client = clientProvider.getEC2Client(event.getEventData().getUserIdentity().getAccountId(),
                Region.getRegion(Regions.fromName(event.getEventData().getAwsRegion())));
        List<String> securityRules = getSecuritySettings(securityGroupId, client);


        for (int i = 0; i < securityGroupId.size(); i++) {
            LOG.info("SAVING RESULT INTO MAGIC DB: {}", securityGroupId.get(i));
        }


        return null;
    }

    private List<String> getSecuritygroup(final String parameters) {

        if (parameters == null) {
            return null; // autoscaling events return parameter as null
        }

        return JsonPath.read(parameters, "$.instancesSet.items[*].networkInterfaceSet.items[*].groupSet.items[*].groupId");
    }

    private List<String> getSecuritySettings(final List<String> securityGroupId, final AmazonEC2Client amazonEC2Client) {
        DescribeSecurityGroupsRequest request = new DescribeSecurityGroupsRequest();
        request.setGroupIds(securityGroupId);

        DescribeSecurityGroupsResult result = amazonEC2Client.describeSecurityGroups(request);

        List<SecurityGroup> securityGroups = result.getSecurityGroups();

        List<String> securityRules = new ArrayList<>();
        for (SecurityGroup securityGroup : securityGroups) {
            securityRules.add(securityGroup.toString());
        }

        return securityRules;
    }

}
