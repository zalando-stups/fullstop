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
package org.zalando.stups.fullstop.jobs.iam;

import com.amazonaws.services.identitymanagement.model.User;
import org.junit.Before;
import org.junit.Test;

import java.util.Date;
import java.util.List;
import java.util.Map;

import static com.google.common.collect.Maps.newHashMap;
import static java.util.Arrays.asList;
import static org.mockito.Mockito.*;

public class NoPasswordJobTest {

    private IdentityManagementDataSource iamDataSource;
    private NoPasswordViolationWriter violationWriter;
    private Map<String, List<User>> usersByAccount;

    @Before
    public void setUp() {
        iamDataSource = mock(IdentityManagementDataSource.class);
        violationWriter = mock(NoPasswordViolationWriter.class);
        usersByAccount = newHashMap();
        usersByAccount.put("account01", asList(new User(), userWithPw(), userWithPw()));
        usersByAccount.put("account02", asList(userWithPw(), new User()));
    }

    private User userWithPw() {
        final User user = new User();
        user.setPasswordLastUsed(new Date());
        return user;
    }

    @Test
    public void testNoPasswordJob() {
        when(iamDataSource.getUsersByAccount()).thenReturn(usersByAccount);

        new NoPasswordsJob(iamDataSource, violationWriter).check();

        verify(iamDataSource).getUsersByAccount();
        verify(violationWriter, times(2)).writeViolation(eq("account01"), any());
        verify(violationWriter).writeViolation(eq("account02"), any());
    }
}
