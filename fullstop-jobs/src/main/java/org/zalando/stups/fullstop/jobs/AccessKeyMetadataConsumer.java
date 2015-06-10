/**
 * Copyright (C) 2015 Zalando SE (http://tech.zalando.com)
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.zalando.stups.fullstop.jobs;

import com.amazonaws.services.identitymanagement.model.AccessKeyMetadata;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.zalando.stups.fullstop.violation.ViolationBuilder;
import org.zalando.stups.fullstop.violation.ViolationSink;

import java.util.function.Consumer;

import static java.lang.String.format;

/**
 * @author jbellmann
 */
@Component
class AccessKeyMetadataConsumer implements Consumer<AccessKeyMetadata> {

    private static final String VIOLATION_MESSAGE = "User [%s] has an active key [%s] older than 1 week.";

    private final ViolationSink violationSink;

    @Autowired
    AccessKeyMetadataConsumer(final ViolationSink violationSink) {
        this.violationSink = violationSink;
    }

    @Override
    public void accept(final AccessKeyMetadata input) {
        violationSink.put(new ViolationBuilder(format(VIOLATION_MESSAGE, input.getUserName(), input.getAccessKeyId()))
                .build());
    }

}
