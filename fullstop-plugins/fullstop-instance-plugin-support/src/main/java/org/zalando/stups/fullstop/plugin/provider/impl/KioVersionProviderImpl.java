package org.zalando.stups.fullstop.plugin.provider.impl;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.slf4j.Logger;
import org.zalando.stups.clients.kio.KioOperations;
import org.zalando.stups.clients.kio.NotFoundException;
import org.zalando.stups.clients.kio.Version;
import org.zalando.stups.fullstop.plugin.EC2InstanceContext;
import org.zalando.stups.fullstop.plugin.provider.KioVersionProvider;

import javax.annotation.Nonnull;
import java.util.Optional;

import static java.util.Optional.empty;
import static java.util.Optional.ofNullable;
import static java.util.concurrent.TimeUnit.MINUTES;
import static org.slf4j.LoggerFactory.getLogger;

public class KioVersionProviderImpl implements KioVersionProvider {

    private final Logger log = getLogger(getClass());

    private final KioOperations kioOperations;

    public KioVersionProviderImpl(KioOperations kioOperations) {
        this.kioOperations = kioOperations;
    }

    private final LoadingCache<EC2InstanceContext, Optional<Version>> cache = CacheBuilder.newBuilder()
            .expireAfterAccess(60, MINUTES)
            .maximumSize(100)
            .build(new CacheLoader<EC2InstanceContext, Optional<Version>>() {
                @Override
                public Optional<Version> load(@Nonnull EC2InstanceContext context) throws Exception {
                    final Optional<Version> kioVersion = getKioVersion(context);
                    if (!kioVersion.isPresent()) {
                        log.warn("Could not find the version {} in KIO.", context);
                    }
                    return kioVersion;
                }
            });

    private Optional<Version> getKioVersion(@Nonnull EC2InstanceContext context) {
        final Optional<String> applicationId = context.getApplicationId();
        final Optional<String> versionId = context.getVersionId();

        if (applicationId.isPresent() && versionId.isPresent()) {
            try {
                return ofNullable(kioOperations.getApplicationVersion(applicationId.get(), versionId.get()));
            } catch (NotFoundException ignored) {
                return empty();
            }
        }

        return empty();
    }

    @Override
    public Optional<Version> apply(EC2InstanceContext context) {
        return cache.getUnchecked(context);
    }
    
}
