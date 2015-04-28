package org.zalando.stups.fullstop.jobs;

import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.util.Date;
import java.util.List;

import org.assertj.core.util.Lists;

import org.junit.Before;
import org.junit.Test;

import org.mockito.Mockito;

import org.zalando.stups.fullstop.violation.ViolationStore;

import com.amazonaws.services.identitymanagement.model.ListUsersResult;
import com.amazonaws.services.identitymanagement.model.User;

public class NoPasswordJobTest {

    private ViolationStore violationStore;

    private IdentityManagementDataSource identityManagementDataSource;

    @Before
    public void setUp() {
        violationStore = mock(ViolationStore.class);
        identityManagementDataSource = mock(IdentityManagementDataSource.class);
    }

    @Test
    public void testNoPasswordJob() {

        Mockito.when(identityManagementDataSource.getListUsersResultPerAccountWithTuple()).thenReturn(
            getListUsersResultPerAccount());

        NoPasswordsJob job = new NoPasswordsJob(violationStore, identityManagementDataSource);

        job.check();

        verify(identityManagementDataSource, atLeastOnce()).getListUsersResultPerAccountWithTuple();
        verify(violationStore, atLeastOnce()).save(Mockito.any());
    }

    protected List<Tuple<String, ListUsersResult>> getListUsersResultPerAccount() {
        List<Tuple<String, ListUsersResult>> result = Lists.newArrayList();
        for (int i = 0; i < 10; i++) {

            result.add(new Tuple<String, ListUsersResult>("1234567" + i, getUsersList()));
        }

        return result;
    }

    protected ListUsersResult getUsersList() {
        ListUsersResult result = new ListUsersResult();
        List<User> usersList = Lists.newArrayList();
        for (int i = 0; i < 7; i++) {
            User user = new User();
            user.setUserId("TESTUSER_ID" + i);
            user.setUserName("TESTUSER_NAME" + i);
            if (i % 2 == 0) {

                // the important thing
                user.setPasswordLastUsed(new Date());
            } else {

                user.setPasswordLastUsed(null);
            }

            usersList.add(user);
        }

        result.setUsers(usersList);
        return result;
    }

}
