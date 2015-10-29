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
import org.junit.Before;
import org.junit.Test;
import org.zalando.stups.fullstop.jobs.config.JobsProperties;

import java.util.Date;
import java.util.List;
import java.util.Map;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.assertj.core.util.Maps.newHashMap;
import static org.joda.time.DateTime.now;
import static org.mockito.Mockito.*;

public class KeyRotationJobTest {

    private IdentityManagementDataSource mockIAMDataSource;
    private KeyRotationViolationWriter mockViolationWriter;
    private Map<String, List<AccessKeyMetadata>> accessKeys;

    @Before
    public void setUp() {
        mockIAMDataSource = mock(IdentityManagementDataSource.class);
        mockViolationWriter = mock(KeyRotationViolationWriter.class);
        accessKeys = newHashMap();
        accessKeys.put("account01", asList(
                upToDate(active(new AccessKeyMetadata())),
                expired(active(new AccessKeyMetadata())),
                expired(inactive(new AccessKeyMetadata()))));
        accessKeys.put("account02", singletonList(upToDate(inactive(new AccessKeyMetadata()))));
    }

    private AccessKeyMetadata expired(AccessKeyMetadata accessKeyMetadata) {
        accessKeyMetadata.setCreateDate(now().minusDays(10).toDate());
        return accessKeyMetadata;
    }

    private AccessKeyMetadata upToDate(AccessKeyMetadata accessKeyMetadata) {
        accessKeyMetadata.setCreateDate(new Date());
        return accessKeyMetadata;
    }

    private AccessKeyMetadata inactive(AccessKeyMetadata accessKeyMetadata) {
        accessKeyMetadata.setStatus("Inactive");
        return accessKeyMetadata;
    }

    private AccessKeyMetadata active(AccessKeyMetadata accessKeyMetadata) {
        accessKeyMetadata.setStatus("Active");
        return accessKeyMetadata;
    }


    @Test
    public void testSimple() {
        when(mockIAMDataSource.getAccessKeysByAccount()).thenReturn(accessKeys);

        new KeyRotationJob(mockIAMDataSource, mockViolationWriter, new JobsProperties()).check();

        verify(mockIAMDataSource).getAccessKeysByAccount();
        verify(mockViolationWriter).writeViolation(eq("account01"), any());
    }
}
