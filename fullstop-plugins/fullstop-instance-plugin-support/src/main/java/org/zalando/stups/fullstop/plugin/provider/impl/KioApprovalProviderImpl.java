package org.zalando.stups.fullstop.plugin.provider.impl;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.slf4j.Logger;
import org.zalando.stups.clients.kio.Approval;
import org.zalando.stups.clients.kio.KioOperations;
import org.zalando.stups.clients.kio.Version;
import org.zalando.stups.fullstop.plugin.EC2InstanceContext;
import org.zalando.stups.fullstop.plugin.provider.KioApprovalProvider;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Optional;

import static java.util.Optional.ofNullable;
import static java.util.concurrent.TimeUnit.MINUTES;
import static org.slf4j.LoggerFactory.getLogger;

public class KioApprovalProviderImpl implements KioApprovalProvider {

    private final Logger log = getLogger(getClass());

    private final KioOperations kioOperations;

    public KioApprovalProviderImpl(KioOperations kioOperations) {
        this.kioOperations = kioOperations;
    }

    private final LoadingCache<EC2InstanceContext, Optional<List<Approval>>> cache = CacheBuilder.newBuilder()
            .expireAfterAccess(60, MINUTES)
            .maximumSize(100)
            .build(new CacheLoader<EC2InstanceContext, Optional<List<Approval>>>() {
                @Override
                public Optional<List<Approval>> load(@Nonnull EC2InstanceContext context) throws Exception {
                    final Optional<List<Approval>> kioApproval = getKioApproval(context);
                    if (!kioApproval.isPresent()) {
                        log.warn("Could not find the Approval {} in KIO.", context);
                    }
                    return kioApproval;
                }
            });

    private Optional<List<Approval>> getKioApproval(@Nonnull EC2InstanceContext context) {

        Optional<String> applicationId = context.getApplicationId();
        Optional<Version> applicationVersion = context.getKioVersion();

        if (applicationId.isPresent() && applicationVersion.isPresent()) {
            return ofNullable(kioOperations.getApplicationVersionApprovals(applicationId.get(),applicationVersion.get().getId()));
        }

        return Optional.empty();
    }

    @Override
    public Optional<List<Approval>> apply(EC2InstanceContext context) {
        return cache.getUnchecked(context);
    }
}
