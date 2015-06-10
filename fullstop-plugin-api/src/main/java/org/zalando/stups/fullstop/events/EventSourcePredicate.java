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
package org.zalando.stups.fullstop.events;

import com.amazonaws.services.cloudtrail.processinglibrary.model.CloudTrailEvent;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Strings.isNullOrEmpty;

/**
 * @author jbellmann
 */
final class EventSourcePredicate extends CloudTrailEventPredicate {

    private static final String MESSAGE = "EventSource should never be null or empty";

    @Deprecated
    public static CloudTrailEventPredicate EC2_EVENT = new EventSourcePredicate("ec2.amazonaws.com");

    private final String eventSourceName;

    public EventSourcePredicate(final String eventSourceName) {
        checkArgument(!isNullOrEmpty(eventSourceName), MESSAGE);
        this.eventSourceName = eventSourceName;
    }

    @Override
    public boolean doTest(final CloudTrailEvent input) {
        return eventSourceName.equals(checkNotNull(input.getEventData()).getEventSource());
    }

}
