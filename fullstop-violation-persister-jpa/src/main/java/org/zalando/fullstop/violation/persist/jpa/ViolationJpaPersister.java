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
package org.zalando.fullstop.violation.persist.jpa;

import org.springframework.boot.actuate.metrics.CounterService;

import org.zalando.stups.fullstop.violation.Violation;
import org.zalando.stups.fullstop.violation.entity.ViolationEntity;
import org.zalando.stups.fullstop.violation.reactor.EventBusViolationHandler;
import org.zalando.stups.fullstop.violation.repository.ViolationRepository;

import reactor.bus.EventBus;

/**
 * @author  jbellmann
 */
public class ViolationJpaPersister extends EventBusViolationHandler {

    private static final String VIOLATIONS_EVENTBUS_QUEUED = "violations.eventbus.queued";
    private static final String VIOLATIONS_PERSISTED_JPA = "violations.persisted.jpa";

    private final ViolationRepository violationRepository;

    private final CounterService counterService;

    public ViolationJpaPersister(final EventBus eventBus, final ViolationRepository violationRepository,
            final CounterService counterService) {
        super(eventBus);
        this.violationRepository = violationRepository;
        this.counterService = counterService;
    }

    protected ViolationEntity buildViolationEntity(final Violation violation) {

        ViolationEntity entity = new ViolationEntity();

        entity.setAccountId(violation.getAccountId());
        entity.setEventId(violation.getEventId());
        entity.setMessage(violation.getMessage());

        entity.setViolationObject(violation.getViolationObject());

        entity.setRegion(violation.getRegion());

        return entity;
    }

    @Override
    public void handleViolation(final Violation violation) {
        this.counterService.decrement(VIOLATIONS_EVENTBUS_QUEUED);

        ViolationEntity entity = buildViolationEntity(violation);

        this.violationRepository.save(entity);
        this.counterService.increment(VIOLATIONS_PERSISTED_JPA);
    }

}
