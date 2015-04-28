package org.zalando.stups.fullstop.jobs;

import java.util.function.Predicate;

import org.joda.time.LocalDate;

import com.amazonaws.services.identitymanagement.model.AccessKeyMetadata;

/**
 * Checks for {@link AccessKeyMetadata} implemented with {@link Predicate}.
 *
 * @author  jbellmann
 */
abstract class AccessKeyMetadataPredicates {

    private static final String ACTIVE = "Active";

    static final Predicate<AccessKeyMetadata> SEVEN_DAYS = withDaysOlderThan(7);

    static final Predicate<AccessKeyMetadata> IS_ACTIVE = activity(ACTIVE);

    static Predicate<AccessKeyMetadata> activity(final String value) {
        return new Predicate<AccessKeyMetadata>() {

            @Override
            public boolean test(final AccessKeyMetadata t) {
                return value.equals(t.getStatus());
            }
        };
    }

    static Predicate<AccessKeyMetadata> withDaysOlderThan(final int days) {
        return new Predicate<AccessKeyMetadata>() {
            @Override
            public boolean test(final AccessKeyMetadata t) {

                return (t.getCreateDate().getTime() < LocalDate.now().minusDays(days).toDate().getTime());
            }
        };
    }

    static Predicate<AccessKeyMetadata> isActiveAndWithDaysOlderThan(final int days) {
        return IS_ACTIVE.and(withDaysOlderThan(days));
    }
}
