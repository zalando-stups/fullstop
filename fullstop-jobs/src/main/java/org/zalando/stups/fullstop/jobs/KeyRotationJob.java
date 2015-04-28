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
