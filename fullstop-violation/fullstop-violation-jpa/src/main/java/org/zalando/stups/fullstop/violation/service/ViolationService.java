package org.zalando.stups.fullstop.violation.service;

import org.joda.time.DateTime;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.zalando.stups.fullstop.violation.entity.ViolationEntity;

import java.util.List;
import java.util.Optional;

/**
 * Created by mrandi.
 */
public interface ViolationService {

    Page<ViolationEntity> findAll(Pageable pageable);

    List<ViolationEntity> findAll();

    ViolationEntity save(ViolationEntity violation);

    ViolationEntity findOne(Long id);

    Page<ViolationEntity> queryViolations(List<String> accounts, DateTime from, DateTime to, Long lastViolation, Boolean checked,
                                          Integer severity, Boolean auditRelevant, String type,
                                          Optional<Boolean> whitelisted, Pageable pageable);

    boolean violationExists(String accountId, String region, String eventId, String instanceId, String violationTypeId);
}
