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
import org.zalando.stups.fullstop.jobs.annotation.EveryDayAtElevenPM;
import org.zalando.stups.fullstop.jobs.common.AccountIdSupplier;

import javax.annotation.PostConstruct;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * IAM Users must not use passwords, but access keys.
 */
@Component
public class NoPasswordsJob {

    private final Logger log = getLogger(NoPasswordsJob.class);

    private final IdentityManagementDataSource iamDataSource;

    private final NoPasswordViolationWriter violationWriter;

    private final AccountIdSupplier allAccountIds;

    @Autowired
    public NoPasswordsJob(final IdentityManagementDataSource iamDataSource,
                          final NoPasswordViolationWriter violationWriter, AccountIdSupplier allAccountIds) {
        this.iamDataSource = iamDataSource;
        this.violationWriter = violationWriter;
        this.allAccountIds = allAccountIds;
    }

    @PostConstruct
    public void init() {
        log.info("{} initialized", getClass().getSimpleName());
    }

    @EveryDayAtElevenPM
    public void check() {
        log.info("Running {}", getClass().getSimpleName());

        allAccountIds.get().forEach(accountId -> {
            log.info("Checking account {} for IAM users with passwords", accountId);
            iamDataSource.getUsers(accountId).stream()
                    .filter(user -> user.getPasswordLastUsed() != null)
                    .forEach(user -> violationWriter.writeViolation(accountId, user));
        });

        log.info("Finished {}", getClass().getSimpleName());
    }
}
