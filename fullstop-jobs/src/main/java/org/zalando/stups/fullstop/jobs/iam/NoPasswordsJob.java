package org.zalando.stups.fullstop.jobs.iam;

import com.amazonaws.services.identitymanagement.model.GetCredentialReportResult;
import com.google.common.collect.Lists;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * IAM Users must not use passwords, but access keys.
 */
@Component
public class NoPasswordsJob implements FullstopJob {

    private static final String ROOT_ACCOUNT = "<root_account>";
    private static final String ROOT_SUFFIX = ":root";
    private final Logger log = getLogger(NoPasswordsJob.class);

    private final IdentityManagementDataSource iamDataSource;

    private final NoPasswordViolationWriter violationWriter;

    private final AccountIdSupplier allAccountIds;

    private final CredentialReportCSVParser csvParser;

    @Autowired
    public NoPasswordsJob(final IdentityManagementDataSource iamDataSource,
                          final NoPasswordViolationWriter violationWriter, final AccountIdSupplier allAccountIds, final CredentialReportCSVParser csvParser) {
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

        final List<Map<String, String>> metaInfoList = Lists.newArrayList();

        for (final String accountId : allAccountIds.get()) {

            final GetCredentialReportResult credentialReportCSV = iamDataSource.getCredentialReportCSV(accountId);
            final List<CSVReportEntry> csvReportEntries = csvParser.apply(credentialReportCSV);

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
                    .forEach(c -> {
                        final Map<String, String> metaInfo = new HashMap<>();
                        metaInfo.put("account_id", accountId);
                        metaInfo.put("user", c.getUser());
                        metaInfo.put("arn", c.getArn());
                        metaInfo.put("is_password_enabled", String.valueOf(c.isPasswordEnabled()));
                        metaInfo.put("is_mfa_active", String.valueOf(c.isMfaActive()));
                        metaInfo.put("is_access_key_1_active", String.valueOf(c.isAccessKey1Active()));
                        metaInfo.put("is_access_key_2_active", String.valueOf(c.isAccessKey2Active()));

                        metaInfoList.add(metaInfo);
                    });
        }

        if (!metaInfoList.isEmpty()){
            violationWriter.writeRootUserViolation(metaInfoList);
        }

        log.info("Finished {}", getClass().getSimpleName());
    }
}
