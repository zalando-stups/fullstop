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

import static java.util.stream.Collectors.toList;

import static org.zalando.stups.fullstop.events.CloudTrailEventPredicate.fromSource;
import static org.zalando.stups.fullstop.events.CloudTrailEventPredicate.withName;
import static org.zalando.stups.fullstop.events.CloudtrailEventSupport.getAccountId;
import static org.zalando.stups.fullstop.events.CloudtrailEventSupport.getRegion;
import static org.zalando.stups.fullstop.events.CloudtrailEventSupport.read;

import java.util.List;
import java.util.function.Function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.stereotype.Component;

import org.zalando.stups.fullstop.aws.ClientProvider;
import org.zalando.stups.fullstop.events.CloudTrailEventPredicate;
import org.zalando.stups.fullstop.violation.Violation;
import org.zalando.stups.fullstop.violation.ViolationStore;

import com.amazonaws.AmazonClientException;

import com.amazonaws.regions.Region;

import com.amazonaws.services.cloudtrail.processinglibrary.model.CloudTrailEvent;
import com.amazonaws.services.ec2.AmazonEC2Client;
import com.amazonaws.services.ec2.model.DescribeSecurityGroupsRequest;
import com.amazonaws.services.ec2.model.DescribeSecurityGroupsResult;
import com.amazonaws.services.ec2.model.SecurityGroup;

import com.google.common.collect.Lists;

/**
 * This plugin only handles EC-2 Events where name of event starts with "Delete".
 *
 * @author  jbellmann
 */
@Component
public class RunInstancePlugin extends AbstractFullstopPlugin {

    private static final Logger LOG = LoggerFactory.getLogger(RunInstancePlugin.class);

    private static final String EC2_SOURCE_EVENTS = "ec2.amazonaws.com";
    private static final String EVENT_NAME = "RunInstances";

    private final CloudTrailEventPredicate eventFilter = fromSource(EC2_SOURCE_EVENTS).andWith(withName(EVENT_NAME));

    private final ViolationStore violationStore;

    private final ClientProvider clientProvider;

    private final Function<SecurityGroup, String> transformer = new SecurityGroupToString();

    @Autowired
    public RunInstancePlugin(final ClientProvider clientProvider, final ViolationStore violationStore) {
        this.clientProvider = clientProvider;
        this.violationStore = violationStore;
    }

    @Override
    public boolean supports(final CloudTrailEvent event) {

        return eventFilter.test(event);
    }

    @Override
    public void processEvent(final CloudTrailEvent event) {

        List<String> securityRules = transformSecurityGroupsIntoStrings(getSecurityGroups(event));

        for (String securityRule : securityRules) {
            String message = String.format("SAVING RESULT INTO MAGIC DB: %s", securityRule);
            LOG.info("WHAT TO DO WITH THIS RULES : {}", message);
// violationStore.save(message);
        }
    }

    protected List<String> transformSecurityGroupsIntoStrings(final List<SecurityGroup> securityGroups) {

        return securityGroups.stream().map(transformer).collect(toList());
    }

    protected List<String> readSecurityGroupIds(final CloudTrailEvent cloudTrailEvent) {

        return read(cloudTrailEvent.getEventData().getResponseElements(),
                "$.instancesSet.items[*].networkInterfaceSet.items[*].groupSet.items[*].groupId", true);
    }

    protected List<SecurityGroup> getSecurityGroups(final CloudTrailEvent event) {

        List<String> securityGroupIds = readSecurityGroupIds(event);

        return getSecurityGroupsForIds(securityGroupIds, event);
    }

    protected List<SecurityGroup> getSecurityGroupsForIds(final List<String> securityGroupIds,
            final CloudTrailEvent event) {

        Region region = getRegion(event);
        String accountId = getAccountId(event);

        AmazonEC2Client amazonEC2Client = getClient(accountId, region);

        if (amazonEC2Client == null) {
            LOG.error("Somehow we could not create an Client with accountId: {} and region: {}", accountId,
                region.toString());
            return Lists.newArrayList();
        } else {
            try {
                DescribeSecurityGroupsRequest request = new DescribeSecurityGroupsRequest();
                request.setGroupIds(securityGroupIds);

                DescribeSecurityGroupsResult result = amazonEC2Client.describeSecurityGroups(request);

                return result.getSecurityGroups();
            } catch (AmazonClientException e) {
                // TODO, better ways?
// LOG.error(e.getMessage(), e);
// throw new RuntimeException(e.getMessage(), e);

                String message = String.format("Unable to get SecurityGroups for SecurityGroupIds [%s] | %s",
                        securityGroupIds.toString(), e.getMessage());
                Violation v = new Violation(accountId, region.getName(), message);
                violationStore.save(v);
                return Lists.newArrayList();
            }

        }

    }

    protected AmazonEC2Client getClient(final String accountId, final Region region) {
        return clientProvider.getClient(AmazonEC2Client.class, accountId, region);
    }

}
