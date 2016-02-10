package org.zalando.stups.fullstop.whitelist;

import org.springframework.beans.factory.annotation.Autowired;
import org.zalando.stups.fullstop.rule.entity.RuleEntity;
import org.zalando.stups.fullstop.rule.repository.RuleEntityRepository;
import org.zalando.stups.fullstop.violation.entity.ViolationEntity;

import java.util.List;

public class WhitelistRules {

    private final RuleEntityRepository ruleEntityRepository;

    private final WhitelistRulesEvaluator whitelistRulesEvaluator;

    @Autowired
    public WhitelistRules(final RuleEntityRepository ruleEntityRepository, final WhitelistRulesEvaluator whitelistRulesEvaluator) {
        this.ruleEntityRepository = ruleEntityRepository;
        this.whitelistRulesEvaluator = whitelistRulesEvaluator;
    }

    public void execute(ViolationEntity violationEntity) {

        if (violationEntity != null) {

            List<RuleEntity> rules = ruleEntityRepository.findAll(); // TODO: that are not expired

            for (RuleEntity rule : rules) {

                if (whitelistRulesEvaluator.apply(rule, violationEntity)){
                    violationEntity.setRuleEntity(rule);
                    violationEntity.setComment("Whitelisted automatically because of:" + rule.getReason());
                    return;
                }

            }

        }
    }

}
