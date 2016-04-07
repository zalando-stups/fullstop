package org.zalando.stups.fullstop.violation.repository.impl;

import org.springframework.data.jpa.repository.support.QueryDslRepositorySupport;
import org.zalando.stups.fullstop.violation.entity.LifecycleEntity;
import org.zalando.stups.fullstop.violation.entity.QApplicationEntity;
import org.zalando.stups.fullstop.violation.entity.QLifecycleEntity;
import org.zalando.stups.fullstop.violation.entity.QVersionEntity;
import org.zalando.stups.fullstop.violation.repository.LifecycleRepositoryCustom;

import java.util.List;

public class LifecycleRepositoryImpl extends QueryDslRepositorySupport implements LifecycleRepositoryCustom {


    public LifecycleRepositoryImpl() {
        super(LifecycleEntity.class);
    }

    @Override
    public List<LifecycleEntity> findByApplicationName(String name) {
        final QLifecycleEntity qLifecycleEntity = new QLifecycleEntity("lifecycle");
        final QApplicationEntity qApplicationEntity = new QApplicationEntity("application");


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

    @Override
    public List<LifecycleEntity> findByApplicationNameAndVersion(String name, String version) {

        final QLifecycleEntity qLifecycleEntity = new QLifecycleEntity("lifecycle");
        final QApplicationEntity qApplicationEntity = new QApplicationEntity("application");
        final QVersionEntity qVersionEntity = new QVersionEntity("version");


        return from(qLifecycleEntity)
                .join(qLifecycleEntity.applicationEntity, qApplicationEntity)
                .join(qLifecycleEntity.versionEntity, qVersionEntity)
                .where(qApplicationEntity.name.eq(name), qVersionEntity.name.eq(version))
                .groupBy(qLifecycleEntity.versionEntity)
                .groupBy(qLifecycleEntity.instanceId)
                .groupBy(qLifecycleEntity.created)
                .groupBy(qLifecycleEntity.id)
                .groupBy(qApplicationEntity.id)
                .groupBy(qVersionEntity.id)
                .orderBy(qLifecycleEntity.created.asc())
                .list(new QLifecycleEntity(qLifecycleEntity));
    }
}
