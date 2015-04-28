package org.zalando.stups.fullstop.jobs;

import static java.lang.String.format;

import java.util.function.Consumer;

import org.springframework.stereotype.Component;

import org.zalando.stups.fullstop.violation.ViolationStore;

import com.amazonaws.services.identitymanagement.model.AccessKeyMetadata;

/**
 * @author  jbellmann
 */
@Component
class AccessKeyMetadataConsumer implements Consumer<AccessKeyMetadata> {

    private static final String VIOLATION_MESSAGE = "User [%s] has an active key [%s] older than 1 week.";

    private final ViolationStore violationStore;

    AccessKeyMetadataConsumer(final ViolationStore violationStore) {
        this.violationStore = violationStore;
    }

    @Override
    public void accept(final AccessKeyMetadata input) {
        violationStore.save(format(VIOLATION_MESSAGE, input.getUserName(), input.getAccessKeyId()));
    }

}
