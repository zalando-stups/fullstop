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

import static com.google.common.collect.Lists.newArrayList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.same;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.ArrayList;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.zalando.stups.fullstop.teams.TeamOperations;
import org.zalando.stups.fullstop.teams.UserTeam;

public class HystrixTeamOperationsTest {

    private static final ArrayList<UserTeam> USER_TEAMS = newArrayList();

    private static final String USER_ID = "foo";

    private TeamOperations mockDelegate;

    private HystrixTeamOperations hystrixTeamOperations;

    @Before
    public void setUp() throws Exception {
        mockDelegate = mock(TeamOperations.class);
        hystrixTeamOperations = new HystrixTeamOperations(mockDelegate);
    }

    @After
    public void tearDown() throws Exception {
        verifyNoMoreInteractions(mockDelegate);
    }

    @Test
    public void testGetTeamsByUser() throws Exception {
        when(mockDelegate.getTeamsByUser(anyString())).thenReturn(USER_TEAMS);
        assertThat(hystrixTeamOperations.getTeamsByUser(USER_ID)).isSameAs(USER_TEAMS);
        verify(mockDelegate).getTeamsByUser(same(USER_ID));
    }
}