package org.zalando.stups.fullstop.jobs.common.impl;

import com.amazonaws.regions.Region;
import com.amazonaws.services.ec2.AmazonEC2Client;
import com.amazonaws.services.ec2.model.DescribeSecurityGroupsRequest;
import com.amazonaws.services.ec2.model.DescribeSecurityGroupsResult;
import com.amazonaws.services.ec2.model.IpPermission;
import com.amazonaws.services.ec2.model.SecurityGroup;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.zalando.stups.fullstop.aws.ClientProvider;
import org.zalando.stups.fullstop.jobs.common.SecurityGroupCheckDetails;
import org.zalando.stups.fullstop.jobs.common.SecurityGroupsChecker;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import static java.util.stream.Collectors.toList;

public class SecurityGroupsCheckerImpl implements SecurityGroupsChecker {

    private final ClientProvider clientProvider;
    private final Predicate<? super IpPermission> isOffending;

    public SecurityGroupsCheckerImpl(final ClientProvider clientProvider, Predicate<? super IpPermission> isOffending) {
        this.clientProvider = clientProvider;
        this.isOffending = isOffending;
    }

    @Override
    public Map<String, SecurityGroupCheckDetails> check(final Collection<String> groupIds, final String account, final Region region) {
        final DescribeSecurityGroupsRequest describeSecurityGroupsRequest = new DescribeSecurityGroupsRequest();
        describeSecurityGroupsRequest.setGroupIds(groupIds);
        final AmazonEC2Client amazonEC2Client = clientProvider.getClient(
                AmazonEC2Client.class,
                account, region);
        final DescribeSecurityGroupsResult describeSecurityGroupsResult = amazonEC2Client.describeSecurityGroups(
                describeSecurityGroupsRequest);


        final ImmutableMap.Builder<String, SecurityGroupCheckDetails> result = ImmutableMap.builder();

        for (final SecurityGroup securityGroup : describeSecurityGroupsResult.getSecurityGroups()) {
            final List<String> offendingRules = securityGroup.getIpPermissions().stream()
                    .filter(isOffending)
                    .map(Object::toString)
                    .collect(toList());
            if (!offendingRules.isEmpty()) {
                final SecurityGroupCheckDetails details = new SecurityGroupCheckDetails(
                        securityGroup.getGroupName(), ImmutableList.copyOf(offendingRules));
                result.put(securityGroup.getGroupId(), details);
            }
        }
        return result.build();
    }
}
