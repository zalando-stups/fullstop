package org.zalando.stups.fullstop.plugin.impl;

import com.amazonaws.services.cloudtrail.processinglibrary.model.CloudTrailEvent;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.zalando.stups.fullstop.aws.ClientProvider;
import org.zalando.stups.fullstop.plugin.EC2InstanceContext;
import org.zalando.stups.fullstop.plugin.provider.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

public class EC2InstanceContextImplTest {

    private Map<String, Double> USER_DATA = Maps.newHashMap();

    private TaupageYamlProvider taupageYamlProviderMock;
    private CloudTrailEvent eventMock;
    private String instanceJsonMock = "";
    private ClientProvider clientProviderMock;
    private AmiIdProvider amiIdProviderMock;
    private AmiProvider amiProviderMock;
    private String taupageNamePrefixMock = "";
    private List<String> taupageOwnersMock = Lists.newArrayList("");
    private KioApplicationProvider kioApplicationProviderMock;
    private KioVersionProvider kioVersionProviderMock;
    private KioApprovalProvider kioApprovalProviderMock;
    private PieroneTagProvider pieroneTagProviderMock;
    private ScmSourceProvider scmSourceProviderMock;

    @Before
    public void setUp() throws Exception {
        USER_DATA.put("application_version", 10.0);
        taupageYamlProviderMock = mock(TaupageYamlProvider.class);
        eventMock = mock(CloudTrailEvent.class);
        clientProviderMock = mock(ClientProvider.class);
        amiIdProviderMock = mock(AmiIdProvider.class);
        amiProviderMock = mock(AmiProvider.class);
        kioApplicationProviderMock = mock(KioApplicationProvider.class);
        kioVersionProviderMock = mock(KioVersionProvider.class);
        kioApprovalProviderMock = mock(KioApprovalProvider.class);
        pieroneTagProviderMock = mock(PieroneTagProvider.class);
        scmSourceProviderMock = mock(ScmSourceProvider.class);
    }

    @After
    public void tearDown() throws Exception {

    }

    @Test
    public void testGetVersionId() throws Exception {
        when(taupageYamlProviderMock.apply(any(EC2InstanceContext.class))).thenReturn(Optional.of(USER_DATA));
        EC2InstanceContext ec2InstanceContext = new EC2InstanceContextImpl(
                eventMock, instanceJsonMock, clientProviderMock, amiIdProviderMock, amiProviderMock, taupageYamlProviderMock,
                taupageNamePrefixMock, taupageOwnersMock, kioApplicationProviderMock, kioVersionProviderMock, kioApprovalProviderMock,
                pieroneTagProviderMock, scmSourceProviderMock);
        Optional<String> versionId = ec2InstanceContext.getVersionId();
        assertThat(versionId).isPresent();
        assertThat(versionId.get()).isExactlyInstanceOf(String.class);
        assertThat(versionId.get()).isEqualTo("10.0");
    }
}