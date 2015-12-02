package org.zalando.stups.fullstop.plugin.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.zalando.stups.fullstop.aws.ClientProvider;
import org.zalando.stups.fullstop.plugin.SaveSecurityGroupsPlugin;
import org.zalando.stups.fullstop.plugin.SecurityGroupProvider;

/**
 * @author jbellmann
 */
@Configuration
@ComponentScan(basePackageClasses = { SaveSecurityGroupsPlugin.class })
public class SaveSecurityGroupsPluginAutoConfiguration {

    @Autowired
    private ClientProvider clientProvider;

    @ConditionalOnMissingBean
    @Bean
    public SecurityGroupProvider securityGroupProvider() {
        return new SecurityGroupProvider(clientProvider);
    }
}
