package org.zalando.stups.fullstop.rule.service.impl;

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.zalando.fullstop.web.api.NotFoundException;
import org.zalando.stups.fullstop.rule.entity.RuleDTO;
import org.zalando.stups.fullstop.rule.entity.RuleEntity;
import org.zalando.stups.fullstop.rule.repository.RuleEntityRepository;
import org.zalando.stups.fullstop.rule.service.RuleEntityService;

import java.util.List;
import java.util.Optional;

import static java.lang.String.format;
import static org.joda.time.DateTimeZone.UTC;

@Service("ruleEntityService")
public class RuleEntityServiceImpl implements RuleEntityService {

    @Autowired
    private RuleEntityRepository ruleEntityRepository;

    private final Logger log = LoggerFactory.getLogger(getClass());


    @Override
    public RuleEntity save(RuleDTO ruleDTO) {

        if (ruleDTO.getExpiryDate() == null) {
            ruleDTO.setExpiryDate(new DateTime(9999, 1, 1, 0, 0, 0, UTC));
        }

        RuleEntity ruleEntity = mapDtoToRuleEntity(ruleDTO);

        RuleEntity entity = ruleEntityRepository.save(ruleEntity);

        log.info("New Whitelisting Rule created {}", ruleEntity);

        return entity;

    }

    @Override
    public RuleEntity update(RuleDTO ruleDTO, Long id) throws NotFoundException {

        Optional.ofNullable(ruleEntityRepository.findOne(id)).
                map(this::invalidateRule).orElseThrow(() -> new NotFoundException(format("No such Rule! Id: %s", id)));

        return save(ruleDTO);

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

    private RuleEntity invalidateRule(RuleEntity ruleEntity) {
        ruleEntity.setExpiryDate(DateTime.now());
        return ruleEntityRepository.save(ruleEntity);
    }

    private RuleEntity mapDtoToRuleEntity(RuleDTO ruleDTO) {
        RuleEntity ruleEntity = new RuleEntity();
        ruleEntity.setAccountId(ruleDTO.getAccountId());
        ruleEntity.setRegion(ruleDTO.getRegion());
        ruleEntity.setApplicationId(ruleDTO.getApplicationId());
        ruleEntity.setApplicationVersion(ruleDTO.getApplicationVersion());
        ruleEntity.setImageName(ruleDTO.getImageName());
        ruleEntity.setImageOwner(ruleDTO.getImageOwner());
        ruleEntity.setReason(ruleDTO.getReason());
        ruleEntity.setExpiryDate(ruleDTO.getExpiryDate());
        ruleEntity.setViolationTypeEntityId(ruleDTO.getViolationTypeEntityId());
        ruleEntity.setVersion(ruleDTO.getVersion());

        return ruleEntity;

    }
}
