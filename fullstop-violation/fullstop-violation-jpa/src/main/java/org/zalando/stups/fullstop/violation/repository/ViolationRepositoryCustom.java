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
package org.zalando.stups.fullstop.violation.repository;

import org.joda.time.DateTime;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import org.zalando.stups.fullstop.violation.entity.CountByAccountAndType;
import org.zalando.stups.fullstop.violation.entity.CountByAppVersionAndType;
import org.zalando.stups.fullstop.violation.entity.ViolationEntity;

import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Created by gkneitschel.
 */
@Repository
public interface ViolationRepositoryCustom {

    Page<ViolationEntity> queryViolations(List<String> accounts, DateTime since, Long lastViolation, Boolean checked,
            Integer severity, Boolean auditRelevant, String type, Pageable pageable);

    boolean violationExists(String accountId, String region, String eventId, String instanceId, String violationType);

    List<CountByAccountAndType> countByAccountAndType(Set<String> accountIds, Optional<DateTime> from,
                                                      Optional<DateTime> to, Optional<Boolean> resolved);

    List<CountByAppVersionAndType> countByAppVersionAndType(String account, Optional<DateTime> from,
                                                      Optional<DateTime> to, Optional<Boolean> resolved);
}
