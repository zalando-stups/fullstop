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

import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.identitymanagement.AmazonIdentityManagementClient;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.zalando.stups.fullstop.aws.ClientProvider;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

/**
 * Created by mrandi.
 */
public class PolicyProviderTest {

    private ClientProvider clientProviderMock;

    private PolicyProvider policyProvider;

    @Before
    public void setUp() throws Exception {
        clientProviderMock = mock(ClientProvider.class);
        policyProvider = new PolicyProvider(clientProviderMock);
    }

    @After
    public void tearDown() throws Exception {
        verifyNoMoreInteractions(clientProviderMock);
    }

    @Test
    public void testGetPolicy() throws Exception {

        when(clientProviderMock.getClient(any(),any(),any())).thenReturn(new AmazonIdentityManagementClient());

        policyProvider.getPolicy("test", Region.getRegion(Regions.AP_SOUTHEAST_2), "test");

        verify(clientProviderMock).getClient(any(), any(), any());
    }

    @Test
    public void testGetPolicyException() throws Exception {

        when(clientProviderMock.getClient(any(),any(),any())).thenReturn(null);

        try {
            policyProvider.getPolicy("test", Region.getRegion(Regions.AP_SOUTHEAST_2), "test");
        } catch (RuntimeException e){
            assertThat(
                    e.getMessage()
                     .startsWith("Somehow we could not create an AmazonIdentityManagementClient with accountId:"));
        }

        verify(clientProviderMock).getClient(any(),any(),any());
    }
}