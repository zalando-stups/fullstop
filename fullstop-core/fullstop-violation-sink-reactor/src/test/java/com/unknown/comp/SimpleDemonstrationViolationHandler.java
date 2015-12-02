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
