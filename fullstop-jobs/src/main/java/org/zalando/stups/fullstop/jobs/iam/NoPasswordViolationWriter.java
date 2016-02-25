package org.zalando.stups.fullstop.jobs.iam;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.zalando.stups.fullstop.jobs.iam.csv.CSVReportEntry;
import org.zalando.stups.fullstop.violation.ViolationBuilder;
import org.zalando.stups.fullstop.violation.ViolationSink;

import java.util.HashMap;
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

    @Autowired
    public NoPasswordViolationWriter(ViolationSink violationSink) {
        this.violationSink = violationSink;
    }

    public void writeNoPasswordViolation(String accountId, CSVReportEntry csvReportEntry) {
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

    public void writeRootUserViolation(String accountId, CSVReportEntry csvReportEntry) {
        log.info("Found IAM root user: {} that has configuration problem in account: {}", csvReportEntry.getUser(), accountId);

        Map<String, String> metaInfo = new HashMap<>();
        metaInfo.put("account_id", accountId);
        metaInfo.put("user", csvReportEntry.getUser());
        metaInfo.put("arn", csvReportEntry.getArn());
        metaInfo.put("is_password_enabled", String.valueOf(csvReportEntry.isPasswordEnabled()));
        metaInfo.put("is_mfa_active", String.valueOf(csvReportEntry.isMfaActive()));
        metaInfo.put("is_access_key_1_active", String.valueOf(csvReportEntry.isAccessKey1Active()));
        metaInfo.put("is_access_key_2_active", String.valueOf(csvReportEntry.isAccessKey2Active()));

        violationSink.put(
                new ViolationBuilder()
                        .withEventId("check-iam-user_" + csvReportEntry.getUser())
                        .withAccountId("put here the account property")
                        .withRegion(NO_REGION)
                        .withPluginFullyQualifiedClassName(NoPasswordsJob.class)
                        .withType(UNSECURED_ROOT_USER)
                        .withMetaInfo(metaInfo)
                        .build());
    }
}
