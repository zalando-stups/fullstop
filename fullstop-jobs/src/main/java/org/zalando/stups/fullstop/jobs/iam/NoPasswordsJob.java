package org.zalando.stups.fullstop.jobs.iam;

import com.amazonaws.services.identitymanagement.model.GetCredentialReportResult;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.zalando.stups.fullstop.jobs.FullstopJob;
import org.zalando.stups.fullstop.jobs.annotation.EveryDayAtElevenPM;
import org.zalando.stups.fullstop.jobs.common.AccountIdSupplier;
import org.zalando.stups.fullstop.jobs.iam.csv.CSVReportEntry;
import org.zalando.stups.fullstop.jobs.iam.csv.CredentialReportCSVParser;

import javax.annotation.PostConstruct;
import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * IAM Users must not use passwords, but access keys.
 */
@Component
public class NoPasswordsJob implements FullstopJob {

    public static final String ROOT_ACCOUNT = "<root_account>";
    public static final String ROOT_SUFFIX = ":root";
    private final Logger log = getLogger(NoPasswordsJob.class);

    private final IdentityManagementDataSource iamDataSource;

    private final NoPasswordViolationWriter violationWriter;

    private final AccountIdSupplier allAccountIds;

    private final CredentialReportCSVParser csvParser;

    @Autowired
    public NoPasswordsJob(final IdentityManagementDataSource iamDataSource,
                          final NoPasswordViolationWriter violationWriter, AccountIdSupplier allAccountIds, CredentialReportCSVParser csvParser) {
        this.iamDataSource = iamDataSource;
        this.violationWriter = violationWriter;
        this.allAccountIds = allAccountIds;
        this.csvParser = csvParser;
    }

    @PostConstruct
    public void init() {
        log.info("{} initialized", getClass().getSimpleName());
    }

    @EveryDayAtElevenPM
    public void run() {
        log.info("Running {}", getClass().getSimpleName());

        for (String accountId : allAccountIds.get()) {

            GetCredentialReportResult credentialReportCSV = iamDataSource.getCredentialReportCSV(accountId);
            List<CSVReportEntry> csvReportEntries = csvParser.apply(credentialReportCSV);

            //check for all users
            log.info("Checking account {} for IAM users with passwords", accountId);
            Stream.of(csvReportEntries)
                    .flatMap(Collection::stream)
                    .filter(CSVReportEntry::isPasswordEnabled)
                    .forEach(c -> violationWriter.writeNoPasswordViolation(accountId, c));

            //check for the root user account
            log.info("Checking account {} for IAM users with mfa, access key", accountId);
            Stream.of(csvReportEntries)
                    .flatMap(Collection::stream)
                    .filter(c -> c.getUser().equals(ROOT_ACCOUNT) || c.getUser().endsWith(ROOT_SUFFIX))
                    .filter(c -> !c.isMfaActive() || c.isAccessKey1Active() || c.isAccessKey2Active())
                    .forEach(c -> violationWriter.writeRootUserViolation(accountId, c));
        }


        log.info("Finished {}", getClass().getSimpleName());
    }
}
