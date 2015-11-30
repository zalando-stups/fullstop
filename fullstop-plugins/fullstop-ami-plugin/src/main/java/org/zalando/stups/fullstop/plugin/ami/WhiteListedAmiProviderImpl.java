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
package org.zalando.stups.fullstop.plugin.ami;

import com.amazonaws.AmazonClientException;
import com.amazonaws.regions.Region;
import com.amazonaws.services.ec2.AmazonEC2Client;
import com.amazonaws.services.ec2.model.DescribeImagesRequest;
import com.amazonaws.services.ec2.model.Image;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.slf4j.Logger;
import org.zalando.stups.fullstop.aws.ClientProvider;
import org.zalando.stups.fullstop.plugin.EC2InstanceContext;

import javax.annotation.Nonnull;
import java.util.Objects;
import java.util.Set;

import static java.lang.String.format;
import static java.util.Collections.emptySet;
import static java.util.concurrent.TimeUnit.MINUTES;
import static java.util.stream.Collectors.toSet;
import static org.slf4j.LoggerFactory.getLogger;

public class WhiteListedAmiProviderImpl implements WhiteListedAmiProvider {

    private final Logger log = getLogger(getClass());

    private final String amiNameStartWith;
    private final String whitelistedAmiAccount;
    private final LoadingCache<CacheKey, Set<String>> cache;
    private final ClientProvider awsClients;

    public WhiteListedAmiProviderImpl(final String amiNameStartWith,
                                      final String whitelistedAmiAccount,
                                      final ClientProvider clientProvider) {
        this.amiNameStartWith = amiNameStartWith;
        this.whitelistedAmiAccount = whitelistedAmiAccount;
        this.awsClients = clientProvider;
        this.cache = CacheBuilder.newBuilder()
                .expireAfterAccess(30, MINUTES)
                .maximumSize(100)
                .build(new CacheLoader<CacheKey, Set<String>>() {
                    @Override
                    public Set<String> load(@Nonnull CacheKey key) throws Exception {
                        final Set<String> result = loadWhiteListedAmis(key);
                        if (result.isEmpty()) {
                            log.warn("No white-listed AMIs found: Owner {}, prefix {}", whitelistedAmiAccount, amiNameStartWith);
                        }

                        return result;
                    }
                });
    }

    @Override
    public Set<String> apply(EC2InstanceContext context) {
        return cache.getUnchecked(key(context));
    }

    private Set<String> loadWhiteListedAmis(CacheKey key) {
        try {
            return awsClients.getClient(AmazonEC2Client.class, key.getAccountId(), key.getRegion())
                    .describeImages(new DescribeImagesRequest().withOwners(whitelistedAmiAccount))
                    .getImages().stream()
                    .filter(image -> image.getName().startsWith(amiNameStartWith))
                    .map(Image::getImageId)
                    .collect(toSet());
        } catch (AmazonClientException e) {
            log.warn(format("Could not list AMIs for owner %s", whitelistedAmiAccount), e);
            return emptySet();
        }
    }

    private static CacheKey key(EC2InstanceContext context) {
        return new CacheKey(context.getAccountId(), context.getRegion());
    }

    private static class CacheKey {
        private final String accountId;
        private final Region region;

        private CacheKey(String accountId, Region region) {
            this.accountId = accountId;
            this.region = region;
        }

        public String getAccountId() {
            return accountId;
        }

        public Region getRegion() {
            return region;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            CacheKey cacheKey = (CacheKey) o;
            return Objects.equals(getAccountId(), cacheKey.getAccountId()) &&
                    Objects.equals(getRegion(), cacheKey.getRegion());
        }

        @Override
        public int hashCode() {
            return Objects.hash(getAccountId(), getRegion());
        }
    }
}
