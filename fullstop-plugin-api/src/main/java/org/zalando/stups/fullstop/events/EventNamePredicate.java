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
package org.zalando.stups.fullstop.events;

import com.amazonaws.services.cloudtrail.processinglibrary.model.CloudTrailEvent;
import com.google.common.base.Preconditions;

import java.util.function.Predicate;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Strings.isNullOrEmpty;

/**
 * @author jbellmann
 */
final class EventNamePredicate extends CloudTrailEventPredicate {

    private static final String MESSAGE = "EventName should never be null or empty";

    @Deprecated
    public static Predicate<CloudTrailEvent> RUN_INSTANCES = new EventNamePredicate("RunInstances");

    private final String eventName;

    public EventNamePredicate(final String eventName) {
        checkArgument(!isNullOrEmpty(eventName), MESSAGE);
        this.eventName = eventName;
    }

    @Override
    public boolean doTest(final CloudTrailEvent input) {
        return eventName.equals(Preconditions.checkNotNull(input.getEventData()).getEventName());
    }
}
