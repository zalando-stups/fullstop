/**
 * Copyright (C) 2015 Zalando SE (http://tech.zalando.com)
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
package org.zalando.stups.fullstop.jobs;

import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.elasticloadbalancing.AmazonElasticLoadBalancingClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.zalando.stups.fullstop.aws.ClientProvider;
import org.zalando.stups.fullstop.jobs.annotation.OnceAnHour;
import org.zalando.stups.fullstop.jobs.config.JobsConfig;
import org.zalando.stups.fullstop.teams.TeamOperations;
import org.zalando.stups.fullstop.violation.ViolationSink;

import javax.annotation.PostConstruct;
import java.util.List;

/**
 * Created by gkneitschel.
 */
@Component
public class FetchElasticLoadBalancersJob {

    private final Logger log = LoggerFactory.getLogger(FetchElasticLoadBalancersJob.class);

    private ViolationSink violationSink;

    private ClientProvider clientProvider;

    private TeamOperations teamOperations;

    private JobsConfig jobsConfig;

    private List<String> accountId;

    @Autowired
    public FetchElasticLoadBalancersJob(ViolationSink violationSink,
            ClientProvider clientProvider, TeamOperations teamOperations, JobsConfig jobsConfig) {
        this.violationSink = violationSink;
        this.clientProvider = clientProvider;
        this.teamOperations = teamOperations;
        this.jobsConfig = jobsConfig;
    }

    @PostConstruct
    public void init() {
        log.info("{} initalized", getClass().getSimpleName());
    }

    @OnceAnHour
    public void check(){
        log.info("Running job {}", getClass().getSimpleName());
        for (String account : accountId) {
            for (String region : jobsConfig.getWhitelistedRegions()) {
                clientProvider.getClient(AmazonElasticLoadBalancingClient.class, account, Region.getRegion(Regions.fromName(region)));

            }


        }

    }
}
