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

import org.springframework.data.jpa.repository.support.QueryDslRepositorySupport;
import org.zalando.stups.fullstop.violation.entity.ApplicationEntity;
import org.zalando.stups.fullstop.violation.entity.QApplicationEntity;
import org.zalando.stups.fullstop.violation.entity.QLifecycleEntity;
import org.zalando.stups.fullstop.violation.repository.ApplicationRepositoryCustom;

import java.util.Collection;

public class ApplicationRepositoryImpl extends QueryDslRepositorySupport implements ApplicationRepositoryCustom {

    public ApplicationRepositoryImpl() {
        super(ApplicationEntity.class);
    }

    @Override
    public ApplicationEntity findByInstanceIds(final String accountId,
                                               final String region,
                                               final Collection<String> instanceIds) {
        final QApplicationEntity qApp = new QApplicationEntity("a");
        final QLifecycleEntity qLifecycle = new QLifecycleEntity("l");

        return from(qLifecycle)
                .join(qLifecycle.applicationEntity, qApp)
                .where(qLifecycle.accountId.eq(accountId),
                        qLifecycle.region.eq(region),
                        qLifecycle.instanceId.in(instanceIds))
                .groupBy(qApp.id)
                .orderBy(qLifecycle.lastModified.max().desc())
                .limit(1)
                .singleResult(qApp);
    }
}
