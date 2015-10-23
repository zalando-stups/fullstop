/**
 * Copyright (C) 2015 Zalando SE (http://tech.zalando.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.zalando.stups.fullstop.violation.repository.impl;

import com.mysema.query.jpa.JPQLQuery;
import com.mysema.query.types.Predicate;
import org.joda.time.DateTime;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.support.QueryDslRepositorySupport;
import org.zalando.stups.fullstop.violation.entity.CountByAccountAndType;
import org.zalando.stups.fullstop.violation.entity.QViolationEntity;
import org.zalando.stups.fullstop.violation.entity.QViolationTypeEntity;
import org.zalando.stups.fullstop.violation.entity.ViolationEntity;
import org.zalando.stups.fullstop.violation.repository.ViolationRepositoryCustom;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static com.google.common.collect.Iterables.isEmpty;
import static com.google.common.collect.Lists.newArrayList;
import static com.mysema.query.types.ExpressionUtils.allOf;
import static com.mysema.query.types.Projections.constructor;
import static java.util.Collections.emptyList;

/**
 * Created by mrandi.
 */
@SuppressWarnings("unused")
public class ViolationRepositoryImpl extends QueryDslRepositorySupport implements ViolationRepositoryCustom {

    private static final String ID = "id";

    private static final Sort SORT_BY_ID = new Sort(ID);

    public ViolationRepositoryImpl() {
        super(ViolationEntity.class);
    }

    @Override
    public Page<ViolationEntity> queryViolations(final List<String> accounts, final DateTime since,
            final Long lastViolation, final Boolean checked, final Integer severity, Boolean auditRelevant,
            String type, final Pageable pageable) {

        QViolationEntity qViolationEntity = QViolationEntity.violationEntity;
        QViolationTypeEntity qViolationTypeEntity = QViolationTypeEntity.violationTypeEntity;

        final JPQLQuery query = from(qViolationEntity).leftJoin(qViolationEntity.violationTypeEntity, qViolationTypeEntity);

        final List<Predicate> predicates = newArrayList();

        if (accounts != null) {
            predicates.add(qViolationEntity.accountId.in(accounts));
        }

        if (since != null) {
            predicates.add(qViolationEntity.created.after(since));
        }

        if (lastViolation != null) {
            predicates.add(qViolationEntity.id.goe(lastViolation));
        }

        if (checked != null) {
            if (checked) {
                predicates.add(qViolationEntity.comment.isNotEmpty());
            }
            else {
                predicates.add(qViolationEntity.comment.isNull().or(qViolationEntity.comment.isEmpty()));
            }
        }

        if (severity != null) {
            predicates.add(qViolationTypeEntity.violationSeverity.eq(severity));
        }

        if (auditRelevant != null) {

            predicates.add(qViolationTypeEntity.isAuditRelevant.eq(auditRelevant));
        }

        if (type != null) {
            predicates.add(qViolationEntity.violationTypeEntity.id.eq(type));
        }

        final long total = query.where(allOf(predicates)).count();

        final Sort sort = pageable.getSort();
        final Sort fixedSort = (sort == null || isEmpty(sort)) ? SORT_BY_ID : sort;
        final PageRequest fixedPage = new PageRequest(pageable.getPageNumber(), pageable.getPageSize(), fixedSort);

        getQuerydsl().applyPagination(fixedPage, query);

        final List<ViolationEntity> list;
        list = total > 0 ? query.where(allOf(predicates)).list(qViolationEntity) : emptyList();

        return new PageImpl<>(list, fixedPage, total);
    }

    @Override
    public boolean violationExists(String accountId, String region, String eventId, String instanceId, String violationType) {
        final QViolationEntity qViolation = new QViolationEntity("v");

        return from(qViolation)
                .where(qViolation.accountId.eq(accountId),
                        qViolation.region.eq(region),
                        qViolation.eventId.eq(eventId),
                        instanceId == null ? qViolation.instanceId.isNull() : qViolation.instanceId.eq(instanceId),
                        qViolation.violationTypeEntity.id.eq(violationType))
                .exists();
    }

    @Override
    public List<CountByAccountAndType> countByAccountAndType(Set<String> accountIds, Optional<DateTime> fromDate,
                                                             Optional<DateTime> toDate, Optional<Boolean> resolved) {
        final QViolationEntity qViolation = new QViolationEntity("v");
        final QViolationTypeEntity qType = new QViolationTypeEntity("t");

        final JPQLQuery query = from(qViolation);
        query.join(qViolation.violationTypeEntity, qType);

        final Collection<Predicate> whereClause = newArrayList();

        if (!accountIds.isEmpty()) {
            whereClause.add(qViolation.accountId.in(accountIds));
        }

        fromDate.map(qViolation.created::after).ifPresent(whereClause::add);
        toDate.map(qViolation.created::before).ifPresent(whereClause::add);
        resolved.map((isResolved)-> isResolved ? qViolation.comment.isNotNull() : qViolation.comment.isNull()).ifPresent(whereClause::add);

        if (!whereClause.isEmpty()) {
            query.where(allOf(whereClause));
        }
        query.groupBy(qViolation.accountId, qType.id);
        query.orderBy(qViolation.accountId.asc(), qType.id.asc());

        return query.list(constructor(CountByAccountAndType.class, qViolation.accountId, qType.id, qViolation.id.count()));
    }
}
