package org.zalando.stups.fullstop.rule.service.impl;

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.zalando.stups.fullstop.rule.entity.RuleDTO;
import org.zalando.stups.fullstop.rule.entity.RuleEntity;
import org.zalando.stups.fullstop.rule.repository.RuleEntityRepository;
import org.zalando.stups.fullstop.rule.service.RuleEntityService;

import java.util.List;

@Service("ruleEntityService")
public class RuleEntityServiceImpl implements RuleEntityService {

    @Autowired
    private RuleEntityRepository ruleEntityRepository;

    private final Logger log = LoggerFactory.getLogger(getClass());


    @Override
    public RuleEntity save(RuleDTO ruleDTO) {

        if (ruleDTO.getExpiryDate() == null) {
            ruleDTO.setExpiryDate(new DateTime(Long.MAX_VALUE));
        }

        RuleEntity ruleEntity = mapDtoToRuleEntity(ruleDTO);

        RuleEntity entity = ruleEntityRepository.save(ruleEntity);

        log.info("New Whitelisting Rule created {}", ruleEntity);

        return entity;

    }

    @Override
    public RuleEntity update(RuleDTO ruleDTO, Long id) {
        RuleEntity newRule;

        if (ruleDTO.getExpiryDate() == null) {
            ruleDTO.setExpiryDate(new DateTime(Long.MAX_VALUE));
        }

        RuleEntity oldRule = ruleEntityRepository.findOne(id);
        if (oldRule == null) {
            log.warn("No such RuleEntity found for updating! Id: {}", id);
            return null;
        }
        invalidateRule(oldRule);

        newRule = mapDtoToRuleEntity(ruleDTO);
        newRule = ruleEntityRepository.save(newRule);
        log.info("Whitelisting Rule updated {}", newRule);
        return newRule;
    }

    @Override
    public RuleEntity findById(Long id) {
        RuleEntity ruleEntity = ruleEntityRepository.findOne(id);
        if (ruleEntity == null) {
            log.warn("No such RuleEntity found! Id: {}", id);
            return null;
        }


        return ruleEntity;
    }

    @Override
    public List<RuleEntity> findByNotExpired() {
        return ruleEntityRepository.findByExpiryDateAfter(DateTime.now());
    }

    @Override
    public List<RuleEntity> findAll() {
        return ruleEntityRepository.findAll();
    }

    private void invalidateRule(RuleEntity ruleEntity) {
        ruleEntity.setExpiryDate(DateTime.now());
        ruleEntityRepository.save(ruleEntity);
    }

    private RuleEntity mapDtoToRuleEntity(RuleDTO ruleDTO){
        RuleEntity ruleEntity = new RuleEntity();
        ruleEntity.setAccountId(ruleDTO.getAccountId());
        ruleEntity.setApplicationId(ruleDTO.getApplicationId());
        ruleEntity.setApplicationVersion(ruleDTO.getApplicationVersion());
        ruleEntity.setImageName(ruleDTO.getImageName());
        ruleEntity.setImageOwner(ruleDTO.getImageOwner());
        ruleEntity.setReason(ruleDTO.getReason());
        ruleEntity.setExpiryDate(ruleDTO.getExpiryDate());
        ruleEntity.setViolationTypeEntityId(ruleDTO.getViolationTypeEntity());

        return ruleEntity;

    }
}
