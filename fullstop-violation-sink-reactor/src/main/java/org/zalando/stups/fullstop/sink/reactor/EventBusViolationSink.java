/**
 * Copyright 2015 Zalando SE
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.zalando.stups.fullstop.sink.reactor;

import org.zalando.stups.fullstop.violation.Violation;
import org.zalando.stups.fullstop.violation.ViolationSink;

import reactor.bus.Event;
import reactor.bus.EventBus;

/**
 * Uses the {@link EventBus} to transport Violations to interested Consumers.
 *
 * @author  jbellmann
 */
public class EventBusViolationSink implements ViolationSink {

    private final String DEFAULT_VIOLATIONS_TOPIC = "/violations";
    private final EventBus eventBus;

    public EventBusViolationSink(final EventBus eventBus) {
        this.eventBus = eventBus;
    }

    @Override
    public void put(final Violation violation) {
        eventBus.notify(DEFAULT_VIOLATIONS_TOPIC, Event.wrap(violation));
    }

}
