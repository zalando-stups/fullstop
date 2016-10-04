package org.zalando.stups.fullstop.plugin.provider.impl;

import com.amazonaws.AmazonClientException;
import com.amazonaws.services.ec2.AmazonEC2Client;
import com.amazonaws.services.ec2.model.DescribeInstancesRequest;
import com.amazonaws.services.ec2.model.Instance;
import com.amazonaws.services.ec2.model.Reservation;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.JsonPathException;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.zalando.stups.fullstop.plugin.EC2InstanceContext;
import org.zalando.stups.fullstop.plugin.provider.AmiIdProvider;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.Optional;

import static java.util.Optional.empty;
import static java.util.concurrent.TimeUnit.MINUTES;
import static org.slf4j.LoggerFactory.getLogger;

public class AmiIdProviderImpl implements AmiIdProvider {

    private static final String IMAGE_ID_JSON_PATH = "$.imageId";

    private final Logger log = getLogger(getClass());

    private final LoadingCache<EC2InstanceContext, Optional<String>> cache = CacheBuilder.newBuilder()
            .expireAfterAccess(1, MINUTES)
            .maximumSize(100)
            .build(new CacheLoader<EC2InstanceContext, Optional<String>>() {
                @Override
                public Optional<String> load(@Nonnull final EC2InstanceContext context) throws Exception {
                    final Optional<String> amiId = getAmiId(context).map(StringUtils::trimToNull);
                    if (!amiId.isPresent()) {
                        log.warn("No AMI id found for {}", context);
                    }
                    return amiId;
                }
            });

    @Override
    public Optional<String> apply(final EC2InstanceContext context) {
        return cache.getUnchecked(context);
    }

    private Optional<String> getAmiId(@Nonnull final EC2InstanceContext context) {
        final Optional<String> amiId = readAmiIdFromJson(context);
        if (amiId.isPresent()) {
            return amiId;
        } else {
            return getAmiIdFromEC2Api(context);
        }
    }

    private Optional<String> readAmiIdFromJson(final EC2InstanceContext context) {
        try {
            return Optional.ofNullable(JsonPath.read(context.getInstanceJson(), IMAGE_ID_JSON_PATH));
        } catch (final JsonPathException ignored) {
            return empty();
        }
    }

    private Optional<String> getAmiIdFromEC2Api(final EC2InstanceContext context) {
        final String instanceId = context.getInstanceId();
        try {
            return context.getClient(AmazonEC2Client.class)
                    .describeInstances(new DescribeInstancesRequest().withInstanceIds(instanceId))
                    .getReservations()
                    .stream()
                    .map(Reservation::getInstances)
                    .flatMap(Collection::stream)
                    .filter(i -> i.getInstanceId().equals(instanceId))
                    .map(Instance::getImageId)
                    .findFirst();
        } catch (final AmazonClientException e) {
            log.warn("Could not describe instance " + instanceId, e);
            return empty();
        }
    }
}

