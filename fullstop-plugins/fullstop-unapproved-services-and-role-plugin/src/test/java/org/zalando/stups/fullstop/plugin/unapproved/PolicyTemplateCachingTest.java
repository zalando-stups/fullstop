package org.zalando.stups.fullstop.plugin.unapproved;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.zalando.stups.fullstop.plugin.unapproved.impl.S3PolicyTemplatesProvider;
import org.zalando.stups.fullstop.s3.S3Service;

import java.util.List;

import static com.google.common.collect.Lists.newArrayList;
import static org.assertj.core.api.Assertions.assertThat;
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

        final List<String> s3Objects = policyTemplatesProvider.getPolicyTemplateNames();

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