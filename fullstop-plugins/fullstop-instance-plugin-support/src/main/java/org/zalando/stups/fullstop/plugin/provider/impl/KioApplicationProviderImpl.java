package org.zalando.stups.fullstop.plugin.provider.impl;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.slf4j.Logger;
import org.zalando.stups.clients.kio.Application;
import org.zalando.stups.clients.kio.KioOperations;
import org.zalando.stups.clients.kio.NotFoundException;
import org.zalando.stups.fullstop.plugin.EC2InstanceContext;
import org.zalando.stups.fullstop.plugin.provider.KioApplicationProvider;

import javax.annotation.Nonnull;
import java.util.Optional;

import static java.util.Optional.*;
import static java.util.concurrent.TimeUnit.MINUTES;
import static org.slf4j.LoggerFactory.getLogger;

public class KioApplicationProviderImpl implements KioApplicationProvider {

    private final Logger log = getLogger(getClass());

    private final KioOperations kioOperations;

    public KioApplicationProviderImpl(final KioOperations kioOperations) {
        this.kioOperations = kioOperations;
    }

    private final LoadingCache<EC2InstanceContext, Optional<Application>> cache = CacheBuilder.newBuilder()
            .expireAfterAccess(60, MINUTES)
            .maximumSize(100)
            .build(new CacheLoader<EC2InstanceContext, Optional<Application>>() {
                @Override
                public Optional<Application> load(@Nonnull final EC2InstanceContext context) throws Exception {
                    final Optional<Application> kioApplication = getKioApplication(context);
                    if (!kioApplication.isPresent()) {
                        log.warn("Could not find the application {} in KIO.", context);
                    }
                    return kioApplication;
                }
            });

    private Optional<Application> getKioApplication(@Nonnull final EC2InstanceContext context) {
        try {
            return context.getApplicationId().map(kioOperations::getApplicationById);
        } catch (final NotFoundException ignored) {
            return empty();
        }
    }

    @Override
    public Optional<Application> apply(final EC2InstanceContext context) {
        return cache.getUnchecked(context);
    }

}
