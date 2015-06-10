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
package org.zalando.stups.fullstop.jobs;

import com.amazonaws.services.identitymanagement.model.AccessKeyMetadata;
import com.amazonaws.services.identitymanagement.model.ListAccessKeysResult;
import org.assertj.core.util.Lists;
import org.joda.time.LocalDate;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.zalando.stups.fullstop.violation.ViolationSink;

import java.util.Date;
import java.util.List;

import static org.mockito.Mockito.*;

/**
 * @author jbellmann
 */
public class KeyRotationJobTest {

    private IdentityManagementDataSource identityManagementDataSource;

    private AccessKeyMetadataConsumer accessKeyMetadataConsumer;

    private ViolationSink violationSink;

    @Before
    public void setUp() {
        this.violationSink = Mockito.mock(ViolationSink.class);
        this.accessKeyMetadataConsumer = new AccessKeyMetadataConsumer(this.violationSink);
        this.identityManagementDataSource = Mockito.mock(IdentityManagementDataSource.class);
    }

    @Test
    public void testSimple() {
        when(identityManagementDataSource.getListAccessKeysResultPerAccountWithTuple()).thenReturn(getList());

        KeyRotationJob job = new KeyRotationJob(identityManagementDataSource, accessKeyMetadataConsumer);

        job.check();

        verify(identityManagementDataSource, atLeastOnce()).getListAccessKeysResultPerAccountWithTuple();
        verify(violationSink, atLeastOnce()).put(Mockito.anyObject());
    }

    protected List<Tuple<String, ListAccessKeysResult>> getList() {
        List<Tuple<String, ListAccessKeysResult>> result = Lists.newArrayList();
        for (int i = 0; i < 3; i++) {

            ListAccessKeysResult listAccessKeysResult = buildAccessKeysResult();
// ListAccessKeyResultPerAccount la = new ListAccessKeyResultPerAccount("123456" + i, listAccessKeysResult);
            result.add(new Tuple<String, ListAccessKeysResult>("123456" + i, listAccessKeysResult));

        }

        return result;
    }

    protected ListAccessKeysResult buildAccessKeysResult() {
        ListAccessKeysResult result = new ListAccessKeysResult();
        List<AccessKeyMetadata> accessKeyMetadata = Lists.newArrayList();
        for (int i = 0; i < 10; i++) {
            AccessKeyMetadata metadata = new AccessKeyMetadata();
            metadata.setAccessKeyId("1234" + i);
            metadata.setCreateDate(new Date(LocalDate.now().minusDays(5 + i).toDate().getTime()));
            metadata.setUserName("tester-" + i);
            metadata.setStatus(i % 2 == 0 ? "Active" : "Inactive");
            accessKeyMetadata.add(metadata);
        }

        result.setAccessKeyMetadata(accessKeyMetadata);

        return result;
    }

}
