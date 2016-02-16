package org.zalando.stups.fullstop.rule.service;

import org.zalando.fullstop.web.api.NotFoundException;
import org.zalando.stups.fullstop.rule.entity.RuleDTO;
import org.zalando.stups.fullstop.rule.entity.RuleEntity;

import java.util.List;

public interface RuleEntityService {

    RuleEntity save(RuleDTO ruleDTO);

    RuleEntity update(RuleDTO ruleDTO, Long id) throws NotFoundException;

    RuleEntity findById(Long id);

    List<RuleEntity> findByNotExpired();

    List<RuleEntity> findAll();
}
