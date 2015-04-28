package org.zalando.stups.fullstop.jobs;

import static org.zalando.stups.fullstop.jobs.AccessKeyMetadataPredicates.isActiveAndWithDaysOlderThan;

import java.util.List;
import java.util.function.Predicate;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.stereotype.Component;

import org.zalando.stups.fullstop.jobs.annotation.EveryDayAtElevenPM;

import com.amazonaws.services.identitymanagement.model.AccessKeyMetadata;
import com.amazonaws.services.identitymanagement.model.ListAccessKeysResult;

/**
 * @author  jbellmann
 */
@Component
public class KeyRotationJob {

    private final IdentityManagementDataSource dataSource;

    private final AccessKeyMetadataConsumer accessKeyMetadataConsumer;

    private final Predicate<AccessKeyMetadata> check = isActiveAndWithDaysOlderThan(7);

    @Autowired
    public KeyRotationJob(final IdentityManagementDataSource dataSource,
            final AccessKeyMetadataConsumer accessKeyMeatadataConsumer) {
        this.accessKeyMetadataConsumer = accessKeyMeatadataConsumer;
        this.dataSource = dataSource;
    }

    /**
     * Runs periodically.
     */
    @EveryDayAtElevenPM
    public void check() {
        for (Tuple<String, ListAccessKeysResult> tuple : getListAccessKeyResultPerAccount()) {
            filter(tuple._2.getAccessKeyMetadata());
        }
    }

    protected void filter(final List<AccessKeyMetadata> accessKeyMeatadataList) {
        accessKeyMeatadataList.stream().filter(check).forEach(accessKeyMetadataConsumer);
    }

    protected List<Tuple<String, ListAccessKeysResult>> getListAccessKeyResultPerAccount() {
        return dataSource.getListAccessKeysResultPerAccountWithTuple();
    }
}
