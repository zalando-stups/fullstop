package org.zalando.stups.fullstop.plugin.provider.impl;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.zalando.stups.fullstop.plugin.EC2InstanceContext;
import org.zalando.stups.fullstop.plugin.provider.PieroneTagProvider;
import org.zalando.stups.pierone.client.PieroneOperations;
import org.zalando.stups.pierone.client.TagSummary;

import java.time.ZonedDateTime;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static com.google.common.collect.Maps.newHashMap;
import static org.mockito.Mockito.*;

public class PieroneTagProviderImplTest {

    private PieroneTagProvider pieroneTagProvider;

    private PieroneOperations pieroneOperationsMock;

    private EC2InstanceContext ec2InstanceContextMock;

    @Before
    public void setUp() {
        final Map<String, PieroneOperations> pieroneOperationsMap = newHashMap();
        pieroneOperationsMock = mock(PieroneOperations.class);

        pieroneOperationsMap.put("pierone.example.org", pieroneOperationsMock);
        pieroneTagProvider = new PieroneTagProviderImpl(pieroneOperationsMap::get);

        ec2InstanceContextMock = mock(EC2InstanceContext.class);
    }

    @After
    public void tearDown() {
        verifyNoMoreInteractions(ec2InstanceContextMock);
    }

    @Test
    public void testPieroneTagFound() throws Exception {
        when(ec2InstanceContextMock.getSource()).thenReturn(Optional.of("pierone.example.org/team/artifact:tag"));

        final Map<String, TagSummary> tagsMap = newHashMap();
        final TagSummary tagSummary = new TagSummary("name", ZonedDateTime.now(), "me");
        tagsMap.put("tag", tagSummary);

        when(pieroneOperationsMock.listTags(eq("team"), eq("artifact"))).thenReturn(tagsMap);


        final Optional<TagSummary> result = pieroneTagProvider.apply(ec2InstanceContextMock);
        assertThat(result).isPresent();

        verify(ec2InstanceContextMock).getSource();
        verify(pieroneOperationsMock).listTags(eq("team"), eq("artifact"));
    }

    @Test
    public void testPieroneTagNotFound() throws Exception {
        when(ec2InstanceContextMock.getSource()).thenReturn(Optional.of("opensource.example.org/team/artifact:tag"));

        final Optional<TagSummary> result = pieroneTagProvider.apply(ec2InstanceContextMock);
        assertThat(result).isEmpty();

        verify(ec2InstanceContextMock).getSource();
    }
}
