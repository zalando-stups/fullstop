package org.zalando.stups.fullstop.jobs.common.impl;

import com.amazonaws.services.ec2.AmazonEC2Client;
import com.amazonaws.services.ec2.model.DescribeInstanceAttributeResult;
import com.amazonaws.services.ec2.model.InstanceAttribute;
import com.amazonaws.util.Base64;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.zalando.stups.fullstop.aws.ClientProvider;
import org.zalando.stups.fullstop.jobs.common.FetchTaupageYaml;

import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

public class FetchTaupageYamlImplTest {

    public static final String INSTANCE_ID = "i-1234";
    public static final String ACCOUNT = "798969";
    public static final String REGION = "eu-west-1";

    private AmazonEC2Client amazonEC2ClientMock;
    private ClientProvider clientProviderMock;

    @Before
    public void setUp() throws Exception {
        amazonEC2ClientMock = mock(AmazonEC2Client.class);
        clientProviderMock = mock(ClientProvider.class);

        when(clientProviderMock.getClient(eq(AmazonEC2Client.class), anyString(), any())).thenReturn(amazonEC2ClientMock);

    }

    @After
    public void tearDown() throws Exception {
        verifyNoMoreInteractions(amazonEC2ClientMock);

    }

    @Test
    public void testGetTaupageYaml() throws Exception {
        when(amazonEC2ClientMock.describeInstanceAttribute(any())).thenReturn(new DescribeInstanceAttributeResult().
                withInstanceAttribute(new InstanceAttribute()
                        .withUserData(Base64.encodeAsString("blub: fdsa".getBytes()))));
        final FetchTaupageYaml fetchTaupageYaml = new FetchTaupageYamlImpl(clientProviderMock);

        final Optional<Map> result = fetchTaupageYaml.getTaupageYaml(INSTANCE_ID, ACCOUNT, REGION);

        assertThat(result).isPresent();

        verify(amazonEC2ClientMock).describeInstanceAttribute(any());
    }

    @Test
    public void testBrokenYaml() throws Exception{
        // a yaml list is not a valid taupage format. Map is required.
        final String yamlData = "- a\n- b\n- c\n";

        when(amazonEC2ClientMock.describeInstanceAttribute(any())).thenReturn(new DescribeInstanceAttributeResult().
                withInstanceAttribute(new InstanceAttribute()
                        .withUserData(Base64.encodeAsString(yamlData.getBytes()))));

        final FetchTaupageYaml fetchTaupageYaml = new FetchTaupageYamlImpl(clientProviderMock);

        final Optional<Map> result = fetchTaupageYaml.getTaupageYaml(INSTANCE_ID, ACCOUNT, REGION);

        assertThat(result).isEmpty();

        verify(amazonEC2ClientMock).describeInstanceAttribute(any());
    }
}