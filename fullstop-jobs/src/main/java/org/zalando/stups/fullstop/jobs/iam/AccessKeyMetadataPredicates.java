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

import com.amazonaws.services.identitymanagement.model.AccessKeyMetadata;
import org.joda.time.LocalDate;

import java.util.function.Predicate;

/**
 * Checks for {@link AccessKeyMetadata} implemented with {@link Predicate}.
 *
 * @author jbellmann
 */
abstract class AccessKeyMetadataPredicates {

    static final Predicate<AccessKeyMetadata> SEVEN_DAYS = withDaysOlderThan(7);

    private static final String ACTIVE = "Active";

    static final Predicate<AccessKeyMetadata> IS_ACTIVE = activity(ACTIVE);

    static Predicate<AccessKeyMetadata> activity(final String value) {
        return t -> value.equals(t.getStatus());
    }

    static Predicate<AccessKeyMetadata> withDaysOlderThan(final int days) {
        return t -> (t.getCreateDate().getTime() < LocalDate.now().minusDays(days).toDate().getTime());
    }

    static Predicate<AccessKeyMetadata> isActiveAndWithDaysOlderThan(final int days) {
        return IS_ACTIVE.and(withDaysOlderThan(days));
    }
}
