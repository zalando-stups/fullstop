/**
 * Copyright (C) 2015 Zalando SE (http://tech.zalando.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
    public SecurityGroupsChecker elbSecurityGroupsChecker(ClientProvider clientProvider) {
        return new SecurityGroupsCheckerImpl(clientProvider, securityGroupExposesNotAllowedPorts(jobsProperties.getElbAllowedPorts()));
    }

    @Bean
    public SecurityGroupsChecker ec2SecurityGroupsChecker(ClientProvider clientProvider) {
        return new SecurityGroupsCheckerImpl(clientProvider, securityGroupExposesNotAllowedPorts(jobsProperties.getEc2AllowedPorts()));
    }
}
