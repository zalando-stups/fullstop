package org.zalando.stups.fullstop.jobs.iam;

import com.amazonaws.regions.Region;
import com.amazonaws.services.identitymanagement.AmazonIdentityManagementClient;
import com.amazonaws.services.identitymanagement.model.AccessKeyMetadata;
import com.amazonaws.services.identitymanagement.model.GenerateCredentialReportResult;
import com.amazonaws.services.identitymanagement.model.GetCredentialReportResult;
import com.amazonaws.services.identitymanagement.model.ListAccessKeysRequest;
import com.amazonaws.services.identitymanagement.model.User;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.zalando.stups.fullstop.aws.AwsRequestUtil;
import org.zalando.stups.fullstop.aws.ClientProvider;

import java.util.List;

import static com.amazonaws.regions.Regions.EU_WEST_1;
import static com.amazonaws.services.identitymanagement.model.ReportStateType.COMPLETE;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static org.slf4j.LoggerFactory.getLogger;

@Component
class IdentityManagementDataSource {

    private static final int MAX_RETRIES = 10;
    private static final int RETRY_TIMEOUT_MILLIS = 500;

    private final Logger log = getLogger(getClass());
    private final ClientProvider clientProvider;

    @Autowired
    IdentityManagementDataSource(final ClientProvider clientProvider) {
        this.clientProvider = clientProvider;
    }

    List<User> getUsers(final String accountId) {
        return AwsRequestUtil.performRequest(getIAMClient(accountId)::listUsers).getUsers();
    }

    List<AccessKeyMetadata> getAccessKeys(final String accountId, final String userName) {
        final ListAccessKeysRequest request = new ListAccessKeysRequest().withUserName(userName);
        final AmazonIdentityManagementClient iamClient = getIAMClient(accountId);
        return AwsRequestUtil.performRequest(() -> iamClient.listAccessKeys(request)).getAccessKeyMetadata();
    }

    GetCredentialReportResult getCredentialReportCSV(final String accountId) {
        final AmazonIdentityManagementClient client = getIAMClient(accountId);

        GenerateCredentialReportResult generationReport;
        int i = 0;
        do {
            Assert.state(i < MAX_RETRIES, "Maximum retries to generate credentials report exceeded");
            log.debug("Poll credentials report for account {}", accountId);
            try {
                MILLISECONDS.sleep(RETRY_TIMEOUT_MILLIS * i);
            } catch (final InterruptedException e) {
                throw new RuntimeException("Could not pull credentials report", e);
            }
            generationReport = AwsRequestUtil.performRequest(client::generateCredentialReport);
            i++;

        } while (!COMPLETE.toString().equals(generationReport.getState()));

        return AwsRequestUtil.performRequest(client::getCredentialReport);
    }

    private AmazonIdentityManagementClient getIAMClient(final String accountId) {
        return clientProvider.getClient(AmazonIdentityManagementClient.class, accountId, Region.getRegion(EU_WEST_1));
    }
}
