package org.zalando.stups.fullstop.jobs.iam;

import com.amazonaws.services.identitymanagement.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.zalando.stups.fullstop.violation.ViolationBuilder;
import org.zalando.stups.fullstop.violation.ViolationSink;

import static org.zalando.stups.fullstop.violation.ViolationType.PASSWORD_USED;

@Component
public class NoPasswordViolationWriter {

    public static final String IAM_USER_EXISTS = "iam-user-exists";
    public static final String NO_REGION = "no-region";

    private final ViolationSink violationSink;

    @Autowired
    public NoPasswordViolationWriter(ViolationSink violationSink) {
        this.violationSink = violationSink;
    }

    public void writeViolation(String accountId, User user) {
        violationSink.put(
                new ViolationBuilder()
                        .withEventId(IAM_USER_EXISTS)
                        .withAccountId(accountId)
                        .withRegion(NO_REGION)
                        .withPluginFullyQualifiedClassName(NoPasswordViolationWriter.class)
                        .withType(PASSWORD_USED)
                        .withMetaInfo(user.getPath())
                        .build());
    }
}
