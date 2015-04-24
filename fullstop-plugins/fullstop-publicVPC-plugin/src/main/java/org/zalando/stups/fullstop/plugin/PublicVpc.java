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
import com.amazonaws.services.ec2.model.DescribeInstancesRequest;
import com.amazonaws.services.ec2.model.DescribeInstancesResult;
import com.amazonaws.services.ec2.model.DescribeSubnetsRequest;
import com.amazonaws.services.ec2.model.DescribeSubnetsResult;
import com.amazonaws.services.ec2.model.DescribeVpcsResult;
import com.amazonaws.services.ec2.model.Filter;
import com.amazonaws.services.ec2.model.Instance;
import com.amazonaws.services.ec2.model.Reservation;
import com.amazonaws.services.ec2.model.Vpc;
import com.jayway.jsonpath.JsonPath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.zalando.stups.fullstop.aws.ClientProvider;
import org.zalando.stups.fullstop.events.CloudtrailEventSupport;
import org.zalando.stups.fullstop.violation.ViolationStore;

import java.util.ArrayList;
import java.util.List;

/**
 * @author mrandi
 */
//@Component
public class PublicVpc extends AbstractFullstopPlugin {

    private static final Logger LOG = LoggerFactory.getLogger(PublicVpc.class);

    private static final String EC2_SOURCE_EVENTS = "ec2.amazonaws.com";
    private static final String EVENT_NAME = "RunInstances";

    private final ClientProvider cachingClientProvider;
    private final ViolationStore violationstore;

    @Value("${fullstop.plugin.properties.whitelistedAmiAccount}")
    private String whitelistedAmiAccount;

    @Autowired
    public PublicVpc(final ClientProvider cachingClientProvider, final ViolationStore violationStore) {
        this.cachingClientProvider = cachingClientProvider;
        this.violationstore = violationStore;
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
        DescribeInstancesRequest describeInstancesRequest = new DescribeInstancesRequest();
        List<String> subnetIds = new ArrayList<>();
        List<String> instanceIds = CloudtrailEventSupport.getInstanceIds(event);
       // Filter filter = new Filter("instanceIds", instanceIds);
        AmazonEC2Client amazonEC2Client = cachingClientProvider.getClient(AmazonEC2Client.class, event.getEventData().getAccountId(), Region.getRegion(Regions.fromName(event.getEventData().getAwsRegion())));

        DescribeInstancesResult describeInstancesResult = amazonEC2Client.describeInstances(describeInstancesRequest.withInstanceIds(instanceIds));
        List<Reservation> reservations = describeInstancesResult.getReservations();

        for (Reservation reservation : reservations) {
            List<Instance> instances = reservation.getInstances();
            for (Instance instance : instances) {
                subnetIds.add(instance.getSubnetId());
            }

        }
        DescribeSubnetsRequest describeSubnetsRequest = new DescribeSubnetsRequest().withSubnetIds(subnetIds);
        DescribeSubnetsResult  describeSubnetsResult = amazonEC2Client.describeSubnets(describeSubnetsRequest);



        LOG.info("VPCs: " + describeSubnetsRequest.toString());
       // violationstore.save("");

    }




    /*private List<String> getInstanceId(final String parameters) {
        if (parameters == null) {
            return null;
        }

        return JsonPath.read(parameters, "$.instancesSet.items[*].instanceId");
    }*/
}
