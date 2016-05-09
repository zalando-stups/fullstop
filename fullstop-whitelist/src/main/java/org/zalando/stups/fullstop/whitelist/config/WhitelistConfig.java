package org.zalando.stups.fullstop.whitelist.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.zalando.stups.fullstop.rule.service.RuleEntityService;
import org.zalando.stups.fullstop.whitelist.WhitelistRules;
import org.zalando.stups.fullstop.whitelist.WhitelistRulesEvaluator;

@Configuration
public class WhitelistConfig {


    @Autowired
    private RuleEntityService ruleEntityService;

    @Bean
    WhitelistRulesEvaluator whitelistRulesEvaluator() {
        return new WhitelistRulesEvaluator();
    }

    @Bean
    WhitelistRules whitelistRules() {
        return new WhitelistRules(whitelistRulesEvaluator(), ruleEntityService);
    }
}
