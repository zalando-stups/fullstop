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
package org.zalando.stups.fullstop.hystrix;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.util.Maps.newHashMap;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.same;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.zalando.stups.fullstop.clients.pierone.PieroneOperations;

public class HystrixPieroneOperationsTest {

    private static final String TEAM_ID = "foo";

    private static final String ARTIFACT_ID = "bar";

    private static final String VERSION_ID = "hello";

    private static final Map<String, String> TAGS = newHashMap();

    private static final Map<String, String> SCM_SOURCE = newHashMap();

    private PieroneOperations mockDelegate;

    private HystrixPieroneOperations hystrixPieroneOperations;

    @Before
    public void setUp() throws Exception {
        mockDelegate = mock(PieroneOperations.class);
        hystrixPieroneOperations = new HystrixPieroneOperations(mockDelegate);
    }

    @After
    public void tearDown() throws Exception {
        verifyNoMoreInteractions(mockDelegate);
    }

    @Test
    public void testListTags() throws Exception {
        when(mockDelegate.listTags(anyString(), anyString())).thenReturn(TAGS);
        assertThat(hystrixPieroneOperations.listTags(TEAM_ID, ARTIFACT_ID)).isSameAs(TAGS);
        verify(mockDelegate).listTags(same(TEAM_ID), same(ARTIFACT_ID));
    }

    @Test
    public void testGetScmSource() throws Exception {
        when(mockDelegate.getScmSource(anyString(), anyString(), anyString())).thenReturn(SCM_SOURCE);
        assertThat(hystrixPieroneOperations.getScmSource(TEAM_ID, ARTIFACT_ID, VERSION_ID)).isSameAs(SCM_SOURCE);
        verify(mockDelegate).getScmSource(same(TEAM_ID), same(ARTIFACT_ID), same(VERSION_ID));
    }
}