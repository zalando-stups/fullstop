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

    Page<ViolationEntity> queryViolations(List<String> accounts, DateTime from, DateTime to, Long lastViolation, Boolean checked,
                                          Integer severity, Boolean auditRelevant, String type, Optional<Boolean> whitelisted, Pageable pageable);

    boolean violationExists(String accountId, String region, String eventId, String instanceId, String violationType);

    List<CountByAccountAndType> countByAccountAndType(Set<String> accountIds, Optional<DateTime> from,
                                                      Optional<DateTime> to, Optional<Boolean> resolved);

    List<CountByAppVersionAndType> countByAppVersionAndType(String account, Optional<DateTime> from,
                                                      Optional<DateTime> to, Optional<Boolean> resolved);
}
