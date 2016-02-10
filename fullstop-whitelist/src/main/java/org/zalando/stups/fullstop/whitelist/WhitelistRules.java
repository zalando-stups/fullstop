package org.zalando.stups.fullstop.whitelist;

import org.springframework.beans.factory.annotation.Autowired;
import org.zalando.stups.fullstop.rule.entity.RuleEntity;
import org.zalando.stups.fullstop.rule.repository.RuleEntityRepository;
import org.zalando.stups.fullstop.violation.entity.ViolationEntity;

import java.util.List;

public class WhitelistRules {

    private final RuleEntityRepository ruleEntityRepository;

    @Autowired
    public WhitelistRules(final RuleEntityRepository ruleEntityRepository) {
        this.ruleEntityRepository = ruleEntityRepository;
    }

    public void isWhitelisted(ViolationEntity entity) {

        if (entity != null) {

            List<RuleEntity> rules = ruleEntityRepository.findAll(); // TODO: that are not expired

            for (RuleEntity rule : rules) {

                WhitelistRulesEvaluator whitelistRulesEvaluator = new WhitelistRulesEvaluator();

                if (whitelistRulesEvaluator.apply(rule, entity)){
                    entity.setRuleEntity(rule);
                    entity.setComment("Whitelisted automatically because of:" + rule.getReason());
                }

            }

        }
    }

}
