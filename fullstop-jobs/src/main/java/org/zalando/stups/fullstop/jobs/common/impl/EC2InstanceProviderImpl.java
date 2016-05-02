package org.zalando.stups.fullstop.jobs.common.impl;

import com.amazonaws.regions.Region;
import com.amazonaws.services.ec2.AmazonEC2Client;
import com.amazonaws.services.ec2.model.DescribeInstancesRequest;
import com.amazonaws.services.ec2.model.Instance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.zalando.stups.fullstop.aws.ClientProvider;
import org.zalando.stups.fullstop.jobs.common.EC2InstanceProvider;

import java.util.Objects;
import java.util.Optional;

@Component
public class EC2InstanceProviderImpl implements EC2InstanceProvider {

    private final ClientProvider clientProvider;

    @Autowired
    public EC2InstanceProviderImpl(ClientProvider clientProvider) {
        this.clientProvider = clientProvider;
    }

    @Override
    public Optional<Instance> getById(String accountId, Region region, String instanceId) {
        return clientProvider.getClient(AmazonEC2Client.class, accountId, region)
                .describeInstances(new DescribeInstancesRequest().withInstanceIds(instanceId))
                .getReservations().stream()
                .flatMap(reservation -> reservation.getInstances().stream())
                .filter(instance -> Objects.equals(instance.getInstanceId(), instanceId))
                .findFirst();
    }
}
