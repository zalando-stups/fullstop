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
package org.zalando.stups.fullstop.jobs.iam;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.zalando.stups.fullstop.jobs.FullstopJob;
import org.zalando.stups.fullstop.jobs.annotation.EveryDayAtTenPM;
import org.zalando.stups.fullstop.jobs.common.AccountIdSupplier;
import org.zalando.stups.fullstop.jobs.config.JobsProperties;

import javax.annotation.PostConstruct;
import java.util.Collection;

import static org.slf4j.LoggerFactory.getLogger;
import static org.zalando.stups.fullstop.jobs.iam.AccessKeyMetadataPredicates.isActiveAndOlderThanDays;

@Component
public class KeyRotationJob implements FullstopJob {

    private final Logger log = getLogger(KeyRotationJob.class);

    private final IdentityManagementDataSource iamDataSource;

    private final KeyRotationViolationWriter violationWriter;

    private final JobsProperties properties;

    private final AccountIdSupplier allAccountIds;

    @Autowired
    public KeyRotationJob(final IdentityManagementDataSource iamDataSource, KeyRotationViolationWriter violationWriter, JobsProperties properties, AccountIdSupplier allAccountIds) {
        this.violationWriter = violationWriter;
        this.iamDataSource = iamDataSource;
        this.properties = properties;
        this.allAccountIds = allAccountIds;
    }

    @PostConstruct
    public void init() {
        log.info("{} initialized", getClass().getSimpleName());
    }

    @EveryDayAtTenPM
    public void run() {
        log.info("Running {}", getClass().getSimpleName());

        allAccountIds.get().forEach(accountId -> {
            log.info("Checking account {} for expired IAM access keys", accountId);
            iamDataSource.getUsers(accountId).stream()
                    .map(u -> iamDataSource.getAccessKeys(accountId, u.getUserName()))
                    .flatMap(Collection::stream)
                    .filter(isActiveAndOlderThanDays(properties.getAccessKeysExpireAfterDays()))
                    .forEach(accessKey -> violationWriter.writeViolation(accountId, accessKey));
        });

        log.info("Finished {}", getClass().getSimpleName());
    }
}
