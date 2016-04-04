package org.zalando.stups.fullstop.violation.repository.impl;

import org.springframework.data.jpa.repository.support.QueryDslRepositorySupport;
import org.zalando.stups.fullstop.violation.entity.LifecycleEntity;
import org.zalando.stups.fullstop.violation.entity.QApplicationEntity;
import org.zalando.stups.fullstop.violation.entity.QLifecycleEntity;
import org.zalando.stups.fullstop.violation.repository.LifecycleRepositoryCustom;

import java.util.List;

public class LifecycleRepositoryImpl extends QueryDslRepositorySupport implements LifecycleRepositoryCustom {


    public LifecycleRepositoryImpl() {
        super(LifecycleEntity.class);
    }

    @Override
    public List<LifecycleEntity> findByApplicationName(String name) {
        final QLifecycleEntity qLifecycleEntity = new QLifecycleEntity("l");
        final QApplicationEntity qApplicationEntity = new QApplicationEntity("a");


        return from(qLifecycleEntity)
                .join(qLifecycleEntity.applicationEntity, qApplicationEntity)
                .where(qApplicationEntity.name.eq(name))
                .groupBy(qLifecycleEntity.versionEntity)
                .groupBy(qLifecycleEntity.instanceId)
                .groupBy(qLifecycleEntity.created)
                .groupBy(qLifecycleEntity.id)
                .groupBy(qApplicationEntity.id)
                .orderBy(qLifecycleEntity.created.asc())
                .list(new QLifecycleEntity(qLifecycleEntity));
    }
}
