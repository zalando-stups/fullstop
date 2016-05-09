package org.zalando.stups.fullstop.jobs.iam;

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.zalando.stups.fullstop.jobs.config.JobsProperties;
import org.zalando.stups.fullstop.jobs.iam.csv.CSVReportEntry;
import org.zalando.stups.fullstop.violation.ViolationBuilder;
import org.zalando.stups.fullstop.violation.ViolationSink;

import java.util.List;
import java.util.Map;

import static java.util.Collections.singletonMap;
import static org.slf4j.LoggerFactory.getLogger;
import static org.zalando.stups.fullstop.violation.ViolationType.PASSWORD_USED;
import static org.zalando.stups.fullstop.violation.ViolationType.UNSECURED_ROOT_USER;

@Component
public class NoPasswordViolationWriter {

    private final Logger log = getLogger(getClass());

    public static final String NO_REGION = "no-region";

    private final ViolationSink violationSink;

    private final JobsProperties jobsProperties;

    @Autowired
    public NoPasswordViolationWriter(final ViolationSink violationSink, final JobsProperties jobsProperties) {
        this.violationSink = violationSink;
        this.jobsProperties = jobsProperties;
    }

    public void writeNoPasswordViolation(final String accountId, final CSVReportEntry csvReportEntry) {
        log.info("Found IAM user {} that has a password in account {}", csvReportEntry.getUser(), accountId);
        violationSink.put(
                new ViolationBuilder()
                        .withEventId("check-iam-user_" + csvReportEntry.getUser())
                        .withAccountId(accountId)
                        .withRegion(NO_REGION)
                        .withPluginFullyQualifiedClassName(NoPasswordsJob.class)
                        .withType(PASSWORD_USED)
                        .withMetaInfo(singletonMap("user_name", csvReportEntry.getUser()))
                        .build());
    }

    public void writeRootUserViolation(final List<Map<String, String>> metaInfoList) {
        log.info("Found IAM root user that has configuration problem");

        violationSink.put(new ViolationBuilder()
                .withEventId("check-iam-root-user-" + DateTime.now().toString("yyyy-MM-dd"))
                .withAccountId(jobsProperties.getManagementAccount())
                .withRegion(NO_REGION)
                .withPluginFullyQualifiedClassName(NoPasswordsJob.class)
                .withType(UNSECURED_ROOT_USER)
                .withMetaInfo(metaInfoList)
                .build());
    }
}
