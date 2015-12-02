package org.zalando.stups.fullstop.jobs.iam;

import com.amazonaws.services.identitymanagement.model.GetCredentialReportResult;
import org.junit.Before;
import org.junit.Test;
import org.zalando.stups.fullstop.jobs.common.AccountIdSupplier;
import org.zalando.stups.fullstop.jobs.iam.csv.CredentialReportCSVParser;
import org.zalando.stups.fullstop.jobs.iam.csv.User;

import static com.google.common.collect.Sets.newHashSet;
import static java.util.Arrays.asList;
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
        when(mockCsvParser.apply(same(report1))).thenReturn(asList(new User("1", false), new User("2", true), new User("3", true)));
        when(mockCsvParser.apply(same(report2))).thenReturn(asList(new User("4", true), new User("5", false)));
    }

    @Test
    public void testNoPasswordJob() {
        new NoPasswordsJob(iamDataSource, violationWriter, mockAccountIdSupplier, mockCsvParser).run();

        verify(mockAccountIdSupplier).get();
        verify(iamDataSource, times(2)).getCredentialReportCSV(anyString());
        verify(mockCsvParser, times(2)).apply(any());
        verify(violationWriter, times(2)).writeViolation(eq("account01"), any());
        verify(violationWriter).writeViolation(eq("account02"), any());
    }
}
