package org.zalando.stups.fullstop.whitelist.config;

import static org.kie.internal.io.ResourceFactory.newClassPathResource;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.drools.template.DataProviderCompiler;
import org.kie.api.KieServices;
import org.kie.api.builder.KieFileSystem;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.StatelessKieSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContextException;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.Scheduled;
import org.zalando.stups.fullstop.rule.entity.RuleEntity;
import org.zalando.stups.fullstop.rule.repository.RuleEntityRepository;
import org.zalando.stups.fullstop.whitelist.RuleDataProvider;

@Configuration
public class WhitelistConfig {

    private static final Logger LOG = LoggerFactory.getLogger("whitelist-logger");

    @Autowired
    private RuleEntityRepository ruleEntityRepository;

    private KieServices kieServices;

    @Bean
    public StatelessKieSession statelessKieSession() {
        kieServices = KieServices.Factory.get();

        rebuildRules();

        final KieContainer kContainer = kieServices.newKieContainer(kieServices.getRepository().getDefaultReleaseId());
        final StatelessKieSession statelessKieSession = kContainer.newStatelessKieSession();
        statelessKieSession.setGlobal("logger", LOG);
        return statelessKieSession;
    }

    @Scheduled(fixedRate = 1000 * 60 * 30, initialDelay = 240_000) // 30 min rate, 4 min delay
    private void rebuildRules() {
        final InputStream inputStream;
        try {
            inputStream = newClassPathResource("drools/TaupageBuildRuleTemplate.drt")
                    .getInputStream();
        } catch (final IOException ex) {
            throw new ApplicationContextException("Couldn't find the rules template resource!", ex);
        }
        final List<RuleEntity> ruleEntities = ruleEntityRepository.findAll();
        final RuleDataProvider ruleDataProvider = new RuleDataProvider(ruleEntities);
        final String droolsFile = new DataProviderCompiler().compile(ruleDataProvider, inputStream);
        final KieFileSystem kieFileSystem = kieServices.newKieFileSystem();
        kieFileSystem.write("src/main/resources/drools/TaupageBuildRuleTemplate.drl", droolsFile);
        kieServices.newKieBuilder(kieFileSystem).buildAll();
    }
}
