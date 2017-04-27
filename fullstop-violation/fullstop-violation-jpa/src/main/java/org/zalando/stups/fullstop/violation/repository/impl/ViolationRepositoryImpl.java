package org.zalando.stups.fullstop.violation.repository.impl;

import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.JPQLQuery;
import org.joda.time.DateTime;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.support.QueryDslRepositorySupport;
import org.springframework.util.Assert;
import org.zalando.stups.fullstop.violation.entity.*;
import org.zalando.stups.fullstop.violation.repository.ViolationRepositoryCustom;

import javax.persistence.Query;
import java.math.BigInteger;
import java.util.*;

import static com.google.common.collect.Iterables.isEmpty;
import static com.google.common.collect.Lists.newArrayList;
import static com.querydsl.core.types.ExpressionUtils.allOf;
import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;
import static javax.persistence.TemporalType.TIMESTAMP;

@SuppressWarnings("unused")
public class ViolationRepositoryImpl extends QueryDslRepositorySupport implements ViolationRepositoryCustom {

    private static final String ID = "id";

    private static final Sort SORT_BY_ID = new Sort(ID);

    public ViolationRepositoryImpl() {
        super(ViolationEntity.class);
    }

    @Override
    public Page<ViolationEntity> queryViolations(final List<String> accounts,
                                                 final DateTime from,
                                                 final DateTime to,
                                                 final Long lastViolation,
                                                 final boolean checked,
                                                 final Integer severity,
                                                 final Integer priority,
                                                 final Boolean auditRelevant,
                                                 final List<String> types,
                                                 final boolean whitelisted,
                                                 final List<String> applicationIds,
                                                 final List<String> applicationVersionIds,
                                                 final Pageable pageable) {

        final QViolationEntity qViolationEntity = QViolationEntity.violationEntity;
        final QViolationTypeEntity qViolationTypeEntity = QViolationTypeEntity.violationTypeEntity;

        final JPQLQuery<ViolationEntity> query = from(qViolationEntity).leftJoin(qViolationEntity.violationTypeEntity, qViolationTypeEntity);

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

        if (whitelisted) {
            predicates.add(qViolationEntity.ruleEntity.isNotNull());
        } else if (checked) {
            predicates.add(qViolationEntity.comment.isNotNull());
            predicates.add(qViolationEntity.ruleEntity.isNull());
        } else {
            predicates.add(qViolationEntity.comment.isNull());
            predicates.add(qViolationEntity.ruleEntity.isNull());
        }

        if (severity != null) {
            predicates.add(qViolationTypeEntity.violationSeverity.eq(severity));
        }

        if (priority != null) {
            predicates.add(qViolationTypeEntity.priority.eq(priority));
        }

        if (auditRelevant != null) {

            predicates.add(qViolationTypeEntity.isAuditRelevant.eq(auditRelevant));
        }

        if (types != null && !types.isEmpty()) {
            predicates.add(qViolationEntity.violationTypeEntity.id.in(types));
        }

        if (applicationIds != null && !applicationIds.isEmpty()) {
            predicates.add(qViolationEntity.application.name.in(applicationIds));
        }

        if (applicationVersionIds != null && !applicationVersionIds.isEmpty()) {
            predicates.add(qViolationEntity.applicationVersion.name.in(applicationVersionIds));
        }

        final long total = query.where(allOf(predicates)).fetchCount();

        final Sort sort = pageable.getSort();
        final Sort fixedSort = (sort == null || isEmpty(sort)) ? SORT_BY_ID : sort;
        final PageRequest fixedPage = new PageRequest(pageable.getPageNumber(), pageable.getPageSize(), fixedSort);

        getQuerydsl().applyPagination(fixedPage, query);

        final List<ViolationEntity> list;
        list = total > 0 ? query.where(allOf(predicates)).fetch() : emptyList();

        return new PageImpl<>(list, fixedPage, total);
    }

    @Override
    public boolean violationExists(final String accountId, final String region, final String eventId, final String instanceId, final String violationType) {
        final QViolationEntity qViolation = new QViolationEntity("v");

        return from(qViolation)
                .where(qViolation.accountId.eq(accountId),
                        qViolation.region.eq(region),
                        qViolation.eventId.eq(eventId),
                        instanceId == null ? qViolation.instanceId.isNull() : qViolation.instanceId.eq(instanceId),
                        qViolation.violationTypeEntity.id.eq(violationType))
                .fetchCount() > 0;
    }

    @Override
    public List<CountByAccountAndType> countByAccountAndType(final Set<String> accountIds,
                                                             final Optional<DateTime> fromDate,
                                                             final Optional<DateTime> toDate,
                                                             final boolean resolved,
                                                             final boolean whitelisted) {
        final QViolationEntity qViolation = new QViolationEntity("v");
        final QViolationTypeEntity qType = new QViolationTypeEntity("t");


        final JPQLQuery<ViolationEntity> query = from(qViolation);
        query.join(qViolation.violationTypeEntity, qType);

        final Collection<Predicate> whereClause = newArrayList();

        if (!accountIds.isEmpty()) {
            whereClause.add(qViolation.accountId.in(accountIds));
        }

        fromDate.map(qViolation.created::after).ifPresent(whereClause::add);
        toDate.map(qViolation.created::before).ifPresent(whereClause::add);

        if (whitelisted) {
            whereClause.add(qViolation.ruleEntity.isNotNull());
        } else if (resolved) {
            whereClause.add(qViolation.comment.isNotNull());
            whereClause.add(qViolation.ruleEntity.isNull());
        } else {
            whereClause.add(qViolation.comment.isNull());
            whereClause.add(qViolation.ruleEntity.isNull());
        }

        query.where(allOf(whereClause));

        query.groupBy(qViolation.accountId, qType.id);
        query.orderBy(qViolation.accountId.asc(), qType.id.asc());

        return query.select(Projections.constructor(CountByAccountAndType.class, qViolation.accountId, qType.id, qViolation.id.count())).fetch();
    }

    @Override
    public List<CountByAppVersionAndType> countByAppVersionAndType(final String account, final Optional<DateTime> fromDate,
                                                                   final Optional<DateTime> toDate, final boolean resolved, final boolean whitelisted) {
        Assert.hasText(account, "account must not be blank");

        final String whitelistedOrResolvedPredicate;

        if (whitelisted) {
            whitelistedOrResolvedPredicate = "AND vio.rule_entity_id IS NOT NULL ";
        } else if (resolved) {
            whitelistedOrResolvedPredicate = "AND vio.comment IS NOT NULL " +
                    "AND vio.rule_entity_id IS NULL ";

        } else {
            whitelistedOrResolvedPredicate = "AND vio.comment IS NULL " +
                    "AND vio.rule_entity_id IS NULL ";
        }

        final String sql = "SELECT app.name AS application, ver.name AS version, vio.violation_type_entity_id AS type, count(DISTINCT vio.id) AS quantity " +
                "FROM fullstop_data.violation vio " +
                "LEFT JOIN fullstop_data.application app ON app.id = vio.application_id " +
                "LEFT JOIN fullstop_data.app_version ver ON ver.id = vio.application_version_id " +
                "WHERE vio.account_id = :account " +
                (fromDate.isPresent() ? "AND vio.created >= :from_date " : "") +
                (toDate.isPresent() ? "AND vio.created <= :to_date " : "") +
                whitelistedOrResolvedPredicate +
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
