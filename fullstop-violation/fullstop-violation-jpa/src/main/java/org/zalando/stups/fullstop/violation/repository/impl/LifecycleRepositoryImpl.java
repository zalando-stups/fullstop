package org.zalando.stups.fullstop.violation.repository.impl;

import com.mysema.query.jpa.JPQLQuery;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.support.QueryDslRepositorySupport;
import org.zalando.stups.fullstop.violation.entity.LifecycleEntity;
import org.zalando.stups.fullstop.violation.entity.QApplicationEntity;
import org.zalando.stups.fullstop.violation.entity.QLifecycleEntity;
import org.zalando.stups.fullstop.violation.entity.QVersionEntity;
import org.zalando.stups.fullstop.violation.repository.LifecycleRepositoryCustom;

import java.util.List;

import static com.google.common.collect.Iterables.isEmpty;
import static java.util.Collections.emptyList;
import static org.apache.logging.log4j.util.Strings.isNotEmpty;

public class LifecycleRepositoryImpl extends QueryDslRepositorySupport implements LifecycleRepositoryCustom {

    private static final String CREATED = "created";
    private static final Sort SORT_BY_CREATED = new Sort(CREATED);


    public LifecycleRepositoryImpl() {
        super(LifecycleEntity.class);
    }


    @Override
    public Page<LifecycleEntity> findByApplicationNameAndVersion(String name, String version, Pageable pageable) {

        final QLifecycleEntity qLifecycleEntity = QLifecycleEntity.lifecycleEntity;
        final QApplicationEntity qApplicationEntity = QApplicationEntity.applicationEntity;
        final QVersionEntity qVersionEntity = QVersionEntity.versionEntity;


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



        long total = queryResult.count();
        final Sort sort = pageable.getSort();
        final Sort fixedSort = (sort == null || isEmpty(sort)) ? SORT_BY_CREATED : sort;

        final PageRequest pageRequest = new PageRequest(pageable.getPageNumber(), pageable.getPageSize(), fixedSort);

        getQuerydsl().applyPagination(pageRequest, queryResult);

        final List<LifecycleEntity> lifecycleEntities = total > 0 ? queryResult.list(new QLifecycleEntity(qLifecycleEntity)) : emptyList();

        return new PageImpl<>(lifecycleEntities, pageRequest, total);

    }
}
