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

import java.util.function.Predicate;

import com.amazonaws.services.identitymanagement.model.User;

/**
 * @author  jbellmann
 */
abstract class UsersPredicates {

    static final Predicate<User> PASSWORD_LAST_USED_HAS_NON_NULL_DATE = new Predicate<User>() {
        @Override
        public boolean test(final User t) {
            return t.getPasswordLastUsed() != null;
        }
    };

}