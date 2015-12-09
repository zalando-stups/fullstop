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

    private static final String ACTIVE = "Active";

    static final Predicate<AccessKeyMetadata> IS_ACTIVE = activity(ACTIVE);

    static Predicate<AccessKeyMetadata> activity(final String value) {
        return t -> value.equals(t.getStatus());
    }

    static Predicate<AccessKeyMetadata> withDaysOlderThan(final int days) {
        return t -> (t.getCreateDate().getTime() < LocalDate.now().minusDays(days).toDate().getTime());
    }

    static Predicate<AccessKeyMetadata> isActiveAndOlderThanDays(final int days) {
        return IS_ACTIVE.and(withDaysOlderThan(days));
    }
}
