package org.zalando.stups.fullstop.plugin.provider.impl;

import com.amazonaws.AmazonClientException;
import com.amazonaws.services.ec2.AmazonEC2Client;
import com.amazonaws.services.ec2.model.DescribeInstanceAttributeResult;
import com.amazonaws.services.ec2.model.InstanceAttribute;
import com.amazonaws.util.Base64;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.zalando.stups.fullstop.plugin.EC2InstanceContext;
import org.zalando.stups.fullstop.plugin.provider.TaupageYamlProvider;
import org.zalando.stups.fullstop.taupage.TaupageYaml;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

public class TaupageYamlProviderImplTest {

    private static final String INSTANCE_ID = "i-asdf";
    private TaupageYamlProvider taupageYamlProvider;
    private EC2InstanceContext ec2InstanceContextMock;
    private AmazonEC2Client amazonEC2ClientMock;

    @Before
    public void setUp() {

        taupageYamlProvider = new TaupageYamlProviderImpl();

        ec2InstanceContextMock = mock(EC2InstanceContext.class);

        amazonEC2ClientMock = mock(AmazonEC2Client.class);

    }

    @After
    public void tearDown() {
        verifyNoMoreInteractions(ec2InstanceContextMock, amazonEC2ClientMock);
    }

    @Test
    public void testApplyWithTaupageAmi() throws Exception {
        when(ec2InstanceContextMock.isTaupageAmi()).thenReturn(Optional.of(true));

        when(ec2InstanceContextMock.getInstanceId()).thenReturn(INSTANCE_ID);
        when(ec2InstanceContextMock.getClient(eq(AmazonEC2Client.class))).thenReturn(amazonEC2ClientMock);
        when(amazonEC2ClientMock.describeInstanceAttribute(any())).thenReturn(new DescribeInstanceAttributeResult().
                withInstanceAttribute(new InstanceAttribute()
                        .withUserData(Base64.encodeAsString("blub: fdsa".getBytes()))));

        final Optional<TaupageYaml> result = taupageYamlProvider.apply(ec2InstanceContextMock);

        assertThat(result).isPresent();


        verify(ec2InstanceContextMock).isTaupageAmi();
        verify(ec2InstanceContextMock).getInstanceId();
        verify(ec2InstanceContextMock).getClient(eq(AmazonEC2Client.class));
        verify(amazonEC2ClientMock).describeInstanceAttribute(any());
    }

    @Test
    public void testApplyWithVersionSimilarToNumber() throws Exception {
        when(ec2InstanceContextMock.isTaupageAmi()).thenReturn(Optional.of(true));

        when(ec2InstanceContextMock.getInstanceId()).thenReturn(INSTANCE_ID);
        when(ec2InstanceContextMock.getClient(eq(AmazonEC2Client.class))).thenReturn(amazonEC2ClientMock);
        when(amazonEC2ClientMock.describeInstanceAttribute(any())).thenReturn(new DescribeInstanceAttributeResult().
                withInstanceAttribute(new InstanceAttribute()
                        .withUserData(Base64.encodeAsString("application_id: fdsa\napplication_version: 6478e18".getBytes()))));

        final Optional<TaupageYaml> result = taupageYamlProvider.apply(ec2InstanceContextMock);

        assertThat(result).isPresent();

        assertThat(result.get().getApplicationId()).isEqualTo("fdsa");
        assertThat(result.get().getApplicationVersion()).isEqualTo("6478e18");

        verify(ec2InstanceContextMock).isTaupageAmi();
        verify(ec2InstanceContextMock).getInstanceId();
        verify(ec2InstanceContextMock).getClient(eq(AmazonEC2Client.class));
        verify(amazonEC2ClientMock).describeInstanceAttribute(any());
    }

    @Test
    public void testApplyWithVersionSimilarToNumber1() throws Exception {
        when(ec2InstanceContextMock.isTaupageAmi()).thenReturn(Optional.of(true));

        when(ec2InstanceContextMock.getInstanceId()).thenReturn(INSTANCE_ID);
        when(ec2InstanceContextMock.getClient(eq(AmazonEC2Client.class))).thenReturn(amazonEC2ClientMock);
        when(amazonEC2ClientMock.describeInstanceAttribute(any())).thenReturn(new DescribeInstanceAttributeResult().
                withInstanceAttribute(new InstanceAttribute()
                        .withUserData(Base64.encodeAsString("application_id: fdsa\napplication_version: '6478e18'".getBytes()))));

        final Optional<TaupageYaml> result = taupageYamlProvider.apply(ec2InstanceContextMock);

        assertThat(result).isPresent();

        assertThat(result.get().getApplicationId()).isEqualTo("fdsa");
        assertThat(result.get().getApplicationVersion()).isEqualTo("6478e18");

        verify(ec2InstanceContextMock).isTaupageAmi();
        verify(ec2InstanceContextMock).getInstanceId();
        verify(ec2InstanceContextMock).getClient(eq(AmazonEC2Client.class));
        verify(amazonEC2ClientMock).describeInstanceAttribute(any());
    }

    @Test
    public void testApplyWithTaupageAmiButInvalidYaml() throws Exception {
        // a yaml list is not a valid taupage format. Map is required.
        final String yamlData = "- a\n- b\n- c\n";

        when(ec2InstanceContextMock.isTaupageAmi()).thenReturn(Optional.of(true));

        when(ec2InstanceContextMock.getInstanceId()).thenReturn(INSTANCE_ID);
        when(ec2InstanceContextMock.getClient(eq(AmazonEC2Client.class))).thenReturn(amazonEC2ClientMock);
        when(amazonEC2ClientMock.describeInstanceAttribute(any())).thenReturn(new DescribeInstanceAttributeResult().
                withInstanceAttribute(new InstanceAttribute()
                        .withUserData(Base64.encodeAsString(yamlData.getBytes()))));

        final Optional<TaupageYaml> result = taupageYamlProvider.apply(ec2InstanceContextMock);

        assertThat(result).isEmpty();


        verify(ec2InstanceContextMock).isTaupageAmi();
        verify(ec2InstanceContextMock).getInstanceId();
        verify(ec2InstanceContextMock).getClient(eq(AmazonEC2Client.class));
        verify(amazonEC2ClientMock).describeInstanceAttribute(any());
    }

    @Test
    public void testApplyWithOtherAmi() throws Exception {
        when(ec2InstanceContextMock.isTaupageAmi()).thenReturn(Optional.of(false));

        when(ec2InstanceContextMock.getInstanceId()).thenReturn(INSTANCE_ID);

        final Optional<TaupageYaml> result = taupageYamlProvider.apply(ec2InstanceContextMock);

        assertThat(result).isEmpty();


        verify(ec2InstanceContextMock).isTaupageAmi();
    }

    @Test
    public void testApplyWithOtherAmiWithException() throws Exception {
        when(ec2InstanceContextMock.isTaupageAmi()).thenReturn(Optional.of(true));

        when(ec2InstanceContextMock.getInstanceId()).thenReturn(INSTANCE_ID);
        when(ec2InstanceContextMock.getClient(eq(AmazonEC2Client.class))).thenReturn(amazonEC2ClientMock);
        when(amazonEC2ClientMock.describeInstanceAttribute(any())).thenThrow(new AmazonClientException("ups..."));


        final Optional<TaupageYaml> result = taupageYamlProvider.apply(ec2InstanceContextMock);

        assertThat(result).isEmpty();


        verify(ec2InstanceContextMock).isTaupageAmi();
        verify(ec2InstanceContextMock).getInstanceId();
        verify(ec2InstanceContextMock).getClient(eq(AmazonEC2Client.class));
        verify(amazonEC2ClientMock).describeInstanceAttribute(any());
    }

    @Test
    public void testApplyWithNoAmi() throws Exception {
        when(ec2InstanceContextMock.isTaupageAmi()).thenReturn(Optional.empty());

        when(ec2InstanceContextMock.getInstanceId()).thenReturn(INSTANCE_ID);

        final Optional<TaupageYaml> result = taupageYamlProvider.apply(ec2InstanceContextMock);

        assertThat(result).isEmpty();


        verify(ec2InstanceContextMock).isTaupageAmi();
    }
}
