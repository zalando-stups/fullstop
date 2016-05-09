package org.zalando.stups.fullstop.jobs.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.zalando.stups.fullstop.aws.ClientProvider;
import org.zalando.stups.fullstop.jobs.common.SecurityGroupsChecker;
import org.zalando.stups.fullstop.jobs.common.impl.SecurityGroupsCheckerImpl;

import static org.zalando.stups.fullstop.jobs.utils.Predicates.securityGroupExposesNotAllowedPorts;

@Configuration
public class JobsConfig {

    @Autowired
    private JobsProperties jobsProperties;

    @Bean
    public SecurityGroupsChecker elbSecurityGroupsChecker(final ClientProvider clientProvider) {
        return new SecurityGroupsCheckerImpl(clientProvider, securityGroupExposesNotAllowedPorts(jobsProperties.getElbAllowedPorts()));
    }

    @Bean
    public SecurityGroupsChecker ec2SecurityGroupsChecker(final ClientProvider clientProvider) {
        return new SecurityGroupsCheckerImpl(clientProvider, securityGroupExposesNotAllowedPorts(jobsProperties.getEc2AllowedPorts()));
    }
}
