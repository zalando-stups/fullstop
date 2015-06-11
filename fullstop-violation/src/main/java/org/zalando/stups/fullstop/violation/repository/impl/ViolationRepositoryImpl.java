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

import static com.google.common.collect.Iterables.isEmpty;
import static com.google.common.collect.Lists.newArrayList;
import static com.mysema.query.types.ExpressionUtils.allOf;
import static java.util.Collections.emptyList;

import java.util.Date;
import java.util.List;

import com.mysema.query.jpa.JPQLQuery;
import com.mysema.query.types.Predicate;
import org.joda.time.DateTime;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.support.QueryDslRepositorySupport;
import org.zalando.stups.fullstop.violation.entity.QViolationEntity;
import org.zalando.stups.fullstop.violation.entity.ViolationEntity;
import org.zalando.stups.fullstop.violation.repository.ViolationRepositoryCustom;

import java.util.Date;
import java.util.List;

import static com.google.common.collect.Lists.newArrayList;
import static com.mysema.query.types.ExpressionUtils.allOf;

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
    public Page<ViolationEntity> queryViolations(List<String> accounts, DateTime since, Long lastViolation, Boolean
            checked, Pageable pageable) {
        QViolationEntity qViolationEntity = QViolationEntity.violationEntity;

        final JPQLQuery query = from(qViolationEntity);

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

        final long total = query.where(allOf(predicates)).count();

        final Sort sort = pageable.getSort();
        final Sort fixedSort = (sort == null || isEmpty(sort)) ? SORT_BY_ID : sort;
        final PageRequest fixedPage = new PageRequest(pageable.getPageNumber(), pageable.getPageSize(), fixedSort);

        getQuerydsl().applyPagination(fixedPage, query);

        final List<ViolationEntity> list;
        list = total > 0 ? query.where(allOf(predicates)).list(qViolationEntity) : emptyList();

        return new PageImpl<>(list, fixedPage, total);
    }
}
