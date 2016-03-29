package org.zalando.stups.fullstop.violation.service;

import org.joda.time.DateTime;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.zalando.stups.fullstop.violation.entity.ViolationEntity;

import java.util.List;

/**
 * Created by mrandi.
 */
public interface ViolationService {

    Page<ViolationEntity> findAll(Pageable pageable);

    List<ViolationEntity> findAll();

    ViolationEntity save(ViolationEntity violation);

    ViolationEntity findOne(Long id);

    Page<ViolationEntity> queryViolations(List<String> accounts, DateTime from, DateTime to, Long lastViolation, boolean checked,
                                          Integer severity, final Integer priority, Boolean auditRelevant, String type,
                                          boolean whitelisted, Pageable pageable);

    boolean violationExists(String accountId, String region, String eventId, String instanceId, String violationTypeId);
}
