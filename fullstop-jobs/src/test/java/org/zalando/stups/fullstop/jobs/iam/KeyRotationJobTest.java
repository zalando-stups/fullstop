package org.zalando.stups.fullstop.jobs.iam;

import com.amazonaws.services.identitymanagement.model.AccessKeyMetadata;
import com.amazonaws.services.identitymanagement.model.User;
import org.junit.Before;
import org.junit.Test;
import org.zalando.stups.fullstop.jobs.common.AccountIdSupplier;
import org.zalando.stups.fullstop.jobs.config.JobsProperties;

import java.util.Date;

import static com.google.common.collect.Sets.newHashSet;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.joda.time.DateTime.now;
import static org.mockito.Mockito.*;

public class KeyRotationJobTest {

    private IdentityManagementDataSource mockIAMDataSource;
    private KeyRotationViolationWriter mockViolationWriter;
    private AccountIdSupplier mockAccountIdSupplier;

    @Before
    public void setUp() {
        mockIAMDataSource = mock(IdentityManagementDataSource.class);
        mockViolationWriter = mock(KeyRotationViolationWriter.class);
        mockAccountIdSupplier = mock(AccountIdSupplier.class);
        when(mockAccountIdSupplier.get()).thenReturn(newHashSet("account01", "account02"));
        when(mockIAMDataSource.getUsers(eq("account01"))).thenReturn(singletonList(new User()));
        when(mockIAMDataSource.getUsers(eq("account02"))).thenReturn(asList(new User(), new User()));
        when(mockIAMDataSource.getAccessKeys(eq("account01"), any())).thenReturn(asList(
                upToDate(active(new AccessKeyMetadata())),
                expired(active(new AccessKeyMetadata())),
                expired(inactive(new AccessKeyMetadata()))));
        when(mockIAMDataSource.getAccessKeys(eq("account02"), any())).thenReturn(singletonList(upToDate(inactive(new AccessKeyMetadata()))));
    }

    private AccessKeyMetadata expired(AccessKeyMetadata accessKeyMetadata) {
        accessKeyMetadata.setCreateDate(now().minusDays(31).toDate());
        return accessKeyMetadata;
    }

    private AccessKeyMetadata upToDate(AccessKeyMetadata accessKeyMetadata) {
        accessKeyMetadata.setCreateDate(new Date());
        return accessKeyMetadata;
    }

    private AccessKeyMetadata inactive(AccessKeyMetadata accessKeyMetadata) {
        accessKeyMetadata.setStatus("Inactive");
        return accessKeyMetadata;
    }

    private AccessKeyMetadata active(AccessKeyMetadata accessKeyMetadata) {
        accessKeyMetadata.setStatus("Active");
        return accessKeyMetadata;
    }


    @Test
    public void testSimple() {

        new KeyRotationJob(mockIAMDataSource, mockViolationWriter, new JobsProperties(), mockAccountIdSupplier).run();

        verify(mockIAMDataSource, times(2)).getUsers(anyString());
        verify(mockIAMDataSource, times(3)).getAccessKeys(anyString(), anyString());
        verify(mockAccountIdSupplier).get();
        verify(mockViolationWriter).writeViolation(eq("account01"), any());
    }
}
