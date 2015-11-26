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
package org.zalando.stups.fullstop.plugin;

import com.amazonaws.services.cloudtrail.processinglibrary.model.CloudTrailEvent;
import com.amazonaws.services.cloudtrail.processinglibrary.model.CloudTrailEventData;

import java.util.Optional;
import java.util.function.Predicate;

public abstract class AbstractEC2InstancePlugin extends AbstractFullstopPlugin {

    private final EC2InstanceContextProvider context;

    protected AbstractEC2InstancePlugin(EC2InstanceContextProvider context) {
        this.context = context;
    }

    @Override
    public boolean supports(CloudTrailEvent cloudTrailEvent) {
        return Optional.ofNullable(cloudTrailEvent)
                .map(CloudTrailEvent::getEventData)
                .filter(e -> "ec2.amazonaws.com".equals(e.getEventSource()))
                .map(CloudTrailEventData::getEventName)
                .filter(supportsEventName())
                .isPresent();
    }

    protected abstract Predicate<? super String> supportsEventName();

    @Override
    public void processEvent(CloudTrailEvent event) {
        context.instancesIn(event).forEach(this::process);
    }

    protected abstract void process(EC2InstanceContext ec2InstanceContext);
}
