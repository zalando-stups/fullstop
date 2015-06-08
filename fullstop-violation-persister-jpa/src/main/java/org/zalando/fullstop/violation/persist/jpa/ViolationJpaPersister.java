package org.zalando.fullstop.violation.persist.jpa;

import org.zalando.stups.fullstop.violation.SystemOutViolationHandler;
import org.zalando.stups.fullstop.violation.Violation;
import org.zalando.stups.fullstop.violation.entity.ViolationEntity;
import org.zalando.stups.fullstop.violation.reactor.EventBusViolationHandler;
import org.zalando.stups.fullstop.violation.repository.ViolationRepository;

import reactor.bus.EventBus;

/**
 * @author  jbellmann
 */
public class ViolationJpaPersister extends EventBusViolationHandler {

    private final ViolationRepository violationRepository;

    public ViolationJpaPersister(final EventBus eventBus, final ViolationRepository violationRepository) {
        super(eventBus, new SystemOutViolationHandler());
        this.violationRepository = violationRepository;
    }

    @Override
    protected void handleViolation(final Violation violation) {

        ViolationEntity entity = buildViolationEntity(violation);

        this.violationRepository.save(entity);
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

}
