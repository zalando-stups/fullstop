package org.zalando.stups.fullstop.jobs.iam;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.zalando.stups.fullstop.jobs.FullstopJob;
import org.zalando.stups.fullstop.jobs.annotation.EveryDayAtElevenPM;
import org.zalando.stups.fullstop.jobs.common.AccountIdSupplier;
import org.zalando.stups.fullstop.jobs.iam.csv.CredentialReportCSVParser;
import org.zalando.stups.fullstop.jobs.iam.csv.User;

import javax.annotation.PostConstruct;
import java.util.Collection;
import java.util.stream.Stream;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * IAM Users must not use passwords, but access keys.
 */
@Component
public class NoPasswordsJob implements FullstopJob {

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

        allAccountIds.get().forEach(accountId -> {
            log.info("Checking account {} for IAM users with passwords", accountId);
            Stream.of(accountId)
                    .map(iamDataSource::getCredentialReportCSV)
                    .map(csvParser::apply)
                    .flatMap(Collection::stream)
                    .filter(User::isPasswordEnabled)
                    .forEach(user -> violationWriter.writeViolation(accountId, user));
        });

        log.info("Finished {}", getClass().getSimpleName());
    }
}
