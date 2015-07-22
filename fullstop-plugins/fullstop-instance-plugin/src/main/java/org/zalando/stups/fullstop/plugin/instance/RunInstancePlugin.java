/**
 * Copyright (C) 2015 Zalando SE (http://tech.zalando.com)
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.zalando.stups.fullstop.plugin.instance;

import com.amazonaws.AmazonClientException;
import com.amazonaws.regions.Region;
import com.amazonaws.services.cloudtrail.processinglibrary.model.CloudTrailEvent;
import com.amazonaws.services.ec2.AmazonEC2Client;
import com.amazonaws.services.ec2.model.DescribeSecurityGroupsRequest;
import com.amazonaws.services.ec2.model.DescribeSecurityGroupsResult;
import com.amazonaws.services.ec2.model.IpPermission;
import com.amazonaws.services.ec2.model.SecurityGroup;
import com.google.common.collect.Sets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.zalando.stups.fullstop.aws.ClientProvider;
import org.zalando.stups.fullstop.events.CloudTrailEventPredicate;
import org.zalando.stups.fullstop.plugin.AbstractFullstopPlugin;
import org.zalando.stups.fullstop.violation.ViolationSink;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;

import static java.util.stream.Collectors.toList;
import static org.zalando.stups.fullstop.events.CloudTrailEventPredicate.fromSource;
import static org.zalando.stups.fullstop.events.CloudTrailEventPredicate.withName;
import static org.zalando.stups.fullstop.events.CloudTrailEventSupport.*;
import static org.zalando.stups.fullstop.plugin.instance.Bool.not;
import static org.zalando.stups.fullstop.plugin.instance.IpPermissionPredicates.withToPort;
import static org.zalando.stups.fullstop.violation.ViolationType.SECURITY_GROUPS_PORT_NOT_ALLOWED;

/**
 * @author jbellmann
 */
@Component
public class RunInstancePlugin extends AbstractFullstopPlugin {

    private static final Logger LOG = LoggerFactory.getLogger(RunInstancePlugin.class);

    private static final String EC2_SOURCE_EVENTS = "ec2.amazonaws.com";

    private static final String EVENT_NAME = "RunInstances";

    private final CloudTrailEventPredicate eventFilter = fromSource(EC2_SOURCE_EVENTS).andWith(withName(EVENT_NAME));

    private final ViolationSink violationSink;

    private final ClientProvider clientProvider;

    private final Function<SecurityGroup, String> transformer = new SecurityGroupToString();

    Predicate<IpPermission> filter = withToPort(443).negate().and(withToPort(22).negate());

    @Autowired
    public RunInstancePlugin(final ClientProvider clientProvider, final ViolationSink violationSink) {
        this.clientProvider = clientProvider;
        this.violationSink = violationSink;
    }

    @Override
    public boolean supports(final CloudTrailEvent event) {

        return eventFilter.test(event);
    }

    @Override
    public void processEvent(final CloudTrailEvent event) {

        if (not(hasPublicIp(event))) {

            // no public IP, so skip more checks
            return;
        }

        Optional<List<SecurityGroup>> securityGroupList = getSecurityGroups(event);
        if (not(securityGroupList.isPresent())) {

            // no securityGroups, maybe instance already down
            return;
        }

        if (securityGroupList.get().stream().anyMatch(SecurityGroupPredicates.anyMatch(filter))) {
            violationSink.put(
                    violationFor(event).withType(SECURITY_GROUPS_PORT_NOT_ALLOWED)
                                       .withMetaInfo(getPorts(securityGroupList.get()))
                                       .build());
        }
    }

    protected Set<String> getPorts(final List<SecurityGroup> securityGroups) {

        Set<String> result = Sets.newHashSet();
        for (SecurityGroup sg : securityGroups) {
            List<IpPermission> ipPermissions = sg.getIpPermissions();
            for (IpPermission p : ipPermissions) {
                result.add(p.getToPort().toString());
            }
        }

        return result;
    }

    protected boolean hasPublicIp(final CloudTrailEvent cloudTrailEvent) {
        return !read(cloudTrailEvent, PUBLIC_IP_JSON_PATH, true).isEmpty();
    }

    protected List<String> transformSecurityGroupsIntoStrings(final List<SecurityGroup> securityGroups) {

        return securityGroups.stream().map(transformer).collect(toList());
    }

    protected List<String> readSecurityGroupIds(final CloudTrailEvent cloudTrailEvent) {

        return read(cloudTrailEvent, SECURITY_GROUP_IDS_JSON_PATH, true);
    }

    protected Optional<List<SecurityGroup>> getSecurityGroups(final CloudTrailEvent event) {

        List<String> securityGroupIds = readSecurityGroupIds(event);

        return getSecurityGroupsForIds(securityGroupIds, event);
    }

    protected Optional<List<SecurityGroup>> getSecurityGroupsForIds(final List<String> securityGroupIds,
            final CloudTrailEvent event) {

        Region region = getRegion(event);
        String accountId = getAccountId(event);

        AmazonEC2Client amazonEC2Client = getClient(accountId, region);

        if (amazonEC2Client == null) {
            throw new RuntimeException(
                    String.format(
                            "Somehow we could not create an Client with accountId: %s and region: %s", accountId,
                            region.toString()));
        }
        else {
            try {
                DescribeSecurityGroupsRequest request = new DescribeSecurityGroupsRequest();
                request.setGroupIds(securityGroupIds);

                DescribeSecurityGroupsResult result = amazonEC2Client.describeSecurityGroups(request);

                return Optional.of(result.getSecurityGroups());
            }
            catch (AmazonClientException e) {
                LOG.warn(
                        "Unable to get SecurityGroups for SecurityGroupIds [{}] | {}",
                        securityGroupIds.toString(),
                        e.getMessage());
                return Optional.empty();
            }

        }

    }

    protected AmazonEC2Client getClient(final String accountId, final Region region) {
        return clientProvider.getClient(AmazonEC2Client.class, accountId, region);
    }

}
