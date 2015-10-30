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

import com.amazonaws.services.identitymanagement.model.GetCredentialReportResult;
import org.junit.Before;
import org.junit.Test;
import org.zalando.stups.fullstop.jobs.common.AccountIdSupplier;
import org.zalando.stups.fullstop.jobs.iam.csv.CredentialReportCSVParser;
import org.zalando.stups.fullstop.jobs.iam.csv.User;

import java.util.stream.Stream;

import static com.google.common.collect.Sets.newHashSet;
import static org.mockito.Mockito.*;

public class NoPasswordJobTest {

    private IdentityManagementDataSource iamDataSource;
    private NoPasswordViolationWriter violationWriter;
    private AccountIdSupplier mockAccountIdSupplier;
    private CredentialReportCSVParser mockCsvParser;

    @Before
    public void setUp() {
        final GetCredentialReportResult report1 = new GetCredentialReportResult();
        final GetCredentialReportResult report2 = new GetCredentialReportResult();

        iamDataSource = mock(IdentityManagementDataSource.class);
        violationWriter = mock(NoPasswordViolationWriter.class);
        mockAccountIdSupplier = mock(AccountIdSupplier.class);
        mockCsvParser = mock(CredentialReportCSVParser.class);
        when(mockAccountIdSupplier.get()).thenReturn(newHashSet("account01", "account02"));
        when(iamDataSource.getCredentialReportCSV(eq("account01"))).thenReturn(report1);
        when(iamDataSource.getCredentialReportCSV(eq("account02"))).thenReturn(report2);
        when(mockCsvParser.apply(same(report1))).thenReturn(Stream.of(new User("1", false), new User("2", true), new User("3", true)));
        when(mockCsvParser.apply(same(report2))).thenReturn(Stream.of(new User("4", true), new User("5", false)));
    }

    @Test
    public void testNoPasswordJob() {
        new NoPasswordsJob(iamDataSource, violationWriter, mockAccountIdSupplier, mockCsvParser).check();

        verify(mockAccountIdSupplier).get();
        verify(iamDataSource, times(2)).getCredentialReportCSV(anyString());
        verify(mockCsvParser, times(2)).apply(any());
        verify(violationWriter, times(2)).writeViolation(eq("account01"), any());
        verify(violationWriter).writeViolation(eq("account02"), any());
    }
}
