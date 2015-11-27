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
package org.zalando.stups.fullstop.plugin.keypair;

import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.JsonPathException;
import org.zalando.stups.fullstop.plugin.AbstractEC2InstancePlugin;
import org.zalando.stups.fullstop.plugin.EC2InstanceContext;
import org.zalando.stups.fullstop.plugin.EC2InstanceContextProvider;
import org.zalando.stups.fullstop.violation.ViolationSink;

import java.util.Optional;
import java.util.function.Predicate;

import static java.util.Collections.singletonMap;
import static java.util.function.Predicate.isEqual;
import static org.apache.commons.lang3.StringUtils.trimToNull;
import static org.zalando.stups.fullstop.violation.ViolationType.EC2_WITH_KEYPAIR;

public class KeyPairPlugin extends AbstractEC2InstancePlugin {

    private final ViolationSink violationSink;

    public KeyPairPlugin(EC2InstanceContextProvider contextProvider, final ViolationSink violationSink) {
        super(contextProvider);
        this.violationSink = violationSink;
    }

    @Override
    protected Predicate<? super String> supportsEventName() {
        // A key pair can only be assigned to an EC instance at launch time.
        // Once the key pair is set it cannot be changed.
        return isEqual(RUN_INSTANCES);
    }

    @Override
    protected void process(EC2InstanceContext context) {
        getKeyName(context).ifPresent(
                k -> violationSink.put(
                        context.violation()
                                .withType(EC2_WITH_KEYPAIR)
                                .withPluginFullyQualifiedClassName(KeyPairPlugin.class)
                                .withMetaInfo(singletonMap("key_name", k))
                                .build()));
    }

    private Optional<String> getKeyName(EC2InstanceContext context) {
        try {
            return Optional.ofNullable(trimToNull(JsonPath.read(context.getInstanceJson(), "$.keyName")));
        } catch (JsonPathException ignored) {
            return Optional.empty();
        }
    }


}
