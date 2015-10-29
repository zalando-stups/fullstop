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

import com.amazonaws.services.identitymanagement.model.User;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.zalando.stups.fullstop.violation.ViolationBuilder;
import org.zalando.stups.fullstop.violation.ViolationSink;

import static java.util.Collections.singletonMap;
import static org.slf4j.LoggerFactory.getLogger;
import static org.zalando.stups.fullstop.violation.ViolationType.PASSWORD_USED;

@Component
public class NoPasswordViolationWriter {

    private final Logger log = getLogger(getClass());

    public static final String NO_REGION = "no-region";

    private final ViolationSink violationSink;

    @Autowired
    public NoPasswordViolationWriter(ViolationSink violationSink) {
        this.violationSink = violationSink;
    }

    public void writeViolation(String accountId, User user) {
        log.info("Found IAM user {} that has a password in account {}", user.getUserName(), accountId);
        violationSink.put(
                new ViolationBuilder()
                        .withEventId("check-iam-user_" + user.getUserId())
                        .withAccountId(accountId)
                        .withRegion(NO_REGION)
                        .withPluginFullyQualifiedClassName(NoPasswordsJob.class)
                        .withType(PASSWORD_USED)
                        .withMetaInfo(singletonMap("user_name", user.getUserName()))
                        .build());
    }
}
