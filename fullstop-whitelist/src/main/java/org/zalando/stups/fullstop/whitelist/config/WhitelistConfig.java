package org.zalando.stups.fullstop.whitelist.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.zalando.stups.fullstop.rule.repository.RuleEntityRepository;

@Configuration
public class WhitelistConfig {

    private static final Logger LOG = LoggerFactory.getLogger("whitelist-logger");

    @Autowired
    private RuleEntityRepository ruleEntityRepository;


}
