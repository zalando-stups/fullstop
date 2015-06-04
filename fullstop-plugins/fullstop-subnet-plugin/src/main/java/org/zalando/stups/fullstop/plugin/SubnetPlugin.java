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

import static com.google.common.collect.Lists.newArrayList;
import static java.lang.String.format;
import static org.zalando.stups.fullstop.events.CloudtrailEventSupport.getInstanceIds;

import java.util.List;
import java.util.stream.Collectors;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.cloudtrail.processinglibrary.model.CloudTrailEvent;
import com.amazonaws.services.cloudtrail.processinglibrary.model.CloudTrailEventData;
import com.amazonaws.services.ec2.AmazonEC2Client;
import com.amazonaws.services.ec2.model.DescribeInstancesRequest;
import com.amazonaws.services.ec2.model.DescribeInstancesResult;
import com.amazonaws.services.ec2.model.DescribeRouteTablesRequest;
import com.amazonaws.services.ec2.model.DescribeRouteTablesResult;
import com.amazonaws.services.ec2.model.Filter;
import com.amazonaws.services.ec2.model.Instance;
import com.amazonaws.services.ec2.model.Reservation;
import com.amazonaws.services.ec2.model.Route;
import com.amazonaws.services.ec2.model.RouteTable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.zalando.stups.fullstop.aws.ClientProvider;
import org.zalando.stups.fullstop.violation.ViolationStore;
import org.zalando.stups.fullstop.violation.entity.ViolationBuilder;

/**
 * @author mrandi
 */
@Component
public class SubnetPlugin extends AbstractFullstopPlugin {

    private static final Logger LOG = LoggerFactory.getLogger(SubnetPlugin.class);

    private static final String EC2_SOURCE_EVENTS = "ec2.amazonaws.com";
    private static final String EVENT_NAME = "RunInstances";

    private final ClientProvider cachingClientProvider;
    private final ViolationStore violationStore;


    @Autowired
    public SubnetPlugin(final ClientProvider cachingClientProvider, final ViolationStore violationStore) {
        this.cachingClientProvider = cachingClientProvider;
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
        List<String> subnetIds = newArrayList();
        List<Filter> SubnetIdFilters = newArrayList();
        DescribeInstancesRequest describeInstancesRequest = new DescribeInstancesRequest();
        List<String> instanceIds = getInstanceIds(event);
        AmazonEC2Client amazonEC2Client = cachingClientProvider.getClient(AmazonEC2Client.class, event.getEventData().getAccountId(),
                Region.getRegion(Regions.fromName(event.getEventData().getAwsRegion())));

        DescribeInstancesResult describeInstancesResult = null;
        try {
            describeInstancesResult = amazonEC2Client.describeInstances(describeInstancesRequest.withInstanceIds(instanceIds));
        } catch (AmazonServiceException e){
            violationStore.save(
                    new ViolationBuilder(e.getMessage()).withEventId(getCloudTrailEventId(event))
                    .withRegion(getCloudTrailEventRegion(event))
                    .withAccoundId(getCloudTrailEventAccountId(event))
                    .build());
            return;
        }


        List<Reservation> reservations = describeInstancesResult.getReservations();
        for (Reservation reservation : reservations) {
            List<Instance> instances = reservation.getInstances();
            subnetIds.addAll(instances.stream().map(Instance::getSubnetId).collect(Collectors.toList()));

        }

        SubnetIdFilters.add(new Filter().withName("association.subnet-id").withValues(subnetIds)); // filter by subnetId
        DescribeRouteTablesRequest describeRouteTablesRequest = new DescribeRouteTablesRequest().withFilters(SubnetIdFilters);
        DescribeRouteTablesResult describeRouteTablesResult = amazonEC2Client.describeRouteTables(describeRouteTablesRequest);
        List<RouteTable> routeTables = describeRouteTablesResult.getRouteTables();
        if (routeTables == null || routeTables.size() == 0) {
            violationStore.save(
                    new ViolationBuilder(format("Instances %s have no routing information associated", instanceIds.toString())).
                    withEventId(getCloudTrailEventId(event))
                    .withRegion(getCloudTrailEventRegion(event))
                    .withAccoundId(getCloudTrailEventAccountId(event))
                    .build());
            return;
        }
        for (RouteTable routeTable : routeTables) {
            List<Route> routes = routeTable.getRoutes();
            routes.stream().filter(route -> route.getState().equals("active") && route.getNetworkInterfaceId() != null &&
                    !route.getNetworkInterfaceId().startsWith("eni")).forEach(route -> violationStore.save(

                    new ViolationBuilder(format("ROUTES: instance %s is running in a public subnet %s",
                            route.getInstanceId(), route.getNetworkInterfaceId())).
                            withEventId(getCloudTrailEventId(event))
                            .withRegion(getCloudTrailEventRegion(event))
                            .withAccoundId(getCloudTrailEventAccountId(event))
                            .build()));
        }

    }

}
