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
package org.zalando.stups.fullstop.jobs.rds;

import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.rds.AmazonRDSClient;
import com.amazonaws.services.rds.model.DBInstance;
import com.amazonaws.services.rds.model.DescribeDBInstancesRequest;
import com.amazonaws.services.rds.model.DescribeDBInstancesResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.zalando.stups.fullstop.aws.ClientProvider;
import org.zalando.stups.fullstop.jobs.common.AccountIdSupplier;
import org.zalando.stups.fullstop.jobs.config.JobsProperties;
import org.zalando.stups.fullstop.violation.Violation;
import org.zalando.stups.fullstop.violation.ViolationBuilder;
import org.zalando.stups.fullstop.violation.ViolationSink;

import javax.annotation.PostConstruct;
import java.util.Map;

import static com.google.common.collect.Maps.newHashMap;
import static org.zalando.stups.fullstop.violation.ViolationType.UNSECURED_ENDPOINT;

@Component
public class FetchRdsJob {


    private static final String EVENT_ID = "checkRdsJob";

    private final Logger log = LoggerFactory.getLogger(FetchRdsJob.class);

    private final AccountIdSupplier allAccountIds;

    private final ClientProvider clientProvider;

    private final JobsProperties jobsProperties;

    private final ViolationSink violationSink;

    @Autowired
    public FetchRdsJob(AccountIdSupplier allAccountIds, ClientProvider clientProvider,
                       JobsProperties jobsProperties,
                       ViolationSink violationSink) {
        this.allAccountIds = allAccountIds;
        this.clientProvider = clientProvider;
        this.jobsProperties = jobsProperties;
        this.violationSink = violationSink;
    }

    @PostConstruct
    public void init() {
        log.info("{} initialized", getClass().getSimpleName());
    }

    @Scheduled(fixedRate = 300_000)
    public void check() {
        for (final String accountId : allAccountIds.get()) {
            Map<String, Object> metadata = newHashMap();
            for (String region : jobsProperties.getWhitelistedRegions()) {
                DescribeDBInstancesResult describeDBInstancesResult = getRds(accountId, region);
                describeDBInstancesResult.getDBInstances().stream().filter(DBInstance::getPubliclyAccessible).forEach(dbInstance -> {
                    metadata.put("unsecuredDatabase", dbInstance.getEndpoint().getAddress());
                    metadata.put("errorMessages", "Unsecured Database! Your DB can be reached from outside");
                    writeViolation(accountId, region, metadata, dbInstance.getEndpoint().getAddress());

                });

            }
        }
    }

    private void writeViolation(String account, String region, Object metaInfo, String rdsEndpoint) {
        ViolationBuilder violationBuilder = new ViolationBuilder();
        Violation violation = violationBuilder.withAccountId(account)
                .withRegion(region)
                .withPluginFullyQualifiedClassName(FetchRdsJob.class)
                .withType(UNSECURED_ENDPOINT)
                .withMetaInfo(metaInfo)
                .withEventId(EVENT_ID)
                .withInstanceId(rdsEndpoint)
                .build();
        violationSink.put(violation);
    }

    private DescribeDBInstancesResult getRds(String accountId, String region) {
        DescribeDBInstancesRequest describeDBInstancesRequest = new DescribeDBInstancesRequest();
        DescribeDBInstancesResult describeDBInstancesResult;

        AmazonRDSClient amazonRDSClient = clientProvider.getClient(AmazonRDSClient.class, accountId,
                Region.getRegion(Regions.fromName(region)));
        describeDBInstancesResult = amazonRDSClient.describeDBInstances(describeDBInstancesRequest);


        return describeDBInstancesResult;
    }
}
