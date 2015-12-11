package org.zalando.fullstop.violation.persist.jpa;

import org.kie.api.runtime.StatelessKieSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.actuate.metrics.CounterService;
import org.zalando.stups.fullstop.violation.Violation;
import org.zalando.stups.fullstop.violation.entity.ViolationEntity;
import org.zalando.stups.fullstop.violation.entity.ViolationTypeEntity;
import org.zalando.stups.fullstop.violation.reactor.EventBusViolationHandler;
import org.zalando.stups.fullstop.violation.repository.ViolationRepository;
import org.zalando.stups.fullstop.violation.repository.ViolationTypeRepository;
import reactor.bus.EventBus;

/**
 * @author jbellmann
 */
public class ViolationJpaPersister extends EventBusViolationHandler {

    private static final String VIOLATIONS_EVENTBUS_QUEUED = "violations.eventbus.queued";

    private static final String VIOLATIONS_PERSISTED_JPA = "violations.persisted.jpa";

    private final Logger log = LoggerFactory.getLogger(ViolationJpaPersister.class);

    private final ViolationRepository violationRepository;

    private final ViolationTypeRepository violationTypeRepository;

    private final CounterService counterService;

    private final StatelessKieSession whitelistRulesSession;

    public ViolationJpaPersister(final EventBus eventBus, final ViolationRepository violationRepository,
                                 final ViolationTypeRepository violationTypeRepository,
                                 final CounterService counterService, final StatelessKieSession whitelistRulesSession) {
        super(eventBus);
        this.violationRepository = violationRepository;
        this.violationTypeRepository = violationTypeRepository;
        this.counterService = counterService;
        this.whitelistRulesSession = whitelistRulesSession;

        this.whitelistRulesSession.setGlobal("logger", log);
    }

    protected ViolationEntity buildViolationEntity(final Violation violation) {

        if (violation == null || violation.getViolationType() == null) {
            log.warn("Violation/Violation-Type must not be null!");
            return null;
        }

        String violationTypeId = violation.getViolationType();

        ViolationEntity entity = new ViolationEntity();

        entity.setAccountId(violation.getAccountId());
        entity.setEventId(violation.getEventId());
        entity.setInstanceId(violation.getInstanceId());

        entity.setPluginFullyQualifiedClassName(violation.getPluginFullyQualifiedClassName());

        entity.setUsername(violation.getUsername());

        ViolationTypeEntity violationTypeEntity = violationTypeRepository.findOne(violationTypeId);

        if (violationTypeEntity != null) {
            entity.setViolationTypeEntity(violationTypeEntity);
        }
        else {
            ViolationTypeEntity vte = new ViolationTypeEntity();
            vte.setId(violationTypeId);
            vte.setViolationSeverity(0);
            vte.setIsAuditRelevant(false);
            vte.setHelpText("This is only a default message");

            ViolationTypeEntity savedViolationTypeEntity = violationTypeRepository.save(vte);

            entity.setViolationTypeEntity(savedViolationTypeEntity);
        }

        entity.setMetaInfo(violation.getMetaInfo());

        entity.setRegion(violation.getRegion());

        whitelistRulesSession.execute(entity);

        return entity;
    }

    @Override
    public void handleViolation(final Violation violation) {
        this.counterService.decrement(VIOLATIONS_EVENTBUS_QUEUED);

        if (violationRepository.violationExists(violation.getAccountId(), violation.getRegion(), violation.getEventId(), violation.getInstanceId(), violation.getViolationType())) {
            log.debug("Violation {} does already exist", violation);
        } else {
            violationRepository.saveAndFlush(buildViolationEntity(violation));
        }

        this.counterService.increment(VIOLATIONS_PERSISTED_JPA);
    }

}
