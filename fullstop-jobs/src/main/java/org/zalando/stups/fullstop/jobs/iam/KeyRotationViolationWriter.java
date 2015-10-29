/**
 * Copyright (C) 2015 Zalando SE (http://tech.zalando.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.zalando.stups.fullstop.jobs.iam;

import com.amazonaws.services.identitymanagement.model.AccessKeyMetadata;
import com.google.common.collect.ImmutableMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.zalando.stups.fullstop.violation.ViolationBuilder;
import org.zalando.stups.fullstop.violation.ViolationSink;

import java.util.Map;

import static org.zalando.stups.fullstop.violation.ViolationType.ACTIVE_KEY_TO_OLD;

@Component
public class KeyRotationViolationWriter {

    public static final String ACCESS_KEYS_EXIST = "access-keys-exist";
    private final ViolationSink violationSink;

    @Autowired
    public KeyRotationViolationWriter(ViolationSink violationSink) {
        this.violationSink = violationSink;
    }

    void writeViolation(String accountId, AccessKeyMetadata accessKey) {
        violationSink.put(
                new ViolationBuilder()
                        .withAccountId(accountId)
                        .withRegion(NoPasswordViolationWriter.NO_REGION)
                        .withEventId(ACCESS_KEYS_EXIST)
                        .withType(ACTIVE_KEY_TO_OLD)
                        .withPluginFullyQualifiedClassName(KeyRotationJob.class)
                        .withMetaInfo(metaMap(accessKey))
                        .build());
    }

    private Map<?, ?> metaMap(AccessKeyMetadata accessKey) {
        return ImmutableMap.builder()
                .put("access_key_id", accessKey.getAccessKeyId())
                .put("user_name", accessKey.getUserName())
                .put("access_key_created", accessKey.getCreateDate())
                .build();
    }
}
