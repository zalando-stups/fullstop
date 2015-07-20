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
package com.unknown.comp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zalando.stups.fullstop.violation.Violation;
import org.zalando.stups.fullstop.violation.reactor.EventBusViolationHandler;
import reactor.bus.EventBus;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author jbellmann
 */
public class SimpleDemonstrationViolationHandler extends EventBusViolationHandler {

    private final Logger log = LoggerFactory.getLogger(SimpleDemonstrationViolationHandler.class);

    private AtomicInteger counter = new AtomicInteger();

    public SimpleDemonstrationViolationHandler(final EventBus eventBus) {
        super(eventBus);
    }

    public void handleViolation(final Violation violation) {
        counter.incrementAndGet();
        log.warn("HERE IT COMES FROM THE EVENT_BUS : {}", violation.toString());
    }

    public int getCount() {
        return counter.get();
    }

}
