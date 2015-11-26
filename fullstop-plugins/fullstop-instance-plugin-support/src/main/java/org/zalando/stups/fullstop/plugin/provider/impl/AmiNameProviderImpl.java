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
import org.zalando.stups.fullstop.plugin.provider.AmiNameProvider;

import javax.annotation.Nonnull;
import java.util.Optional;

import static java.util.Optional.empty;
import static java.util.concurrent.TimeUnit.MINUTES;
import static org.slf4j.LoggerFactory.getLogger;

public class AmiNameProviderImpl implements AmiNameProvider {

    private final Logger log = getLogger(getClass());

    private final LoadingCache<EC2InstanceContext, Optional<String>> cache = CacheBuilder.newBuilder()
            .expireAfterAccess(1, MINUTES)
            .maximumSize(100)
            .build(new CacheLoader<EC2InstanceContext, Optional<String>>() {
                @Override
                public Optional<String> load(@Nonnull EC2InstanceContext context) throws Exception {
                    final Optional<String> amiName = getAmiName(context);
                    if (!amiName.isPresent()) {
                        log.warn("Could not find the AMI name for {}", context);
                    }
                    return amiName;
                }
            });

    private Optional<String> getAmiName(@Nonnull EC2InstanceContext context) {
        final Optional<String> amiId = context.getAmiId();
        try {
            return amiId
                    .map(id -> context
                            .getClient(AmazonEC2Client.class)
                            .describeImages(new DescribeImagesRequest().withImageIds(id)))
                    .map(DescribeImagesResult::getImages)
                    .flatMap(images -> images.stream().findFirst())
                    .map(Image::getName);
        } catch (AmazonClientException e) {
            log.warn("Could not get AMI name of" + amiId.get(), e);
            return empty();
        }
    }

    @Override
    public Optional<String> apply(EC2InstanceContext context) {
        return cache.getUnchecked(context);
    }
}
