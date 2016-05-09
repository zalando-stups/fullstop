package org.zalando.stups.fullstop.whitelist;

import org.springframework.beans.factory.annotation.Autowired;
import org.zalando.stups.fullstop.rule.entity.RuleEntity;
import org.zalando.stups.fullstop.rule.service.RuleEntityService;
import org.zalando.stups.fullstop.violation.entity.ViolationEntity;

import java.util.List;

public class WhitelistRules {

    private final WhitelistRulesEvaluator whitelistRulesEvaluator;

    private final RuleEntityService ruleEntityService;

    @Autowired
    public WhitelistRules(final WhitelistRulesEvaluator whitelistRulesEvaluator, final RuleEntityService ruleEntityService) {
        this.whitelistRulesEvaluator = whitelistRulesEvaluator;
        this.ruleEntityService = ruleEntityService;
    }

    public void execute(final ViolationEntity violationEntity) {

        if (violationEntity != null) {

            final List<RuleEntity> rules = ruleEntityService.findByNotExpired();
            if (rules == null) {
                return;
            }

            for (final RuleEntity rule : rules) {

                if (whitelistRulesEvaluator.apply(rule, violationEntity)){
                    violationEntity.setRuleEntity(rule);
                    violationEntity.setComment("Whitelisted automatically because of:" + rule.getReason());
                    return;
                }

            }

        }
    }

}
