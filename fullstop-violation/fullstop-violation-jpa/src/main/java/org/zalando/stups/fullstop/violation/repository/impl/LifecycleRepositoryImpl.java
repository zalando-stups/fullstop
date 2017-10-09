package org.zalando.stups.fullstop.violation.repository.impl;

import com.querydsl.jpa.JPQLQuery;
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
    public Page<LifecycleEntity> findByApplicationNameAndVersion(final String name, final String version, final Pageable pageable) {

        final QLifecycleEntity qLifecycleEntity = QLifecycleEntity.lifecycleEntity;
        final QApplicationEntity qApplicationEntity = QApplicationEntity.applicationEntity;
        final QVersionEntity qVersionEntity = QVersionEntity.versionEntity;


        final JPQLQuery<LifecycleEntity> query = from(qLifecycleEntity).leftJoin(qLifecycleEntity.applicationEntity, qApplicationEntity);

        if (version != null && isNotEmpty(version)) {
            query.join(qLifecycleEntity.versionEntity, qVersionEntity);
            query.where(qVersionEntity.name.eq(version));
        }

        query.where(qApplicationEntity.name.eq(name));

        final long total = query.fetchCount();

        query.groupBy(qLifecycleEntity.versionEntity,
                qLifecycleEntity.instanceId,
                qLifecycleEntity.created,
                qLifecycleEntity.id,
                qApplicationEntity.id);

        if (version != null && isNotEmpty(version)) {
            query.groupBy(qVersionEntity.id);
        }


        final Sort sort = pageable.getSort();
        final Sort fixedSort = (sort == null || isEmpty(sort)) ? SORT_BY_CREATED : sort;

        final PageRequest pageRequest = new PageRequest(pageable.getPageNumber(), pageable.getPageSize(), fixedSort);

        getQuerydsl().applyPagination(pageRequest, query);

        final List<LifecycleEntity> lifecycleEntities = total > 0 ? query.fetch() : emptyList();

        return new PageImpl<>(lifecycleEntities, pageRequest, total);

    }
}
