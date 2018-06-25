package org.zalando.stups.fullstop.aws;

import com.amazonaws.AmazonWebServiceClient;
import com.amazonaws.ClientConfiguration;
import com.amazonaws.auth.STSAssumeRoleSessionCredentialsProvider;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.regions.Region;
import com.amazonaws.services.securitytoken.AWSSecurityTokenService;
import com.amazonaws.services.securitytoken.AWSSecurityTokenServiceClientBuilder;
import com.google.common.base.MoreObjects;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.cache.RemovalNotification;
import org.slf4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.ReflectionUtils;

import javax.annotation.Nonnull;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * @author jbellmann
 */
@Service
public class CachingClientProvider implements ClientProvider {

    private final Logger log = getLogger(getClass());

    private static final String ROLE_SESSION_NAME = "fullstop";

    private static final String ROLE_ARN_FIRST = "arn:aws:iam::";

    private static final String ROLE_ARN_LAST = ":role/fullstop";

    private static final int MAX_ERROR_RETRY = 15;

    private LoadingCache<Key<?>, Value> cache = null;
    private AWSSecurityTokenService awsSecurityTokenService;

    @Override
    public <T extends AmazonWebServiceClient> T getClient(final Class<T> type, final String accountId, final Region region) {
        final Key<T> k = new Key<>(type, accountId, region);
        return type.cast(cache.getUnchecked(k).client);
    }

    @PostConstruct
    public void init() {
        awsSecurityTokenService = AWSSecurityTokenServiceClientBuilder.defaultClient();
        // TODO this parameters have to be configurable
        cache = CacheBuilder.newBuilder()
                .maximumSize(500)
                .expireAfterAccess(50, TimeUnit.MINUTES)
                .removalListener(this::removalHook)
                .build(createCacheLoader());
    }

    @PreDestroy
    public void tearDown(){
        cache.invalidateAll();
        awsSecurityTokenService.shutdown();
    }

    private CacheLoader<Key<?>, Value> createCacheLoader() {
        return new CacheLoader<Key<?>, Value>() {
            @Override
            public Value load(@Nonnull final Key<?> key) {
                log.debug("CacheLoader active for Key : {}", key);
                final STSAssumeRoleSessionCredentialsProvider tempCredentials = new STSAssumeRoleSessionCredentialsProvider
                        .Builder(buildRoleArn(key.accountId), ROLE_SESSION_NAME).withStsClient(awsSecurityTokenService)
                        .build();

                final String builderName = key.type.getName() + "Builder";
                final Class<?> className = ClassUtils.resolveClassName(builderName, ClassUtils.getDefaultClassLoader());
                final Method method = ClassUtils.getStaticMethod(className, "standard");
                Assert.notNull(method, "Could not find standard() method in class:'" + className.getName() + "'");

                final AwsClientBuilder<?, ?> builder = (AwsClientBuilder<?, ?>) ReflectionUtils.invokeMethod(method, null);
                builder.withCredentials(tempCredentials);
                builder.withRegion(key.region.getName());
                builder.withClientConfiguration(new ClientConfiguration().withMaxErrorRetry(MAX_ERROR_RETRY));
                final AmazonWebServiceClient client = (AmazonWebServiceClient) builder.build();
                return new Value(client, tempCredentials);
            }
        };
    }

    private void removalHook(RemovalNotification<Key<?>, Value> notification) {
        log.debug("Shutting down expired client for key: {}", notification.getKey());
        final Value value = notification.getValue();

        final AmazonWebServiceClient client = value.client;
        client.shutdown();

        final STSAssumeRoleSessionCredentialsProvider tempCredentials = value.tempCredentials;
        tempCredentials.close();
    }

    private String buildRoleArn(final String accountId) {
        return ROLE_ARN_FIRST + accountId + ROLE_ARN_LAST;
    }

    private static final class Key<K extends AmazonWebServiceClient> {
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

    private static final class Value {
        private final AmazonWebServiceClient client;
        private final STSAssumeRoleSessionCredentialsProvider tempCredentials;

        private Value(AmazonWebServiceClient client, STSAssumeRoleSessionCredentialsProvider tempCredentials) {
            this.client = client;
            this.tempCredentials = tempCredentials;
        }
    }

}
