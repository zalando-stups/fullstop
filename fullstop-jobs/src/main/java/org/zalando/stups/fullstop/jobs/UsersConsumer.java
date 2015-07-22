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

import com.amazonaws.services.identitymanagement.model.User;
import org.zalando.stups.fullstop.violation.ViolationBuilder;
import org.zalando.stups.fullstop.violation.ViolationSink;

import java.util.function.Consumer;

import static org.zalando.stups.fullstop.violation.ViolationType.PASSWORD_USED;

/**
 * @author jbellmann
 */
class UsersConsumer implements Consumer<User> {

    private final ViolationSink violationSink;

    private final String accountId;

    UsersConsumer(final ViolationSink violationSink, final String accountId) {
        this.violationSink = violationSink;
        this.accountId = accountId;
    }

    @Override
    public void accept(final User t) {
        violationSink.put(
                new ViolationBuilder().withAccountId(accountId)
                                      .withType(PASSWORD_USED)
                                      .withMetaInfo(t.getUserName())
                                      .build());
    }

}
