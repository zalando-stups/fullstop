package org.zalando.stups.fullstop.violation.repository.impl;

import org.springframework.data.jpa.repository.support.QueryDslRepositorySupport;
import org.zalando.stups.fullstop.violation.entity.ApplicationEntity;
import org.zalando.stups.fullstop.violation.entity.QApplicationEntity;
import org.zalando.stups.fullstop.violation.entity.QLifecycleEntity;
import org.zalando.stups.fullstop.violation.repository.ApplicationRepositoryCustom;

import java.util.Collection;

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
}
