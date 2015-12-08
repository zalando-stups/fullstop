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
