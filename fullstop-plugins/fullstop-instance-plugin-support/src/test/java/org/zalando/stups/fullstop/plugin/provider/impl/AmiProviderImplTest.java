package org.zalando.stups.fullstop.plugin.provider.impl;

import com.amazonaws.AmazonClientException;
import com.amazonaws.services.ec2.AmazonEC2Client;
import com.amazonaws.services.ec2.model.DescribeImagesRequest;
import com.amazonaws.services.ec2.model.DescribeImagesResult;
import com.amazonaws.services.ec2.model.Image;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.zalando.stups.fullstop.plugin.EC2InstanceContext;
import org.zalando.stups.fullstop.plugin.provider.AmiProvider;

import java.util.Optional;

import static com.google.common.collect.Lists.newArrayList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class AmiProviderImplTest {

    public static final String AMI_ID = "ami-234";
    public static final String AMI_NAME = "ami name";
    private AmiProvider amiProvider;
    private EC2InstanceContext ec2InstanceContextMock;
    private AmazonEC2Client amazonEC2ClientMock;

    @Before
    public void setUp() {

        amiProvider = new AmiProviderImpl();

        ec2InstanceContextMock = mock(EC2InstanceContext.class);

        amazonEC2ClientMock = mock(AmazonEC2Client.class);

    }

    @After
    public void tearDown() {
        verifyNoMoreInteractions(ec2InstanceContextMock, amazonEC2ClientMock);
    }

    @Test
    public void testApplyAmiFound() throws Exception {

        when(ec2InstanceContextMock.getAmiId()).thenReturn(Optional.of(AMI_ID));
        when(ec2InstanceContextMock.getClient(eq(AmazonEC2Client.class))).thenReturn(amazonEC2ClientMock);

        final DescribeImagesRequest describeImagesRequest = new DescribeImagesRequest().withImageIds(AMI_ID);
        when(amazonEC2ClientMock.describeImages(eq(describeImagesRequest)))
                .thenReturn(new DescribeImagesResult()
                        .withImages(newArrayList(new Image()
                                .withImageId(AMI_ID)
                                .withName(AMI_NAME))
                        )
                );

        final Optional<Image> result = amiProvider.apply(ec2InstanceContextMock);

        assertThat(result).isPresent();

        verify(ec2InstanceContextMock).getAmiId();
        verify(ec2InstanceContextMock).getClient(eq(AmazonEC2Client.class));
        verify(amazonEC2ClientMock).describeImages(eq(describeImagesRequest));
    }

    @Test
    public void testApplyAmiNotFound() throws Exception {

        when(ec2InstanceContextMock.getAmiId()).thenReturn(Optional.of(AMI_ID));
        when(ec2InstanceContextMock.getClient(eq(AmazonEC2Client.class))).thenReturn(amazonEC2ClientMock);

        final DescribeImagesRequest describeImagesRequest = new DescribeImagesRequest().withImageIds(AMI_ID);
        when(amazonEC2ClientMock.describeImages(eq(describeImagesRequest)))
                .thenReturn(null);

        final Optional<Image> result = amiProvider.apply(ec2InstanceContextMock);

        assertThat(result).isEmpty();

        verify(ec2InstanceContextMock).getAmiId();
        verify(ec2InstanceContextMock).getClient(eq(AmazonEC2Client.class));
        verify(amazonEC2ClientMock).describeImages(eq(describeImagesRequest));
    }

    @Test
    public void testApplyAmiNotFoundWithException() throws Exception {

        when(ec2InstanceContextMock.getAmiId()).thenReturn(Optional.of(AMI_ID));
        when(ec2InstanceContextMock.getClient(eq(AmazonEC2Client.class))).thenReturn(amazonEC2ClientMock);

        final DescribeImagesRequest describeImagesRequest = new DescribeImagesRequest().withImageIds(AMI_ID);
        when(amazonEC2ClientMock.describeImages(eq(describeImagesRequest)))
                .thenThrow(new AmazonClientException("oops, I did it again... Britney"));

        final Optional<Image> result = amiProvider.apply(ec2InstanceContextMock);

        assertThat(result).isEmpty();

        verify(ec2InstanceContextMock).getAmiId();
        verify(ec2InstanceContextMock).getClient(eq(AmazonEC2Client.class));
        verify(amazonEC2ClientMock).describeImages(eq(describeImagesRequest));
    }
}