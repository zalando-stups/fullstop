package org.zalando.stups.fullstop.jobs.iam;

import com.google.common.collect.ImmutableMap;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.zalando.stups.fullstop.jobs.FullstopJob;
import org.zalando.stups.fullstop.jobs.annotation.EveryDayAtTenPM;
import org.zalando.stups.fullstop.jobs.common.AccountIdSupplier;
import org.zalando.stups.fullstop.jobs.config.JobsProperties;
import org.zalando.stups.fullstop.jobs.exception.JobExceptionHandler;

import javax.annotation.PostConstruct;
import java.util.Collection;

import static org.slf4j.LoggerFactory.getLogger;
import static org.zalando.stups.fullstop.jobs.iam.AccessKeyMetadataPredicates.isActiveAndOlderThanDays;

@Component
public class KeyRotationJob implements FullstopJob {

    private final Logger log = getLogger(KeyRotationJob.class);

    private final IdentityManagementDataSource iamDataSource;

    private final KeyRotationViolationWriter violationWriter;

    private final JobsProperties properties;

    private final AccountIdSupplier allAccountIds;
    private final JobExceptionHandler jobExceptionHandler;

    @Autowired
    public KeyRotationJob(final IdentityManagementDataSource iamDataSource,
                          final KeyRotationViolationWriter violationWriter,
                          final JobsProperties properties,
                          final AccountIdSupplier allAccountIds,
                          final JobExceptionHandler jobExceptionHandler) {
        this.violationWriter = violationWriter;
        this.iamDataSource = iamDataSource;
        this.properties = properties;
        this.allAccountIds = allAccountIds;
        this.jobExceptionHandler = jobExceptionHandler;
    }

    @PostConstruct
    public void init() {
        log.info("{} initialized", getClass().getSimpleName());
    }

    @EveryDayAtTenPM
    public void run() {
        log.info("Running {}", getClass().getSimpleName());

        allAccountIds.get().forEach(accountId -> {
            try {
                log.debug("Checking account {} for expired IAM access keys", accountId);
                iamDataSource.getUsers(accountId).stream()
                        .map(u -> iamDataSource.getAccessKeys(accountId, u.getUserName()))
                        .flatMap(Collection::stream)
                        .filter(isActiveAndOlderThanDays(properties.getAccessKeysExpireAfterDays()))
                        .forEach(accessKey -> violationWriter.writeViolation(accountId, accessKey));
            } catch (Exception e) {
                jobExceptionHandler.onException(e, ImmutableMap.of(
                        "job", this.getClass().getSimpleName(),
                        "aws_account_id", accountId));

            }
        });

        log.info("Finished {}", getClass().getSimpleName());
    }
}
