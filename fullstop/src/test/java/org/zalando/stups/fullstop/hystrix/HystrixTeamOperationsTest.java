package org.zalando.stups.fullstop.hystrix;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.zalando.stups.fullstop.teams.TeamOperations;
import org.zalando.stups.fullstop.teams.Account;

import java.util.ArrayList;

import static com.google.common.collect.Lists.newArrayList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class HystrixTeamOperationsTest {

    private static final ArrayList<Account> ACCOUNTS = newArrayList();

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
        when(mockDelegate.getAwsAccountsByUser(anyString())).thenReturn(ACCOUNTS);
        assertThat(hystrixTeamOperations.getAwsAccountsByUser(USER_ID)).isSameAs(ACCOUNTS);
        verify(mockDelegate).getAwsAccountsByUser(same(USER_ID));
    }
}