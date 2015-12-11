package org.zalando.stups.fullstop.whitelist;

import static org.kie.internal.io.ResourceFactory.newClassPathResource;

import java.io.IOException;
import java.io.InputStream;

import org.drools.template.DataProviderCompiler;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.kie.api.KieServices;
import org.kie.api.builder.KieFileSystem;
import org.kie.api.runtime.StatelessKieSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContextException;
import org.zalando.stups.fullstop.rule.entity.RuleEntity;
import org.zalando.stups.fullstop.violation.ViolationType;
import org.zalando.stups.fullstop.violation.entity.ViolationEntity;
import org.zalando.stups.fullstop.violation.entity.ViolationTypeEntity;

import com.google.common.collect.Lists;

public class TaupageBuildRulesTest {

    private static final Logger LOG = LoggerFactory.getLogger(TaupageBuildRulesTest.class);

    private StatelessKieSession kieSession;

    @Before
    public void setUp() throws Exception {

        final KieServices kieServices = KieServices.Factory.get();
        final InputStream inputStream;
        try {
            inputStream = newClassPathResource("drools/TestRuleTemplate.drt")
                    .getInputStream();
        } catch (final IOException ex) {
            throw new ApplicationContextException("Couldn't find the rules template resource!", ex);
        }

        final ViolationTypeEntity violationTypeEntity = new ViolationTypeEntity(ViolationType.EC2_WITH_KEYPAIR);
        final RuleEntity entity = new RuleEntity();
        entity.setRuleName("1st Rule");
        entity.setAccountId("123");
        entity.setViolationTypeEntity(violationTypeEntity);
        final RuleDataProvider ruleDataProvider = new RuleDataProvider(Lists.newArrayList(entity));
        final String droolsFile = new DataProviderCompiler().compile(ruleDataProvider, inputStream);

        final KieFileSystem kieFileSystem = kieServices.newKieFileSystem();
        kieFileSystem.write("src/main/resources/drools/TestRule.drl", droolsFile);
        kieServices.newKieBuilder(kieFileSystem).buildAll();

        kieSession = kieServices.newKieContainer(kieServices.getRepository().getDefaultReleaseId())
                .newStatelessKieSession();

        kieSession.setGlobal("logger", LOG);
    }

    @Test
    public void testRuleMatches() throws Exception {
        final ViolationEntity violationEntity = new ViolationEntity("001", "123", "eu-west-1", "abc", new Object(),
                null,
                "testuser");
        final ViolationTypeEntity violationTypeEntity = new ViolationTypeEntity(ViolationType.EC2_WITH_KEYPAIR);
        violationEntity.setViolationTypeEntity(violationTypeEntity);

        kieSession.execute(violationEntity);

        Assert.assertEquals("Automatic resolution by whitelisting", violationEntity.getComment());
    }

    @Test
    public void testRuleMatchesNot() throws Exception {
        final ViolationEntity violationEntityPositive = new ViolationEntity("001", "123", "eu-west-1", "abc", new
                Object(),
                null,
                "testuser");
        final ViolationTypeEntity violationTypeEntityPositive = new ViolationTypeEntity(ViolationType.EC2_WITH_KEYPAIR);
        violationEntityPositive.setViolationTypeEntity(violationTypeEntityPositive);

        kieSession.execute(violationEntityPositive);

        Assert.assertEquals("Automatic resolution by whitelisting", violationEntityPositive.getComment());

        final ViolationEntity violationEntity = new ViolationEntity("002", "456", "eu-west-1", "abc"
                , new Object(), null, "testuser");
        final ViolationTypeEntity violationTypeEntity = new ViolationTypeEntity(ViolationType.EC2_WITH_KEYPAIR);
        violationEntity.setViolationTypeEntity(violationTypeEntity);

        kieSession.execute(violationEntity);

        Assert.assertNull(violationEntity.getComment());

        final ViolationEntity violationEntity2 = new ViolationEntity("002", "123", "eu-west-1", "abc"
                , new Object(), null, "testuser");
        final ViolationTypeEntity violationTypeEntity2 = new ViolationTypeEntity(ViolationType.ACTIVE_KEY_TOO_OLD);
        violationEntity2.setViolationTypeEntity(violationTypeEntity2);

        kieSession.execute(violationEntity2);

        Assert.assertNull(violationEntity.getComment());

        final ViolationEntity violationEntity3 = new ViolationEntity("002", "456", "eu-west-1", "abc"
                , new Object(), null, "testuser");
        final ViolationTypeEntity violationTypeEntity3 = new ViolationTypeEntity(ViolationType.ACTIVE_KEY_TOO_OLD);
        violationEntity3.setViolationTypeEntity(violationTypeEntity3);

        kieSession.execute(violationEntity3);

        Assert.assertNull(violationEntity.getComment());
    }
}