package org.zalando.stups.fullstop.rule.service.impl;

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

        RuleEntity ruleEntity = mapDtoToRuleEntity(ruleDTO);

        RuleEntity entity = ruleEntityRepository.save(ruleEntity);

        log.info("New Whitelisting Rule created {}", ruleEntity);

        return entity;

    }

    @Override
    public RuleEntity update(RuleDTO ruleDTO, Long id) {
        RuleEntity ruleEntity = ruleEntityRepository.findOne(id);
        if (ruleEntity == null) {
            log.warn("No such RuleEntity! ID: {}", id);
            return null;
        }

        ruleEntity = updateRuleEntity(ruleEntity, ruleDTO);

        ruleEntityRepository.save(ruleEntity);
        log.info("Whitelisting Rule updated {}", ruleEntity);
        return ruleEntity;
    }

    @Override
    public RuleEntity findById(Long id) {
        return ruleEntityRepository.findOne(id);
    }

    @Override
    public List<RuleEntity> findAll() {
        return ruleEntityRepository.findAll();
    }

    private RuleEntity updateRuleEntity(RuleEntity ruleEntity, RuleDTO ruleDTO) {
        return mapEntity(ruleDTO, ruleEntity);
    }

    private RuleEntity mapDtoToRuleEntity(RuleDTO ruleDTO){
        RuleEntity ruleEntity = new RuleEntity();

        return mapEntity(ruleDTO, ruleEntity);

    }

    private RuleEntity mapEntity(RuleDTO ruleDTO, RuleEntity ruleEntity) {
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
