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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
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
public class ViolationRepositoryImpl extends QueryDslRepositorySupport implements ViolationRepositoryCustom {

    public ViolationRepositoryImpl() {
        super(ViolationEntity.class);
    }

    @Override
    public Page<ViolationEntity> queryViolations(List<String> accounts, Date since, Long lastViolation, Boolean
            checked, Pageable pageable) {
        QViolationEntity qViolationEntity = QViolationEntity.violationEntity;

        final JPQLQuery query = from(qViolationEntity);

        final List<Predicate> mainPredicates = newArrayList();

        if (accounts != null) {
            mainPredicates.add(qViolationEntity.accountId.in(accounts));
        }

        if (since != null) {
            mainPredicates.add(qViolationEntity.created.after(new DateTime(since)));
        }

        if (lastViolation != null){
            mainPredicates.add(qViolationEntity.id.goe(lastViolation));
        }

        if (checked != null && checked) {
            mainPredicates.add(qViolationEntity.comment.isNotEmpty());
        } else {
            // both right?
        }

        List<ViolationEntity> list = query.where(allOf(mainPredicates)).orderBy(qViolationEntity.id.asc(),
                qViolationEntity.created.asc())
                .list(qViolationEntity);

        // TODO: fix pageable
        return new PageImpl<>(list);
    }
}
