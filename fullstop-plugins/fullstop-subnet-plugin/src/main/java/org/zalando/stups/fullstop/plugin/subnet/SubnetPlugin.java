package org.zalando.stups.fullstop.plugin.subnet;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.cloudtrail.processinglibrary.model.CloudTrailEvent;
import com.amazonaws.services.cloudtrail.processinglibrary.model.CloudTrailEventData;
import com.amazonaws.services.ec2.AmazonEC2Client;
import com.amazonaws.services.ec2.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.zalando.stups.fullstop.aws.ClientProvider;
import org.zalando.stups.fullstop.plugin.AbstractFullstopPlugin;
import org.zalando.stups.fullstop.violation.ViolationSink;

import java.util.List;

import static com.google.common.collect.Lists.newArrayList;
import static org.zalando.stups.fullstop.events.CloudTrailEventSupport.getInstanceIds;
import static org.zalando.stups.fullstop.events.CloudTrailEventSupport.violationFor;
import static org.zalando.stups.fullstop.violation.ViolationType.EC2_RUN_IN_PUBLIC_SUBNET;

// @Component
// TODO this code does not work at all.
// It is not possible to get a subnet's routing tables using the filter "association.subnet-id", when the subnet is not
// explicitly associated with the routing table. This is the case for our DMZ subnets / routing tables.
// I'll leave the plugin deactivated, until we have working solution.
public class SubnetPlugin extends AbstractFullstopPlugin {

    private static final Logger LOG = LoggerFactory.getLogger(SubnetPlugin.class);

    private static final String EC2_SOURCE_EVENTS = "ec2.amazonaws.com";

    private static final String EVENT_NAME = "RunInstances";

    private final ClientProvider cachingClientProvider;

    private final ViolationSink violationSink;

    @Autowired
    public SubnetPlugin(final ClientProvider cachingClientProvider, final ViolationSink violationSink) {
        this.cachingClientProvider = cachingClientProvider;
        this.violationSink = violationSink;
    }

    @Override
    public boolean supports(final CloudTrailEvent event) {
        final CloudTrailEventData cloudTrailEventData = event.getEventData();
        final String eventSource = cloudTrailEventData.getEventSource();
        final String eventName = cloudTrailEventData.getEventName();

        return eventSource.equals(EC2_SOURCE_EVENTS) && eventName.equals(EVENT_NAME);
    }

    @Override
    public void processEvent(final CloudTrailEvent event) {
        final List<Filter> SubnetIdFilters = newArrayList();
        final List<String> instanceIds = getInstanceIds(event);
        final AmazonEC2Client amazonEC2Client = cachingClientProvider
                .getClient(
                        AmazonEC2Client.class, event.getEventData().getAccountId(),
                        Region.getRegion(Regions.fromName(event.getEventData().getAwsRegion())));


        final List<Reservation> reservations = fetchReservations(amazonEC2Client ,event, instanceIds);

        if (reservations == null || reservations.isEmpty()) {
            return;
        }

        for (final Reservation reservation : reservations) {
            final List<Instance> instances = reservation.getInstances();
            for (final Instance instance : instances) {

                final List<RouteTable> routeTables = fetchRouteTables(SubnetIdFilters, amazonEC2Client, instance);

                if (routeTables == null || routeTables.size() == 0) {
                    LOG.warn(
                            "Not Routetable found in: {} for ids: {}",
                            SubnetPlugin.class.getName(),
                            instance.getSubnetId());

                    return;
                }

                for (final RouteTable routeTable : routeTables) {
                    final List<Route> routes = routeTable.getRoutes();
                    routes.stream()
                          .filter(
                                  route -> route.getState().equals("active") && route.getNetworkInterfaceId() != null &&
                                          !route.getNetworkInterfaceId().startsWith("eni")).forEach(
                            route ->
                                    violationFor(event).withInstanceId(instance.getInstanceId())
                                                       .withPluginFullyQualifiedClassName(
                                                               SubnetPlugin.class)
                                                       .withType(EC2_RUN_IN_PUBLIC_SUBNET)
                                                       .withMetaInfo(
                                                               newArrayList(
                                                                       route.getInstanceId(),
                                                                       route.getNetworkInterfaceId()))
                                                       .build());
                }

            }
        }
    }

    private List<Reservation> fetchReservations(final AmazonEC2Client amazonEC2Client, final CloudTrailEvent event, final List<String> instanceIds){
        final DescribeInstancesRequest describeInstancesRequest = new DescribeInstancesRequest();


        DescribeInstancesResult describeInstancesResult = null;
        try {
            describeInstancesResult = amazonEC2Client
                    .describeInstances(describeInstancesRequest.withInstanceIds(instanceIds));
        }
        catch (final AmazonServiceException e) {

            LOG.warn("Subnet plugin: {}", e.getErrorMessage());
            return null;
        }

        return describeInstancesResult.getReservations();

    }

    private List<RouteTable> fetchRouteTables(final List<Filter> subnetIdFilters, final AmazonEC2Client amazonEC2Client,
                                              final Instance instance) {
        subnetIdFilters.add(
                new Filter().withName("association.subnet-id")
                            .withValues(instance.getSubnetId())); // filter by subnetId
        final DescribeRouteTablesRequest describeRouteTablesRequest = new DescribeRouteTablesRequest()
                .withFilters(subnetIdFilters);

        final DescribeRouteTablesResult describeRouteTablesResult = amazonEC2Client
                .describeRouteTables(describeRouteTablesRequest);
        return describeRouteTablesResult.getRouteTables();
    }

}
