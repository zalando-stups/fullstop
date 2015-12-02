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
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static java.util.concurrent.TimeUnit.MINUTES;
import static org.slf4j.LoggerFactory.getLogger;

public class KioApprovalProviderImpl implements KioApprovalProvider {

    private final Logger log = getLogger(getClass());

    private final KioOperations kioOperations;

    public KioApprovalProviderImpl(KioOperations kioOperations) {
        this.kioOperations = kioOperations;
    }

    private final LoadingCache<EC2InstanceContext, List<Approval>> cache = CacheBuilder.newBuilder()
            .expireAfterAccess(60, MINUTES)
            .maximumSize(100)
            .build(new CacheLoader<EC2InstanceContext, List<Approval>>() {
                @Override
                public List<Approval> load(@Nonnull EC2InstanceContext context) throws Exception {
                    final List<Approval> kioApproval = getKioApproval(context);
                    if (kioApproval == null && kioApproval.isEmpty()) {
                        log.warn("Could not find the Approval {} in KIO.", context);
                    }
                    return kioApproval;
                }
            });

    private List<Approval> getKioApproval(@Nonnull EC2InstanceContext context) {

        Optional<String> applicationId = context.getApplicationId();
        Optional<Version> applicationVersion = context.getKioVersion();

        if (applicationId.isPresent() && applicationVersion.isPresent()) {
            return kioOperations.getApplicationVersionApprovals(applicationId.get(),applicationVersion.get().getId());
        }

        return Collections.emptyList();
    }

    @Override
    public List<Approval> apply(EC2InstanceContext context) {
        return cache.getUnchecked(context);
    }
}
