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
package org.zalando.stups.fullstop.jobs;

import com.amazonaws.services.identitymanagement.model.ListUsersResult;
import com.amazonaws.services.identitymanagement.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.zalando.stups.fullstop.jobs.annotation.EveryDayAtElevenPM;
import org.zalando.stups.fullstop.violation.ViolationSink;

import javax.annotation.PostConstruct;
import java.util.List;

import static org.zalando.stups.fullstop.jobs.UsersPredicates.PASSWORD_LAST_USED_HAS_NON_NULL_DATE;

/**
 * @author jbellmann
 */
// @Component
public class NoPasswordsJob {

    private final Logger log = LoggerFactory.getLogger(NoPasswordsJob.class);

    private final ViolationSink violationSink;

    private final IdentityManagementDataSource identityManagementDataSource;

    @Autowired
    public NoPasswordsJob(final ViolationSink violationSink,
            final IdentityManagementDataSource identityManagementDataSource) {
        this.identityManagementDataSource = identityManagementDataSource;
        this.violationSink = violationSink;
    }

    @PostConstruct
    public void init() {
        log.info("{} initialized", getClass().getSimpleName());
    }

    @EveryDayAtElevenPM
    public void check() {

        log.info("Running Job {}", getClass().getSimpleName());
        for (Tuple<String, ListUsersResult> listUsersResultPerAccount : getListUsersResultPerAccount()) {
            filter(listUsersResultPerAccount._1, listUsersResultPerAccount._2.getUsers());
        }

    }

    protected void filter(final String accountId, final List<User> users) {
        final UsersConsumer consumer = new UsersConsumer(violationSink, accountId);
        users.stream().filter(PASSWORD_LAST_USED_HAS_NON_NULL_DATE).forEach(consumer);
    }

    protected List<Tuple<String, ListUsersResult>> getListUsersResultPerAccount() {
        return this.identityManagementDataSource.getListUsersResultPerAccountWithTuple();
    }
}
