package org.zalando.stups.fullstop.violation.repository.impl;

import com.google.common.collect.ImmutableSet;
import org.springframework.data.jpa.repository.support.QueryDslRepositorySupport;
import org.zalando.stups.fullstop.violation.entity.AccountRegion;
import org.zalando.stups.fullstop.violation.entity.ApplicationEntity;
import org.zalando.stups.fullstop.violation.entity.QApplicationEntity;
import org.zalando.stups.fullstop.violation.entity.QLifecycleEntity;
import org.zalando.stups.fullstop.violation.repository.ApplicationRepositoryCustom;

import java.util.Collection;
import java.util.Set;

import static com.mysema.query.types.Projections.constructor;

public class ApplicationRepositoryImpl extends QueryDslRepositorySupport implements ApplicationRepositoryCustom {

    public ApplicationRepositoryImpl() {
        super(ApplicationEntity.class);
    }

    @Override
    public ApplicationEntity findByInstanceIds(final String accountId,
                                               final String region,
                                               final Collection<String> instanceIds) {
        final QApplicationEntity qApp = new QApplicationEntity("a");
        final QLifecycleEntity qLifecycle = new QLifecycleEntity("l");

        return from(qLifecycle)
                .join(qLifecycle.applicationEntity, qApp)
                .where(qLifecycle.accountId.eq(accountId),
                        qLifecycle.region.eq(region),
                        qLifecycle.instanceId.in(instanceIds))
                .groupBy(qApp.id)
                .orderBy(qLifecycle.lastModified.max().desc())
                .limit(1)
                .singleResult(qApp);
    }

    @Override
    public Set<AccountRegion> findDeployments(String applicationId) {
        final QLifecycleEntity qLifecycle = new QLifecycleEntity("l");
        final QApplicationEntity qApplication = new QApplicationEntity("a");

        return ImmutableSet.copyOf(from(qLifecycle)
                .join(qLifecycle.applicationEntity, qApplication)
                .where(qApplication.name.eq(applicationId))
                .distinct()
                .list(constructor(AccountRegion.class, qLifecycle.accountId, qLifecycle.region)));
    }
}
