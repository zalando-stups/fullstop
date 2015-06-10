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
package org.zalando.stups.fullstop.aws;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.stereotype.Service;

import com.amazonaws.AmazonWebServiceClient;

import com.amazonaws.auth.STSAssumeRoleSessionCredentialsProvider;

import com.amazonaws.regions.Region;

import com.google.common.base.MoreObjects;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

/**
 * @author  jbellmann
 */
@Service
public class CachingClientProvider implements ClientProvider {

    private static final String ROLE_SESSION_NAME = "fullstop";

    private static final String ROLE_ARN_FIRST = "arn:aws:iam::";

    private static final String ROLE_ARN_LAST = ":role/fullstop";

    private LoadingCache<Key<?>, Object> cache = null;

    public CachingClientProvider() { }

    @Override
    public <T> T getClient(final Class<T> type, final String accountId, final Region region) {
        try {
            return type.cast(cache.get(new Key(type, accountId, region)));
        } catch (ExecutionException e) {
            throw new RuntimeException("Unable to create client.", e);
        }
    }

    @PostConstruct
    public void init() {

        // TODO
        // this parameters have to be configurable
        cache = CacheBuilder.newBuilder().maximumSize(500).expireAfterWrite(50, TimeUnit.MINUTES).build(
                new CacheLoader<Key<?>, Object>() {
                    private final Logger logger = LoggerFactory.getLogger(CacheLoader.class);

                    @Override
                    public Object load(final Key<?> key) throws Exception {
                        logger.debug("CacheLoader active for Key : {}", key);

                        Object client = key.region.createClient(key.type,
                                new STSAssumeRoleSessionCredentialsProvider(buildRoleArn(key.accountId),
                                    ROLE_SESSION_NAME), null);
                        return client;
                    }
                });
    }

    protected String buildRoleArn(final String accountId) {
        return ROLE_ARN_FIRST + accountId + ROLE_ARN_LAST;
    }

    static final class Key<K extends AmazonWebServiceClient> {
        private final Class<K> type;
        private final String accountId;
        private final Region region;

        Key(final Class<K> type, final String accountId, final Region region) {
            this.type = type;
            this.accountId = accountId;
            this.region = region;
        }

        @Override
        public int hashCode() {
            int hashCode = type.hashCode();
            hashCode = hashCode + accountId.hashCode();
            hashCode = hashCode + region.hashCode();
            return hashCode;
        }

        @Override
        public boolean equals(final Object obj) {
            if (obj == null) {
                return false;
            }

            if (!(obj instanceof Key)) {
                return false;
            }

            final Key<K> other = (Key<K>) obj;
            return this.type.equals(other.type) && this.accountId.equals(other.accountId)
                    && this.region.equals(other.region);
        }

        @Override
        public String toString() {
            return MoreObjects.toStringHelper(this).add("type", type.getName()).add("accountId", accountId)
                              .add("region", region.getName()).toString();
        }

    }

}
