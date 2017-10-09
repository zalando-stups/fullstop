package org.zalando.stups.fullstop.plugin.impl;

import com.amazonaws.services.cloudtrail.processinglibrary.model.CloudTrailEvent;
import com.google.common.collect.Lists;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.zalando.stups.fullstop.aws.ClientProvider;
import org.zalando.stups.fullstop.plugin.EC2InstanceContext;
import org.zalando.stups.fullstop.plugin.provider.AmiIdProvider;
import org.zalando.stups.fullstop.plugin.provider.AmiProvider;
import org.zalando.stups.fullstop.plugin.provider.KioApplicationProvider;
import org.zalando.stups.fullstop.plugin.provider.PieroneTagProvider;
import org.zalando.stups.fullstop.plugin.provider.ScmSourceProvider;
import org.zalando.stups.fullstop.plugin.provider.TaupageYamlProvider;
import org.zalando.stups.fullstop.taupage.TaupageYaml;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class EC2InstanceContextImplTest {

    private TaupageYaml taupageYaml;
    private TaupageYamlProvider taupageYamlProviderMock;
    private CloudTrailEvent eventMock;
    private final String instanceJsonMock = "";
    private ClientProvider clientProviderMock;
    private AmiIdProvider amiIdProviderMock;
    private AmiProvider amiProviderMock;
    private final String taupageNamePrefixMock = "";
    private final List<String> taupageOwnersMock = Lists.newArrayList("");
    private KioApplicationProvider kioApplicationProviderMock;
    private PieroneTagProvider pieroneTagProviderMock;
    private ScmSourceProvider scmSourceProviderMock;

    @Before
    public void setUp() throws Exception {
        taupageYaml = new TaupageYaml("fullstop", "10.0", "Docker", "stups/fullstop:10.0");

        taupageYamlProviderMock = mock(TaupageYamlProvider.class);
        eventMock = mock(CloudTrailEvent.class);
        clientProviderMock = mock(ClientProvider.class);
        amiIdProviderMock = mock(AmiIdProvider.class);
        amiProviderMock = mock(AmiProvider.class);
        kioApplicationProviderMock = mock(KioApplicationProvider.class);
        pieroneTagProviderMock = mock(PieroneTagProvider.class);
        scmSourceProviderMock = mock(ScmSourceProvider.class);
    }

    @After
    public void tearDown() throws Exception {

    }

    @Test
    public void testGetVersionId() throws Exception {
        when(taupageYamlProviderMock.apply(any(EC2InstanceContext.class))).thenReturn(Optional.of(taupageYaml));
        final EC2InstanceContext ec2InstanceContext = new EC2InstanceContextImpl(
                eventMock, instanceJsonMock, clientProviderMock, amiIdProviderMock, amiProviderMock, taupageYamlProviderMock,
                taupageNamePrefixMock, taupageOwnersMock, kioApplicationProviderMock,
                pieroneTagProviderMock, scmSourceProviderMock);
        final Optional<String> versionId = ec2InstanceContext.getVersionId();
        assertThat(versionId).isPresent();
        assertThat(versionId.get()).isExactlyInstanceOf(String.class);
        assertThat(versionId.get()).isEqualTo("10.0");
    }

    @Test
    public void TestNull() throws Exception{
        taupageYaml = new TaupageYaml("fullstop", null, "Docker", "stups/fullstop:10.0");

        when(taupageYamlProviderMock.apply(any(EC2InstanceContext.class))).thenReturn(Optional.of(taupageYaml));
        final EC2InstanceContext ec2InstanceContext = new EC2InstanceContextImpl(
                eventMock, instanceJsonMock, clientProviderMock, amiIdProviderMock, amiProviderMock, taupageYamlProviderMock,
                taupageNamePrefixMock, taupageOwnersMock, kioApplicationProviderMock,
                pieroneTagProviderMock, scmSourceProviderMock);
        final Optional<String> versionId = ec2InstanceContext.getVersionId();
        assertThat(versionId).isEmpty();
    }
}
