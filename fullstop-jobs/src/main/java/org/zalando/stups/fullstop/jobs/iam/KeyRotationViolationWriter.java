package org.zalando.stups.fullstop.jobs.iam;

import com.amazonaws.services.identitymanagement.model.AccessKeyMetadata;
import com.google.common.collect.ImmutableMap;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.zalando.stups.fullstop.violation.ViolationBuilder;
import org.zalando.stups.fullstop.violation.ViolationSink;

import java.util.Map;

import static org.slf4j.LoggerFactory.getLogger;
import static org.zalando.stups.fullstop.violation.ViolationType.ACTIVE_KEY_TOO_OLD;

/**
 * Access Keys have to be rotated regularly
 */
@Component
public class KeyRotationViolationWriter {

    private final Logger log = getLogger(getClass());

    private final ViolationSink violationSink;

    @Autowired
    public KeyRotationViolationWriter(final ViolationSink violationSink) {
        this.violationSink = violationSink;
    }

    void writeViolation(final String accountId, final AccessKeyMetadata accessKey) {
        log.info("Found user {} with expired access key {} in account {}", accessKey.getUserName(), accessKey.getAccessKeyId(), accountId);
        violationSink.put(
                new ViolationBuilder()
                        .withAccountId(accountId)
                        .withRegion(NoPasswordViolationWriter.NO_REGION)
                        .withEventId("check-access-key_" + accessKey.getAccessKeyId())
                        .withType(ACTIVE_KEY_TOO_OLD)
                        .withPluginFullyQualifiedClassName(KeyRotationJob.class)
                        .withMetaInfo(metaMap(accessKey))
                        .build());
    }

    private Map<?, ?> metaMap(final AccessKeyMetadata accessKey) {
        return ImmutableMap.builder()
                .put("access_key_id", accessKey.getAccessKeyId())
                .put("user_name", accessKey.getUserName())
                .put("access_key_created", accessKey.getCreateDate())
                .build();
    }
}
