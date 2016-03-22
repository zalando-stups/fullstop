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
import java.util.Optional;

/**
 * Created by mrandi.
 */
@Service
public class ViolationServiceImpl implements ViolationService {

    @Autowired
    private ViolationRepository violationRepository;

    @Override
    public Page<ViolationEntity> findAll(Pageable pageable) {
        return violationRepository.findAll(pageable);
    }

    @Override
    public List<ViolationEntity> findAll() {
        return violationRepository.findAll();
    }

    @Override
    public ViolationEntity save(ViolationEntity violation) {
        return violationRepository.save(violation);
    }

    @Override
    public ViolationEntity findOne(Long id) {
        return violationRepository.findOne(id);
    }

    @Override
    public Page<ViolationEntity> queryViolations(List<String> accounts, DateTime from, DateTime to, Long lastViolation,
                                                 boolean checked, Integer severity, Boolean auditRelevant, String type,
                                                 boolean whitelisted, Pageable pageable) {
        return violationRepository.queryViolations(accounts, from, to, lastViolation, checked, severity, auditRelevant, type, whitelisted, pageable);
    }

    @Override
    public boolean violationExists(String accountId, String region, String eventId, String instanceId, String violationTypeId) {
        return violationRepository.violationExists(accountId, region, eventId, instanceId, violationTypeId);
    }
}
