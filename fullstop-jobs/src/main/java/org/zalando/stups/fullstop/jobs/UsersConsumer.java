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
