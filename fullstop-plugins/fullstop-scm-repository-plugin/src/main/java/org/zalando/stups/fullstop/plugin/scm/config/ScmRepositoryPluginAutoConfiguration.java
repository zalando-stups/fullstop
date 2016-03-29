package org.zalando.stups.fullstop.plugin.scm.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.zalando.kontrolletti.KontrollettiOperations;
import org.zalando.stups.fullstop.plugin.EC2InstanceContextProvider;
import org.zalando.stups.fullstop.plugin.scm.ScmRepositoryPlugin;
import org.zalando.stups.fullstop.violation.ViolationSink;

@Configuration
public class ScmRepositoryPluginAutoConfiguration {

    @Bean
    public ScmRepositoryPlugin scmRepositoryPlugin(
            final EC2InstanceContextProvider contextProvider,
            final ViolationSink violationSink,
            final KontrollettiOperations kontrollettiOperations) {
        return new ScmRepositoryPlugin(contextProvider, kontrollettiOperations, violationSink);
    }
}
