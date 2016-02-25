package org.zalando.stups.fullstop.whitelist;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.zalando.stups.fullstop.rule.entity.RuleEntity;
import org.zalando.stups.fullstop.violation.Violation;
import org.zalando.stups.fullstop.violation.ViolationBuilder;

import static org.junit.Assert.*;

public class WhitelistRulesEvaluatorTest {

    Violation violation;
    RuleEntity ruleEntity;

    @Before
    public void setUp() throws Exception {
        violation = new ViolationBuilder().build();
        ruleEntity = new RuleEntity();
    }

    @After
    public void tearDown() throws Exception {

    }

    @Test
    public void testApply() throws Exception {

    }
}