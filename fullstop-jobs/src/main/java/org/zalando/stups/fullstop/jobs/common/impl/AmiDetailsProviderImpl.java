package org.zalando.stups.fullstop.jobs.common.impl;

import com.amazonaws.regions.Region;
import com.amazonaws.services.ec2.AmazonEC2Client;
import com.amazonaws.services.ec2.model.DescribeImagesRequest;
import com.amazonaws.services.ec2.model.DescribeImagesResult;
import com.amazonaws.services.ec2.model.Image;
import com.google.common.collect.ImmutableMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.zalando.stups.fullstop.aws.ClientProvider;
import org.zalando.stups.fullstop.jobs.common.AmiDetailsProvider;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

@Component
public class AmiDetailsProviderImpl implements AmiDetailsProvider {

    private final ClientProvider clientProvider;

    @Autowired
    public AmiDetailsProviderImpl(final ClientProvider clientProvider) {
        this.clientProvider = clientProvider;
    }

    @Override
    public Map<String, String> getAmiDetails(final String accountId, final Region region, final String amiId) {
        final ImmutableMap.Builder<String, String> result = ImmutableMap.builder();
        result.put("ami_id", amiId);

        final AmazonEC2Client ec2 = clientProvider.getClient(AmazonEC2Client.class, accountId, region);
        final Optional<Image> ami = Optional.ofNullable(new DescribeImagesRequest().withImageIds(amiId))
                .map(ec2::describeImages)
                .map(DescribeImagesResult::getImages)
                .map(List::stream)
                .flatMap(Stream::findFirst);

        ami.map(Image::getName).ifPresent(name -> result.put("ami_name", name));
        ami.map(Image::getOwnerId).ifPresent(owner -> result.put("ami_owner_id", owner));
        return result.build();
    }
}
