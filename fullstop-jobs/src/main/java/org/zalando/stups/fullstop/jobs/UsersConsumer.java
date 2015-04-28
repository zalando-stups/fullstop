/**
 * Copyright 2015 Zalando SE
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.zalando.stups.fullstop.jobs;

import java.util.function.Consumer;

import org.zalando.stups.fullstop.violation.ViolationStore;

import com.amazonaws.services.identitymanagement.model.User;

/**
 * @author  jbellmann
 */
class UsersConsumer implements Consumer<User> {

    private final ViolationStore violationStore;
    private final String accountId;

    UsersConsumer(final ViolationStore violationStore, final String accountId) {
        this.violationStore = violationStore;
        this.accountId = accountId;
    }

    @Override
    public void accept(final User t) {
        String message = String.format("Password was used by %s with accountId : %s", t.getUserName(), accountId);
        violationStore.save(message);
    }

}
