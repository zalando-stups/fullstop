package org.zalando.stups.fullstop.plugin.scm.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.zalando.kontrolletti.KontrollettiOperations;
import org.zalando.stups.clients.kio.KioOperations;
import org.zalando.stups.fullstop.events.UserDataProvider;
import org.zalando.stups.fullstop.plugin.scm.ScmRepositoryPlugin;
import org.zalando.stups.fullstop.violation.ViolationSink;
import org.zalando.stups.pierone.client.PieroneOperations;

@Configuration
public class ScmRepositoryPluginAutoConfiguration {

    @Bean
    public ScmRepositoryPlugin scmRepositoryPlugin(
            final ViolationSink violationSink,
            final KioOperations kioOperations,
            final PieroneOperations pieroneOperations,
            final KontrollettiOperations kontrollettiOperations,
            final UserDataProvider userDataProvider) {
        return new ScmRepositoryPlugin(
                violationSink, kioOperations, pieroneOperations,
                kontrollettiOperations,
                userDataProvider);
    }
}
