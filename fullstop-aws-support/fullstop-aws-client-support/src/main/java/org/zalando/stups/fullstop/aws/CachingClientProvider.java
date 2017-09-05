package org.zalando.stups.fullstop.aws;

import com.amazonaws.AmazonWebServiceClient;
import com.amazonaws.ClientConfiguration;
import com.amazonaws.auth.STSAssumeRoleSessionCredentialsProvider;
import com.amazonaws.regions.Region;
import com.google.common.base.MoreObjects;
import com.google.common.cache.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Nonnull;
import javax.annotation.PostConstruct;
import java.util.concurrent.TimeUnit;

/**
 * @author jbellmann
 */
@Service
public class CachingClientProvider implements ClientProvider {

    private final Logger logger = LoggerFactory.getLogger(CacheLoader.class);

    private static final String ROLE_SESSION_NAME = "fullstop";

    private static final String ROLE_ARN_FIRST = "arn:aws:iam::";

    private static final String ROLE_ARN_LAST = ":role/fullstop";

    private static final int MAX_ERROR_RETRY = 15;

    private LoadingCache<Key<?>, ? extends AmazonWebServiceClient> cache = null;

    public CachingClientProvider() {
    }

    @Override
    public <T extends AmazonWebServiceClient> T getClient(final Class<T> type, final String accountId, final Region region) {
        @SuppressWarnings("unchecked")
        final Key k = new Key(type, accountId, region);
        return type.cast(cache.getUnchecked(k));
    }

    @PostConstruct
    public void init() {
        // TODO
        // this parameters have to be configurable
        cache = CacheBuilder.newBuilder()
                .maximumSize(500)
                .expireAfterAccess(50, TimeUnit.MINUTES)
                .removalListener((RemovalNotification<Key<?>, AmazonWebServiceClient> notification) -> {
                    logger.debug("Shutting down expired client for key: {}", notification.getKey());
                    notification.getValue().shutdown();
                }).build(new CacheLoader<Key<?>, AmazonWebServiceClient>() {
                    @Override
                    public AmazonWebServiceClient load(@Nonnull final Key<?> key) throws Exception {
                        logger.debug("CacheLoader active for Key : {}", key);
                        return key.region.createClient(
                                key.type,
                                new STSAssumeRoleSessionCredentialsProvider(
                                        buildRoleArn(key.accountId),
                                        ROLE_SESSION_NAME),
                                new ClientConfiguration().withMaxErrorRetry(MAX_ERROR_RETRY));
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

            final Key other = (Key) obj;
            return this.type.equals(other.type) && this.accountId.equals(other.accountId) && this.region.equals(other.region);
        }

        @Override
        public String toString() {
            return MoreObjects.toStringHelper(this).add("type", type.getName()).add("accountId", accountId)
                              .add("region", region.getName()).toString();
        }

    }

}
