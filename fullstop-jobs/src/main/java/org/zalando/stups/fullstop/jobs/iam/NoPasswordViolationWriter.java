package org.zalando.stups.fullstop.jobs.iam;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.zalando.stups.fullstop.jobs.iam.csv.CSVReportEntry;
import org.zalando.stups.fullstop.violation.ViolationBuilder;
import org.zalando.stups.fullstop.violation.ViolationSink;

import static java.util.Collections.singletonMap;
import static org.slf4j.LoggerFactory.getLogger;
import static org.zalando.stups.fullstop.violation.ViolationType.PASSWORD_USED;

@Component
public class NoPasswordViolationWriter {

    private final Logger log = getLogger(getClass());

    public static final String NO_REGION = "no-region";

    private final ViolationSink violationSink;

    @Autowired
    public NoPasswordViolationWriter(ViolationSink violationSink) {
        this.violationSink = violationSink;
    }

    public void writeViolation(String accountId, CSVReportEntry csvReportEntry) {
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
}
