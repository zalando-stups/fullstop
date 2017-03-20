package org.zalando.stups.fullstop.plugin.scm.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.zalando.stups.fullstop.plugin.EC2InstanceContextProvider;
import org.zalando.stups.fullstop.plugin.scm.Repositories;
import org.zalando.stups.fullstop.plugin.scm.ScmRepositoryPlugin;
import org.zalando.stups.fullstop.violation.ViolationSink;

@Configuration
@EnableConfigurationProperties(ScmRepositoryPluginProperties.class)
public class ScmRepositoryPluginAutoConfiguration {

    @Bean
    public Repositories repositories(final ScmRepositoryPluginProperties properties) {
        return new Repositories(properties);
    }

    @Bean
    public ScmRepositoryPlugin scmRepositoryPlugin(
            final EC2InstanceContextProvider contextProvider,
            final ViolationSink violationSink,
            final Repositories repositories,
            final ScmRepositoryPluginProperties properties) {
        return new ScmRepositoryPlugin(contextProvider, repositories, violationSink, properties);
    }
}
