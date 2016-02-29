package org.zalando.stups.fullstop.violation.repository.impl;

import com.mysema.query.jpa.JPQLQuery;
import com.mysema.query.types.Predicate;
import com.mysema.query.types.expr.BooleanExpression;
import org.joda.time.DateTime;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.support.QueryDslRepositorySupport;
import org.springframework.util.Assert;
import org.zalando.stups.fullstop.violation.entity.*;
import org.zalando.stups.fullstop.violation.repository.ViolationRepositoryCustom;

import javax.persistence.Query;
import java.math.BigInteger;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static com.google.common.collect.Iterables.isEmpty;
import static com.google.common.collect.Lists.newArrayList;
import static com.mysema.query.types.ExpressionUtils.allOf;
import static com.mysema.query.types.Projections.constructor;
import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;
import static javax.persistence.TemporalType.TIMESTAMP;

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
    public Page<ViolationEntity> queryViolations(final List<String> accounts, final DateTime from, final DateTime to,
                                                 final Long lastViolation, final Boolean checked, final Integer severity,
                                                 final Boolean auditRelevant, final String type,
                                                 final Optional<Boolean> whitelisted, final Pageable pageable) {

        QViolationEntity qViolationEntity = QViolationEntity.violationEntity;
        QViolationTypeEntity qViolationTypeEntity = QViolationTypeEntity.violationTypeEntity;

        final JPQLQuery query = from(qViolationEntity).leftJoin(qViolationEntity.violationTypeEntity, qViolationTypeEntity);

        final List<Predicate> predicates = newArrayList();

        if (accounts != null) {
            predicates.add(qViolationEntity.accountId.in(accounts));
        }

        if (from != null) {
            predicates.add(qViolationEntity.created.after(from));
        }

        if (to != null) {
            predicates.add(qViolationEntity.created.before(to));
        }

        if (lastViolation != null) {
            predicates.add(qViolationEntity.id.goe(lastViolation));
        }

        if (checked != null) {
            if (checked) {
                BooleanExpression resolvedViolation = qViolationEntity.comment.isNotEmpty();
                predicates.add(resolvedViolation);
            } else {
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

        if (whitelisted.isPresent()) {
            if (whitelisted.get()) {
                BooleanExpression whitelistedViolations = qViolationEntity.ruleEntity.id.isNotNull();
                predicates.add(whitelistedViolations);
            } else {
                predicates.add(qViolationEntity.ruleEntity.id.isNull());
            }
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
        resolved.map((isResolved) -> isResolved ? qViolation.comment.isNotNull() : qViolation.comment.isNull()).ifPresent(whereClause::add);

        if (!whereClause.isEmpty()) {
            query.where(allOf(whereClause));
        }
        query.groupBy(qViolation.accountId, qType.id);
        query.orderBy(qViolation.accountId.asc(), qType.id.asc());

        return query.list(constructor(CountByAccountAndType.class, qViolation.accountId, qType.id, qViolation.id.count()));
    }

    @Override
    public List<CountByAppVersionAndType> countByAppVersionAndType(String account, Optional<DateTime> fromDate,
                                                                   Optional<DateTime> toDate, Optional<Boolean> resolved) {
        Assert.hasText(account, "account must not be blank");

        final String sql = "SELECT app.name AS application, ver.name AS version, vio.violation_type_entity_id AS type, count(DISTINCT vio.id) AS quantity " +
                "FROM fullstop_data.violation vio " +
                "LEFT JOIN fullstop_data.lifecycle l ON l.account_id = vio.account_id AND l.region = vio.region AND l.instance_id = vio.instance_id " +
                "LEFT JOIN fullstop_data.application app ON app.id = l.application " +
                "LEFT JOIN fullstop_data.app_version ver ON ver.id = l.application_version " +
                "WHERE vio.account_id = :account " +
                (fromDate.isPresent() ? "AND vio.created >= :from_date " : "") +
                (toDate.isPresent() ? "AND vio.created <= :to_date " : "") +
                resolved.map((resolvedViolations) -> "AND vio.comment IS " + (resolvedViolations ? "NOT NULL " : "NULL ")).orElse("") +
                "GROUP BY app.id, ver.id, vio.violation_type_entity_id " +
                "ORDER BY app.name ASC NULLS LAST, ver.created DESC NULLS LAST, vio.violation_type_entity_id ASC ";

        final Query query = getEntityManager().createNativeQuery(sql);
        query.setParameter("account", account);
        fromDate.ifPresent((d) -> query.setParameter("from_date", d.toDate(), TIMESTAMP));
        toDate.ifPresent((d) -> query.setParameter("to_date", d.toDate(), TIMESTAMP));

        final List<?> results = query.getResultList();
        return results.stream()
                .map((o) -> (Object[]) o)
                .map(row -> new CountByAppVersionAndType((String) row[0], (String) row[1], (String) row[2], ((BigInteger) row[3]).longValue()))
                .collect(toList());
    }
}
