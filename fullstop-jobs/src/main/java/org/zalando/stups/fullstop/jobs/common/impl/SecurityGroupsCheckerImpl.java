/**
 * Copyright (C) 2015 Zalando SE (http://tech.zalando.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.zalando.stups.fullstop.jobs.common.impl;

import com.amazonaws.regions.Region;
import com.amazonaws.services.ec2.AmazonEC2Client;
import com.amazonaws.services.ec2.model.DescribeSecurityGroupsRequest;
import com.amazonaws.services.ec2.model.DescribeSecurityGroupsResult;
import com.amazonaws.services.ec2.model.SecurityGroup;
import org.zalando.stups.fullstop.aws.ClientProvider;
import org.zalando.stups.fullstop.jobs.common.SecurityGroupsChecker;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

import static java.util.stream.Collectors.toSet;

/**
 * Created by gkneitschel.
 */
public class SecurityGroupsCheckerImpl implements SecurityGroupsChecker {

    private final ClientProvider clientProvider;
    private final Predicate<? super SecurityGroup> predicate;

    public SecurityGroupsCheckerImpl(ClientProvider clientProvider, Predicate<? super SecurityGroup> predicate) {
        this.clientProvider = clientProvider;
        this.predicate = predicate;
    }

    @Override
    public Set<String> check(Collection<String> groupIds, String account, Region region) {
        DescribeSecurityGroupsRequest describeSecurityGroupsRequest = new DescribeSecurityGroupsRequest();
        describeSecurityGroupsRequest.setGroupIds(groupIds);
        AmazonEC2Client amazonEC2Client = clientProvider.getClient(
                AmazonEC2Client.class,
                account, region);
        DescribeSecurityGroupsResult describeSecurityGroupsResult = amazonEC2Client.describeSecurityGroups(
                describeSecurityGroupsRequest);

        List<SecurityGroup> securityGroups = describeSecurityGroupsResult.getSecurityGroups();
        return securityGroups.stream().filter(predicate).map(SecurityGroup::getGroupId).collect(toSet());
    }
}
