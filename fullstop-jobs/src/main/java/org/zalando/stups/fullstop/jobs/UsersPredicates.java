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
