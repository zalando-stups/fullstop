package org.zalando.stups.fullstop.jobs.common.impl;

import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.ec2.AmazonEC2Client;
import com.amazonaws.services.ec2.model.DescribeTagsRequest;
import com.amazonaws.services.ec2.model.Filter;
import com.amazonaws.services.ec2.model.TagDescription;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;
import org.zalando.stups.fullstop.aws.ClientProvider;
import org.zalando.stups.fullstop.jobs.common.TaupageExpirationTimeProvider;

import java.time.ZonedDateTime;

import static java.time.format.DateTimeFormatter.ISO_DATE_TIME;

@Component
public class TaupageExpirationTimeProviderImpl implements TaupageExpirationTimeProvider {

    static final String TAG_KEY = "ExpirationTime";

    private final ClientProvider clientProvider;

    public TaupageExpirationTimeProviderImpl(ClientProvider clientProvider) {
        this.clientProvider = clientProvider;
    }

    @Override
    @Cacheable(cacheNames = "ami-expiration-time", cacheManager = "oneMinuteTTLCacheManager")
    public ZonedDateTime getExpirationTime(String regionName, String imageOwner, String imageId) {
        // tags are only visible in the owning account of the image
        final AmazonEC2Client ec2 = clientProvider.getClient(AmazonEC2Client.class, imageOwner,
                Region.getRegion(Regions.fromName(regionName)));
        final DescribeTagsRequest tagsRequest = new DescribeTagsRequest().withFilters(
                new Filter("resource-id").withValues(imageId),
                new Filter("resource-type").withValues("image"),
                new Filter("key").withValues(TAG_KEY));
        return ec2.describeTags(tagsRequest).getTags().stream()
                .findFirst()
                .map(TagDescription::getValue)
                .map(value -> ZonedDateTime.parse(value, ISO_DATE_TIME))
                .orElse(null);
    }
}
