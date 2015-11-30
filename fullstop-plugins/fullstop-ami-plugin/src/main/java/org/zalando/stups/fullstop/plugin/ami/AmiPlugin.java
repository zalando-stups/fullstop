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
package org.zalando.stups.fullstop.plugin.ami;

import com.google.common.collect.ImmutableMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.zalando.stups.fullstop.plugin.AbstractEC2InstancePlugin;
import org.zalando.stups.fullstop.plugin.EC2InstanceContext;
import org.zalando.stups.fullstop.plugin.EC2InstanceContextProvider;
import org.zalando.stups.fullstop.violation.ViolationSink;

import java.util.Set;
import java.util.function.Predicate;

import static java.util.function.Predicate.isEqual;
import static org.zalando.stups.fullstop.violation.ViolationType.WRONG_AMI;

public class AmiPlugin extends AbstractEC2InstancePlugin {

    private final ViolationSink violationSink;
    private final WhiteListedAmiProvider whiteListedAmiProvider;

    @Autowired
    public AmiPlugin(final EC2InstanceContextProvider contextProvider,
                     final ViolationSink violationSink,
                     final WhiteListedAmiProvider whiteListedAmiProvider) {
        super(contextProvider);
        this.violationSink = violationSink;
        this.whiteListedAmiProvider = whiteListedAmiProvider;
    }

    @Override
    protected Predicate<? super String> supportsEventName() {
        return isEqual(RUN_INSTANCES);
    }

    @Override
    protected void process(EC2InstanceContext context) {
        final Set<String> whiteListedAmiIds = whiteListedAmiProvider.apply(context);

        if (whiteListedAmiIds.isEmpty()) {
            return;
        }

        context.getAmiId().ifPresent(amiId -> {
            if (!whiteListedAmiIds.contains(amiId)) {
                violationSink.put(
                        context.violation()
                                .withType(WRONG_AMI)
                                .withPluginFullyQualifiedClassName(AmiPlugin.class)
                                .withMetaInfo(ImmutableMap.of(
                                        "ami_id", amiId,
                                        "ami_name", context.getAmiName().orElse("")))
                                .build());
            }
        });
    }
}
