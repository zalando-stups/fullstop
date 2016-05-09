package org.zalando.stups.fullstop.plugin.provider.impl;

import com.amazonaws.AmazonClientException;
import com.amazonaws.services.ec2.AmazonEC2Client;
import com.amazonaws.services.ec2.model.DescribeImagesRequest;
import com.amazonaws.services.ec2.model.DescribeImagesResult;
import com.amazonaws.services.ec2.model.Image;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.slf4j.Logger;
import org.zalando.stups.fullstop.plugin.EC2InstanceContext;
import org.zalando.stups.fullstop.plugin.provider.AmiProvider;

import javax.annotation.Nonnull;
import java.util.Optional;

import static java.util.Optional.empty;
import static java.util.concurrent.TimeUnit.MINUTES;
import static org.slf4j.LoggerFactory.getLogger;

public class AmiProviderImpl implements AmiProvider {

    private final Logger log = getLogger(getClass());

    private final LoadingCache<EC2InstanceContext, Optional<Image>> cache = CacheBuilder.newBuilder()
            .expireAfterAccess(1, MINUTES)
            .maximumSize(100)
            .build(new CacheLoader<EC2InstanceContext, Optional<Image>>() {
                @Override
                public Optional<Image> load(@Nonnull final EC2InstanceContext context) throws Exception {
                    final Optional<Image> amiName = getAmi(context);
                    if (!amiName.isPresent()) {
                        log.warn("Could not find the AMI for {}", context);
                    }
                    return amiName;
                }
            });

    private Optional<Image> getAmi(@Nonnull final EC2InstanceContext context) {
        final Optional<String> amiId = context.getAmiId();
        try {
            return amiId
                    .map(id -> context
                            .getClient(AmazonEC2Client.class)
                            .describeImages(new DescribeImagesRequest().withImageIds(id)))
                    .map(DescribeImagesResult::getImages)
                    .flatMap(images -> images.stream().findFirst());
        } catch (final AmazonClientException e) {
            log.warn("Could not get AMI of: " + amiId.get(), e);
            return empty();
        }
    }

    @Override
    public Optional<Image> apply(final EC2InstanceContext context) {
        return cache.getUnchecked(context);
    }
}
