package org.zalando.stups.fullstop.plugin.provider.impl;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.zalando.stups.fullstop.plugin.EC2InstanceContext;
import org.zalando.stups.fullstop.plugin.provider.ScmSourceProvider;
import org.zalando.stups.pierone.client.PieroneOperations;

import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static com.google.common.collect.Maps.newHashMap;
import static org.mockito.Mockito.*;

public class ScmSourceProviderImplTest {

    private ScmSourceProvider scmSourceProvider;

    private EC2InstanceContext ec2InstanceContextMock;

    @Before
    public void setUp() {

        Map<String, PieroneOperations> pieroneOperationsMap = newHashMap();
        PieroneOperations pieroneOperationsMock = mock(PieroneOperations.class);

        pieroneOperationsMap.put("pierone.example.org", pieroneOperationsMock);
        scmSourceProvider = new ScmSourceProviderImpl(pieroneOperationsMap::get);

        ec2InstanceContextMock = mock(EC2InstanceContext.class);
    }

    @After
    public void tearDown() {
        verifyNoMoreInteractions(ec2InstanceContextMock);
    }

    @Test
    public void testScmSourceNotFound() {
        when(ec2InstanceContextMock.getSource()).thenReturn(Optional.of("opensource.example.org/team/artifact:tag"));
        Optional<Map<String, String>> result = scmSourceProvider.apply(ec2InstanceContextMock);

        assertThat(result).isEmpty();

        verify(ec2InstanceContextMock).getSource();
    }

    @Test
    public void testScmSource() {
        when(ec2InstanceContextMock.getSource()).thenReturn(Optional.of("pierone.example.org/team/artifact:tag"));

        Optional<Map<String, String>> result = scmSourceProvider.apply(ec2InstanceContextMock);

        assertThat(result).isPresent();

        verify(ec2InstanceContextMock).getSource();
    }
}
