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
package org.zalando.stups.fullstop.plugin.unapproved;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.zalando.stups.fullstop.plugin.unapproved.impl.S3PolicyTemplatesProvider;
import org.zalando.stups.fullstop.s3.S3Service;

import java.util.List;

import static com.google.common.collect.Lists.newArrayList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

/**
 * Created by mrandi.
 */
public class PolicyTemplateCachingTest {

    private PolicyTemplatesProvider policyTemplatesProvider;

    private S3Service s3ServiceMock;

    @Before
    public void setUp() throws Exception {
        s3ServiceMock = mock(S3Service.class);
        policyTemplatesProvider = new S3PolicyTemplatesProvider(s3ServiceMock);
    }

    @After
    public void tearDown() throws Exception {
        verifyNoMoreInteractions(s3ServiceMock);
    }

    @Test
    public void testGetS3Objects() throws Exception {
        when(s3ServiceMock.listS3Objects(any(), any())).thenReturn(newArrayList("test", "test"));

        List<String> s3Objects = policyTemplatesProvider.getPolicyTemplateNames();

        assertThat(s3Objects).isNotEmpty();

        verify(s3ServiceMock).listS3Objects(any(), any());
    }

    @Test
    public void testGetPolicyTemplate() throws Exception {
        when(s3ServiceMock.downloadObject(any(), any())).thenReturn("test object");

        policyTemplatesProvider.getPolicyTemplate("test");

        verify(s3ServiceMock).downloadObject(any(), any());
    }

}