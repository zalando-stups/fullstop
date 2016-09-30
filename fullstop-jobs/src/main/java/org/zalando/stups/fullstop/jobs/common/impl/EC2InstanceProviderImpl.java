package org.zalando.stups.fullstop.jobs.common.impl;

import com.amazonaws.regions.Region;
import com.amazonaws.services.ec2.AmazonEC2Client;
import com.amazonaws.services.ec2.model.DescribeInstancesRequest;
import com.amazonaws.services.ec2.model.Instance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;
import org.zalando.stups.fullstop.aws.AwsRequestUtil;
import org.zalando.stups.fullstop.aws.ClientProvider;
import org.zalando.stups.fullstop.jobs.common.EC2InstanceProvider;

import java.util.Objects;
import java.util.Optional;

@Component
public class EC2InstanceProviderImpl implements EC2InstanceProvider {

    private final ClientProvider clientProvider;

    @Autowired
    public EC2InstanceProviderImpl(final ClientProvider clientProvider) {
        this.clientProvider = clientProvider;
    }

    @Override
    @Cacheable(cacheNames = "ec2-instance", cacheManager = "twoHoursTTLCacheManager")
    public Optional<Instance> getById(final String accountId, final Region region, final String instanceId) {
        final AmazonEC2Client ec2Client = clientProvider.getClient(AmazonEC2Client.class, accountId, region);
        final DescribeInstancesRequest request = new DescribeInstancesRequest().withInstanceIds(instanceId);
        return AwsRequestUtil.performRequest(() -> ec2Client.describeInstances(request))
                .getReservations().stream()
                .flatMap(reservation -> reservation.getInstances().stream())
                .filter(instance -> Objects.equals(instance.getInstanceId(), instanceId))
                .findFirst();
    }
}
