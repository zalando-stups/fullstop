package org.zalando.stups.fullstop.violation.service.impl;

import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.zalando.stups.fullstop.violation.entity.ViolationEntity;
import org.zalando.stups.fullstop.violation.repository.ViolationRepository;
import org.zalando.stups.fullstop.violation.service.ViolationService;

import java.util.List;

/**
 * Created by mrandi.
 */
@Service
public class ViolationServiceImpl implements ViolationService {

    @Autowired
    private ViolationRepository violationRepository;

    @Override
    public Page<ViolationEntity> findAll(final Pageable pageable) {
        return violationRepository.findAll(pageable);
    }

    @Override
    public List<ViolationEntity> findAll() {
        return violationRepository.findAll();
    }

    @Override
    public ViolationEntity save(final ViolationEntity violation) {
        return violationRepository.save(violation);
    }

    @Override
    public ViolationEntity findOne(final Long id) {
        return violationRepository.findOne(id);
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
        return violationRepository.queryViolations(accounts, from, to, lastViolation, checked, severity, priority,
                auditRelevant, types, whitelisted, applicationIds, applicationVersionIds, pageable);
    }

    @Override
    public boolean violationExists(final String accountId,
                                   final String region,
                                   final String eventId,
                                   final String instanceId,
                                   final String violationTypeId) {
        return violationRepository.violationExists(accountId, region, eventId, instanceId, violationTypeId);
    }
}
