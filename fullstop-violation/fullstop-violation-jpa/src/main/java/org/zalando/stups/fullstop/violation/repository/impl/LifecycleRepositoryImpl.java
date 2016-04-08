package org.zalando.stups.fullstop.violation.repository.impl;

import com.mysema.query.jpa.JPQLQuery;
import org.springframework.data.jpa.repository.support.QueryDslRepositorySupport;
import org.zalando.stups.fullstop.violation.entity.LifecycleEntity;
import org.zalando.stups.fullstop.violation.entity.QApplicationEntity;
import org.zalando.stups.fullstop.violation.entity.QLifecycleEntity;
import org.zalando.stups.fullstop.violation.entity.QVersionEntity;
import org.zalando.stups.fullstop.violation.repository.LifecycleRepositoryCustom;

import java.util.List;

import static org.apache.logging.log4j.util.Strings.isNotEmpty;

public class LifecycleRepositoryImpl extends QueryDslRepositorySupport implements LifecycleRepositoryCustom {


    public LifecycleRepositoryImpl() {
        super(LifecycleEntity.class);
    }


    @Override
    public List<LifecycleEntity> findByApplicationNameAndVersion(String name, String version) {

        final QLifecycleEntity qLifecycleEntity = new QLifecycleEntity("lifecycle");
        final QApplicationEntity qApplicationEntity = new QApplicationEntity("application");
        final QVersionEntity qVersionEntity = new QVersionEntity("version");


        JPQLQuery queryResult = from(qLifecycleEntity).join(qLifecycleEntity.applicationEntity, qApplicationEntity);

        if (version != null && isNotEmpty(version)) {
            queryResult.join(qLifecycleEntity.versionEntity, qVersionEntity);
            queryResult.where(qVersionEntity.name.eq(version));
        }

        queryResult.where(qApplicationEntity.name.eq(name))
                .groupBy(qLifecycleEntity.versionEntity,
                        qLifecycleEntity.instanceId,
                        qLifecycleEntity.created,
                        qLifecycleEntity.id,
                        qApplicationEntity.id);

        if (version != null && isNotEmpty(version)) {
            queryResult.groupBy(qVersionEntity.id);
        }

        List<LifecycleEntity> lifecycleEntities = queryResult.orderBy(qLifecycleEntity.created.asc())
                .list(new QLifecycleEntity(qLifecycleEntity));

        return lifecycleEntities;

    }
}
