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
package org.zalando.stups.fullstop.plugin.ami;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.regions.Region;
import com.amazonaws.services.ec2.AmazonEC2Client;
import com.amazonaws.services.ec2.model.DescribeImagesRequest;
import com.amazonaws.services.ec2.model.DescribeImagesResult;
import com.amazonaws.services.ec2.model.Image;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.zalando.stups.fullstop.aws.ClientProvider;
import org.zalando.stups.fullstop.plugin.EC2InstanceContext;

import java.util.Set;

import static com.amazonaws.regions.Region.getRegion;
import static com.amazonaws.regions.Regions.EU_WEST_1;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

public class WhiteListedAmiProviderTest {

    private static final String ACCOUNT_ID = "123456789";
    private static final Region REGION = getRegion(EU_WEST_1);

    private WhiteListedAmiProviderImpl whiteListedAmiProvider;

    private ClientProvider mockClientProvider;
    private AmazonEC2Client mockEC2Client;
    private EC2InstanceContext mockContext;
    private DescribeImagesResult describeImagesResult;

    @Before
    public void setUp() throws Exception {
        mockClientProvider = mock(ClientProvider.class);
        mockContext = mock(EC2InstanceContext.class);
        mockEC2Client = mock(AmazonEC2Client.class);
        describeImagesResult = new DescribeImagesResult();

        whiteListedAmiProvider = new WhiteListedAmiProviderImpl("Taupage", ACCOUNT_ID, mockClientProvider);

        when(mockClientProvider.getClient(eq(AmazonEC2Client.class), anyString(), any(Region.class))).thenReturn(mockEC2Client);
        when(mockContext.getAccountId()).thenReturn(ACCOUNT_ID);
        when(mockContext.getRegion()).thenReturn(REGION);
    }

    @After
    public void tearDown() throws Exception {
        verifyNoMoreInteractions(mockClientProvider, mockEC2Client, mockContext);
    }

    @Test
    public void testLoadAmisSuccessfully() throws Exception {
        when(mockEC2Client.describeImages(any(DescribeImagesRequest.class)))
                .thenReturn(describeImagesResult.withImages(
                        new Image().withImageId("1").withName("Taupage-0815"),
                        new Image().withImageId("2").withName("Ubuntu"),
                        new Image().withImageId("3").withName("Taupage-4711")));

        // run it the first time
        final Set<String> firstResult = whiteListedAmiProvider.apply(mockContext);
        assertThat(firstResult).containsOnly("1", "3");

        // now test if it as been chached
        assertThat(whiteListedAmiProvider.apply(mockContext)).isSameAs(firstResult);

        verify(mockContext, times(2)).getAccountId();
        verify(mockContext, times(2)).getRegion();
        verify(mockClientProvider).getClient(eq(AmazonEC2Client.class), eq(ACCOUNT_ID), eq(REGION));
        verify(mockEC2Client).describeImages(any(DescribeImagesRequest.class));
    }

    @Test
    public void testExceptionOnLoading() throws Exception {
        when(mockEC2Client.describeImages(any(DescribeImagesRequest.class))).thenThrow(new AmazonServiceException("Oops"));

        final Set<String> amis = whiteListedAmiProvider.apply(mockContext);

        assertThat(amis).isEmpty();

        verify(mockContext).getAccountId();
        verify(mockContext).getRegion();
        verify(mockClientProvider).getClient(eq(AmazonEC2Client.class), eq(ACCOUNT_ID), eq(REGION));
        verify(mockEC2Client).describeImages(any(DescribeImagesRequest.class));
    }
}
